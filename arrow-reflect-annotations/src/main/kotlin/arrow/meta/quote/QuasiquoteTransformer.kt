package arrow.meta.module.impl.arrow.meta.quote

import org.jetbrains.kotlin.KtInMemoryTextSourceFile
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.lightTree.LightTree2Fir
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.scopes.kotlinScopeProvider
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.readSourceFileWithMapping


class QuasiquoteTransformer {

  private val file: (String) -> KtSourceFile = { code ->
    KtInMemoryTextSourceFile("quasiquoteTransformerFile.kt", null, code)
  }

  companion object {

    inline fun <reified T : FirDeclaration> declaration( session: FirSession, code: String): T? {
      return QuasiquoteTransformer().getDeclarations(session, code).firstOrNull {
        it is T
      } as? T
    }

    inline fun <reified T : FirExpression> expression(session: FirSession, code: String): T? {
      return QuasiquoteTransformer().getExpressions(session, code).firstOrNull {
        it is T
      } as? T
    }
  }

  fun getDeclarations(session: FirSession, code: String): List<FirDeclaration> {
    val declarations: MutableList<FirDeclaration> = mutableListOf()
    val file = session.buildFirFile(file = file(code), scopeProvider = session.kotlinScopeProvider)
    file.accept(object : FirVisitorVoid() {
      override fun visitElement(element: FirElement) {
        if (element is FirDeclaration) declarations.add(element)
        element.acceptChildren(this)
      }
    })
    return declarations
  }

  fun getExpressions(session: FirSession, code: String): List<FirExpression> {
    val expressions: MutableList<FirExpression> = mutableListOf()
    val expressionCode: () -> String = {
      //language=kotlin
      """
        fun quasiquoteTransformerDummy() {
          $code
        }
      """.trimIndent()
    }
    val file = session.buildFirFile(file = file(expressionCode()), scopeProvider = session.kotlinScopeProvider)
    file.accept(object : FirVisitorVoid() {
      override fun visitElement(element: FirElement) {
        if (element is FirExpression) expressions.add(element)
        element.acceptChildren(this)
      }
    })
    return expressions
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

