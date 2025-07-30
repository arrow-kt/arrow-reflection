package arrow.meta.module.impl.arrow.meta.quote

import arrow.meta.module.impl.arrow.meta.quote.typeInference.QuasiquoteFirDeclarationTypeInference
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.lexer.KotlinLexer

@JvmInline
value class Declr private constructor(private val code: String)  {

  companion object {
    operator fun invoke(scope: () -> String): Declr {
      return Declr(code = scope())
    }
  }

  inline fun <reified T : FirDeclaration> findFir(session: FirSession, predicate: (T) -> Boolean = { true }): FirDeclrQuote<T> {
    val fir = QuasiquoteTransformer.declaration<T>(session = session, code = show(), predicate = predicate)
    return if (fir == null) {
      DeclrQuoteError(error = "Not possible to transform code in Fir")
    } else {
      EvaluatedFirDeclr(fir = fir)
    }
  }

  fun fir(session: FirSession): FirDeclrQuote<FirDeclaration> {
    val fir = QuasiquoteFirDeclarationTypeInference(lexer = KotlinLexer()).declaration(session = session, code = show())
    return if (fir == null) {
      DeclrQuoteError("Not possible to transform code in Fir inferring type")
    } else {
      EvaluatedFirDeclr(fir = fir)
    }
  }

  fun show(): String = code

  override fun toString(): String {
    return code
  }
}

sealed interface FirDeclrQuote<out T : FirDeclaration>

@JvmInline
value class EvaluatedFirDeclr<T : FirDeclaration>(val fir: T) : FirDeclrQuote<T>

@JvmInline
value class DeclrQuoteError(val error: String) : FirDeclrQuote<Nothing>

fun Declr.splice(): String = show()
