package arrow.reflect.compiler.plugin

import arrow.meta.TemplateCompiler
import arrow.reflect.compiler.plugin.fir.FirArrowReflectExtensionRegistrar
import arrow.reflect.compiler.plugin.ir.IrMetaExtensionRegistrar
import arrow.reflect.compiler.plugin.targets.ClasspathMetaScanner
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.platform.CommonPlatforms

class ArrowReflectCompilerPluginRegistrar : CompilerPluginRegistrar() {

  override val supportsK2: Boolean = true

  override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
    val sourceCache: MutableMap<Pair<Int, Int>, String> = mutableMapOf()
    val templateCompiler =
      TemplateCompiler({}, CommonPlatforms.defaultCommonPlatform, configuration, sourceCache)
    val metaTargets = ClasspathMetaScanner.classPathMetaTargets()
    FirExtensionRegistrarAdapter.registerExtension(
      FirArrowReflectExtensionRegistrar(templateCompiler, metaTargets)
    )
    IrGenerationExtension.registerExtension(IrMetaExtensionRegistrar(templateCompiler, metaTargets))
  }
}
