package arrow.reflect.compiler.plugin.fir.codegen

import arrow.meta.FromTemplate
import arrow.meta.Meta
import arrow.meta.TemplateCompiler
import arrow.meta.module.impl.arrow.meta.FirMetaContext
import arrow.reflect.compiler.plugin.fir.checkers.isMetaAnnotated
import arrow.reflect.compiler.plugin.fir.checkers.metaAnnotations
import arrow.reflect.compiler.plugin.targets.MetaTarget
import arrow.reflect.compiler.plugin.targets.MetagenerationTarget
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.buildSimpleFunctionCopy
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildConstExpression
import org.jetbrains.kotlin.fir.expressions.impl.FirAnnotationArgumentMappingImpl
import org.jetbrains.kotlin.fir.extensions.AnnotationFqn
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.metaAnnotated
import org.jetbrains.kotlin.fir.resolve.constructType
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.ConeLookupTagBasedType
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.ConstantValueKind
import kotlin.reflect.KClass

class FirMetaCodegenExtension(
  session: FirSession,
  val templateCompiler: TemplateCompiler,
  val metaTargets: List<MetaTarget>
) : FirDeclarationGenerationExtension(session) {

  val metaContext = FirMetaContext(templateCompiler, session)

  private inline fun <reified Out> invokeMeta(annotations: List<FirAnnotation>, superType: KClass<*>, methodName: String): Out? {
    val args = emptyList<KClass<*>>()
    val retType = Out::class
    return MetaTarget.find(annotations.mapNotNull { it.fqName(session)?.asString() }.toSet(), methodName, superType, MetagenerationTarget.Fir, args, retType, metaTargets)?.let { target ->
      val result = target.method.invoke(target.companion.objectInstance, metaContext)
      result as? Out
    }
  }

  private inline fun <reified In, reified Out> invokeMeta(annotations: List<FirAnnotation>,superType: KClass<*>, methodName: String, arg: In): Out? {
    val args = listOf(In::class)
    val retType = Out::class
    return MetaTarget.find(annotations.mapNotNull { it.fqName(session)?.asString() }.toSet(), methodName, superType, MetagenerationTarget.Fir, args, retType, metaTargets)?.let { target ->
      val result = target.method.invoke(target.companion.objectInstance, metaContext, arg)
      result as? Out
    }
  }

  private inline fun <reified In1, reified In2, reified Out> invokeMeta(
    annotations: List<FirAnnotation>,
    superType: KClass<*>,
    methodName: String,
    arg: In1,
    arg2: In2
  ): Out? {
    val args = listOf(In1::class, In2::class)
    val retType = Out::class
    return MetaTarget.find(annotations.mapNotNull { it.fqName(session)?.asString() }.toSet(), methodName, superType, MetagenerationTarget.Fir, args, retType, metaTargets)?.let { target ->
      val result = target.method.invoke(target.companion.objectInstance, metaContext, arg, arg2)
      result as? Out
    }
  }

  override fun generateClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
    val annotations = session.symbolProvider.getClassLikeSymbolByClassId(classId)?.fir?.metaAnnotations(session).orEmpty()
    val firClass: FirClass? = invokeMeta(annotations, Meta.Generate.TopLevel.Class::class, "classes", classId)
    return firClass?.symbol ?: super.generateClassLikeDeclaration(classId)
  }

  override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
    return if (context.owner.isMetaAnnotated()) {
      val annotations = context.owner.fir.metaAnnotations(session)
      val constructors: List<FirConstructor>? =
        invokeMeta(annotations, Meta.Generate.Members.Constructors::class, "constructors", context)
      constructors?.map { it.symbol } ?: super.generateConstructors(context)
    } else super.generateConstructors(context)
  }

  private val fromTemplateAnnotationClassId: ClassId
    get() =
      ClassId.fromString(
        FromTemplate::class.java.canonicalName.replace(".", "/")
      )

  private val fromTemplateClassLikeSymbol: FirClassLikeSymbol<*>
    get() =
      checkNotNull(
        session.symbolProvider.getClassLikeSymbolByClassId(fromTemplateAnnotationClassId)
      ) {
        // TODO: rename this artifact if it is wrong before publishing the final release
        "@CompileTime annotation is missing, add io.arrow-kt.arrow-inject-annotations"
      }

  private val fromTemplateAnnotationType: ConeLookupTagBasedType
    get() = fromTemplateClassLikeSymbol.fir.symbol.constructType(emptyArray(), false)


  override fun generateFunctions(
    callableId: CallableId,
    context: MemberGenerationContext?
  ): List<FirNamedFunctionSymbol> {
    return if (context?.owner?.isMetaAnnotated() == true) {
      val superType = Meta.Generate.Members.Functions::class
      val metaAnnotations = context.owner.fir.metaAnnotations(session)
      val functions: List<FirSimpleFunction>? =
        invokeMeta(metaAnnotations, superType, "functions", callableId, context) ?: invokeMeta(
          metaAnnotations,
          superType,
          "functions",
          callableId
        )
      val decls = context.owner.fir.declarations as? MutableList<FirDeclaration>
      val patched = functions?.map { simpleFunction ->
        buildSimpleFunctionCopy(simpleFunction) {
          annotations += buildAnnotation {
            annotationTypeRef = buildResolvedTypeRef { type = fromTemplateAnnotationType }
            argumentMapping = FirAnnotationArgumentMappingImpl(
              null,
              mapOf(
                Name.identifier("parent") to buildConstExpression(
                  null,
                  ConstantValueKind.String,
                  callableId.classId?.asString() ?: error("expected class name in callable"),
                  mutableListOf(),
                  true
                )
              )
            )
          }
          symbol = FirNamedFunctionSymbol(callableId).also {
            //it.bind(simpleFunction)
          }
          dispatchReceiverType = context.owner.defaultType()
        }
      }
      decls?.addAll(patched.orEmpty())
      patched?.map { it.symbol } ?: super.generateFunctions(callableId, context)
    } else super.generateFunctions(callableId, context)
  }

  override fun generateProperties(callableId: CallableId, context: MemberGenerationContext?): List<FirPropertySymbol> {
    return if (context?.owner?.isMetaAnnotated() == true) {
      val superType = Meta.Generate.Members.Properties::class
      val metaAnnotations = context.owner.fir.metaAnnotations(session)
      val properties: List<FirProperty>? =
        invokeMeta(metaAnnotations, superType, "properties", callableId, context) ?: invokeMeta(
          metaAnnotations,
          superType,
          "properties",
          callableId
        )
      properties?.map { it.symbol } ?: super.generateProperties(callableId, context)
    } else super.generateProperties(callableId, context)
  }

  override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>): Set<Name> =
    constructors(classSymbol).orEmpty() +
      properties(classSymbol).orEmpty() +
      functions(classSymbol).orEmpty()


  fun functions(classSymbol: FirClassSymbol<*>): Set<Name>? =
    if (classSymbol.isMetaAnnotated())
      invokeMeta(classSymbol.fir.metaAnnotations(session), Meta.Generate.Members.Functions::class, "functions", classSymbol)
    else null

  private fun FirClassSymbol<*>.isMetaAnnotated(): Boolean =
    fir.isMetaAnnotated(session)

  fun properties(classSymbol: FirClassSymbol<*>): Set<Name>? =
    if (classSymbol.isMetaAnnotated())
      invokeMeta(classSymbol.fir.metaAnnotations(session), Meta.Generate.Members.Properties::class, "functions", classSymbol)
    else null

  fun constructors(classSymbol: FirClassSymbol<*>): Set<Name>? =
    if (classSymbol.isMetaAnnotated())
      invokeMeta(classSymbol.fir.metaAnnotations(session) ,Meta.Generate.Members.Constructors::class, "constructors", classSymbol)
    else null

  fun nestedClasses(classSymbol: FirClassSymbol<*>): Set<Name>? =
    if (classSymbol.isMetaAnnotated())
      invokeMeta(classSymbol.fir.metaAnnotations(session) , Meta.Generate.Members.NestedClasses::class, "nestedClasses", classSymbol)
    else null

  override fun getNestedClassifiersNames(classSymbol: FirClassSymbol<*>): Set<Name> =
    nestedClasses(classSymbol).orEmpty()

  fun topLevelFunctions(): Set<CallableId>? =
    invokeMeta(emptyList(), Meta.Generate.TopLevel.Functions::class, "functions")

  fun topLevelClasses(): Set<ClassId>? =
    invokeMeta(emptyList(), Meta.Generate.TopLevel.Class::class, "classes")

  fun topLevelProperties(): Set<CallableId>? =
    invokeMeta(emptyList(), Meta.Generate.TopLevel.Properties::class, "properties")

  override fun getTopLevelCallableIds(): Set<CallableId> =
    topLevelProperties().orEmpty() + topLevelFunctions().orEmpty()

  override fun getTopLevelClassIds(): Set<ClassId> =
    topLevelClasses().orEmpty()

  override fun hasPackage(packageFqName: FqName): Boolean {
    return true
  }

  val metaAnnotatedPredicate: DeclarationPredicate
    get() = metaAnnotated(AnnotationFqn("arrow.meta.Meta"))

  override fun FirDeclarationPredicateRegistrar.registerPredicates() {
    register(metaAnnotatedPredicate)
  }
}
