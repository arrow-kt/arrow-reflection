package arrow.meta.module.impl.arrow.meta.macro.compilation

import arrow.meta.module.impl.arrow.meta.quote.Kotlin
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

data class TransformClassContext(
  val session: FirSession,
  private val scope: List<FirDeclaration>
) {

  fun Kotlin(code: () -> String): Kotlin {
    return Kotlin(session = session, scope = scope, code = code)
  }

  fun Kotlin(scope: FirClass, code: () -> String): Kotlin {
    return Kotlin(session = session, scope = listOf(scope), code = code)
  }
}

interface TransformClassCompilation : MacroCompilation {

  fun transform(context: TransformClassContext): TransformClassFactory
}

class TransformClassFactory(
  val firClass: FirClass,
  private val context: TransformClassContext
) : MacroCompilation {

  private val states: MutableList<TransformClassState> = mutableListOf()

  fun function(build: TransformClassContext.() -> Kotlin) {
    val code = context.build()
    val function = code.firstIsInstanceOrNull<FirSimpleFunction>() ?: return
    states.add(
      TransformClassState.Function(
        firFile = code,
        firClass = firClass,
        firSimpleFunction = function
      )
    )
  }

  fun states(): List<TransformClassState> = states
}

sealed interface TransformClassState {

  data class Function(
    val firFile: Kotlin,
    val firClass: FirClass,
    val firSimpleFunction: FirSimpleFunction
  ) : TransformClassState
}
