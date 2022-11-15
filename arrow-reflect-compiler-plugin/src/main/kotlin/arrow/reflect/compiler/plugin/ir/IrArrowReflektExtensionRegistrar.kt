package arrow.reflect.compiler.plugin.ir

import arrow.reflect.compiler.plugin.fir.TemplateCompiler
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class IrArrowReflectExtensionRegistrar(templateCompiler: TemplateCompiler) : IrGenerationExtension {

  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
  }
}

