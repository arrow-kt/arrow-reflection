package arrow.reflect.compiler.plugin.fir.checkers

import arrow.meta.FirMetaCheckerContext
import arrow.meta.Meta
import arrow.meta.TemplateCompiler
import arrow.reflect.compiler.plugin.fir.transformers.FirMetaTransformer
import arrow.reflect.compiler.plugin.targets.MetaInvoke
import arrow.reflect.compiler.plugin.targets.MetaTarget
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirBasicDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirBasicExpressionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.type.TypeCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.name.FqName
import kotlin.reflect.KClass

class FirMetaAdditionalCheckersExtension(
  session: FirSession,
  val templateCompiler: TemplateCompiler,
  val metaTargets: List<MetaTarget>
) : FirAdditionalCheckersExtension(session) {

  val invokeMeta = MetaInvoke(session, metaTargets)

  override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
    override val basicDeclarationCheckers: Set<FirBasicDeclarationChecker> = setOf(
      object : FirBasicDeclarationChecker() {
        override fun check(declaration: FirDeclaration, context: CheckerContext, reporter: DiagnosticReporter) {
          invokeChecker(Meta.Checker.Declaration::class, declaration, session, context, reporter)
          if (!templateCompiler.compiling && declaration is FirFile) {
            val transformer = FirMetaTransformer(session, templateCompiler, metaTargets, context, reporter)
            transformer.transformDeclaration(declaration, Unit)
          }
        }
      }
    )
  }

  override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
    override val basicExpressionCheckers: Set<FirBasicExpressionChecker> = setOf(
      object : FirBasicExpressionChecker() {
        override fun check(expression: FirStatement, context: CheckerContext, reporter: DiagnosticReporter) {
          invokeChecker(Meta.Checker.Expression::class, expression, session, context, reporter)
        }
      }
    )
  }

  private inline fun <reified E : FirElement> invokeChecker(
      superType: KClass<*>,
      element: E,
      session: FirSession,
      context: CheckerContext,
      reporter: DiagnosticReporter
  ) {
    if (element is FirAnnotationContainer && element.isMetaAnnotated(session)) {
        val annotations = element.metaAnnotations(session)
        val metaContext = FirMetaCheckerContext(templateCompiler, session, context, reporter)
        invokeMeta<E, Unit>(
          metaContext,
          annotations,
          superType = superType,
          methodName = "check",
          element
        )
      }
    }

  override val typeCheckers: TypeCheckers
    get() = super.typeCheckers

}

fun FirAnnotationContainer.metaAnnotations(session: FirSession): List<FirAnnotation> {
  val elementAnnotations = annotations.filter {
    val annotation = it.classId
    if (annotation != null) {
      val annotationSymbol = session.symbolProvider.getClassLikeSymbolByClassId(annotation)
      val metaAnnotations = annotationSymbol?.annotations.orEmpty()
      if (metaAnnotations.isEmpty()) {
        false
      } else {
        metaAnnotations.any {
          it.fqName(session) == FqName(Meta::class.java.canonicalName)
        }
      }
    } else false
  }
  val ownerAnnotations: List<FirAnnotation> = if (this is FirFunctionCall) {
    toResolvedCallableSymbol()?.fir?.metaAnnotations(session).orEmpty()
  } else emptyList()
  return elementAnnotations + ownerAnnotations
}

fun FirAnnotationContainer.isMetaAnnotated(session: FirSession): Boolean =
  metaAnnotations(session).isNotEmpty()
