package arrow.reflect.compiler.plugin.targets

import arrow.meta.module.Module
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfoList
import org.jetbrains.kotlin.ir.IrElement

internal object ClasspathMetaScanner {
  internal fun classPathMetaTargets(): List<MetaTarget> {
    val result = ClassGraph().acceptPackages("arrow.meta.module.impl").enableAllInfo()
      .scan()
    val metaModuleImpls: ClassInfoList = result.getClassesImplementing(Module::class.java)
    val metaTypes = metaModuleImpls.flatMap {
      it.declaredMethodInfo.map { it.typeSignatureOrTypeDescriptor.resultType.toString() }
    }
    val classesAndCompanions = metaTypes.mapNotNull {
      val klass = try {
        Class.forName(it)
      } catch (e: ClassNotFoundException) {
        null
      }
      val companion = klass?.declaredClasses?.firstOrNull()
      if (klass != null && companion != null) klass to companion
      else null
    }
    val targets = classesAndCompanions.flatMap { (klass, companion) ->
      companion.declaredMethods.map {
        val target =
          if (it.returnType.`package`.name.startsWith(IrElement::class.java.`package`.name))
            MetagenerationTarget.Ir
          else
            MetagenerationTarget.Fir
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
