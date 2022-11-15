package arrow.reflect.compiler.plugin.fir

import arrow.reflect.compiler.plugin.fir.codegen.FirMetaCodegenExtension
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class FirArrowReflectExtensionRegistrar(templateCompiler: TemplateCompiler) : FirExtensionRegistrar() {

  override fun ExtensionRegistrarContext.configurePlugin() {
    +{ session: FirSession -> FirMetaCodegenExtension(session) }
  }
}
