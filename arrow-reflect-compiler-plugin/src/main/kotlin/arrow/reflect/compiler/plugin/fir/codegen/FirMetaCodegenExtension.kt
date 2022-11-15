package arrow.reflect.compiler.plugin.fir.codegen

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class FirMetaCodegenExtension(session: FirSession) : FirDeclarationGenerationExtension(session) {
  override fun generateClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
    return super.generateClassLikeDeclaration(classId)
  }

  override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
    context.owner
    return super.generateConstructors(context)
  }

  override fun generateFunctions(
    callableId: CallableId,
    context: MemberGenerationContext?
  ): List<FirNamedFunctionSymbol> {
    return super.generateFunctions(callableId, context)
  }

  override fun generateProperties(callableId: CallableId, context: MemberGenerationContext?): List<FirPropertySymbol> {
    return super.generateProperties(callableId, context)
  }

  override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>): Set<Name> {
    return super.getCallableNamesForClass(classSymbol)
  }

  override fun getNestedClassifiersNames(classSymbol: FirClassSymbol<*>): Set<Name> {
    return super.getNestedClassifiersNames(classSymbol)
  }

  override fun getTopLevelCallableIds(): Set<CallableId> {
    return super.getTopLevelCallableIds()
  }

  override fun getTopLevelClassIds(): Set<ClassId> {
    return super.getTopLevelClassIds()
  }

  override fun hasPackage(packageFqName: FqName): Boolean {
    return super.hasPackage(packageFqName)
  }

  override fun FirDeclarationPredicateRegistrar.registerPredicates() {
    //TODO("Not yet implemented")
  }
}
