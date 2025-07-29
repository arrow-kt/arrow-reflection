package arrow.meta.module.impl.arrow.meta.quote

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirReturnExpression
import org.jetbrains.kotlin.fir.expressions.FirWhenExpression
import org.jetbrains.kotlin.fir.expressions.impl.FirElseIfTrueCondition
import org.jetbrains.kotlin.lexer.KotlinLexer
import org.jetbrains.kotlin.lexer.KtTokens

class QuasiquoteFirTypeInference private constructor(
  private val lexer: KotlinLexer
) {

  companion object {
    fun expression(session: FirSession, code: String): FirExpression? {
      return QuasiquoteFirTypeInference(lexer = KotlinLexer()).expression(session = session, code = code)
    }
  }

  private fun expression(session: FirSession, code: String): FirExpression? {
    lexer.start(code)
    return when(lexer.tokenType) {
      KtTokens.RETURN_KEYWORD -> QuasiquoteTransformer.expression<FirReturnExpression>(session = session, code = code)
      KtTokens.IF_KEYWORD -> QuasiquoteTransformer.expression<FirElseIfTrueCondition>(session = session, code = code)
      KtTokens.WHEN_KEYWORD -> QuasiquoteTransformer.expression<FirWhenExpression>(session = session, code = code)
      else -> null
    }
  }
}
