package arrow.reflect.compiler.plugin.fir.codegen

import arrow.meta.TemplateCompiler
import arrow.meta.module.impl.arrow.meta.macro.compilation.*
import arrow.reflect.compiler.plugin.targets.macro.MacroInvoke
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirImplementationDetail
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.plugin.createMemberFunction
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
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

  private val classFunctionTransformations2: MutableMap<Name, MutableList<TransformClassState.Function>> = mutableMapOf()

  @OptIn(FirImplementationDetail::class)
  override fun generateFunctions(callableId: CallableId, context: MemberGenerationContext?): List<FirNamedFunctionSymbol> {
    val owner = context?.owner ?: return emptyList()
    val states = classFunctionTransformations2[callableId.callableName] ?: return emptyList()
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
    val compilation = macro(
      session,
      context = object : MacroContext {},
      element = fir,
      annotations = annotations
    ).filterIsInstance<TransformClassCompilation>().map {
      it.transform(context = TransformClassContext(
        session = session,
        scope = listOf()
      ))
    }
    val functionStates = compilation.flatMap { it.states() }.filterIsInstance<TransformClassState.Function>()
    functionStates.forEach { functionState ->
      val functionName = functionState.firSimpleFunction.name
      classFunctionTransformations2.getOrPut(functionName) { mutableListOf() }.add(functionState)
    }
    return functionStates.map { it.firSimpleFunction.name }.toSet()
  }
}
