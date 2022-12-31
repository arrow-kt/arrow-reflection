package arrow.meta.macros

import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import kotlin.reflect.KClass

data class Kotlin<out Out: FirElement> (val target: KClass<out Out>, @Language("kotlin") val value: String) {
  override fun toString(): String =
    value
}

inline fun <reified E: FirElement> Kotlin(@Language("kotlin") value: String): Kotlin<E> =
  Kotlin(E::class, value)

sealed interface Transform<in In: FirElement, out Out: FirElement> {
  class Replace<In: FirElement, Out: FirElement>(val invoke: (In) -> Kotlin<Out>) : Transform<In, Out>
  sealed interface Generate<In : FirElement, Out: FirElement> : Transform<In, Out> {
    sealed interface TopLevel<In: FirElement, Out: FirDeclaration> : Generate<In, Out> {
      class Class<In: FirElement, Out: FirClass>(val invoke: (In, ClassId?) -> Kotlin<Out>) : TopLevel<In, Out>
      class Function<In : FirElement, Out: FirSimpleFunction>(val invoke: (In, CallableId?) -> Kotlin<Out>) : TopLevel<In, Out>
      class Property<In: FirElement, Out: FirProperty>(val invoke: (In, CallableId?) -> Kotlin<Out>) : TopLevel<In, Out>
    }
    sealed interface Member<In: FirElement, Out: FirElement> : Generate<In, Out> {
      class NestedClass<In: FirElement, Out: FirClass>(val invoke: (In, ClassId?) -> Kotlin<Out>) : Member<In, Out>
      class Function<In : FirElement, Out: FirSimpleFunction>(val invoke: (In, CallableId?) -> Kotlin<Out>) : Member<In, Out>
      class Property<In: FirElement, Out: FirProperty>(val invoke: (In, CallableId?) -> Kotlin<Out>) : Member<In, Out>
    }
  }
}
