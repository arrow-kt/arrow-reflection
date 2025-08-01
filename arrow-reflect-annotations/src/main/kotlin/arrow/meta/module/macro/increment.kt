package arrow.meta.module.impl.arrow.meta.module.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.module.impl.arrow.meta.quote.Kotlin
import arrow.meta.samples.Increment
import org.jetbrains.kotlin.fir.expressions.FirExpression

@Macro(target = Increment::class)
fun MacroContext.increment(expression: FirExpression): MacroCompilation {
  return transform<FirExpression> {
    val exp = Kotlin(session) {
      "${+expression} + 1"
    }
    expression
  }
}
