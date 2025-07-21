package arrow.reflect.compiler.plugin.targets

import arrow.meta.FirMetaContext
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.resolve.fqName
import kotlin.reflect.KClass

class MetaInvoke(val session: FirSession, val metaTargets: List<MetaTarget>) {

  @PublishedApi
  internal fun getCompanionInstance(companionClass: KClass<*>): Any {
    // For companion objects, they are Kotlin objects with a static field
    // The field name can vary based on Kotlin version
    val possibleFieldNames = listOf("\$\$INSTANCE", "INSTANCE", "Companion")
    
    for (fieldName in possibleFieldNames) {
      try {
        val field = companionClass.java.getDeclaredField(fieldName)
        if (java.lang.reflect.Modifier.isStatic(field.modifiers)) {
          field.isAccessible = true
          return field.get(null) ?: continue
        }
      } catch (e: NoSuchFieldException) {
        // Try next field name
      }
    }
    
    // Try using Kotlin reflection
    try {
      companionClass.objectInstance?.let { return it }
      companionClass.nestedClasses.firstOrNull()?.objectInstance?.let { return it }
    } catch (e: Exception) {
      // Ignore and fall through
    }
    
    throw IllegalStateException("Cannot find companion object instance for $companionClass. Fields: ${companionClass.java.declaredFields.map { it.name }}")
  }

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
      val companionInstance = getCompanionInstance(target.companion)
      val result = target.method.invoke(companionInstance, metaContext, arg, arg2, arg3)
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
        val companionInstance = getCompanionInstance(target.companion)
        val result = target.method.invoke(companionInstance, metaContext)
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
        val companionInstance = getCompanionInstance(target.companion)
        val result = target.method.invoke(companionInstance, metaContext, arg)
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
        val companionInstance = getCompanionInstance(target.companion)
        val result = target.method.invoke(companionInstance, metaContext, arg, arg2)
        result as? Out
      }
  }
}
