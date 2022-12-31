package arrow.meta.samples

import arrow.meta.FirMetaCheckerContext
import arrow.meta.FirMetaContext
import arrow.meta.Meta
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.*
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.fir.visitors.FirVisitor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance
import kotlin.reflect.*


@Target(AnnotationTarget.EXPRESSION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Reflect {
  companion object :
    //Meta.FrontendTransformer.RegularClass,
    Meta.FrontendTransformer.GetClassCall,
    Meta.Generate.TopLevel.Class {

    override fun FirMetaContext.classes(): Set<ClassId> {
      val reflected = session.predicateBasedProvider.getSymbolsByPredicate(LookupPredicate.create { annotatedOrUnder(FqName(Reflect::class.java.canonicalName)) })
      return reflected.filterIsInstance<FirClassLikeSymbol<*>>().map { ClassId.fromString(it.classId.asFqNameString().replace(".", "/") + Reflect::class.java.simpleName) }.toSet()
    }

    @OptIn(SymbolInternals::class)
    override fun FirMetaContext.classes(classId: ClassId): FirClass {
      val targetClassId = ClassId.fromString(classId.asSingleFqName().asString().removeSuffix(Reflect::class.java.simpleName))
      val target = session.symbolProvider.getClassLikeSymbolByClassId(targetClassId)?.fir as? FirRegularClass

      return target?.let { compile(kClass(it)) } ?: error("Could not find class $targetClassId")
    }

//    override fun FirMetaMemberGenerationContext.nestedClasses(firClass: FirClassSymbol<*>): Set<Name> {
//      val isReflected =
//        firClass.annotations.any { it.annotationTypeRef.source?.text == Reflect::class.java.simpleName }
//      return if (isReflected) {
//        setOf(Name.identifier("Reflected"))
//      } else emptySet()
//    }

//    @OptIn(SymbolInternals::class)
//    override fun FirMetaMemberGenerationContext.nestedClasses(classId: ClassId): List<FirClass> =
//      classId.parentClassId?.let { parentId ->
//        val parent = session.symbolProvider.getClassLikeSymbolByClassId(parentId)?.fir as? FirRegularClass
//        parent?.declarations?.filterIsInstance<FirClass>()?.filter { classId == it.classId } ?: emptyList()
//      } ?: emptyList()

    override fun FirMetaCheckerContext.getClassCall(getClassCall: FirGetClassCall): FirStatement {
      return "${+getClassCall.argument.typeRef}${Reflect::class.java.simpleName}()".call
    }

//    override fun FirMetaCheckerContext.regularClass(regularClass: FirRegularClass): FirStatement {
//      val kClassSource = kClass(regularClass)
//      return regularClass.addDeclarations(compile<FirClass>(kClassSource))
//    }
  }
}

interface CompileTimeReflected<T : Any> : KClass<T>

@OptIn(SymbolInternals::class)
fun FirMetaContext.kClass(firClass: FirRegularClass): String {
  val supertypes = firClass.superTypeRefs
  val constructors = firClass.indexedDeclarations<FirConstructor>()
  val properties = firClass.indexedDeclarations<FirProperty>()
  val functions = firClass.indexedDeclarations<FirSimpleFunction>()
  val nestedClasses = firClass.indexedDeclarations<FirRegularClass>()
  val sealedSubclasses = when {
    firClass.isSealed -> firClass
      .getSealedClassInheritors(session)
      .mapNotNull { session.symbolProvider.getClassLikeSymbolByClassId(it)?.fir }
      .filterIsInstance<FirRegularClass>()

    else -> emptyList()
  }
  //language=kotlin
  return """
     import kotlin.reflect.*

     class Reflected : arrow.meta.samples.CompileTimeReflected<${firClass.classId.asFqNameString()}> {

       // constructors 
       ${constructors.joinToString("\n") { (n, it) -> kFunction(n, it) }}
       
       // properties 
       ${properties.joinToString("\n") { (_, it) -> kProperty(firClass, it) }} 
       
       //functions
       ${functions.joinToString("\n") { (n, it) -> kFunction(n, it) }}

       //nested classes
       ${nestedClasses.joinToString("\n") { (_, it) -> kClass(it) }}

       //sealed subclases
       ${sealedSubclasses.joinToString("\n") { kClass(it) }} 
        

       override val annotations: List<Annotation> by lazy { ${reflectedAnnotations(firClass)} }

       override val constructors: Collection<KFunction<${firClass.classId.asFqNameString()}>> by lazy {
            listOf(${constructors.joinToString { (n, it) -> "${kFunctionName(n, it)}()" }})
       }
       
       override val members: Collection<KCallable<*>> by lazy {
            listOf(${properties.joinToString { (_, it) -> "${+it.name}()" }}) + 
              listOf(${functions.joinToString { (n, it) -> "${kFunctionName(n, it)}()" }})
       }
       

       override val nestedClasses: Collection<KClass<*>> by lazy { 
            listOf(${nestedClasses.joinToString { (_, it) -> "${+it.name}()" }}) 
       }
       
       override val sealedSubclasses: List<KClass<out ${firClass.classId.asFqNameString()}>> by lazy {
            listOf(${sealedSubclasses.joinToString { "${+it.name}()" }})
       }
       
       
       override val supertypes: List<KType> by lazy { 
            listOf(${supertypes.joinToString { kType(it) }})
       }
        

       override val isAbstract: Boolean by lazy { ${firClass.isAbstract} }
       override val isCompanion: Boolean by lazy { ${firClass.isCompanion} }
       override val isData: Boolean by lazy { ${firClass.isData} }
       override val isFinal: Boolean by lazy { ${firClass.isFinal} }
       override val isFun: Boolean by lazy { ${firClass.isFun} }
       override val isInner: Boolean by lazy { ${firClass.isInner} }
       override val isOpen: Boolean by lazy { ${firClass.isOpen} }
       override val isSealed: Boolean by lazy { ${firClass.isSealed} }
       override val isValue: Boolean by lazy { ${firClass.isInline} }
       override val objectInstance: ${firClass.classId.asFqNameString()}? by lazy { 
            ${if (firClass.classKind == ClassKind.OBJECT) firClass.classId.asFqNameString() else null} 
       }
       override val qualifiedName: String by lazy { "${firClass.classId.asFqNameString()}" }
       override val simpleName: String by lazy { "${firClass.classId.shortClassName}" }
       override val typeParameters: List<KTypeParameter> by lazy { 
            listOf(${firClass.typeParameters.joinToString { kTypeParameter(it.symbol.fir) }}) 
       }
       override val visibility: KVisibility? by lazy { ${kVisibility(firClass.visibility)} }

       override fun equals(other: Any?): Boolean =
         TODO() //${firClass.classId.asFqNameString()}::class.equals(other)

       override fun hashCode(): Int =
         TODO() //${firClass.classId.asFqNameString()}::class.hashCode()

       override fun isInstance(value: Any?): Boolean =
         value is ${firClass.classId.asFqNameString()}
     }
  """
}

private inline fun <reified D : FirDeclaration> FirRegularClass.indexedDeclarations(): List<Pair<Int, D>> =
  declarations.filterIsInstance<D>().filter { !it.isSynthetic }.mapIndexed { index, t -> index to t }


private fun FirMetaContext.reflectedAnnotations(container: FirAnnotationContainer): String =
  """listOf(${
    container.annotations.mapNotNull { ann -> ann.fqName(session)?.let { ann to it.asString() } }
      .joinToString { (ann, fqName) -> "$fqName(${ann.argumentMapping.mapping.entries.joinToString { (k, v) -> "${+k} = ${+v}" }})" }
  })"""

@OptIn(SymbolInternals::class)
private fun FirMetaContext.kFunction(n: Int, fn: FirFunction): String =
  source(
    """
inner class ${kFunctionName(n, fn)} : KFunction<${+fn.returnTypeRef}> {
  override val annotations: List<Annotation> by lazy { ${reflectedAnnotations(fn)} }
  override val isAbstract: Boolean by lazy { ${fn.isAbstract} }
  override val isFinal: Boolean by lazy { ${fn.isFinal} }
  override val isOpen: Boolean by lazy { ${fn.isOpen} }
  override val name: String by lazy { "${name(fn)}" }
  override val parameters: List<KParameter> by lazy {
    listOf(${fn.valueParameters.mapIndexed { i, p -> kParameter(i, p) }.joinToString()})
  }
  override val returnType: KType by lazy { ${kType(fn.returnTypeRef)} }
  override val typeParameters: List<KTypeParameter> by lazy {
    listOf(${fn.typeParameters.joinToString { kTypeParameter(it.symbol.fir) }})
  }
  override val visibility: KVisibility? by lazy { ${kVisibility(fn.visibility)} }

  override fun call(vararg args: Any?): ${+fn.returnTypeRef} {
    TODO("Not yet implemented")
  }

  override fun callBy(args: Map<KParameter, Any?>): ${+fn.returnTypeRef} {
    TODO("Not yet implemented")
  }

  override val isExternal: Boolean by lazy { ${fn.isExternal} }
  override val isInfix: Boolean by lazy { ${fn.isInfix} }
  override val isInline: Boolean by lazy { ${fn.isInline} }
  override val isOperator: Boolean by lazy { ${fn.isOperator} }
  override val isSuspend: Boolean by lazy { ${fn.isSuspend} }
}
"""
  )

private fun FirMetaContext.kFunctionName(n: Int, fn: FirFunction): String =
  when (fn) {
    is FirSimpleFunction -> +fn.name
    is FirConstructor -> "constructor$n"
    else -> error("expected FirSimpleFunction or FirConstructor")
  }

private fun name(member: FirMemberDeclaration): String =
  when (member) {
    is FirSimpleFunction -> member.name.asString()
    is FirConstructor -> Name.special("<init>").asString()
    else -> Name.special("<anonymous>").asString()
  }

@OptIn(SymbolInternals::class)
private fun FirMetaContext.kType(type: FirTypeRef): String =
  source(
    """
  object : KType {
    override val annotations: List<Annotation> by lazy { ${reflectedAnnotations(type)} }
    override val arguments: List<KTypeProjection> by lazy {
      listOf(${type.extractTypeArguments().joinToString { kTypeProjection(it) }})
    }
    override val classifier: KClassifier? by lazy {
      null //${
      type.coneType.toRegularClassSymbol(session)?.fir?.classId?.asFqNameString()?.let { "$it::class" } ?: "null"
    }
    }
    override val isMarkedNullable: Boolean by lazy { ${type.isMarkedNullable} }
  }
"""
  )

internal class ReflectTypeProjectionWithVariance(
  override val source: KtSourceElement?,
  override var typeRef: FirTypeRef,
  override val variance: Variance,
) : FirTypeProjectionWithVariance() {
  override fun <R, D> acceptChildren(visitor: FirVisitor<R, D>, data: D) {
    typeRef.accept(visitor, data)
  }

  override fun <D> transformChildren(transformer: FirTransformer<D>, data: D): ReflectTypeProjectionWithVariance {
    typeRef = typeRef.transform(transformer, data)
    return this
  }
}

fun ProjectionKind.toVariance(): Variance =
  when (this) {
    ProjectionKind.STAR -> Variance.INVARIANT
    ProjectionKind.IN -> Variance.IN_VARIANCE
    ProjectionKind.OUT -> Variance.OUT_VARIANCE
    ProjectionKind.INVARIANT -> Variance.INVARIANT
  }

fun FirTypeRef.extractTypeArguments(): List<FirTypeProjection> {
  return when (this) {
    is FirResolvedTypeRef -> type.typeArguments.mapNotNull {
      val resolved = it.type?.toFirResolvedTypeRef()
      if (resolved != null) ReflectTypeProjectionWithVariance(null, resolved, it.kind.toVariance())
      else null
    }

    is FirUserTypeRef -> qualifier.flatMap { it.typeArgumentList.typeArguments.map { it } }
    else -> emptyList()
  }
}

private fun FirMetaContext.kProperty(owner: FirRegularClass, prop: FirProperty): String =
  source(
    """
  inner class ${+prop.name} : KProperty1<${owner.renderType()}, ${+prop.returnTypeRef}> {
    override val annotations: List<Annotation> by lazy {
      listOf()
    }
    override val isAbstract: Boolean by lazy { ${prop.isAbstract} }
    override val isFinal: Boolean by lazy { ${prop.isFinal} }
    override val isOpen: Boolean by lazy { ${prop.isOpen} }
    override val isSuspend: Boolean by lazy { ${prop.isSuspend} }
    override val name: String by lazy { "${+prop.name}" }
    override val parameters: List<KParameter> by lazy { ${kParameters(prop)} }
    override val returnType: KType by lazy { ${kType(prop.returnTypeRef)} }
    override val typeParameters: List<KTypeParameter> by lazy { listOf(${
      prop.typeParameters.joinToString {
        kTypeParameter(
          it
        )
      }
    }) }
    override val visibility: KVisibility? by lazy { ${kVisibility(prop.visibility)} }
  
    override fun call(vararg args: Any?): ${+prop.returnTypeRef} {
      TODO("Not yet implemented")
    }
  
    override fun callBy(args: Map<KParameter, Any?>): ${+prop.returnTypeRef} {
      TODO("Not yet implemented")
    }
  
    override val isConst: Boolean by lazy { ${prop.isConst} }
    override val isLateinit: Boolean by lazy { ${prop.isLateInit} }
    override val getter: KProperty1.Getter<${owner.renderType()}, ${+prop.returnTypeRef}> by lazy {
      TODO()
    }
  
    override fun get(receiver: ${owner.renderType()}): ${+prop.returnTypeRef} {
      TODO("Not yet implemented")
    }
  
    override fun getDelegate(receiver: ${owner.renderType()}): Any? {
      TODO("Not yet implemented")
    }
  
    override fun invoke(p1: ${owner.renderType()}): ${+prop.returnTypeRef} {
      TODO("Not yet implemented")
    }

  }
"""
  )

private fun FirRegularClass.renderType(): String =
  defaultType().type.renderReadableWithFqNames().replace("/", ".")


private fun FirMetaContext.kTypeParameter(type: FirTypeParameter): String =
  source(
    """
object : KTypeParameter {
  override val annotations: List<Annotation> by lazy { ${reflectedAnnotations(type)} }
  override val isReified: Boolean by lazy { ${type.isReified} }
  override val name: String by lazy { "${type.name}" }
  override val upperBounds: List<KType> by lazy {
    listOf(${type.bounds.joinToString { kType(it) }})
  }
  override val variance: KVariance by lazy { ${kVariance(type.variance)} }
}
"""
  )

private fun kVariance(variance: Variance): String =
  when (variance) {
    Variance.INVARIANT -> "Variance.INVARIANT"
    Variance.IN_VARIANCE -> "KVariance.IN"
    Variance.OUT_VARIANCE -> "KVariance.OUT"
  }

private fun kVisibility(visibility: Visibility): String =
  when (visibility) {
    Visibilities.Public -> "KVisibility.PUBLIC"
    Visibilities.Protected -> "KVisibility.PROTECTED"
    Visibilities.Internal -> "KVisibility.INTERNAL"
    Visibilities.Private -> "KVisibility.PRIVATE"
    else -> "" +
      "KVisibility.PUBLIC"
  }

private fun FirMetaContext.kParameters(fn: FirMemberDeclaration): String =
  if (fn is FirFunction)
    """listOf(${fn.valueParameters.mapIndexed { n, p -> kParameter(n, p) }.joinToString()})"""
  else "emptyList()"

private fun FirMetaContext.kParameter(n: Int, param: FirValueParameter): String =
  source(
    """
  object : KParameter {
    override val annotations: List<Annotation> by lazy { ${reflectedAnnotations(param)} }
    override val index: Int by lazy { $n }
    override val isOptional: Boolean by lazy { ${param.defaultValue != null} }
    override val isVararg: Boolean by lazy { ${param.isVararg} }
    override val kind: KParameter.Kind by lazy { TODO() }
    override val name: String? by lazy { "${param.name}" }
    override val type: KType by lazy { ${kType(param.returnTypeRef)} }
  }
"""
  )

private fun FirMetaContext.kTypeProjection(projection: FirTypeProjection): String =
  when (projection) {
    is FirStarProjection -> "KTypeProjection.STAR"
    is FirTypeProjectionWithVariance -> source(
      """
      KTypeProjection(${kVariance(projection.variance)}, ${kType(projection.typeRef)})
    """
    )

    else -> error("expected FirStarProjection or FirTypeProjectionWithVariance")
  }


