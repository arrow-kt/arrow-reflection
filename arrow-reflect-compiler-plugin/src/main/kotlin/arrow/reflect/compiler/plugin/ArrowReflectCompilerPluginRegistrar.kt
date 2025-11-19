package arrow.reflect.compiler.plugin

import arrow.meta.TemplateCompiler
import arrow.reflect.compiler.plugin.fir.FirArrowReflectExtensionRegistrar
import arrow.reflect.compiler.plugin.ir.IrArrowReflectExtension
import arrow.reflect.compiler.plugin.targets.ClasspathMetaScanner
import arrow.reflect.compiler.plugin.targets.macro.ClasspathMacroScanner
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

class ArrowReflectCompilerPluginRegistrar : CompilerPluginRegistrar() {

  override val supportsK2: Boolean = true

  override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
    val macro = ClasspathMacroScanner.scanMacros()
    val templateCompiler = TemplateCompiler(configuration)
    val metaTargets = ClasspathMetaScanner.classPathMetaTargets()
    FirExtensionRegistrarAdapter.registerExtension(FirArrowReflectExtensionRegistrar(templateCompiler, metaTargets, macro))
    IrGenerationExtension.registerExtension(IrArrowReflectExtension(macro))
  }
}
