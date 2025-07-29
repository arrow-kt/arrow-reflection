package arrow.meta.module.impl.arrow.meta.quote

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirExpression

@JvmInline
value class Expr private constructor(private val code: String)  {

  companion object {
    fun quote(scope: () -> String): Expr {
      return Expr(code = scope())
    }
  }

  //inline fun <reified T : FirExpression> fir(session: FirSession): FirExprQuote<T> {
    //val fir = QuasiquoteTransformer.expression<T>(session = session, code = show())
    //return if (fir == null) {
      //ExprQuoteError(error = "Not possible to transform code in FIR")
    //} else {
      //EvaluatedFirExpr(fir = fir)
    //}
  //}

  fun fir(session: FirSession): FirExprQuote<FirExpression> {
    val fir = QuasiquoteFirTypeInference.expression(session = session, code = show())
    return if (fir == null) {
      ExprQuoteError(error = "Not possible to transform code in FIR inferring type")
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

fun Expr.splice(): String = show()
