package arrow.meta.module.impl.arrow.meta.macro.compilation

import org.jetbrains.kotlin.fir.FirElement

interface MacroContext {

  fun diagnostics(scope: DiagnosticsContext.() -> Unit): DiagnosticsCompilation {
    return object : DiagnosticsCompilation {
      override fun runCompilation(context: DiagnosticsContext) {
        context.scope()
      }
    }
  }

  fun <T : FirElement> transform(scope: DiagnosticsContext.() -> T): TransformCompilation<T> {
    return object : TransformCompilation<T> {
      override fun transform(context: DiagnosticsContext): T {
        return context.scope()
      }
    }
  }
}
