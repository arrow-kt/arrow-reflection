package arrow.meta.samples

import arrow.meta.Diagnostics
import arrow.meta.FirMetaCheckerContext
import arrow.meta.Meta
import arrow.meta.samples.Errors.META_LOG
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.expressions.FirExpression
import kotlin.annotation.AnnotationTarget.*

object Errors : Diagnostics.Error {
  val META_LOG by error1()
}

@Target(
  CLASS,
  ANNOTATION_CLASS,
  TYPE_PARAMETER,
  PROPERTY,
  FIELD,
  LOCAL_VARIABLE,
  VALUE_PARAMETER,
  CONSTRUCTOR,
  FUNCTION,
  PROPERTY_GETTER,
  PROPERTY_SETTER,
  TYPE,
  EXPRESSION,
  FILE,
  TYPEALIAS
)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Log {
  companion object : Meta.Checker.Declaration<FirDeclaration>, Meta.Checker.Expression<FirExpression>, Diagnostics(META_LOG) {

    fun FirMetaCheckerContext.checkElement(
      expression: FirElement
    ) {
      expression.report(
        META_LOG, "found error on expression: ${+expression}"
      )
    }

    override fun FirMetaCheckerContext.check(
      declaration: FirDeclaration,
    ) = checkElement(declaration)

    override fun FirMetaCheckerContext.check(
      expression: FirExpression
    ) = checkElement(expression)
  }

}
