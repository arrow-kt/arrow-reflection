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

open class DiagnosticsContext(
  open val session: FirSession,
  private val diagnosticReporter: DiagnosticReporter,
  private val checkerContext: CheckerContext
) {

  fun report(element: FirElement, factory: KtDiagnosticFactory1<String>, msg: String) {
    diagnosticReporter.reportOn(
      element.source,
      factory,
      msg,
      checkerContext
    )
  }
}

context(context: DiagnosticsContext)
fun FirElement.report(factory: KtDiagnosticFactory1<String>, msg: String) {
  context.report(element = this, factory = factory, msg = msg)
}
