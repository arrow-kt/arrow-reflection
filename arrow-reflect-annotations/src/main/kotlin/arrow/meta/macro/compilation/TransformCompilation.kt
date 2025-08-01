package arrow.meta.module.impl.arrow.meta.macro.compilation

import org.jetbrains.kotlin.fir.FirElement

interface TransformCompilation<T : FirElement> : MacroCompilation {

  fun transform(context: DiagnosticsContext): T
}
