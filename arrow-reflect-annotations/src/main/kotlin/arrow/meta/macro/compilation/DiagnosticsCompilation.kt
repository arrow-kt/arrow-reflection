package arrow.meta.module.impl.arrow.meta.macro.compilation

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory1
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext

interface DiagnosticsCompilation : MacroCompilation {

  fun runCompilation(context: DiagnosticsContext)
}

data class DiagnosticsContext(
  val session: FirSession,
  private val diagnosticReporter: DiagnosticReporter,
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
