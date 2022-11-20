package arrow.reflect.compiler.plugin.fir.codegen

import arrow.meta.TemplateCompiler
import arrow.reflect.compiler.plugin.fir.transformer.Transformer
import arrow.reflect.compiler.plugin.targets.MetaTarget
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.extensions.AnnotationFqn
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.metaAnnotated
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class FirMetaCodegenExtension(
  session: FirSession,
  val templateCompiler: TemplateCompiler,
  val metaTargets: List<MetaTarget>
) : FirDeclarationGenerationExtension(session) {

  val transformer: Transformer = Transformer(session, templateCompiler, metaTargets)

  //
//  override fun generateClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
//    val firClass: FirClass? = invokeMeta("classes", classId)
//    return firClass?.symbol ?: super.generateClassLikeDeclaration(classId)
//  }
//
//  override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
//    val constructors: List<FirConstructor>? = invokeMeta("constructors", context)
//    return constructors?.map { it.symbol } ?: super.generateConstructors(context)
//  }
//
//  override fun generateFunctions(
//    callableId: CallableId,
//    context: MemberGenerationContext?
//  ): List<FirNamedFunctionSymbol> {
//    val functions: List<FirSimpleFunction>? =
//      context?.let { invokeMeta("functions", callableId, it) } ?: invokeMeta("functions", callableId)
//    return functions?.map { it.symbol } ?: super.generateFunctions(callableId, context)
//  }
//
//  override fun generateProperties(callableId: CallableId, context: MemberGenerationContext?): List<FirPropertySymbol> {
//    val properties: List<FirProperty>? =
//      context?.let { invokeMeta("properties", callableId, it) } ?: invokeMeta("properties", callableId)
//    return properties?.map { it.symbol } ?: super.generateProperties(callableId, context)
//  }
//
  override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>): Set<Name> {
    val firClass: FirClass = classSymbol.fir.transform<FirClass, Unit>(transformer, Unit)
    return emptySet()
  }
//
//  override fun getNestedClassifiersNames(classSymbol: FirClassSymbol<*>): Set<Name> =
//    nestedClasses(classSymbol)
//
  override fun getTopLevelCallableIds(): Set<CallableId> {
    return super.getTopLevelCallableIds()
  }

  override fun getTopLevelClassIds(): Set<ClassId> {
    return super.getTopLevelClassIds()
  }

  override fun hasPackage(packageFqName: FqName): Boolean {
    return true
  }

  val metaAnnotatedPredicate: DeclarationPredicate
    get() = metaAnnotated(AnnotationFqn("arrow.meta.Meta"))

  override fun FirDeclarationPredicateRegistrar.registerPredicates() {
    register(metaAnnotatedPredicate)
  }
}
