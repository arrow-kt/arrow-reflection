package arrow.reflect.compiler.plugin.targets.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import io.github.classgraph.AnnotationInfo
import io.github.classgraph.ClassGraph
import kotlin.reflect.KClass

object ClasspathMacroScanner {

  fun scanMacros(): MacroInvoke {
    val macros = classpathMacroScanner()
    return MacroInvoke.build(macros = macros)
  }

  private fun classpathMacroScanner(): List<MacroTarget> {
    val result = ClassGraph().acceptPackages("arrow.meta.module.impl").enableAllInfo().scan()
    val macros = result.getClassesWithMethodAnnotation(Macro::class.java)
    val targets = macros.flatMap {
      it.methodInfo
    }.filter { methodInfo ->
      methodInfo.hasAnnotation(Macro::class.java)
    }.map { methodInfo ->
      val params = methodInfo.parameterInfo.map { param ->
        try { Class.forName(param.typeDescriptor.toString()).kotlin } catch (_: Exception) { null }
      }.filterNotNull()
      val annotationInfo = methodInfo.annotationInfo.firstOrNull { it.name == Macro::class.java.name }
      val targetClass = annotationInfo?.targetParameter()
      MacroTarget(
        params = params,
        targetClass = targetClass,
        method = methodInfo.loadClassAndGetMethod()
      )
    }
    return targets
  }

  private fun AnnotationInfo?.targetParameter(): KClass<*>? {
    return try {
      val klass = (this?.parameterValues?.find { it.name == "target" }?.value ?: Nothing::class).toString().replace(".class", "")
      if (klass == Void::class.java.canonicalName) {
        null
      } else {
        Class.forName(klass).kotlin
      }
    } catch (_: Exception) {
      null
    }
  }
}
