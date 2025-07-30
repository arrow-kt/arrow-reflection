package arrow.meta.module.impl.arrow.meta.quote.typeInference

import arrow.meta.module.impl.arrow.meta.quote.QuasiquoteTransformer
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.impl.FirElseIfTrueCondition
import org.jetbrains.kotlin.lexer.KotlinLexer
import org.jetbrains.kotlin.lexer.KtTokens

class QuasiquoteFirExpressionTypeInference(private val lexer: KotlinLexer) {

  fun expression(session: FirSession, code: String): FirStatement? {
    lexer.start(code)
    return when(lexer.tokenType) {
      KtTokens.RETURN_KEYWORD -> toFirExpression<FirReturnExpression>(session = session, code = code)
      KtTokens.IF_KEYWORD -> toFirExpression<FirElseIfTrueCondition>(session = session, code = code)
      KtTokens.WHEN_KEYWORD -> toFirExpression<FirWhenExpression>(session = session, code = code)
      KtTokens.THROW_KEYWORD -> toFirExpression<FirThrowExpression>(session = session, code = code)
      KtTokens.BREAK_KEYWORD -> toFirExpression<FirBreakExpression>(session = session, code = code)
      KtTokens.CONTINUE_KEYWORD -> toFirExpression<FirContinueExpression>(session = session, code = code)
      KtTokens.TRY_KEYWORD -> toFirExpression<FirTryExpression>(session = session, code = code)
      KtTokens.FOR_KEYWORD, KtTokens.WHILE_KEYWORD -> toFirExpression<FirWhileLoop>(session = session, code = code)
      KtTokens.DO_KEYWORD -> toFirExpression<FirDoWhileLoop>(session = session, code = code)
      else -> null
    }
  }

  private inline fun <reified T : FirStatement> toFirExpression(session: FirSession, code: String): T? {
    return QuasiquoteTransformer.expression<T>(session = session, code = code)
  }
}
