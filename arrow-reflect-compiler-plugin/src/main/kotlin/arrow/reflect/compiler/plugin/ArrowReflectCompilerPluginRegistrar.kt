package arrow.reflect.compiler.plugin

import arrow.meta.FrontendScopeCache
import arrow.meta.TemplateCompiler
import arrow.reflect.compiler.plugin.fir.FirArrowReflectExtensionRegistrar
import arrow.reflect.compiler.plugin.targets.ClasspathMetaScanner
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

class ArrowReflectCompilerPluginRegistrar : CompilerPluginRegistrar() {

  override val supportsK2: Boolean = true

  override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
    val frontendScopeCache = FrontendScopeCache()
    val templateCompiler = TemplateCompiler(configuration, frontendScopeCache)
    val metaTargets = ClasspathMetaScanner.classPathMetaTargets()
    FirExtensionRegistrarAdapter.registerExtension(
      FirArrowReflectExtensionRegistrar(templateCompiler, metaTargets)
    )
  }
}
