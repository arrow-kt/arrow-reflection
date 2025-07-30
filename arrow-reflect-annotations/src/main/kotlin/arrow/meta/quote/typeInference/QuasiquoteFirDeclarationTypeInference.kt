package arrow.meta.module.impl.arrow.meta.quote.typeInference

import arrow.meta.module.impl.arrow.meta.quote.QuasiquoteTransformer
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.lexer.KotlinLexer
import org.jetbrains.kotlin.lexer.KtTokens

class QuasiquoteFirDeclarationTypeInference(private val lexer: KotlinLexer)  {

  fun declaration(session: FirSession, code: String): FirDeclaration? {
    lexer.start(code)
    return when(lexer.tokenType) {
      KtTokens.IDENTIFIER -> checkIdentifierToken(session = session, code = code, token = lexer.tokenText)
      KtTokens.CLASS_KEYWORD,
      KtTokens.OBJECT_KEYWORD,
      KtTokens.INTERFACE_KEYWORD -> toFirDeclaration<FirClass>(session = session, code = code)
      KtTokens.FUN_KEYWORD -> toFirDeclaration<FirFunction>(session = session, code = code)
      KtTokens.VAL_KEYWORD, KtTokens.VAR_KEYWORD -> toFirDeclaration<FirProperty>(session = session, code = code)
      KtTokens.TYPE_ALIAS_KEYWORD -> toFirDeclaration<FirTypeAlias>(session = session, code = code)
      else -> null
    }
  }

  private fun checkIdentifierToken(session: FirSession, code: String, token: String): FirDeclaration? {
    return when(token) {
      KtTokens.SEALED_KEYWORD.value,
      KtTokens.ANNOTATION_KEYWORD.value,
      KtTokens.COMPANION_KEYWORD.value,
      KtTokens.ENUM_KEYWORD.value -> toFirDeclaration<FirClass>(session = session, code = code)
      else -> null
    }
  }

  private inline fun <reified T : FirDeclaration> toFirDeclaration(session: FirSession, code: String): T? {
    return QuasiquoteTransformer.declaration<T>(session = session, code = code)
  }
}
