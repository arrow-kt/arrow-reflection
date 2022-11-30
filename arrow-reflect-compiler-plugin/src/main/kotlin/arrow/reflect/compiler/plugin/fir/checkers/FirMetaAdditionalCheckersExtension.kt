package arrow.reflect.compiler.plugin.fir.checkers

import arrow.meta.Meta
import arrow.meta.TemplateCompiler
import arrow.reflect.compiler.plugin.targets.MetaTarget
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
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.expressions.classId
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.name.FqName

class FirMetaAdditionalCheckersExtension(
  session: FirSession,
  val templateCompiler: TemplateCompiler,
  val metaTargets: List<MetaTarget>
) : FirAdditionalCheckersExtension(session) {

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
          }
        }
      }
    )
  }

  override val typeCheckers: TypeCheckers
    get() = super.typeCheckers
}

fun FirAnnotationContainer.isMetaAnnotated(session: FirSession): Boolean =
  annotations.any {
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
