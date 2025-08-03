package arrow.reflect.compiler.plugin.fir

import arrow.meta.TemplateCompiler
import arrow.reflect.compiler.plugin.fir.checkers.FirMetaAdditionalCheckersExtension
import arrow.reflect.compiler.plugin.fir.checkers.MacroFirTransformationCheckerExtension
import arrow.reflect.compiler.plugin.fir.codegen.FirMetaCodegenExtension
import arrow.reflect.compiler.plugin.targets.MetaTarget
import arrow.reflect.compiler.plugin.targets.macro.MacroInvoke
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class FirArrowReflectExtensionRegistrar(
  val templateCompiler: TemplateCompiler,
  val metaTargets: List<MetaTarget>,
  val macro: MacroInvoke
) :
  FirExtensionRegistrar() {

  override fun ExtensionRegistrarContext.configurePlugin() {
    +{ session: FirSession ->
      templateCompiler.session = session
      FirMetaCodegenExtension(session, templateCompiler, metaTargets)
    }
    +{ session: FirSession ->
      templateCompiler.session = session
      FirMetaAdditionalCheckersExtension(session, templateCompiler, metaTargets, macro)
    }
    +{ session: FirSession ->
      MacroFirTransformationCheckerExtension(session = session, macro = macro)
    }
  }

}

