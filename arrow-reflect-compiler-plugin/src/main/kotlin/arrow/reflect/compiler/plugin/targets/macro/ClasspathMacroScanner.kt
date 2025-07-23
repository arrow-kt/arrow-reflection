package arrow.reflect.compiler.plugin.targets.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import io.github.classgraph.ClassGraph

object ClasspathMacroScanner {

  fun classpathMacroScanner(): List<MacroTarget> {
    val result = ClassGraph().acceptPackages("arrow.meta.module.impl").enableAllInfo().scan()
    val macros = result.getClassesWithMethodAnnotation(Macro::class.java)
    val targets = macros.flatMap { it.methodInfo }.map { methodInfo ->
      val params = methodInfo.parameterInfo.map { param -> param.typeDescriptor }
      MacroTarget(
        params = params,
        method = methodInfo.loadClassAndGetMethod()
      )
    }
    return targets
  }
}
