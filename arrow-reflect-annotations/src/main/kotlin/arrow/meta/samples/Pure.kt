package arrow.meta.samples

import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import kotlin.annotation.AnnotationTarget.*


@Target(CLASS, PROPERTY, CONSTRUCTOR, FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Pure

@Pure
fun restrictEffects(fn: FirSimpleFunction) {
  // Function implementation
}



