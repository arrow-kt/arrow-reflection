package arrow.meta.module.impl.arrow.meta.macro.compilation

import arrow.meta.module.impl.arrow.meta.macro.compilation.transformclassfactory.TransformClassFactory
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirClass

interface MacroContext

context(_: MacroContext)
fun diagnostics(scope: DiagnosticsContext.() -> Unit): DiagnosticsCompilation {
  return object : DiagnosticsCompilation {
    override fun runCompilation(context: DiagnosticsContext) {
      context.scope()
    }
  }
}

context(_: MacroContext)
fun <T : FirElement> transform(scope: TransformContext.() -> T): TransformCompilation<T> {
  return object : TransformCompilation<T> {
    override fun transform(context: TransformContext): T {
      return context.scope()
    }
  }
}

context(_: MacroContext)
fun FirClass.transform(scope: TransformClassFactory.() -> Unit): TransformClassCompilation {
  return object : TransformClassCompilation {
    override fun transform(context: TransformClassContext): TransformClassFactory {
      return TransformClassFactory(
        firClass = this@transform,
        context = context
      ).also { scope(it) }
    }
  }
}
