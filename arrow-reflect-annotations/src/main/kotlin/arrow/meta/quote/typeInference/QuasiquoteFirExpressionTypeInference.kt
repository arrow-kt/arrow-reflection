package arrow.meta.module.impl.arrow.meta.quote.typeInference

import org.jetbrains.kotlin.lexer.KotlinLexer
import org.jetbrains.kotlin.lexer.KtTokens

class QuasiquoteFirExpressionTypeInference(private val lexer: KotlinLexer) {

  fun isExpression(code: String): Boolean {
    lexer.start(code)
    return when(lexer.tokenType) {
      KtTokens.RETURN_KEYWORD,
      KtTokens.IF_KEYWORD,
      KtTokens.WHEN_KEYWORD,
      KtTokens.THROW_KEYWORD,
      KtTokens.BREAK_KEYWORD,
      KtTokens.CONTINUE_KEYWORD,
      KtTokens.TRY_KEYWORD,
      KtTokens.FOR_KEYWORD,
      KtTokens.WHILE_KEYWORD,
      KtTokens.DO_KEYWORD -> true
      else -> false
    }
  }
}
