package arrow.reflect.compiler.plugin.fir.checkers

import arrow.meta.Meta
import arrow.meta.TemplateCompiler
import arrow.meta.module.impl.arrow.meta.FirMetaContext
import arrow.reflect.compiler.plugin.targets.MetaTarget
import arrow.reflect.compiler.plugin.targets.MetagenerationTarget
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirBasicDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirBasicExpressionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.type.TypeCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.expressions.classId
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.name.FqName
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
    return MetaTarget.find(annotations.mapNotNull { it.fqName(session)?.asString() }.toSet(), methodName, superType, MetagenerationTarget.Fir, args, retType, metaTargets)?.let { target ->
      val result = target.method.invoke(target.companion.objectInstance, metaContext, arg, arg2, arg3)
      result as? Out
    }
  }

  override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
    override val basicDeclarationCheckers: Set<FirBasicDeclarationChecker> = setOf(
      object : FirBasicDeclarationChecker() {
        override fun check(declaration: FirDeclaration, context: CheckerContext, reporter: DiagnosticReporter) {
          if (declaration.isMetaAnnotated(session)) {
            templateCompiler.addToSourceCache(declaration)
          }
        }
      }
    )
  }
  override val expressionCheckers: ExpressionCheckers = object : ExpressionCheckers() {
    override val basicExpressionCheckers: Set<FirBasicExpressionChecker> = setOf(
      object : FirBasicExpressionChecker() {
        override fun check(expression: FirStatement, context: CheckerContext, reporter: DiagnosticReporter) {
          if (expression.isMetaAnnotated(session)) {
            templateCompiler.addToSourceCache(expression)
            if (expression is FirExpression) {
              val annotations = expression.metaAnnotations(session)
              invokeMeta<FirExpression, CheckerContext, DiagnosticReporter, Unit>(
                annotations,
                superType = Meta.Checker.Expression::class,
                methodName = "check",
                expression,
                arg2 = context,
                arg3 = reporter
              )
            }

          }
        }
      }
    )
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
