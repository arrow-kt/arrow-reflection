package arrow.reflect.compiler.plugin.fir.codegen

import arrow.meta.*
import arrow.reflect.compiler.plugin.fir.checkers.isMetaAnnotated
import arrow.reflect.compiler.plugin.fir.checkers.metaAnnotations
import arrow.reflect.compiler.plugin.targets.MetaInvoke
import arrow.reflect.compiler.plugin.targets.MetaTarget
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.buildSimpleFunctionCopy
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationCall
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.extensions.AnnotationFqn
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.NestedClassGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.ConeLookupTagBasedType
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.visitors.FirVisitor
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.text
import org.jetbrains.kotlin.types.ConstantValueKind

@OptIn(org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi::class)
class FirMetaCodegenExtension(
  session: FirSession,
  val templateCompiler: TemplateCompiler,
  val metaTargets: List<MetaTarget>
) : FirDeclarationGenerationExtension(session) {

  val invokeMeta = MetaInvoke(session, metaTargets)

  private fun metaContext(generationContext: MemberGenerationContext?): FirMetaContext =
    FirMetaMemberGenerationContext(templateCompiler, session, generationContext)

  @OptIn(org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi::class)
  override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
    val x = 0
    return if (!templateCompiler.compiling) {
      val firClass: FirClass? =
        if (!classId.isNestedClass)
          invokeMeta(true, metaContext(null), emptyList(), Meta.Generate.TopLevel.Class::class, "classes", classId)
        else
          invokeMeta(
            true,
            metaContext(null),
            emptyList(),
            Meta.Generate.Members.NestedClasses::class,
            "nestedClasses",
            classId
          )
      firClass?.symbol
    } else null
  }

  override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
    return if (context.owner.isMetaAnnotated()) {
      val annotations = context.owner.fir.metaAnnotations(session)
      val constructors: List<FirConstructor>? =
        invokeMeta(
          false,
          metaContext(context),
          annotations,
          Meta.Generate.Members.Constructors::class,
          "constructors",
          context
        )
      constructors?.map { it.symbol } ?: super.generateConstructors(context)
    } else super.generateConstructors(context)
  }

  private val fromTemplateAnnotationClassId: ClassId
    get() = ClassId.fromString(FromTemplate::class.java.canonicalName.replace(".", "/"))

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
        invokeMeta(false, metaContext(context), metaAnnotations, superType, "functions", callableId, context)
          ?: invokeMeta(false, metaContext(context), metaAnnotations, superType, "functions", context)
      val patched = patchedfunctions(functions, callableId, context)
      patched?.map { it.symbol } ?: super.generateFunctions(callableId, context)
    } else {
      super.generateFunctions(callableId, context)
    }
  }

  private fun patchedfunctions(
    functions: List<FirSimpleFunction>?,
    callableId: CallableId,
    context: MemberGenerationContext
  ): List<FirSimpleFunction>? = functions?.map { simpleFunction ->
    buildSimpleFunctionCopy(simpleFunction) {
      resolvePhase = FirResolvePhase.BODY_RESOLVE
      symbol = FirNamedFunctionSymbol(callableId)
      dispatchReceiverType = context.owner.defaultType()
    }
  }

  override fun generateProperties(
    callableId: CallableId,
    context: MemberGenerationContext?
  ): List<FirPropertySymbol> {
    return if (context?.owner?.isMetaAnnotated() == true) {
      val superType = Meta.Generate.Members.Properties::class
      val metaAnnotations = context.owner.fir.metaAnnotations(session)
      val properties: List<FirProperty>? =
        invokeMeta(false, metaContext(context), metaAnnotations, superType, "properties", callableId, context)
          ?: invokeMeta(false, metaContext(context), metaAnnotations, superType, "properties", context)
      properties?.map { it.symbol } ?: super.generateProperties(callableId, context)
    } else super.generateProperties(callableId, context)
  }

  override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> =
    constructors(classSymbol).orEmpty() +
      properties(classSymbol).orEmpty() +
      functions(classSymbol).orEmpty()

  fun functions(classSymbol: FirClassSymbol<*>): Set<Name>? =
    if (classSymbol.isMetaAnnotated())
      invokeMeta(
        false,
        metaContext(null),
        classSymbol.fir.metaAnnotations(session),
        Meta.Generate.Members.Functions::class,
        "functions",
        classSymbol
      )
    else null

  private fun FirClassSymbol<*>.isMetaAnnotated(): Boolean = fir.isMetaAnnotated(session)

  fun properties(classSymbol: FirClassSymbol<*>): Set<Name>? =
    if (classSymbol.isMetaAnnotated())
      invokeMeta(
        false,
        metaContext(null),
        classSymbol.fir.metaAnnotations(session),
        Meta.Generate.Members.Properties::class,
        "properties",
        classSymbol
      )
    else null

  fun constructors(classSymbol: FirClassSymbol<*>): Set<Name>? =
    if (classSymbol.isMetaAnnotated())
      invokeMeta(
        false,
        metaContext(null),
        classSymbol.fir.metaAnnotations(session),
        Meta.Generate.Members.Constructors::class,
        "constructors",
        classSymbol,
      )
    else null

  fun nestedClasses(classSymbol: FirClassSymbol<*>): Set<Name>? =
    //if (classSymbol.isMetaAnnotated()) // TODO() can't lookup annotations on this phases are they are not resolved
    invokeMeta(
      true, // can't resolve annotations here
      metaContext(null),
      classSymbol.fir.metaAnnotations(session),
      Meta.Generate.Members.NestedClasses::class,
      "nestedClasses",
      classSymbol,
    )
  // else null

  override fun getNestedClassifiersNames(classSymbol: FirClassSymbol<*>, context: NestedClassGenerationContext): Set<Name> =
    nestedClasses(classSymbol).orEmpty()

  fun topLevelFunctions(): Set<CallableId>? =
    // TODO maybe in all top levels we can't resolve annotations and need a global approach
    invokeMeta(false, metaContext(null), emptyList(), Meta.Generate.TopLevel.Functions::class, "functions")

  fun topLevelClasses(): Set<ClassId>? =
    invokeMeta(true, metaContext(null), emptyList(), Meta.Generate.TopLevel.Class::class, "classes")

  fun topLevelProperties(): Set<CallableId>? =
    invokeMeta(false, metaContext(null), emptyList(), Meta.Generate.TopLevel.Properties::class, "properties")

  @OptIn(org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi::class)
  override fun getTopLevelCallableIds(): Set<CallableId> =
    topLevelProperties().orEmpty() + topLevelFunctions().orEmpty()

  @OptIn(org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi::class)
  override fun getTopLevelClassIds(): Set<ClassId> =
    topLevelClasses().orEmpty()

  override fun hasPackage(packageFqName: FqName): Boolean {
    return true
  }


  val metaAnnotatedPredicate: DeclarationPredicate
    get() = DeclarationPredicate.create { 
      metaAnnotated(setOf(FqName("arrow.meta.Meta")), includeItself = true) 
    }

  override fun FirDeclarationPredicateRegistrar.registerPredicates() {
    register(metaAnnotatedPredicate)
  }
}
