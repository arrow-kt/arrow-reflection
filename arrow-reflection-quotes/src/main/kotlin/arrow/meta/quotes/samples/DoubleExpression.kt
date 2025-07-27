package arrow.meta.quotes.samples

import arrow.meta.FirMetaCheckerContext
import arrow.meta.Meta
import arrow.meta.quotes.*
import arrow.meta.quotes.builders.*
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class DoubleExpression {
  companion object : Meta.FrontendTransformer.Expression {
    
    override fun FirMetaCheckerContext.expression(
      expression: FirExpression
    ): FirStatement {
      // Use quasiquotes to create: expression * 2
      return +quote {
        // Wrap the original expression and multiply by 2
        binaryOp(
          Expr<Any?>(expression),
          "times",
          intLiteral(2)
        )
      }
    }
  }
}