package arrow.meta.samples

import arrow.meta.Diagnostics
import arrow.meta.FirMetaCheckerContext
import arrow.meta.Meta
import arrow.meta.samples.pure.createCallGraph
import arrow.meta.samples.pure.render
import arrow.meta.samples.pure.unsafeCalls
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.org.objectweb.asm.*
import kotlin.annotation.AnnotationTarget.*


object PureErrors : Diagnostics.Error {
  val CallGraphIncludesIO by error1()
}

val restrictedNameSpaces = setOf(
  FqName("kotlin.io"),
  FqName("java.io"),
  FqName("foo.bar")
)

@Target(
  CLASS, PROPERTY, CONSTRUCTOR, FUNCTION
)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Pure {
  companion object : Meta.Checker.Declaration<FirDeclaration>, Diagnostics(PureErrors.CallGraphIncludesIO) {

    override fun FirMetaCheckerContext.check(
      declaration: FirDeclaration,
    ) {
      if (declaration is FirSimpleFunction) {
        val callGraph = createCallGraph(null, declaration)
        val unsafeCallsInGraph = callGraph.unsafeCalls(restrictedNameSpaces)
        println(callGraph.render())
        unsafeCallsInGraph.forEach {
          it.localCall?.report(PureErrors.CallGraphIncludesIO, "Detected unsafe call at ${it.compilerMessage()}")
            ?: declaration.report(PureErrors.CallGraphIncludesIO, "Detected unsafe call at ${it.compilerMessage()}")
        }
      }
    }

  }

}




