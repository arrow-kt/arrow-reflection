package arrow.reflect.compiler.plugin.targets.macro

import arrow.meta.module.impl.arrow.meta.macro.compilation.DiagnosticsCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.DiagnosticsContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.TransformCompilation
import arrow.reflect.compiler.plugin.fir.transformers.FirMacroTransformer
import org.jetbrains.kotlin.fir.FirElement

object MacroCompiler {

  fun <E : FirElement> compile(element: E, compilations: List<MacroCompilation>, diagnostics: DiagnosticsContext) {
    val diagnosticsCompilation = mutableListOf<DiagnosticsCompilation>()
    val transformCompilation = mutableListOf<TransformCompilation<*>>()
    compilations.forEach { compilation ->
      when(compilation) {
        is DiagnosticsCompilation -> diagnosticsCompilation.add(compilation)
        is TransformCompilation<*> -> transformCompilation.add(compilation)
      }
    }
    diagnosticsCompilation.compile(diagnostics = diagnostics)
    transformCompilation.compile(element = element, diagnostics = diagnostics)
  }

  private fun List<DiagnosticsCompilation>.compile(diagnostics: DiagnosticsContext) {
    forEach { compilation ->
      compilation.runCompilation(context = diagnostics)
    }
  }

  private fun <E : FirElement> List<TransformCompilation<*>>.compile(element: E, diagnostics: DiagnosticsContext) {
    forEach { compilation ->
      FirMacroTransformer(
        compilation = compilation,
        diagnostics = diagnostics
      ).transformElement(element, element)
    }
  }
}
