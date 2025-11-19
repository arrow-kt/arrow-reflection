package arrow.reflect.compiler.plugin.ir

import arrow.reflect.compiler.plugin.targets.macro.MacroInvoke
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

class IrArrowReflectExtension(
  private val macro: MacroInvoke
) : IrGenerationExtension {

  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    moduleFragment.acceptChildrenVoid(MacroIrFunctionTransformer(macro = macro))
  }
}
