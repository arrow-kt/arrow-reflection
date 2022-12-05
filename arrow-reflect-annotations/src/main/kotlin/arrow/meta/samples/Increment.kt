package arrow.meta.samples

import arrow.meta.FirMetaContext
import arrow.meta.Meta
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.expressions.FirCall
import org.jetbrains.kotlin.fir.expressions.FirConstExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Increment {
  companion object :
    Meta.FrontendTransformer.ConstExpression {

    override fun FirMetaContext.constExpression(
      constExpression: FirConstExpression<*>,
      context: CheckerContext
    ): FirStatement =
      //language=kotlin
      """
        val x = ${+constExpression} + 1
      """.frontend<FirCall>(context.containingDeclarations)

  }
}
