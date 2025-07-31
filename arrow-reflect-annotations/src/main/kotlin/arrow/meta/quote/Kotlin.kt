package arrow.meta.module.impl.arrow.meta.quote

import arrow.meta.module.impl.arrow.meta.quote.typeInference.QuasiquoteFirExpressionTypeInference
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.lexer.KotlinLexer

typealias Kotlin = Iterable<FirElement>

fun Kotlin(session: FirSession, code: () -> String): Kotlin {
  val code = code()
  val isExpression = QuasiquoteFirExpressionTypeInference(lexer = KotlinLexer()).isExpression(code = code)
  return QuasiquoteIterable(file = FirKotlinCodeTransformer.transform(session = session, code = code, isExpression = isExpression))
}
