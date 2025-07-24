package arrow.meta.module.impl.arrow.meta.macro

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
annotation class Macro(val targets: Array<KClass<*>> = [])
