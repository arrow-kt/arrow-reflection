package arrow.meta.module.impl.arrow.meta.quotes

import arrow.meta.module.impl.arrow.meta.quotes.lighterAST.LighterASTDeclarationTransformer
import arrow.meta.module.impl.arrow.meta.quotes.lighterAST.LighterASTExpressionTransformer
import org.jetbrains.kotlin.diagnostics.Node
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.lightTree.LightTree2Fir
import org.jetbrains.kotlin.fir.lightTree.LightTreeParsingErrorListener
import org.jetbrains.kotlin.fir.scopes.kotlinScopeProvider

class QuasiquoteTransformer(
  val declarationTransformer: LighterASTDeclarationTransformer,
  val expressionTransformer: LighterASTExpressionTransformer,
) {

  companion object {

    fun <T : FirDeclaration> declaration(code: String, session: FirSession): T? {
      val baseScopeProvider = session.kotlinScopeProvider
      val flyweightCapableTree = LightTree2Fir.buildLightTree(code, object : LightTreeParsingErrorListener {
        override fun onError(startOffset: Int, endOffset: Int, message: String?) {
          println()
        }
      })
      val declarationTransformer = LighterASTDeclarationTransformer(session, baseScopeProvider, flyweightCapableTree)
      return QuasiquoteTransformer(
        declarationTransformer = declarationTransformer,
        expressionTransformer = LighterASTExpressionTransformer(session, flyweightCapableTree, declarationTransformer)
      ).getDeclarations(flyweightCapableTree.root).firstOrNull {
        @Suppress("UNCHECKED_CAST")
        it as? T != null
      } as? T
    }

    inline fun <reified T : FirExpression> expression(code: String, session: FirSession): T? {
      val baseScopeProvider = session.kotlinScopeProvider
      val flyweightCapableTree = LightTree2Fir.buildLightTree(code, object : LightTreeParsingErrorListener {
        override fun onError(startOffset: Int, endOffset: Int, message: String?) {
          println()
        }
      })
      val declarationTransformer = LighterASTDeclarationTransformer(session, baseScopeProvider, flyweightCapableTree)
      return QuasiquoteTransformer(
        declarationTransformer = declarationTransformer,
        expressionTransformer = LighterASTExpressionTransformer(session, flyweightCapableTree, declarationTransformer)
      ).getExpressions(flyweightCapableTree.root)
    }
  }

  fun getDeclarations(node: Node): List<FirDeclaration> {
    return declarationTransformer.getDeclarationsFrom(node = node)
  }

  inline fun <reified T : FirExpression> getExpressions(node: Node): T {
    return expressionTransformer.getAsFirExpression<T>(node, "")
  }
}

