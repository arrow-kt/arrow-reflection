package arrow.reflect.compiler.plugin.targets.macro

import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import org.jetbrains.kotlin.fir.FirElement
import kotlin.reflect.full.isSubclassOf

data class MacroRegistry(
  val firMacroRegistry: FirMacroRegistry,
  val irMacroRegistry: List<MacroTarget> = listOf()
)

@ConsistentCopyVisibility
data class FirMacroRegistry private constructor(
  private val macros: List<MacroTarget>
) {

  companion object {
    fun buildRegistry(macros: List<MacroTarget>): FirMacroRegistry {
      return FirMacroRegistry(macros = macros.filterFirMacros())
    }

    private fun List<MacroTarget>.filterFirMacros(): List<MacroTarget> {
      return filter {
        it.params.size == 2 && it.params[1].isSubclassOf(FirElement::class)
      }
    }
  }

  internal operator fun <T : FirElement> invoke(
    context: MacroContext,
    element: T,
    annotations: Set<String>
  ): List<MacroCompilation> {
    return macros.filter { macro ->
      if (macro.targetClass != null) {
        macro.nodeParameterIsSupertypeOf(node = element) && macro.targetClass.java.canonicalName in annotations
      } else {
        macro.nodeParameterIsSupertypeOf(node = element)
      }
    }.mapNotNull {
      it.method.invoke(this, context, element) as? MacroCompilation
    }
  }

  private fun <T : FirElement> MacroTarget.nodeParameterIsSupertypeOf(node: T): Boolean = node::class.isSubclassOf(params[1])
}
