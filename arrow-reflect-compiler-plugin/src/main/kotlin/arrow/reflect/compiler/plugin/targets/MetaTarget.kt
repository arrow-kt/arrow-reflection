package arrow.reflect.compiler.plugin.targets

import arrow.meta.FirMetaContext
import arrow.meta.IrMetaContext
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

data class MetaTarget(
  val annotation: KClass<*>,
  val target: MetagenerationTarget,
  val companion: KClass<*>,
  val args: List<KClass<*>>,
  val returnType: KClass<*>,
  val method: Method
) {

  companion object {
    fun find(
      annotations : Set<String>,
      methodName: String,
      supertype: KClass<*>?,
      target: MetagenerationTarget,
      args: List<KClass<*>>,
      returnType: KClass<*>,
      targets: List<MetaTarget>
    ): MetaTarget? =
      targets.find {
        (it.annotation.java.canonicalName in annotations) &&
        (supertype == null || it.companion.allSuperclasses.any {
          it == supertype
        }) &&
          it.method.name == methodName &&
          it.target == target &&
          (it.args == listOf(FirMetaContext::class) + args ||
            it.args == listOf(IrMetaContext::class) + args) &&
          toKotlin(it.returnType) == returnType
      }

    private fun toKotlin(klass: KClass<*>): KClass<*> =
      when (klass) {
        Void::class -> Unit::class
        else -> klass
      }
  }


}
