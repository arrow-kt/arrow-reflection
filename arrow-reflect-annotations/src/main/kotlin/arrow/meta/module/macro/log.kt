package arrow.meta.module.impl.arrow.meta.module.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import arrow.meta.module.impl.arrow.meta.macro.compilation.DiagnosticsContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.samples.ImmutableErrors.error1
import arrow.meta.samples.Log
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.expressions.FirExpression

private val META_LOG by error1()

@Macro(target = Log::class)
fun MacroContext.log(declaration: FirDeclaration): MacroCompilation {
  return diagnostics {
    checkElement(expression = declaration)
  }
}

@Macro(target = Log::class)
fun MacroContext.log(expression: FirExpression): MacroCompilation {
  return diagnostics {
    checkElement(expression = expression)
  }
}

private fun DiagnosticsContext.checkElement(
  expression: FirElement
) {
  expression.report(
    META_LOG, "found error on expression: $expression"
  )
}
