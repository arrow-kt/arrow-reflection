package arrow.reflect.compiler.plugin.targets.macro

import arrow.meta.module.impl.arrow.meta.macro.compilation.DiagnosticsCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.DiagnosticsContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.reflect.compiler.plugin.fir.transformers.FirMacroTransformer
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirFile

object MacroCompiler {

  fun compileDiagnostics(compilations: List<MacroCompilation>, diagnostics: DiagnosticsContext) {
    compilations.forEach { compilation ->
      if (compilation is DiagnosticsCompilation) {
        compilation.runCompilation(context = diagnostics)
      }
    }
  }

  fun compileTransformCompilation(
    session: FirSession,
    file: FirFile,
    macro: MacroInvoke,
    checkerContext: CheckerContext,
    diagnosticsReporter: DiagnosticReporter
  ) {
    FirMacroTransformer(
      session = session,
      macro = macro,
      diagnosticReporter = diagnosticsReporter,
      checkerContext = checkerContext
    ).transformFile(file, file)
  }
}
