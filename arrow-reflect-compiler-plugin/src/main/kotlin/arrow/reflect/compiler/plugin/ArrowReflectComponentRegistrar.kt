package arrow.reflect.compiler.plugin

import arrow.reflect.compiler.plugin.fir.FirArrowReflectExtensionRegistrar
import arrow.reflect.compiler.plugin.ir.IrArrowReflectExtensionRegistrar
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

class ArrowReflectComponentRegistrar : CompilerPluginRegistrar() {

  override val supportsK2: Boolean = true

  override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
    FirExtensionRegistrarAdapter.registerExtension(FirArrowReflectExtensionRegistrar())
    IrGenerationExtension.registerExtension(IrArrowReflectExtensionRegistrar())
  }
}
