package arrow.meta.samples

import arrow.meta.Diagnostics
import arrow.meta.FirMetaCheckerContext
import arrow.meta.Meta
import arrow.meta.samples.IncrementErrors.IncrementNotInConstantExpression
import arrow.meta.samples.IncrementErrors.IncrementNotInConstantInt
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.types.ConstantValueKind

object IncrementErrors : Diagnostics.Error {
  val IncrementNotInConstantExpression by error1()
  val IncrementNotInConstantInt by error1()
}

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Increment {
  companion object :
    Meta.Checker.Expression<FirExpression>,
    Meta.FrontendTransformer.Expression,
    Diagnostics(IncrementNotInConstantExpression, IncrementNotInConstantInt) {

    override fun FirMetaCheckerContext.check(expression: FirExpression) {
      if (expression !is FirLiteralExpression)
        expression.report(
          IncrementNotInConstantExpression,
          "@Increment only works on constant expressions of type `Int`"
        )

      if (expression is FirLiteralExpression && expression.value !is Int)
        expression.report(
          IncrementNotInConstantInt,
          "found `${+expression}` but @Increment expects a constant of type `Int`"
        )
    }

    override fun FirMetaCheckerContext.expression(
      expression: FirExpression
    ): FirStatement {
      check(expression)
      //language=kotlin
      return "${+expression} + 1".call
    }

  }
}
