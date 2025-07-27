package arrow.meta.quotes.samples

import arrow.meta.*
import arrow.meta.quotes.*
import arrow.meta.quotes.builders.*
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement

/**
 * Demonstrates conditional expression building with the quasiquote API.
 * This annotation wraps expressions in a null-safe check.
 * 
 * Example:
 * ```
 * val result = @QuasiquoteConditional someValue
 * // Transforms to: if (someValue != null) someValue else "default"
 * ```
 */
@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class QuasiquoteConditional {
  companion object : Meta.FrontendTransformer.Expression {
    
    override fun FirMetaCheckerContext.expression(
      expression: FirExpression
    ): FirStatement {
      // Build a conditional expression using the quote API
      val result = quote {
        val expr = expression.toExpr<Any?>()
        
        // Create: if (expr == null) "default" else expr
        ifExpr(
          condition = binaryOp(expr, "equals", nullLiteral()) as Expr<Boolean>,
          thenBranch = stringLiteral("default"),
          elseBranch = expr
        )
      }
      
      // Return the spliced expression
      return +result
    }
  }
}