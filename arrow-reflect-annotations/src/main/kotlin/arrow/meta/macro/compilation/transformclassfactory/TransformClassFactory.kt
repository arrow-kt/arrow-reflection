package arrow.meta.module.impl.arrow.meta.macro.compilation.transformclassfactory

import arrow.meta.module.impl.arrow.meta.macro.compilation.TransformClassContext
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction

class TransformClassFactory(
  val firClass: FirClass,
  val context: TransformClassContext
) {

  private val states: MutableList<TransformClassState> = mutableListOf()

  fun FirSimpleFunction?.create() {
    if (this == null) return
    states.add(
      TransformClassState.Function(
        context = firClass,
        firSimpleFunction = this
      )
    )
  }

  fun FirProperty?.create() {
    if (this == null) return
    states.add(
      TransformClassState.Property(
        context = firClass,
        firProperty = this
      )
    )
  }

  fun states(): List<TransformClassState> = states
}

sealed interface TransformClassState {

  data class Function(
    val context: FirClass,
    val firSimpleFunction: FirSimpleFunction
  ) : TransformClassState

  data class Property(
    val context: FirClass,
    val firProperty: FirProperty
  ) : TransformClassState
}
