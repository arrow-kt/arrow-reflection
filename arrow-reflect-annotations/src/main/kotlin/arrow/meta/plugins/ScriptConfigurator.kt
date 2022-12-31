package arrow.meta.plugins

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.builder.FirFileBuilder
import org.jetbrains.kotlin.fir.declarations.builder.FirScriptBuilder

interface ScriptConfigurator : FrontendPlugin {
  fun FirScriptBuilder.configure(fileBuilder: FirFileBuilder)
  class Builder(override val session: FirSession) : ScriptConfigurator, FrontendPlugin.Builder() {
    var configureScript: FirScriptBuilder.(FirFileBuilder) -> Unit = {}

    override fun FirScriptBuilder.configure(fileBuilder: FirFileBuilder) {
      configureScript(fileBuilder)
    }
  }

  companion object {
    operator fun invoke(init: Builder.() -> Unit): (FirSession) -> ScriptConfigurator = { Builder(it).apply(init) }
  }
}
