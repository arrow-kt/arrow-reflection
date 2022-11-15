package arrow.reflect.compiler.plugin

import arrow.reflect.compiler.plugin.fir.FirArrowReflectExtensionRegistrar
import arrow.reflect.compiler.plugin.fir.TemplateCompiler
import arrow.reflect.compiler.plugin.ir.IrArrowReflectExtensionRegistrar
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.platform.CommonPlatforms

class ArrowReflectComponentRegistrar : CompilerPluginRegistrar() {

  override val supportsK2: Boolean = true

  override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
    val templateCompiler = TemplateCompiler({  }, CommonPlatforms.defaultCommonPlatform, configuration)
    FirExtensionRegistrarAdapter.registerExtension(FirArrowReflectExtensionRegistrar(templateCompiler))
    IrGenerationExtension.registerExtension(IrArrowReflectExtensionRegistrar(templateCompiler))
  }
}
