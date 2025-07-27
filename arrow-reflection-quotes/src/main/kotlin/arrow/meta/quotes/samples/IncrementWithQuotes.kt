package arrow.meta.quotes.samples

import arrow.meta.Diagnostics
import arrow.meta.FirMetaCheckerContext
import arrow.meta.Meta
import arrow.meta.quotes.*
import arrow.meta.quotes.builders.*
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.types.ConstantValueKind

object IncrementWithQuotesErrors : Diagnostics.Error {
  val IncrementNotInConstantExpression by error1()
  val IncrementNotInConstantInt by error1()
}

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class IncrementWithQuotes {
  companion object :
    Meta.Checker.Expression<FirExpression>,
    Meta.FrontendTransformer.Expression,
    Diagnostics(IncrementWithQuotesErrors.IncrementNotInConstantExpression, IncrementWithQuotesErrors.IncrementNotInConstantInt) {

    override fun FirMetaCheckerContext.check(expression: FirExpression) {
      if (expression !is FirLiteralExpression)
        expression.report(
          IncrementWithQuotesErrors.IncrementNotInConstantExpression,
          "@IncrementWithQuotes only works on constant expressions of type `Int`"
        )

      if (expression is FirLiteralExpression && expression.value !is Int)
        expression.report(
          IncrementWithQuotesErrors.IncrementNotInConstantInt,
          "found `${+expression}` but @IncrementWithQuotes expects a constant of type `Int`"
        )
    }

    override fun FirMetaCheckerContext.expression(
      expression: FirExpression
    ): FirStatement {
      check(expression)
      
      // Use quasiquotes instead of template compiler
      return when (expression) {
        is FirLiteralExpression -> {
          val value = expression.value as? Int ?: return expression
          // Create a new literal expression with incremented value
          +quote { 
            intLiteral(value + 1)
          }
        }
        else -> {
          // For non-literal expressions, create: expression + 1
          +quote {
            binaryOp(
              Expr<Any?>(expression),
              "plus",
              intLiteral(1)
            )
          }
        }
      }
    }
  }
}