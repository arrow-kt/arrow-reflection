package arrow.reflect.compiler.plugin.fir.codegen

import arrow.meta.TemplateCompiler
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.TransformClassCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.TransformClassContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.transformclassfactory.TransformClassState
import arrow.reflect.compiler.plugin.targets.macro.MacroInvoke
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.utils.hasBackingField
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.plugin.createMemberProperty
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

class FirMacroCodegenExtension(
  session: FirSession,
  private val macro: MacroInvoke,
  private val compilerConfiguration: CompilerConfiguration,
  private val templateCompiler: TemplateCompiler
) : FirDeclarationGenerationExtension(session) {

  class MacroGeneratedFunctionKey : GeneratedDeclarationKey()
  class MacroGeneratedPropertyKey : GeneratedDeclarationKey()

  private val classFunctionStates: MutableMap<Name, MutableList<TransformClassState.Function>> = mutableMapOf()
  private val classPropertyStates: MutableMap<Name, MutableList<TransformClassState.Property>> = mutableMapOf()

  override fun generateProperties(callableId: CallableId, context: MemberGenerationContext?): List<FirPropertySymbol> {
    val owner = context?.owner ?: return emptyList()
    val states = classPropertyStates[callableId.callableName] ?: return emptyList()
    return states.map { property ->
      val key = MacroGeneratedPropertyKey()
      val firProperty = property.firProperty
      macro.classTransformation()[key] = property
      macro.bindIrActualizedResult(session = session, compilerConfiguration = compilerConfiguration)
      createMemberProperty(
        owner = owner,
        key = key,
        name = firProperty.name,
        returnType = firProperty.returnTypeRef.coneType,
        isVal = firProperty.isVal,
        hasBackingField = firProperty.hasBackingField
      ).symbol
    }
  }

  override fun generateFunctions(callableId: CallableId, context: MemberGenerationContext?): List<FirNamedFunctionSymbol> {
    val owner = context?.owner ?: return emptyList()
    val states = classFunctionStates[callableId.callableName] ?: return emptyList()
    return states.map { function ->
      val key = MacroGeneratedFunctionKey()
      macro.classTransformation()[key] = function
      val memberFunction = createMemberFunction(owner, key, function.firSimpleFunction.name, function.firSimpleFunction.returnTypeRef.coneType) {
        function.firSimpleFunction.valueParameters.forEach { valueParameter ->
          valueParameter(
            name = valueParameter.name,
            type = valueParameter.returnTypeRef.coneType,
            isCrossinline = valueParameter.isCrossinline,
            isNoinline = valueParameter.isNoinline,
            isVararg = valueParameter.isVararg,
            hasDefaultValue = valueParameter.defaultValue != null
          )
        }
      }
      macro.bindIrActualizedResult(session = session, compilerConfiguration = compilerConfiguration)
      memberFunction.symbol
    }
  }

  override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
    if (templateCompiler.compiling) return setOf()
    templateCompiler.compiling = true
    val fir = classSymbol.fir
    val annotations = (fir as? FirAnnotationContainer)?.annotations ?: emptyList()
    val states = macro(
      session,
      context = object : MacroContext {},
      element = fir,
      annotations = annotations
    ).filterIsInstance<TransformClassCompilation>().map {
      it.transform(context = TransformClassContext(
        session = session,
        scope = listOf()
      ))
    }.flatMap { it.states() }
    return states.classFunctionSymbols() + states.classPropertySymbols()
  }

  private fun List<TransformClassState>.classPropertySymbols(): Set<Name> {
    val propertyStates = filterIsInstance<TransformClassState.Property>()
    propertyStates.forEach { propertyState ->
      val propertyName = propertyState.firProperty.name
      classPropertyStates.getOrPut(propertyName) { mutableListOf() }.add(propertyState)
    }
    return propertyStates.map { it.firProperty.name }.toSet()
  }

  private fun List<TransformClassState>.classFunctionSymbols(): Set<Name> {
    val functionStates = filterIsInstance<TransformClassState.Function>()
    functionStates.forEach { functionState ->
      val functionName = functionState.firSimpleFunction.name
      classFunctionStates.getOrPut(functionName) { mutableListOf() }.add(functionState)
    }
    return functionStates.map { it.firSimpleFunction.name }.toSet()
  }
}
