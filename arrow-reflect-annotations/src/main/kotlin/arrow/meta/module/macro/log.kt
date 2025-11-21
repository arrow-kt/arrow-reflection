package arrow.meta.module.impl.arrow.meta.module.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import arrow.meta.module.impl.arrow.meta.macro.compilation.DiagnosticsContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.diagnosticError
import arrow.meta.module.impl.arrow.meta.macro.compilation.diagnostics
import arrow.meta.module.impl.arrow.meta.macro.compilation.report
import arrow.meta.module.impl.arrow.meta.macro.compilation.unaryPlus
import arrow.meta.samples.Log
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.expressions.FirExpression

private val META_LOG by diagnosticError()

@Macro(target = Log::class)
context(_: MacroContext)
fun log(declaration: FirDeclaration): MacroCompilation {
  return diagnostics {
    declaration.report()
  }
}

@Macro(target = Log::class)
context(_: MacroContext)
fun log(expression: FirExpression): MacroCompilation {
  return diagnostics {
    expression.report()
  }
}

context(_: MacroContext, _: DiagnosticsContext)
private fun FirElement.report(
) {
  report(META_LOG, "found error on expression: ${+this}")
}
