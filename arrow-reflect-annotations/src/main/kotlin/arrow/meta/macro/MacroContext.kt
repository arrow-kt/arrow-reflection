package arrow.meta.module.impl.arrow.meta.macro

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory1
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext

data class MacroContext(
  val diagnosticReporter: DiagnosticReporter,
  private val checkerContext: CheckerContext
) {

  fun FirElement.report(factory: KtDiagnosticFactory1<String>, msg: String) {
    diagnosticReporter.reportOn(
      source,
      factory,
      msg,
      checkerContext
    )
  }
}
