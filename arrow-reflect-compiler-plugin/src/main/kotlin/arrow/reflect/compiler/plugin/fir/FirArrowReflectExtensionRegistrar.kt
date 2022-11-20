package arrow.reflect.compiler.plugin.fir

import arrow.meta.TemplateCompiler
import arrow.reflect.compiler.plugin.fir.checkers.FirMetaAdditionalCheckersExtension
import arrow.reflect.compiler.plugin.fir.codegen.FirMetaCodegenExtension
import arrow.reflect.compiler.plugin.targets.MetaTarget
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class FirArrowReflectExtensionRegistrar(val templateCompiler: TemplateCompiler, val metaTargets: List<MetaTarget>) : FirExtensionRegistrar() {

  override fun ExtensionRegistrarContext.configurePlugin() {
    +{ session: FirSession ->
      templateCompiler.existingFirSession = session
      FirMetaCodegenExtension(session, templateCompiler, metaTargets) }
    +{ session: FirSession ->
      templateCompiler.existingFirSession = session
      FirMetaAdditionalCheckersExtension(session, templateCompiler, metaTargets) }
  }
}
