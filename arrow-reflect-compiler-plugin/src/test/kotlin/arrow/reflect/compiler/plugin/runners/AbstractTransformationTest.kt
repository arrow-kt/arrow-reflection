package arrow.reflect.compiler.plugin.runners

import arrow.reflect.compiler.plugin.services.ExtensionRegistrarConfigurator
import arrow.reflect.compiler.plugin.services.PluginAnnotationsConfigurator
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.runners.codegen.AbstractFirBlackBoxCodegenTestBase
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.configuration.CommonEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.configuration.JvmEnvironmentConfigurator

open class AbstractTransformationTest : AbstractFirBlackBoxCodegenTestBase(FirParser.LightTree) {
  override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
    return EnvironmentBasedStandardLibrariesPathProvider
  }

  override fun configure(builder: TestConfigurationBuilder) {
    super.configure(builder)

    with(builder) {
      defaultDirectives {
        +CodegenTestDirectives.DUMP_IR
        +FirDiagnosticsDirectives.FIR_DUMP
        +JvmEnvironmentConfigurationDirectives.FULL_JDK

        +CodegenTestDirectives.IGNORE_DEXING // Avoids loading R8 from the classpath.
      }

      useConfigurators(
        ::CommonEnvironmentConfigurator,
        ::JvmEnvironmentConfigurator,
        ::PluginAnnotationsConfigurator,
        ::ExtensionRegistrarConfigurator
      )
    }
  }
}
