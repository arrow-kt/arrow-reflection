package arrow.reflect.compiler.plugin.services

import arrow.reflect.compiler.plugin.fir.FirArrowReflectExtensionRegistrar
import arrow.reflect.compiler.plugin.ir.IrArrowReflectExtensionRegistrar
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices

class ExtensionRegistrarConfigurator(
  testServices: TestServices,
) : EnvironmentConfigurator(testServices) {
  override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
    module: TestModule,
    configuration: CompilerConfiguration
  ) {
    FirExtensionRegistrarAdapter.registerExtension(FirArrowReflectExtensionRegistrar())
    IrGenerationExtension.registerExtension(IrArrowReflectExtensionRegistrar())
  }
}
