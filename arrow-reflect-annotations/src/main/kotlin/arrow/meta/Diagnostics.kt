package arrow.meta

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.Renderer
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.renderWithType

abstract class Diagnostics(val head: KtDiagnosticFactory1<String>, vararg factories: KtDiagnosticFactory1<String>) : BaseDiagnosticRendererFactory() {

  interface Error {
    fun error1(): DiagnosticFactory1DelegateProvider<String> = error1<PsiElement, String>()
  }

  init {
    RootDiagnosticRendererFactory.registerFactory(this)
  }

  override val MAP: KtDiagnosticFactoryToRendererMap =
    KtDiagnosticFactoryToRendererMap(this::class.java.canonicalName).also { map ->
      (listOf(head) + factories).forEach { factory ->
        map.put(
          factory,
          "{0}",
          Renderer { t: String -> t }
        )
      }
    }

  fun FirExpression.report(factory : KtDiagnosticFactory1<String>, msg: String, context: CheckerContext, reporter: DiagnosticReporter) {
    reporter.reportOn(
      source,
      factory,
      renderWithType(),
      context,
      AbstractSourceElementPositioningStrategy.DEFAULT
    )
  }
}
