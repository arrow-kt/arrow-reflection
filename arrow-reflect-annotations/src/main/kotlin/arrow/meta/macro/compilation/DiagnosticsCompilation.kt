package arrow.meta.module.impl.arrow.meta.macro.compilation

import org.jetbrains.kotlin.diagnostics.DiagnosticFactory1DelegateProvider
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory1
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.diagnostics.warning1
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.renderReadableWithFqNames
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.text

interface DiagnosticsCompilation : MacroCompilation {

  fun runCompilation(context: DiagnosticsContext)
}

fun diagnosticError(): DiagnosticFactory1DelegateProvider<String> = error1<KtElement, String>()
fun diagnosticWarning(): DiagnosticFactory1DelegateProvider<String> = warning1<KtElement, String>()

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

  operator fun FirElement.unaryPlus(): String =
    (this as? FirTypeRef)?.coneType?.renderReadableWithFqNames()?.replace("/", ".")
      ?: source?.text?.toString()
      ?: error("$this has no source psi text element")
}
