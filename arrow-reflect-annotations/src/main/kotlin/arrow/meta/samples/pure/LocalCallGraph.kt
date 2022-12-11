package arrow.meta.samples.pure

import arrow.meta.FirMetaCheckerContext
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.resolvedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid

internal fun FirMetaCheckerContext.createLocalCallGraph(
  call : FirFunctionCall?,
  function: FirSimpleFunction, cache: ProcessingCache
): LocalFunction {
  val existingReference = cache.localFunction(function)
  return if (existingReference == null) {
    val functions = function.calledMethods()
    val childGraphs = functions.map { (call, firFunction) ->
      if (firFunction !in cache.processingFirFunctions) {
        cache.processingFirFunctions.add(firFunction)
        createCallGraph(call, firFunction, cache)
      } else cache.localFunction(firFunction) ?: RecursiveFirCall(call, firFunction) //recursive ends here
    }
    LocalFunction(call, function, childGraphs.toSet()).also {
      cache.processedLocals.add(it)
    }
  } else existingReference
}


@OptIn(SymbolInternals::class)
private fun FirSimpleFunction.calledMethods(): Map<FirFunctionCall, FirSimpleFunction> {
  val calledMethods = mutableMapOf<FirFunctionCall, FirSimpleFunction>()
  val visitor = object : FirVisitorVoid() {
    override fun visitElement(element: FirElement) {
      element.acceptChildren(this)
    }

    override fun visitFunctionCall(functionCall: FirFunctionCall) {
      // If the current element is a function call, add it to the list of vertices

      // Look up the callee reference element for the function call
      val callee = functionCall.calleeReference.resolvedSymbol?.fir
      if (callee is FirSimpleFunction) {
        calledMethods[functionCall] = callee
      }
    }
  }
  accept(visitor)

  return calledMethods
}
