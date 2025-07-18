package arrow.reflect.compiler.plugin.targets

import arrow.meta.module.Module
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfoList

internal object ClasspathMetaScanner {
  internal fun classPathMetaTargets(): List<MetaTarget> {
    val result = ClassGraph().acceptPackages("arrow.meta.module.impl").enableAllInfo()
      .scan()
    val metaModuleImpls: ClassInfoList = result.getClassesImplementing(Module::class.java)
    val metaTypes = metaModuleImpls.flatMap { moduleInfo ->
      // Get the property getter methods (getIncrement, getProduct, etc.)
      val getters = moduleInfo.declaredMethodInfo.filter { methodInfo ->
        methodInfo.name.startsWith("get") && methodInfo.parameterInfo.isEmpty()
      }
      getters.map { getter ->
        getter.typeSignatureOrTypeDescriptor.resultType.toString()
      }
    }
    val classesAndCompanions = metaTypes.mapNotNull { companionClassName ->
      // The companion class name is like "arrow.meta.samples.Increment$Companion"
      // We need to extract the annotation class name (without $Companion)
      val annotationClassName = companionClassName.removeSuffix("\$Companion")
      
      val annotationClass = try {
        Class.forName(annotationClassName)
      } catch (e: Exception) {
        null
      }
      
      val companionClass = try {
        Class.forName(companionClassName.replace('$', '.'))
      } catch (e: Exception) {
        try {
          // Try with the $ notation
          Class.forName(companionClassName)
        } catch (e2: Exception) {
          null
        }
      }
      
      if (annotationClass != null && companionClass != null) {
        annotationClass to companionClass
      } else null
    }
    val targets = classesAndCompanions.flatMap { (klass, companion) ->
      companion.declaredMethods.map {
        val target = MetagenerationTarget.Fir
        MetaTarget(
          klass.kotlin,
          target,
          companion.kotlin,
          it.parameters.map { it.type.kotlin },
          it.returnType.kotlin,
          it
        )
      }
    }
    return targets
  }
}
