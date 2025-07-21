package arrow.reflect.compiler.plugin.runners

import arrow.reflect.compiler.plugin.services.*
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.frontend.fir.FirFrontendFacade
import org.jetbrains.kotlin.test.initIdeaConfiguration
import org.jetbrains.kotlin.test.model.DependencyKind
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import org.jetbrains.kotlin.test.builders.firHandlersStep
import org.jetbrains.kotlin.test.frontend.fir.handlers.FirDumpHandler
import org.jetbrains.kotlin.test.model.FrontendKinds
import org.jetbrains.kotlin.test.services.configuration.CommonEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.configuration.JvmEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.sourceProviders.MainFunctionForBlackBoxTestsSourceProvider
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider
import org.junit.jupiter.api.BeforeAll

abstract class BaseTestRunner : AbstractKotlinCompilerTest() {
  companion object {
    @BeforeAll
    @JvmStatic
    fun setUp() {
      initIdeaConfiguration()
    }
  }

  override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
    return EnvironmentBasedStandardLibrariesPathProvider
  }
}

fun TestConfigurationBuilder.commonFirWithPluginFrontendConfiguration() {
  globalDefaults {
    frontend = FrontendKinds.FIR
    targetBackend = TargetBackend.JVM_IR
    targetPlatform = JvmPlatforms.defaultJvmPlatform
    dependencyKind = DependencyKind.Binary
  }

  defaultDirectives {
    +FirDiagnosticsDirectives.ENABLE_PLUGIN_PHASES
    +FirDiagnosticsDirectives.FIR_DUMP
    FirDiagnosticsDirectives.FIR_PARSER with FirParser.LightTree
    +AdditionalFilesDirectives.SOME_FILE_DIRECTIVE
    +ConfigurationDirectives.WITH_STDLIB
  }
  
  useConfigurators(
    ::CommonEnvironmentConfigurator,
    ::JvmEnvironmentConfigurator,
    ::PluginAnnotationsConfigurator,
    ::ExtensionRegistrarConfigurator
  )
  
  useCustomRuntimeClasspathProviders(
    ::MetaRuntimeClasspathProvider
  )

  useAdditionalSourceProviders(
    ::AdditionalFilesProvider,
    ::MainFunctionForBlackBoxTestsSourceProvider
  )

  facadeStep(::FirFrontendFacade)
  
  firHandlersStep {
    useHandlers(::FirDumpHandler)
  }
}
