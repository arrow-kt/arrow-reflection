package arrow.reflect.compiler.plugin.targets.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import io.github.classgraph.ClassGraph

object ClasspathMacroScanner {

  fun classpathMacroScanner(): List<MacroTarget> {
    val result = ClassGraph().acceptPackages("arrow.meta.module.impl").enableAllInfo().scan()
    val macros = result.getClassesWithMethodAnnotation(Macro::class.java)
    val targets = macros.flatMap {
      it.methodInfo
    }.filter { methodInfo ->
      methodInfo.hasAnnotation(Macro::class.java)
    }.map { methodInfo ->
      val params = methodInfo.parameterInfo.map { param -> param.typeDescriptor }

      val annotationInfo = methodInfo.annotationInfo.firstOrNull { it.name == Macro::class.java.name }

      val targetAnnotations: List<String> = annotationInfo?.parameterValues?.find { it.name == "targets" }?.value?.let { value ->
        (value as? Array<*>)?.map { it.toString() }
      } ?: emptyList()

      MacroTarget(
        params = params,
        targetAnnotations = targetAnnotations,
        method = methodInfo.loadClassAndGetMethod()
      )
    }
    return targets
  }
}
