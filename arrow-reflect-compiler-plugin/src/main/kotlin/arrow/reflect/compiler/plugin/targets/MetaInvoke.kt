package arrow.reflect.compiler.plugin.targets

import arrow.meta.FirMetaContext
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.resolve.fqName
import kotlin.reflect.KClass

class MetaInvoke(val session: FirSession, val metaTargets: List<MetaTarget>) {

  inline operator fun <reified In1, reified In2, reified In3, reified Out> invoke(
    unresolvedAnnotations: Boolean,
    metaContext: FirMetaContext,
    annotations: List<FirAnnotation>,
    superType: KClass<*>,
    methodName: String,
    arg: In1,
    arg2: In2,
    arg3: In3,
  ): Out? {
    val args = listOf(In1::class, In2::class, In3::class)
    val retType = Out::class
    return MetaTarget.find(
      unresolvedAnnotations,
      annotations.mapNotNull { it.fqName(session)?.asString() }.toSet(),
      methodName,
      superType,
      MetagenerationTarget.Fir,
      args,
      metaTargets
    )?.let { target ->
      val result = target.method.invoke(target.companion.objectInstance, metaContext, arg, arg2, arg3)
      result as? Out
    }
  }

  inline operator fun <reified Out> invoke(
    unresolvedAnnotations: Boolean,
    metaContext: FirMetaContext,
    annotations: List<FirAnnotation>,
    superType: KClass<*>,
    methodName: String,
  ): Out? {
    val args = emptyList<KClass<*>>()
    val retType = Out::class
    return MetaTarget.find(
      unresolvedAnnotations,
      annotations.mapNotNull { it.fqName(session)?.asString() }.toSet(),
      methodName,
      superType,
      MetagenerationTarget.Fir,
      args,
      metaTargets
    )
      ?.let { target ->
        val result = target.method.invoke(target.companion.objectInstance, metaContext)
        result as? Out
      }
  }

  inline operator fun <reified In, reified Out> invoke(
    unresolvedAnnotations: Boolean,
    metaContext: FirMetaContext,
    annotations: List<FirAnnotation>,
    superType: KClass<*>,
    methodName: String,
    arg: In,
  ): Out? {
    val args = listOf(In::class)
    val retType = Out::class
    return MetaTarget.find(
      unresolvedAnnotations,
      annotations.mapNotNull { it.fqName(session)?.asString() }.toSet(),
      methodName,
      superType,
      MetagenerationTarget.Fir,
      args,
      metaTargets
    )
      ?.let { target ->
        val result = target.method.invoke(target.companion.objectInstance, metaContext, arg)
        result as? Out
      }
  }

  inline operator fun <reified In1, reified In2, reified Out> invoke(
    unresolvedAnnotations: Boolean,
    metaContext: FirMetaContext,
    annotations: List<FirAnnotation>,
    superType: KClass<*>,
    methodName: String,
    arg: In1,
    arg2: In2,
  ): Out? {
    val args = listOf(In1::class, In2::class)
    val retType = Out::class
    return MetaTarget.find(
      unresolvedAnnotations,
      annotations.mapNotNull { it.fqName(session)?.asString() }.toSet(),
      methodName,
      superType,
      MetagenerationTarget.Fir,
      args,
      metaTargets
    )
      ?.let { target ->
        val result = target.method.invoke(target.companion.objectInstance, metaContext, arg, arg2)
        result as? Out
      }
  }
}
