package arrow.reflect.compiler.plugin.targets.macro

import io.github.classgraph.TypeSignature
import java.lang.reflect.Method

data class MacroTarget(
  val params: List<TypeSignature>,
  val targetAnnotations: List<String>,
  val method: Method
)
