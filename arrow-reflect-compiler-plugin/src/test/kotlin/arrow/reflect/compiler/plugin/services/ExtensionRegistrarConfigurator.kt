package arrow.reflect.compiler.plugin.services

import arrow.meta.TemplateCompiler
import arrow.reflect.compiler.plugin.fir.FirArrowReflectExtensionRegistrar
import arrow.reflect.compiler.plugin.ir.IrMetaExtensionRegistrar
import arrow.reflect.compiler.plugin.targets.ClasspathMetaScanner
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.utils.TestDisposable

class ExtensionRegistrarConfigurator(
  testServices: TestServices,
) : EnvironmentConfigurator(testServices) {
  override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
    module: TestModule,
    configuration: CompilerConfiguration
  ) {
    val metaTargets = ClasspathMetaScanner.classPathMetaTargets()
    val sourceCache :MutableMap<Pair<Int, Int>, String> = mutableMapOf()
    val templateCompiler = TemplateCompiler(TestDisposable(), module.targetPlatform, configuration, sourceCache)
    FirExtensionRegistrarAdapter.registerExtension(FirArrowReflectExtensionRegistrar(templateCompiler, metaTargets))
    IrGenerationExtension.registerExtension(IrMetaExtensionRegistrar(templateCompiler, metaTargets))
  }
}


