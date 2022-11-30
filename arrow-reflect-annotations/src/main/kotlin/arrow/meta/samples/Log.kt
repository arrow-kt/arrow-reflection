package arrow.meta.samples

import arrow.meta.Diagnostics
import arrow.meta.Meta
import arrow.meta.module.impl.arrow.meta.FirMetaContext
import arrow.meta.samples.Errors.META_LOG
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.renderWithType

object Errors : Diagnostics.Error {
  val META_LOG by error1()
}

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Log {
  companion object :
    Meta.Checker.Expression<FirExpression>,
    Diagnostics(META_LOG) {

    override fun FirMetaContext.check(expression: FirExpression, context: CheckerContext, reporter: DiagnosticReporter) {
      expression.report(
        META_LOG,
        expression.renderWithType(),
        context,
        reporter
      )
    }
  }

}
