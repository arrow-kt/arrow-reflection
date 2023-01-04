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
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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
      val nameArg = expression
        .disallowLambdaCaptureAnnotation()?.findArgumentByName(Name.identifier(DisallowLambdaCapture::msg.name))
      val userMsg =
        if (nameArg is FirConstExpression<*> && nameArg.kind == ConstantValueKind.String) nameArg.value as? String
        else null
      scopeDeclarations.filterIsInstance<FirAnonymousFunction>().forEach { scope ->
        if (scope != null && scope.inlineStatus != InlineStatus.Inline) {
          expression.report(
            UnsafeCaptureDetected,
            userMsg
              ?: "detected call to member @DisallowLambdaCapture `${+expression}` in non-inline anonymous function"
          )
        }
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

interface Raise<in E> {
  @DisallowLambdaCapture("It's unsafe to capture `raise` inside non-inline anonymous functions")
  fun raise(e: E): Nothing
}

context(Raise<String>)
fun shouldNotCature(): () -> Unit {
  return { raise("boom") }
}

context(Raise<String>)
fun inlineCaptureOk(): Unit {
  listOf(1, 2, 3).map { raise("boom") }
}

@OptIn(ExperimentalContracts::class)
fun exactlyOne(f: () -> Unit): Unit {
  contract {
    callsInPlace(f, InvocationKind.EXACTLY_ONCE)
  }
}

@OptIn(ExperimentalContracts::class)
fun exactlyOnce(f: () -> Unit): Unit {
  contract {
    callsInPlace(f, InvocationKind.EXACTLY_ONCE)
  }
}

context(Raise<String>)
fun ok(): () -> Unit = {
  listOf(1).map { raise("boom") }
}

fun main() {
  val raise = object : Raise<String> {
    override fun raise(e: String): Nothing = TODO()
  }
  val leaked = raise.run {
    ok()
  }
  leaked()
}


