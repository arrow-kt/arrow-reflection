package arrow.meta.module.impl.arrow.meta.quote

import arrow.meta.module.impl.arrow.meta.quote.typeInference.QuasiquoteFirExpressionTypeInference
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.lexer.KotlinLexer

typealias Kotlin = Iterable<FirElement>

fun Kotlin(session: FirSession, scope: List<FirDeclaration> = listOf(), code: () -> String): Kotlin {
  val code = code()
  val isExpression = QuasiquoteFirExpressionTypeInference(lexer = KotlinLexer()).isExpression(code = code)
  val firFile = FirKotlinCodeTransformer.transform(session = session, code = code, isExpression = isExpression, scope = scope)
  return QuasiquoteIterable(file = firFile)
}
