package arrow.meta.samples.pure

import arrow.meta.module.impl.arrow.meta.macro.compilation.DiagnosticsContext
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.resolve.firClassLike
import org.jetbrains.kotlin.load.kotlin.JvmPackagePartSource
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

fun DiagnosticsContext.createCallGraph(
  call: FirFunctionCall?,
  function: FirSimpleFunction,
  cache: ProcessingCache = ProcessingCache(mutableSetOf(), mutableSetOf(), mutableSetOf(), mutableSetOf())
): AnalyzedFunction {
  return if (function.origin == FirDeclarationOrigin.Library) {
    val classId = (function.containerSource as? JvmPackagePartSource)?.knownJvmBinaryClass?.classId
    val callableId = function.symbol.callableId
    val fnClass = classId?.asFqNameString() ?: error("no class found for $callableId")
    val fnClassType = Class.forName(fnClass)
    val argClasses =
      function.valueParameters.mapNotNull { it.returnTypeRef.firClassLike(session)?.symbol?.classId }.mapNotNull {
        if (it == ClassId(FqName("kotlin"), Name.identifier("Any"))) java.lang.Object::class.java
        else Class.forName(it.asFqNameString())
      }.toSet()
    val foundMethod = fnClassType.declaredMethods.find {
      val paramsClasses = it.parameters.map { it.type }
      val allParamsInOriginalFunction = paramsClasses.all { it in argClasses }
      it.name == function.name.asString() && allParamsInOriginalFunction
    }
    if (foundMethod != null) {
      createLibraryCallGraph(call, foundMethod, cache)
    } else LocalFunction(call, function, emptySet())
  } else {
    createLocalCallGraph(call, function, cache)
  }
}
