package arrow.reflect.compiler.plugin.services

import arrow.reflect.compiler.plugin.fir.FirArrowReflectExtensionRegistrar
import arrow.reflect.compiler.plugin.fir.TemplateCompiler
import arrow.reflect.compiler.plugin.ir.IrArrowReflectExtensionRegistrar
import org.jetbrains.kotlin.analyzer.common.CommonPlatformAnalyzerServices
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.jvm.compiler.*
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Modality.*
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.backend.*
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.ir.backend.js.jsResolveLibraries
import org.jetbrains.kotlin.ir.backend.js.resolverLogger
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.js.resolve.JsPlatformAnalyzerServices
import org.jetbrains.kotlin.library.resolver.KotlinResolvedLibrary
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.isCommon
import org.jetbrains.kotlin.platform.js.isJs
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.platform.konan.isNative
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices
import org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatformAnalyzerServices
import org.jetbrains.kotlin.resolve.konan.platform.NativePlatformAnalyzerServices
import org.jetbrains.kotlin.test.frontend.fir.*
import org.jetbrains.kotlin.test.model.*
import org.jetbrains.kotlin.test.services.*
import org.jetbrains.kotlin.test.services.configuration.JsEnvironmentConfigurator
import org.jetbrains.kotlin.test.utils.TestDisposable
import java.io.File
import java.nio.file.Paths

class ExtensionRegistrarConfigurator(
  testServices: TestServices,
) : EnvironmentConfigurator(testServices) {
  @OptIn(SessionConfiguration::class)
  override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
    module: TestModule,
    configuration: CompilerConfiguration
  ) {

    val templateCompiler = TemplateCompiler(TestDisposable(), module.targetPlatform, configuration)

    val result = templateCompiler.compileSource(
      //language=kotlin
      """
        val x = 0
      """.trimIndent(),
      true
    )

    templateCompiler.run {
      val firProperty =
        //language=kotlin
        """
        val x = 0
      """.frontend<FirProperty>()

      val irProperty =
        //language=kotlin
        """
        val x = 0
      """.backend<IrProperty>()

    }

    FirExtensionRegistrarAdapter.registerExtension(FirArrowReflectExtensionRegistrar(templateCompiler))
    IrGenerationExtension.registerExtension(IrArrowReflectExtensionRegistrar(templateCompiler))
  }
}




operator fun FirElement?.not(): String =
  this?.psi?.text
    ?: (this as? FirTypeRef)?.coneType?.renderForDebugInfo()
    ?: this?.render()
    ?: ""

@JvmName("typeParametersTemplate")
operator fun List<FirTypeParameter>?.not(): String =
  when (this) {
    null -> ""
    else ->
      when {
        isEmpty() -> ""
        else -> joinToString(prefix = "<", separator = ", ", postfix = ">") {
          it.psi?.text ?: it.render()
        }
      }
  }

@JvmName("valueParametersTemplate")
operator fun List<FirValueParameter>?.not(): String =
  when (this) {
    null -> "()"
    else ->
      when {
        isEmpty() -> "()"
        else -> joinToString(prefix = "(", separator = ", ", postfix = ")") {
          it.psi?.text ?: it.render()
        }
      }
  }

@JvmName("statementsTemplate")
operator fun List<FirStatement>?.not(): String =
  when (this) {
    null -> ""
    else ->
      when {
        isEmpty() -> ""
        else -> joinToString(separator = "\n") {
          it.psi?.text ?: it.render()
        }
      }
  }


private fun buildDependencyList(
  module: TestModule,
  moduleName: Name,
  moduleInfoProvider: FirModuleInfoProvider,
  analyzerServices: PlatformDependentAnalyzerServices,
  configureDependencies: DependencyListForCliModule.Builder.() -> Unit,
) = DependencyListForCliModule.build(moduleName, module.targetPlatform, analyzerServices) {
  configureDependencies()
  sourceDependencies(moduleInfoProvider.getRegularDependentSourceModules(module))
  sourceFriendsDependencies(moduleInfoProvider.getDependentFriendSourceModules(module))
  sourceDependsOnDependencies(moduleInfoProvider.getDependentDependsOnSourceModules(module))
}

private fun DependencyListForCliModule.Builder.configureJsDependencies(
  module: TestModule,
  testServices: TestServices,
) {
  val (runtimeKlibsPaths, transitiveLibraries, friendLibraries) = getJsDependencies(module, testServices)

  dependencies(runtimeKlibsPaths.map { Paths.get(it).toAbsolutePath() })
  dependencies(transitiveLibraries.map { it.toPath().toAbsolutePath() })

  friendDependencies(friendLibraries.map { it.toPath().toAbsolutePath() })
}

private fun getJsDependencies(
  module: TestModule,
  testServices: TestServices
): Triple<List<String>, List<File>, List<File>> {
  val runtimeKlibsPaths = JsEnvironmentConfigurator.getRuntimePathsForModule(module, testServices)
  val transitiveLibraries =
    JsEnvironmentConfigurator.getKlibDependencies(module, testServices, DependencyRelation.RegularDependency)
  val friendLibraries =
    JsEnvironmentConfigurator.getKlibDependencies(module, testServices, DependencyRelation.FriendDependency)
  return Triple(runtimeKlibsPaths, transitiveLibraries, friendLibraries)
}

private fun getAllJsDependenciesPaths(module: TestModule, testServices: TestServices): List<String> {
  val (runtimeKlibsPaths, transitiveLibraries, friendLibraries) = getJsDependencies(module, testServices)
  return runtimeKlibsPaths + transitiveLibraries.map { it.path } + friendLibraries.map { it.path }
}

fun resolveJsLibraries(
  module: TestModule,
  testServices: TestServices,
  configuration: CompilerConfiguration
): List<KotlinResolvedLibrary> {
  val paths = getAllJsDependenciesPaths(module, testServices)
  val repositories = configuration[JSConfigurationKeys.REPOSITORIES] ?: emptyList()
  val logger = configuration.resolverLogger
  return jsResolveLibraries(paths, repositories, logger).getFullResolvedList()
}

fun TargetPlatform.getAnalyzerServices(): PlatformDependentAnalyzerServices {
  return when {
    isJvm() -> JvmPlatformAnalyzerServices
    isJs() -> JsPlatformAnalyzerServices
    isNative() -> NativePlatformAnalyzerServices
    isCommon() -> CommonPlatformAnalyzerServices
    else -> error("Unknown target platform: $this")
  }
}
