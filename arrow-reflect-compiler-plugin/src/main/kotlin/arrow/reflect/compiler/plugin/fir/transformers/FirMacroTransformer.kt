package arrow.reflect.compiler.plugin.fir.transformers

import arrow.meta.module.impl.arrow.meta.macro.compilation.DiagnosticsContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.TransformCompilation
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.visitors.FirTransformer

class FirMacroTransformer<T : FirElement>(
  private val compilation: TransformCompilation<T>,
  private val diagnostics: DiagnosticsContext
) : FirTransformer<FirElement>() {

  override fun <E : FirElement> transformElement(element: E, data: FirElement): E {
    return (compilation as? TransformCompilation<E>)?.run {
      this.transform(context = diagnostics)
    } ?: element
  }
}
