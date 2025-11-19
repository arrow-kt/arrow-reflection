package arrow.meta.module.impl.arrow.meta.module.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.diagnosticError
import arrow.meta.samples.Increment
import org.jetbrains.kotlin.fir.expressions.FirCall
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement

private val IncrementNotInConstantExpression by diagnosticError()
private val IncrementNotInConstantInt by diagnosticError()

@Macro(target = Increment::class)
fun MacroContext.incrementChecker(statement: FirStatement): MacroCompilation {
  return diagnostics {
    when {
      statement !is FirLiteralExpression -> {
        statement.report(
          IncrementNotInConstantExpression,
          "@Increment only works on constant expressions of type `Int`"
        )
      }
      statement.value !is Int && statement.value !is Long -> {
        statement.report(
          IncrementNotInConstantInt,
          "found `${+statement}` but @Increment expects a constant of type `Int`"
        )
      }
    }
  }
}

@Macro(target = Increment::class)
fun MacroContext.incrementTransformation(statement: FirStatement): MacroCompilation {
  return transform {
    val exp = Kotlin { "${+statement} + 1" }
    exp.firstOrNull { it is FirCall } ?: statement
  }
}
