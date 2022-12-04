package arrow.meta.samples

import arrow.meta.IrMetaContext
import arrow.meta.Meta
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Increment {
  companion object : Meta.Transform.Const {
    override fun IrMetaContext.transform(expression: IrConst<*>): IrExpression =
      //language=kotlin
      """
        ${+expression} + 1
      """.const
  }
}
