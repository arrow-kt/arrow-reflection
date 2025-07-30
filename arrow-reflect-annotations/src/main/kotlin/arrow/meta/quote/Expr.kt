package arrow.meta.module.impl.arrow.meta.quote

import arrow.meta.module.impl.arrow.meta.quote.typeInference.QuasiquoteFirExpressionTypeInference
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.lexer.KotlinLexer

@JvmInline
value class Expr private constructor(private val code: String)  {

  companion object {
    operator fun invoke(scope: () -> String): Expr {
      return Expr(code = scope())
    }
  }

  inline fun <reified T : FirStatement> findFir(session: FirSession, predicate: (T) -> Boolean = { true }): FirExprQuote<T> {
    val fir = QuasiquoteTransformer.expression<T>(session = session, code = show(), predicate = predicate)
    return if (fir == null) {
      ExprQuoteError(error = "Not possible to transform code in Fir")
    } else {
      EvaluatedFirExpr(fir = fir)
    }
  }

  fun fir(session: FirSession): FirExprQuote<FirStatement> {
    val fir = QuasiquoteFirExpressionTypeInference(lexer = KotlinLexer()).expression(session = session, code = show())
    return if (fir == null) {
      ExprQuoteError(error = "Not possible to transform code in Fir inferring type")
    } else {
      EvaluatedFirExpr(fir = fir)
    }
  }

  fun show(): String = code

  override fun toString(): String {
    return code
  }
}

sealed interface FirExprQuote<out T : FirStatement>

@JvmInline
value class EvaluatedFirExpr<T : FirStatement>(val fir: T) : FirExprQuote<T>

@JvmInline
value class ExprQuoteError(val error: String) : FirExprQuote<Nothing>

fun Expr.splice(): String = show()
