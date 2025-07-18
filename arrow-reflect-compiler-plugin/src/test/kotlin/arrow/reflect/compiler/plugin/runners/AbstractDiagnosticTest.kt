package arrow.reflect.compiler.plugin.runners

import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.configuration.baseFirDiagnosticTestConfiguration
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.frontend.fir.FirFrontendFacade
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.model.DependencyKind
import arrow.reflect.compiler.plugin.services.*

abstract class AbstractDiagnosticTest : BaseTestRunner() {
  override fun configure(builder: TestConfigurationBuilder) = with(builder) {
    baseFirDiagnosticTestConfiguration(frontendFacade = ::FirFrontendFacade)
    
    defaultDirectives {
      +FirDiagnosticsDirectives.ENABLE_PLUGIN_PHASES
      +FirDiagnosticsDirectives.FIR_DUMP
      FirDiagnosticsDirectives.FIR_PARSER with FirParser.LightTree
      +AdditionalFilesDirectives.SOME_FILE_DIRECTIVE
    }

    globalDefaults {
      targetBackend = TargetBackend.JVM_IR
      targetPlatform = JvmPlatforms.defaultJvmPlatform
      dependencyKind = DependencyKind.Source
    }

    useConfigurators(
      ::PluginAnnotationsConfigurator,
      ::ExtensionRegistrarConfigurator
    )
    
    useCustomRuntimeClasspathProviders(
      ::MetaRuntimeClasspathProvider
    )

    useAdditionalSourceProviders(::AdditionalFilesProvider)
  }

  override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
    return EnvironmentBasedStandardLibrariesPathProvider
  }
}
