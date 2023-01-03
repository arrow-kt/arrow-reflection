package arrow.meta.samples

import arrow.meta.Diagnostics
import arrow.meta.FirMetaCheckerContext
import arrow.meta.Meta
import arrow.meta.samples.DisallowLambdaCaptureErrors.UnsafeCaptureDetected
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.InlineStatus
import org.jetbrains.kotlin.fir.declarations.findArgumentByName
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirConstExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.ConstantValueKind

object DisallowLambdaCaptureErrors : Diagnostics.Error {
  val UnsafeCaptureDetected by error1()
}

@Meta
@Target(AnnotationTarget.FUNCTION)
annotation class DisallowLambdaCapture(val msg: String = "") {
  companion object : Meta.Checker.Expression<FirFunctionCall>,
    Diagnostics(UnsafeCaptureDetected) {

    val annotation = DisallowLambdaCapture::class.java

    override fun FirMetaCheckerContext.check(expression: FirFunctionCall) {
      val scope = scopeDeclarations.filterIsInstance<FirAnonymousFunction>().firstOrNull()
      if (scope != null && scope.inlineStatus != InlineStatus.Inline) {
        val nameArg = expression
          .disallowLambdaCaptureAnnotation()?.findArgumentByName(Name.identifier(DisallowLambdaCapture::msg.name))
        val userMsg =
          if (nameArg is FirConstExpression<*> && nameArg.kind == ConstantValueKind.String) nameArg.value as? String
          else null
        expression.report(
          UnsafeCaptureDetected,
          userMsg ?: "detected call to member @DisallowLambdaCapture `${+expression}` in non-inline anonymous function"
        )
      }
    }

    private fun FirFunctionCall.disallowLambdaCaptureAnnotation(): FirAnnotation? =
      toResolvedCallableSymbol()?.fir?.getAnnotationByClassId(
        ClassId(
          FqName(annotation.`package`.name),
          Name.identifier(annotation.simpleName)
        )
      )
  }
}


