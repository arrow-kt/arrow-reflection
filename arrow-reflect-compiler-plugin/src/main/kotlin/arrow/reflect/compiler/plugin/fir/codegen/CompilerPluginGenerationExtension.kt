package arrow.reflect.compiler.plugin.fir.codegen

import arrow.meta.plugins.Generation
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class CompilerPluginGenerationExtension(session: FirSession, val compilerPlugin: Generation) :
  FirDeclarationGenerationExtension(session) {
  override fun generateClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
    return compilerPlugin.generateClassLikeDeclaration(classId)
  }

  override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
    return compilerPlugin.generateConstructors(context)
  }

  override fun generateFunctions(
    callableId: CallableId,
    context: MemberGenerationContext?
  ): List<FirNamedFunctionSymbol> {
    return compilerPlugin.generateFunctions(callableId, context)
  }

  override fun generateProperties(callableId: CallableId, context: MemberGenerationContext?): List<FirPropertySymbol> {
    return compilerPlugin.generateProperties(callableId, context)
  }

  override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>): Set<Name> {
    return compilerPlugin.getCallableNamesForClass(classSymbol)
  }

  override fun getNestedClassifiersNames(classSymbol: FirClassSymbol<*>): Set<Name> {
    return compilerPlugin.getNestedClassifiersNames(classSymbol)
  }

  override fun getTopLevelCallableIds(): Set<CallableId> {
    return compilerPlugin.getTopLevelCallableIds()
  }

  override fun getTopLevelClassIds(): Set<ClassId> {
    return compilerPlugin.getTopLevelClassIds()
  }

  override fun hasPackage(packageFqName: FqName): Boolean {
    return compilerPlugin.hasPackage(packageFqName)
  }

  override fun FirDeclarationPredicateRegistrar.registerPredicates() {
    register(compilerPlugin.metaAnnotatedPredicate)
  }
}
