package arrow.reflect.compiler.plugin.targets

import arrow.meta.FirMetaCheckerContext
import arrow.meta.FirMetaMemberGenerationContext
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.isSubclassOf

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
      unresolvedAnnotations: Boolean,
      annotations : Set<String>,
      methodName: String,
      supertype: KClass<*>?,
      target: MetagenerationTarget,
      args: List<KClass<*>>,
      targets: List<MetaTarget>
    ): MetaTarget? =
      targets.find {
        (unresolvedAnnotations || it.annotation.java.canonicalName in annotations) &&
        (supertype == null || it.companion.allSuperclasses.any {
          it == supertype
        }) &&
          it.method.name == methodName &&
          it.target == target &&
          ((listOf(FirMetaCheckerContext::class) + args).subtypesOf(it.args) ||
          (listOf(FirMetaMemberGenerationContext::class) + args).subtypesOf(it.args))
//          toKotlin(it.returnType) == returnType
      }

    private fun toKotlin(klass: KClass<*>): KClass<*> =
      when (klass) {
        Void::class -> Unit::class
        else -> klass
      }

    private fun List<KClass<*>>.subtypesOf(other : List<KClass<*>>): Boolean =
      size == other.size && this.zip(other).all { (a, b) ->
        a == b || a.isSubclassOf(b)
      }
  }


}
