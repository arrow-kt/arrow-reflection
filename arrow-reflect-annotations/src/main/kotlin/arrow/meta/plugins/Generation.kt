package arrow.meta.plugins

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

interface Generation : FrontendPlugin {
  fun generateClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>?
  fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol>
  fun generateFunctions(callableId: CallableId, context: MemberGenerationContext?): List<FirNamedFunctionSymbol>
  fun generateProperties(callableId: CallableId, context: MemberGenerationContext?): List<FirPropertySymbol>
  fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>): Set<Name>
  fun getNestedClassifiersNames(classSymbol: FirClassSymbol<*>): Set<Name>
  fun getTopLevelCallableIds(): Set<CallableId>
  fun getTopLevelClassIds(): Set<ClassId>
  fun hasPackage(packageFqName: FqName): Boolean

  class Builder(override val session: FirSession) : Generation, FrontendPlugin.Builder() {
    var classLikeDeclaration: (ClassId) -> FirClassLikeSymbol<*>? = { null }
    var constructors: (MemberGenerationContext) -> List<FirConstructorSymbol> = { emptyList() }
    var functions: (CallableId, MemberGenerationContext?) -> List<FirNamedFunctionSymbol> =
      { _, _ -> emptyList() }
    var properties: (CallableId, MemberGenerationContext?) -> List<FirPropertySymbol> = { _, _ -> emptyList() }
    var callableNamesForClass: (FirClassSymbol<*>) -> Set<Name> = { emptySet() }
    var nestedClassifiersNames: (FirClassSymbol<*>) -> Set<Name> = { emptySet() }
    var topLevelCallableIds: () -> Set<CallableId> = { emptySet() }
    var topLevelClassIds: () -> Set<ClassId> = { emptySet() }
    var containsPackage: (FqName) -> Boolean = { false }
    override fun generateClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
      return classLikeDeclaration(classId)
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
      return constructors(context)
    }

    override fun generateFunctions(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirNamedFunctionSymbol> {
      return functions(callableId, context)
    }

    override fun generateProperties(
        callableId: CallableId,
        context: MemberGenerationContext?
    ): List<FirPropertySymbol> {
      return properties(callableId, context)
    }

    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>): Set<Name> {
      return callableNamesForClass(classSymbol)
    }

    override fun getNestedClassifiersNames(classSymbol: FirClassSymbol<*>): Set<Name> {
      return nestedClassifiersNames(classSymbol)
    }

    override fun getTopLevelCallableIds(): Set<CallableId> {
      return topLevelCallableIds()
    }

    override fun getTopLevelClassIds(): Set<ClassId> {
      return topLevelClassIds()
    }

    override fun hasPackage(packageFqName: FqName): Boolean {
      return containsPackage(packageFqName)
    }
  }

  companion object {
    operator fun invoke(init: Builder.() -> Unit): (FirSession) -> Generation =
      { Builder(it).apply(init) }
  }
}
