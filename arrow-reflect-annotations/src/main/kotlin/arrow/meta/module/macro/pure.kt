package arrow.meta.module.impl.arrow.meta.module.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.diagnosticError
import arrow.meta.samples.Pure
import arrow.meta.samples.pure.createCallGraph
import arrow.meta.samples.pure.render
import arrow.meta.samples.pure.unsafeCalls
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.name.FqName

private val CallGraphIncludesIO by diagnosticError()

private val restrictedNameSpaces = setOf(
  FqName("kotlin.io"),
  FqName("java.io"),
  FqName("foo.bar")
)

@Macro(target = Pure::class)
fun MacroContext.pure(function: FirSimpleFunction): MacroCompilation {
  return diagnostics {
    val callGraph = createCallGraph(null, function)
    val unsafeCallsInGraph = callGraph.unsafeCalls(restrictedNameSpaces)
    println(callGraph.render())
    unsafeCallsInGraph.forEach {
      it.localCall?.report(CallGraphIncludesIO, "Detected unsafe call at ${it.compilerMessage()}")
        ?: function.report(CallGraphIncludesIO, "Detected unsafe call at ${it.compilerMessage()}")
    }
  }
}
