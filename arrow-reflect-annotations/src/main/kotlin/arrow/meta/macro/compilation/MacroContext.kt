package arrow.meta.module.impl.arrow.meta.macro.compilation

interface MacroContext {

  fun diagnostics(scope: DiagnosticsContext.() -> Unit): DiagnosticsCompilation {
    return object : DiagnosticsCompilation {
      override fun runCompilation(context: DiagnosticsContext) {
        context.scope()
      }
    }
  }
}
