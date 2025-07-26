package arrow.reflect.compiler.plugin.targets.macro

import java.lang.reflect.Method
import kotlin.reflect.KClass

data class MacroTarget(
  val params: List<KClass<*>>,
  val targetClass: KClass<*>?,
  val method: Method
)
