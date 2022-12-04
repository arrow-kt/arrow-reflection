package arrow.reflect.compiler.plugin.fir.checkers

import arrow.meta.Meta
import arrow.meta.TemplateCompiler
import arrow.meta.module.impl.arrow.meta.FirMetaContext
import arrow.reflect.compiler.plugin.targets.MetaTarget
import arrow.reflect.compiler.plugin.targets.MetagenerationTarget
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
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import kotlin.reflect.KClass

class FirMetaAdditionalCheckersExtension(
  session: FirSession,
  val templateCompiler: TemplateCompiler,
  val metaTargets: List<MetaTarget>
) : FirAdditionalCheckersExtension(session) {

  val metaContext = FirMetaContext(templateCompiler, session)

  private inline fun <reified In1, reified In2, reified In3, reified Out> invokeMeta(
    annotations: List<FirAnnotation>,
    superType: KClass<*>,
    methodName: String,
    arg: In1,
    arg2: In2,
    arg3: In3,
  ): Out? {
    val args = listOf(In1::class, In2::class, In3::class)
    val retType = Out::class
    return MetaTarget.find(
      annotations.mapNotNull { it.fqName(session)?.asString() }.toSet(),
      methodName,
      superType,
      MetagenerationTarget.Fir,
      args,
      retType,
      metaTargets
    )?.let { target ->
      val result = target.method.invoke(target.companion.objectInstance, metaContext, arg, arg2, arg3)
      result as? Out
    }
  }

  override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
    override val basicDeclarationCheckers: Set<FirBasicDeclarationChecker> = setOf(
      object : FirBasicDeclarationChecker() {
        override fun check(declaration: FirDeclaration, context: CheckerContext, reporter: DiagnosticReporter) {
          templateCompiler.frontEndScopeCache.addDeclaration(context, declaration)
          invokeChecker(Meta.Checker.Declaration::class, declaration, session, context, reporter)
        }
      }
    )
  }

  override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
    override val basicExpressionCheckers: Set<FirBasicExpressionChecker> = setOf(
      object : FirBasicExpressionChecker() {
        override fun check(expression: FirStatement, context: CheckerContext, reporter: DiagnosticReporter) {
          templateCompiler.frontEndScopeCache.addElement(context, expression)
          invokeChecker(Meta.Checker.Expression::class, expression, session, context, reporter)
          if (expression is FirFunctionCall) {
            val declaration = context.containingDeclarations.firstIsInstanceOrNull<FirSimpleFunction>()
            if (declaration != null) {
              // invoke a potential expression transformation
              val annotations = expression.toResolvedCallableReference()?.resolvedSymbol?.fir?.metaAnnotations(session).orEmpty()
              invokeMeta<FirDeclaration, CheckerContext, DiagnosticReporter, Unit>(
                annotations,
                superType = Meta.Checker.Declaration::class,
                methodName = "check",
                declaration,
                arg2 = context,
                arg3 = reporter
              )
            }
          }
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
        invokeMeta<E, CheckerContext, DiagnosticReporter, Unit>(
          annotations,
          superType = superType,
          methodName = "check",
          element,
          arg2 = context,
          arg3 = reporter
        )
      }
    }

  override val typeCheckers: TypeCheckers
    get() = super.typeCheckers

}

fun FirAnnotationContainer.metaAnnotations(session: FirSession): List<FirAnnotation> =
  annotations.filter {
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

fun FirAnnotationContainer.isMetaAnnotated(session: FirSession): Boolean =
  metaAnnotations(session).isNotEmpty()
