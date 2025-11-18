package arrow.reflect.compiler.plugin.targets.macro

import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.reflect.compiler.plugin.ir.generation.ArrowReflectFir2IrVisitor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.resolve.fqName

class MacroInvoke private constructor(private val registry: MacroRegistry) {

  companion object {

    fun build(macros: List<MacroTarget>): MacroInvoke {
      return MacroInvoke(registry = buildRegistry(macros = macros))
    }

    private fun buildRegistry(macros: List<MacroTarget>): MacroRegistry {
      return MacroRegistry(
        firMacroRegistry = FirMacroRegistry.buildRegistry(macros),
        classTransformationRegistry = ClassTransformationRegistry.empty(),
        irActualizedResult = null
      )
    }
  }

  internal operator fun <T : FirElement> invoke(
    session: FirSession,
    context: MacroContext,
    element: T,
    annotations: List<FirAnnotation>
  ): List<MacroCompilation> {
    return registry.firMacroRegistry(
      context,
      element,
      annotations.mapNotNull { it.fqName(session)?.asString() }.toSet()
    )
  }

  internal fun classTransformation(): ClassTransformationRegistry = registry.classTransformationRegistry

  internal fun bindIrActualizedResult(
    session: FirSession,
    compilerConfiguration: CompilerConfiguration
  ) {
    if (registry.irActualizedResult != null) return
    registry.irActualizedResult = ArrowReflectFir2IrVisitor.create(
      session = session,
      compilerConfiguration = compilerConfiguration
    )
  }

  internal fun irActualizedResult(): ArrowReflectFir2IrVisitor? = registry.irActualizedResult
}
