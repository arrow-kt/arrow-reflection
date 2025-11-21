package arrow.meta.module.impl.arrow.meta.macro.compilation

import arrow.meta.module.impl.arrow.meta.macro.compilation.transformclassfactory.TransformClassFactory
import arrow.meta.module.impl.arrow.meta.quote.Kotlin
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirDeclaration

data class TransformClassContext(
  val session: FirSession,
  private val scope: List<FirDeclaration>
) {

  fun Kotlin(scope: FirClass, code: () -> String): Kotlin {
    return Kotlin(session = session, scope = listOf(scope), code = code)
  }
}

interface TransformClassCompilation : MacroCompilation {

  fun transform(context: TransformClassContext): TransformClassFactory
}
