package arrow.meta.samples.pure

import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import java.lang.reflect.Method

class ProcessingCache(
  val processingMethods: MutableSet<Method>,
  val processingFirFunctions: MutableSet<FirSimpleFunction>,
  val processedLocals: MutableSet<LocalFunction>,
  val processedRemote: MutableSet<RemoteFunction>
) {
  fun add(analyzedFunction: AnalyzedFunction) {
    when (analyzedFunction) {
      is LocalFunction -> processedLocals.add(analyzedFunction)
      is RemoteFunction -> processedRemote.add(analyzedFunction)
      is RecursiveFirCall -> {}
      is RecursiveMethodCall -> {}
    }
  }

  fun localFunction(fn: FirSimpleFunction): LocalFunction? =
    processedLocals.firstOrNull { it.value == fn }

  fun remoteFunction(fn: Method): RemoteFunction? =
    processedRemote.firstOrNull { it.method == fn }
}
