package arrow.reflect.compiler.plugin.targets.macro

import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
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
      return MacroRegistry(firMacroRegistry = FirMacroRegistry.buildRegistry(macros), irMacroRegistry = listOf())
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
}
