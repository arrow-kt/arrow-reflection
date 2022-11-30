package arrow.meta.samples

import arrow.meta.Meta
import arrow.meta.module.impl.arrow.meta.FirMetaContext
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.Renderer
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.renderWithType


abstract class Diagnostics(name: String) : BaseDiagnosticRendererFactory() {

  init {
    RootDiagnosticRendererFactory.registerFactory(this)
  }

  val META_LOG by error1<PsiElement, String>()

  override val MAP: KtDiagnosticFactoryToRendererMap =
    KtDiagnosticFactoryToRendererMap(name).also { map ->
        map.put(
          META_LOG,
          "{0}",
          Renderer { t: String -> t }
        )
    }

  fun FirExpression.report(msg: String, context: CheckerContext, reporter: DiagnosticReporter) {
    reporter.reportOn(
      source,
      META_LOG,
      renderWithType(),
      context,
      AbstractSourceElementPositioningStrategy.DEFAULT
    )
  }
}

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Log {
  companion object :
    Meta.Checker.Expression<FirExpression>,
    Diagnostics("Log") {

    override fun FirMetaContext.check(expression: FirExpression, context: CheckerContext, reporter: DiagnosticReporter) {
      expression.report(
        expression.renderWithType(),
        context,
        reporter
      )
    }
  }

}
