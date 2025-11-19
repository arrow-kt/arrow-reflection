package arrow.meta.module.impl.arrow.meta.macro.compilation

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirDeclaration

interface TransformCompilation<T : FirElement> : MacroCompilation {

  fun transform(context: TransformContext): T
}

data class TransformContext(
  override val session: FirSession,
  private val diagnosticReporter: DiagnosticReporter,
  private val checkerContext: CheckerContext,
  private val scope: List<FirDeclaration>
) : DiagnosticsContext(session, diagnosticReporter, checkerContext) {

  fun Kotlin(code: () -> String): Iterable<FirElement> {
    return arrow.meta.module.impl.arrow.meta.quote.Kotlin(session = session, scope = scope, code = code)
  }
}
