package arrow.reflect.compiler.plugin.services

import arrow.meta.FrontendScopeCache
import arrow.meta.TemplateCompiler
import arrow.reflect.compiler.plugin.fir.FirArrowReflectExtensionRegistrar
import arrow.reflect.compiler.plugin.targets.ClasspathMetaScanner
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
    val metaTargets = ClasspathMetaScanner.classPathMetaTargets()
    val frontendScopeCache = FrontendScopeCache()
    val templateCompiler = TemplateCompiler(configuration, frontendScopeCache)
    FirExtensionRegistrarAdapter.registerExtension(FirArrowReflectExtensionRegistrar(templateCompiler, metaTargets))
  }
}


