package arrow.meta.module.impl.arrow.meta.module.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import arrow.meta.module.impl.arrow.meta.macro.compilation.DiagnosticsContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.diagnosticError
import arrow.meta.samples.Log
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.expressions.FirExpression

private val META_LOG by diagnosticError()

@Macro(target = Log::class)
fun MacroContext.log(declaration: FirDeclaration): MacroCompilation {
  return diagnostics {
    checkElement(expression = declaration, diagnosticsContext = this)
  }
}

@Macro(target = Log::class)
fun MacroContext.log(expression: FirExpression): MacroCompilation {
  return diagnostics {
    checkElement(expression = expression, diagnosticsContext = this)
  }
}

private fun MacroContext.checkElement(
  expression: FirElement,
  diagnosticsContext: DiagnosticsContext
) {
  with(diagnosticsContext) {
    expression.report(
      META_LOG, "found error on expression: ${+expression}"
    )
  }
}
