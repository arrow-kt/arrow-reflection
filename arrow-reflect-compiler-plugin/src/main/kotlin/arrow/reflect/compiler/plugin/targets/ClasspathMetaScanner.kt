package arrow.reflect.compiler.plugin.targets

import arrow.meta.module.Module
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfoList

internal object ClasspathMetaScanner {
  internal fun classPathMetaTargets(): List<MetaTarget> {
    val result = ClassGraph()
      .acceptPackages("arrow.meta.module.impl", "arrow.meta.quotes", "arrow.meta.quotes.module.impl")
      .enableAllInfo()
      .scan()
    val metaModuleImpls: ClassInfoList = result.getClassesImplementing(Module::class.java)
    val metaTypes = metaModuleImpls.flatMap { moduleInfo ->
      // Get the property getter methods (getIncrement, getProduct, etc.)
      val getters = moduleInfo.declaredMethodInfo.filter { methodInfo ->
        methodInfo.name.startsWith("get") && methodInfo.parameterInfo.isEmpty()
      }
      getters.map { getter ->
        val resultType = getter.typeSignatureOrTypeDescriptor.resultType.toString()
        resultType
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
        Class.forName(companionClassName)
      } catch (e: Exception) {
        null
      }
      
      if (annotationClass != null && companionClass != null) {
        annotationClass to companionClass
      } else null
    }
    val targets = classesAndCompanions.flatMap { (klass, companion) ->
      companion.declaredMethods.map { method ->
        val target = MetagenerationTarget.Fir
        MetaTarget(
          klass.kotlin,
          target,
          companion.kotlin,
          method.parameters.map { it.type.kotlin },
          method.returnType.kotlin,
          method
        )
      }
    }
    return targets
  }
}
