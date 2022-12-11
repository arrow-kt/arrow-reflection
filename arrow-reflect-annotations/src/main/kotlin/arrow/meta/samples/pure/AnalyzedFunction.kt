package arrow.meta.samples.pure

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi
import java.lang.reflect.Method


sealed class AnalyzedFunction {
  abstract val renderName: String
  abstract val calls: Set<AnalyzedFunction>
  abstract val localCall: FirFunctionCall?
  abstract fun compilerMessage(): CompilerMessageSourceLocation?
}

fun AnalyzedFunction.unsafeCalls(
  restrictedNameSpaces: Set<FqName>,
  acc: MutableSet<AnalyzedFunction> = mutableSetOf()
): Set<AnalyzedFunction> {
  when (this) {
    is LocalFunction -> {
      if (value.symbol.callableId.packageName in restrictedNameSpaces) {
        acc.add(this)
      }
      calls.flatMap { it.unsafeCalls(restrictedNameSpaces, acc) }.toSet()
    }

    is RecursiveFirCall -> emptySet()
    is RecursiveMethodCall -> emptySet()
    is RemoteFunction -> {
      if (FqName(method.declaringClass.`package`.name) in restrictedNameSpaces) {
        acc.add(this)
      }
      calls.flatMap { it.unsafeCalls(restrictedNameSpaces, acc) }.toSet()
    }
  }
  return acc
}


data class RecursiveFirCall(override val localCall: FirFunctionCall, val value: FirSimpleFunction) :
  AnalyzedFunction() {
  override val renderName: String = "[Recursive] ${value.symbol.callableId.asSingleFqName().asString()}"
  override val calls: Set<AnalyzedFunction> = emptySet()
  override fun compilerMessage(): CompilerMessageSourceLocation? =
    MessageUtil.psiElementToMessageLocation(localCall.source.psi)
}

data class RecursiveMethodCall(val value: Method) : AnalyzedFunction() {
  override val renderName: String = "[Recursive] ${value.declaringClass.canonicalName + "." + value.name}"
  override val calls: Set<AnalyzedFunction> = emptySet()
  override val localCall: FirFunctionCall? = null
  override fun compilerMessage(): CompilerMessageSourceLocation? = null
}

data class LocalFunction(
  override val localCall: FirFunctionCall?,
  val value: FirSimpleFunction,
  override val calls: Set<AnalyzedFunction>
) : AnalyzedFunction() {
  override val renderName: String = "[Local] ${value.symbol.callableId.asSingleFqName().asString()}"
  override fun compilerMessage(): CompilerMessageSourceLocation? =
    MessageUtil.createMessageLocation(
      /* path = */ value.symbol.callableId.asSingleFqName().asString(),
      /* lineContent = */ null,
      /* line = */ -1,
      /* column = */ -1,
      /* endLine = */ -1,
      /* endColumn = */ -1
    )
}

data class RemoteFunction(
  override val localCall: FirFunctionCall?,
  val method: Method,
  override val calls: Set<AnalyzedFunction>
) : AnalyzedFunction() {
  override val renderName: String = "[Binaries] ${method.declaringClass.canonicalName + "." + method.name}"

  override fun compilerMessage(): CompilerMessageSourceLocation? {
    val declaringClass = method.declaringClass
    val className = declaringClass.canonicalName
    val methodName = method.name

    return MessageUtil.createMessageLocation(
      /* path = */ "$className.$methodName",
      /* lineContent = */ null,
      /* line = */ -1,
      /* column = */ -1,
      /* endLine = */ -1,
      /* endColumn = */ -1
    )
  }

}

fun AnalyzedFunction.render(indentation: String = "", isLastChild: Boolean = true): String {
  val children = if (calls.isEmpty()) "" else calls.joinToString("") {
    val childIndentation = if (isLastChild) "$indentation    " else "$indentation│   "
    it.render(childIndentation, it == this@render.calls.last())
  }
  val connector = if (isLastChild) "└" else "├"
  return "$indentation$connector── $renderName\n$children"
}
