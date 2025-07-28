package arrow.meta.module.impl.arrow.meta.quote

import arrow.meta.module.impl.arrow.meta.quotes.QuasiquoteTransformer
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirExpression

@JvmInline
value class Expr(private val code: String)  {

  companion object {
    fun quote(scope: () -> String): Expr {
      return Expr(code = scope())
    }
  }

  inline fun <reified T : FirExpression> fir(session: FirSession): FirExprQuote<T> {
    val fir = QuasiquoteTransformer.expression<T>(code = show(), session = session)
    return if (fir == null) {
      ExprQuoteError(error = "Not possible to transform code in FIR")
    } else {
      EvaluatedFirExpr(fir = fir)
    }
  }

  fun show(): String = code

  override fun toString(): String {
    return code
  }
}

sealed interface FirExprQuote<out T : FirExpression>

@JvmInline
value class EvaluatedFirExpr<T : FirExpression>(private val fir: T) : FirExprQuote<T>

@JvmInline
value class ExprQuoteError(val error: String) : FirExprQuote<Nothing>

fun quote(scope: () -> String): Expr {
  return Expr(code = scope())
}

fun Expr.splice(): String = show()
