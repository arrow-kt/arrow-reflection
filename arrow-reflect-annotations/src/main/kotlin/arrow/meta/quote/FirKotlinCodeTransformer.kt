package arrow.meta.module.impl.arrow.meta.quote

import arrow.meta.TemplateCompiler.FirTotalResolveProcessor
import org.jetbrains.kotlin.KtInMemoryTextSourceFile
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.lightTree.LightTree2Fir
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.scopes.kotlinScopeProvider
import org.jetbrains.kotlin.readSourceFileWithMapping

object FirKotlinCodeTransformer {

  private val file: (String) -> KtSourceFile = { code ->
    KtInMemoryTextSourceFile("_firKotlinTransformerDummyFile.kt", null, code)
  }

  fun transform(
    session: FirSession,
    code: String,
    isExpression: Boolean,
    scope: List<FirDeclaration>
  ): FirFile {
    val expressionCode: () -> String = {
      """
        fun _firKotlinTransformerDummy() {
          $code
        }
      """.trimIndent()
    }
    val fir = session.buildFirFile(file = file(if (isExpression) expressionCode() else code), scopeProvider = session.kotlinScopeProvider)
    return session.runResolution(fir = fir, scopeSession = ScopeSession(), scopeDeclarations = scope)
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

  private fun FirSession.runResolution(
    fir: FirFile,
    scopeSession: ScopeSession,
    scopeDeclarations: List<FirDeclaration>
  ): FirFile {
    val resolveProcessor = FirTotalResolveProcessor(this, scopeSession, scopeDeclarations)
    resolveProcessor.process(listOf(fir))
    return fir
  }
}
