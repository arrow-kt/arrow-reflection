package arrow.meta.module.impl.arrow.meta.quote

import org.jetbrains.kotlin.KtInMemoryTextSourceFile
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.lightTree.LightTree2Fir
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.scopes.kotlinScopeProvider
import org.jetbrains.kotlin.readSourceFileWithMapping

object FirKotlinCodeTransformer {

  private val file: (String) -> KtSourceFile = { code ->
    KtInMemoryTextSourceFile("_firKotlinTransformerDummyFile.kt", null, code)
  }

  fun transform(session: FirSession, code: String, isExpression: Boolean): FirFile {
    val expressionCode: () -> String = {
      """
        fun _firKotlinTransformerDummy() {
          $code
        }
      """.trimIndent()
    }

    return session.buildFirFile(file = file(if (isExpression) expressionCode() else code), scopeProvider = session.kotlinScopeProvider)
  }

  private fun FirSession.buildFirFile(
    file: KtSourceFile,
    scopeProvider: FirKotlinScopeProvider,
    diagnosticsReporter: DiagnosticReporter? = null
  ): FirFile {
    val builder = LightTree2Fir(this, scopeProvider, diagnosticsReporter)
    val (code, linesMapping) = file.getContentsAsStream().reader(Charsets.UTF_8).use {
      it.readSourceFileWithMapping()
    }
    return builder.buildFirFile(code, file, linesMapping)
  }
}
