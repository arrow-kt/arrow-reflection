package arrow.meta.samples

import arrow.meta.Diagnostics
import arrow.meta.FirMetaContext
import arrow.meta.Meta
import arrow.meta.samples.IncrementErrors.IncrementNotInConstantExpression
import arrow.meta.samples.IncrementErrors.IncrementNotInConstantInt
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.expressions.FirCall
import org.jetbrains.kotlin.fir.expressions.FirConstExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.types.ConstantValueKind

object IncrementErrors : Diagnostics.Error {
  val IncrementNotInConstantExpression by error1()
  val IncrementNotInConstantInt by error1()
}

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Increment {
  companion object : Meta.FrontendTransformer.Expression,
    Diagnostics(IncrementNotInConstantExpression, IncrementNotInConstantInt) {

    override fun FirMetaContext.expression(
      expression: FirExpression, context: CheckerContext, reporter: DiagnosticReporter
    ): FirStatement {
      if (expression !is FirConstExpression<*>) expression.report(
        IncrementNotInConstantExpression,
        "Increments only works on constant expressions of type `Int`",
        context,
        reporter
      )

      if (expression is FirConstExpression<*> && expression.kind != ConstantValueKind.Int) expression.report(
        IncrementNotInConstantInt, "`${+expression}` should be an `Int`", context, reporter
      )

      //language=kotlin
      return """
        val x = ${+expression} + 1
      """.frontend<FirCall>(context.containingDeclarations)
    }

  }
}
