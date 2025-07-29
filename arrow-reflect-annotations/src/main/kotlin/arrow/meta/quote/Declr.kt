package arrow.meta.module.impl.arrow.meta.quote

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration

@JvmInline
value class Declr private constructor(private val code: String)  {

  companion object {
    fun quote(scope: () -> String): Declr {
      return Declr(code = scope())
    }
  }

  inline fun <reified T : FirDeclaration> fir(session: FirSession): FirDeclrQuote<T> {
    val fir = QuasiquoteTransformer.declaration<T>(session = session, code = show())
    return if (fir == null) {
      DeclrQuoteError(error = "Not possible to transform code in FIR")
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
value class EvaluatedFirDeclr<T : FirDeclaration>(private val fir: T) : FirDeclrQuote<T>

@JvmInline
value class DeclrQuoteError(val error: String) : FirDeclrQuote<Nothing>

fun Declr.splice(): String = show()
