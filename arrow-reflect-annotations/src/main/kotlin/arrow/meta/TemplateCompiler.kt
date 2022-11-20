package arrow.meta

import com.intellij.openapi.Disposable
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.kotlin.analyzer.common.CommonPlatformAnalyzerServices
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.jvm.JvmIrDeserializerImpl
import org.jetbrains.kotlin.cli.common.CLICompiler
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.CommonCompilerPerformanceManager
import org.jetbrains.kotlin.cli.common.checkKotlinPackageUsage
import org.jetbrains.kotlin.cli.common.fir.FirDiagnosticsCompilerResultsReporter
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.*
import org.jetbrains.kotlin.cli.common.modules.ModuleBuilder
import org.jetbrains.kotlin.cli.jvm.compiler.*
import org.jetbrains.kotlin.cli.jvm.config.jvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.jvmModularRoots
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.backend.Fir2IrResult
import org.jetbrains.kotlin.fir.backend.jvm.JvmFir2IrExtensions
import org.jetbrains.kotlin.fir.checkers.registerExtendedCommonCheckers
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.java.FirProjectSessionProvider
import org.jetbrains.kotlin.fir.pipeline.buildFirFromKtFiles
import org.jetbrains.kotlin.fir.pipeline.convertToIr
import org.jetbrains.kotlin.fir.pipeline.runCheckers
import org.jetbrains.kotlin.fir.pipeline.runResolution
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.session.FirSessionFactoryHelper
import org.jetbrains.kotlin.fir.session.IncrementalCompilationContext
import org.jetbrains.kotlin.fir.session.environment.AbstractProjectEnvironment
import org.jetbrains.kotlin.fir.session.environment.AbstractProjectFileSearchScope
import org.jetbrains.kotlin.incremental.createDirectory
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.backend.jvm.serialization.JvmIrMangler
import org.jetbrains.kotlin.load.kotlin.incremental.IncrementalPackagePartProvider
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCompilationComponents
import org.jetbrains.kotlin.modules.Module
import org.jetbrains.kotlin.modules.TargetId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.CommonPlatforms
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.isCommon
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.platform.konan.isNative
import org.jetbrains.kotlin.progress.ProgressIndicatorAndCompilationCanceledStatus
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices
import org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatformAnalyzerServices
import org.jetbrains.kotlin.resolve.multiplatform.isCommonSource
import org.jetbrains.kotlin.utils.addToStdlib.runIf
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class TemplateResult(
  val fir: FirResult,
  val ir: Fir2IrResult
)

class FirResult(
  val session: FirSession,
  val scopeSession: ScopeSession,
  val files: List<FirFile>
)

class TemplateCompiler(
  disposable: Disposable,
  targetPlatform: TargetPlatform,
  private val projectConfiguration: CompilerConfiguration,
  internal val sourceCache: MutableMap<Pair<Int, Int>, String>
) {

  private var counter = AtomicInteger(0)

  private val projectEnvironment: AbstractProjectEnvironment
  private val messageCollector: MessageCollector
  private val ktPsiFactory: KtPsiFactory
  private val buildFile: File? = null
  private val chunk: List<Module>
  val templatesFolder = File(File("."), "/build/meta/templates")

  init {
    val configFiles =
      when {
        targetPlatform.isJvm() -> EnvironmentConfigFiles.JVM_CONFIG_FILES
        targetPlatform.isNative() -> EnvironmentConfigFiles.NATIVE_CONFIG_FILES
        targetPlatform.isCommon() -> EnvironmentConfigFiles.METADATA_CONFIG_FILES
        //targetPlatform.isJs() -> EnvironmentConfigFiles.JS_CONFIG_FILES
        else -> error("Unsupported ${targetPlatform}")
      }

    val environment: KotlinCoreEnvironment = KotlinCoreEnvironment.createForProduction(
      disposable, projectConfiguration, configFiles
    )

    messageCollector = environment.messageCollector

    projectEnvironment =
      VfsBasedProjectEnvironment(
        environment.project,
        VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)
      ) { environment.createPackagePartProvider(it) }

    ktPsiFactory = KtPsiFactory(environment.project)

    if (!templatesFolder.exists()) templatesFolder.createDirectory()
    templatesFolder.deleteOnExit()

    chunk = listOfNotNull(
      ModuleBuilder(
        "meta templates module",
        templatesFolder.absolutePath, "java-production"
      )
    )
  }

  fun addToSourceCache(element: FirElement) {
    val text = element.psi?.text
    val source = element.source
    if (text != null && source != null) {
      sourceCache[source.startOffset to source.endOffset] = text
    }
  }

  fun isInSourceCache(element: IrElement): Boolean =
    sourceCache[element.startOffset to element.endOffset] != null


  lateinit var existingFirSession: FirSession

  @OptIn(ExperimentalCompilerApi::class)
  fun compileSource(
    source: String,
    extendedAnalysisMode: Boolean
  ): List<TemplateResult> {
    val performanceManager = projectConfiguration.get(CLIConfigurationKeys.PERF_MANAGER)

    messageCollector.report(
      STRONG_WARNING,
      "ATTENTION!\n This build uses experimental K2 compiler: \n  -Xuse-k2"
    )

    val notSupportedPlugins = mutableListOf<String?>().apply {
      projectConfiguration.get(ComponentRegistrar.PLUGIN_COMPONENT_REGISTRARS)
        .collectIncompatiblePluginNamesTo(this, ComponentRegistrar::supportsK2)
      projectConfiguration.get(CompilerPluginRegistrar.COMPILER_PLUGIN_REGISTRARS)
        .collectIncompatiblePluginNamesTo(this, CompilerPluginRegistrar::supportsK2)
    }

    if (notSupportedPlugins.isNotEmpty()) {
      messageCollector.report(
        ERROR,
        """
                    |There are some plugins incompatible with K2 compiler:
                    |${notSupportedPlugins.joinToString(separator = "\n|") { "  $it" }}
                    |Please remove -Xuse-k2
                """.trimMargin()
      )
      return arrayListOf()
    }
    if (projectConfiguration.languageVersionSettings.supportsFeature(LanguageFeature.MultiPlatformProjects)) {
      messageCollector.report(
        ERROR,
        "K2 compiler does not support multi-platform projects yet, so please remove -Xuse-k2 flag"
      )
      return arrayListOf()
    }

    val targetIds = projectConfiguration.get(JVMConfigurationKeys.MODULES)?.map(::TargetId)
    val incrementalComponents = projectConfiguration.get(JVMConfigurationKeys.INCREMENTAL_COMPILATION_COMPONENTS)
    val isMultiModuleChunk = chunk.size > 1

    val next = counter.incrementAndGet()
    val fileName = "meta.template_$next.kt"
    println("parsing source:\n$source")
    val allSources = //additionalFiles.map { ktPsiFactory.createPhysicalFile(it.name, it.readText())}
      listOfNotNull(
        ktPsiFactory.createPhysicalFile(fileName, source)
      )

    // TODO: run lowerings for all modules in the chunk, then run codegen for all modules.
    val outputs: ArrayList<TemplateResult> = arrayListOf()
    val project = (projectEnvironment as? VfsBasedProjectEnvironment)?.project
    for (module in chunk) {
      val moduleConfiguration = projectConfiguration.applyModuleProperties(module, buildFile)
      val context = CompilationContext(
        module,
        module.getSourceFiles(
          allSources,
          (projectEnvironment as? VfsBasedProjectEnvironment)?.localFileSystem,
          isMultiModuleChunk,
          buildFile
        ),
        projectEnvironment,
        messageCollector,
        moduleConfiguration.getBoolean(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME),
        moduleConfiguration,
        performanceManager,
        targetIds,
        incrementalComponents,
        extendedAnalysisMode,
        firExtensionRegistrars = project?.let { FirExtensionRegistrar.getInstances(it) } ?: emptyList(),
        irGenerationExtensions = project?.let { IrGenerationExtension.getInstances(it) } ?: emptyList()
      )
      val result =
        try {
          context.compileModule()
        } catch (e: AssertionError) {
          //TODO this emits nothing because this message collector is not associated to the surrounding compiler error stream
          val path = File(templatesFolder, fileName)
          val message = e.message ?: ":0:0"
          val lineAndColumnMatch = ":(.*\\d):(.*\\d)".toRegex().find(message)?.destructured
          if (lineAndColumnMatch != null) {
            val (line, column) = lineAndColumnMatch
            messageCollector.report(
              ERROR, message, CompilerMessageLocation.create(
                path = path.absolutePath, line = line.toInt(), column = column.toInt(), lineContent = e.localizedMessage
              )
            )
          }
          null
        }

      val templateResult = result ?: return arrayListOf()
      outputs += templateResult
    }

    return outputs
  }

  private fun <T : Any> List<T>?.collectIncompatiblePluginNamesTo(
    destination: MutableList<String?>,
    supportsK2: T.() -> Boolean
  ) {
    this?.filter { !it.supportsK2() && it::class.java.canonicalName != CLICompiler.SCRIPT_PLUGIN_REGISTRAR_NAME }
      ?.mapTo(destination) { it::class.qualifiedName }
  }

  private fun CompilationContext.compileModule(): TemplateResult? {
    performanceManager?.notifyAnalysisStarted()
    ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

    if (!checkKotlinPackageUsage(moduleConfiguration, allSources)) return null

    val renderDiagnosticNames = moduleConfiguration.getBoolean(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME)

    val diagnosticsReporter = DiagnosticReporterFactory.createPendingReporter()
    val firResult = runFrontend(allSources, diagnosticsReporter).also {
      performanceManager?.notifyAnalysisFinished()
    }
    if (firResult == null) {
      FirDiagnosticsCompilerResultsReporter.reportToMessageCollector(
        diagnosticsReporter,
        messageCollector,
        renderDiagnosticNames
      )
      return null
    }

    performanceManager?.notifyGenerationStarted()
    performanceManager?.notifyIRTranslationStarted()

    val fir2IrExtensions = JvmFir2IrExtensions(moduleConfiguration, JvmIrDeserializerImpl(), JvmIrMangler)
    val linkViaSignatures = moduleConfiguration.getBoolean(JVMConfigurationKeys.LINK_VIA_SIGNATURES)
    val fir2IrResult = firResult.session.convertToIr(
      firResult.scopeSession, firResult.files, fir2IrExtensions, irGenerationExtensions, linkViaSignatures
    )

    performanceManager?.notifyIRTranslationFinished()

    FirDiagnosticsCompilerResultsReporter.reportToMessageCollector(
      diagnosticsReporter,
      messageCollector,
      renderDiagnosticNames
    )

    performanceManager?.notifyIRGenerationFinished()
    performanceManager?.notifyGenerationFinished()
    ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

    return TemplateResult(firResult, fir2IrResult)
  }

  private fun CompilationContext.runFrontend(
    ktFiles: List<KtFile>,
    diagnosticsReporter: BaseDiagnosticsCollector,
  ): FirResult? {
    @Suppress("NAME_SHADOWING")
    var ktFiles = ktFiles
    val syntaxErrors = ktFiles.fold(false) { errorsFound, ktFile ->
      AnalyzerWithCompilerReport.reportSyntaxErrors(ktFile, messageCollector).isHasErrors or errorsFound
    }

    var sourceScope = (projectEnvironment as VfsBasedProjectEnvironment).getSearchScopeByPsiFiles(ktFiles) +
      projectEnvironment.getSearchScopeForProjectJavaSources()

    var librariesScope = projectEnvironment.getSearchScopeForProjectLibraries()

    val providerAndScopeForIncrementalCompilation = createComponentsForIncrementalCompilation(sourceScope)

    providerAndScopeForIncrementalCompilation?.precompiledBinariesFileScope?.let {
      librariesScope -= it
    }

    val languageVersionSettings = moduleConfiguration.languageVersionSettings

    val commonKtFiles = ktFiles.filter { it.isCommonSource == true }

    val sessionProvider = FirProjectSessionProvider()

    fun createSession(
      name: String,
      platform: TargetPlatform,
      analyzerServices: PlatformDependentAnalyzerServices,
      sourceScope: AbstractProjectFileSearchScope,
      needRegisterJavaElementFinder: Boolean,
      dependenciesConfigurator: DependencyListForCliModule.Builder.() -> Unit = {}
    ): FirSession {
      return existingFirSession ?: FirSessionFactoryHelper.createSessionWithDependencies(
        Name.identifier(name),
        platform,
        analyzerServices,
        externalSessionProvider = sessionProvider,
        projectEnvironment,
        languageVersionSettings,
        sourceScope,
        librariesScope,
        lookupTracker = moduleConfiguration.get(CommonConfigurationKeys.LOOKUP_TRACKER),
        enumWhenTracker = moduleConfiguration.get(CommonConfigurationKeys.ENUM_WHEN_TRACKER),
        providerAndScopeForIncrementalCompilation,
        firExtensionRegistrars,
        needRegisterJavaElementFinder,
        dependenciesConfigurator = {
          //sourceDependsOnDependencies(listOfNotNull(existingFirSession.moduleData))
          sourceDependencies(listOfNotNull(existingFirSession.moduleData))
          dependencies(moduleConfiguration.jvmClasspathRoots.map { it.toPath() })
          dependencies(moduleConfiguration.jvmModularRoots.map { it.toPath() })
          friendDependencies(moduleConfiguration[JVMConfigurationKeys.FRIEND_PATHS] ?: emptyList())
          dependenciesConfigurator()
        }
      ) {
        if (extendedAnalysisMode) {
          registerExtendedCommonCheckers()
        }
      }
    }

    val commonSession = runIf(
      languageVersionSettings.supportsFeature(LanguageFeature.MultiPlatformProjects) && commonKtFiles.isNotEmpty()
    ) {
      val commonSourcesScope = projectEnvironment.getSearchScopeByPsiFiles(commonKtFiles)
      sourceScope -= commonSourcesScope
      ktFiles = ktFiles.filterNot { it.isCommonSource == true }
      createSession(
        "${module.getModuleName()}-common",
        CommonPlatforms.defaultCommonPlatform,
        CommonPlatformAnalyzerServices,
        commonSourcesScope,
        needRegisterJavaElementFinder = false
      )
    }

    val session = createSession(
      module.getModuleName(),
      JvmPlatforms.unspecifiedJvmPlatform,
      JvmPlatformAnalyzerServices,
      sourceScope,
      needRegisterJavaElementFinder = true
    ) {
      if (commonSession != null) {
        sourceDependsOnDependencies(listOf(commonSession.moduleData))
      }
      friendDependencies(module.getFriendPaths())
    }

    val commonRawFir = commonSession?.buildFirFromKtFiles(commonKtFiles)
    val rawFir = session.buildFirFromKtFiles(ktFiles)

    // TODO we may want to tweak this parts so it doesn't run all checks in fir and macros are more lenient
    // in terms of their scope
    commonSession?.apply {
      val (commonScopeSession, commonFir) = runResolution(commonRawFir!!)
      runCheckers(commonScopeSession, commonFir, diagnosticsReporter)
    }

    val (scopeSession, fir) = session.runResolution(rawFir)
    session.runCheckers(scopeSession, fir, diagnosticsReporter)

    // TODO()  better handle scopes, we don't want to do a full analysis just FirRaw conversion
    // return if (syntaxErrors || diagnosticsReporter.hasErrors) null else FirResult(session, scopeSession, fir)
    return if (syntaxErrors) null else FirResult(session, scopeSession, fir)
  }

  private fun CompilationContext.createComponentsForIncrementalCompilation(
    sourceScope: AbstractProjectFileSearchScope
  ): IncrementalCompilationContext? {
    if (targetIds == null || incrementalComponents == null) return null
    val directoryWithIncrementalPartsFromPreviousCompilation =
      moduleConfiguration[JVMConfigurationKeys.OUTPUT_DIRECTORY]
        ?: return null
    val incrementalCompilationScope = directoryWithIncrementalPartsFromPreviousCompilation.walk()
      .filter { it.extension == "class" }
      .let { projectEnvironment.getSearchScopeByIoFiles(it.asIterable()) }
      .takeIf { !it.isEmpty }
      ?: return null
    val packagePartProvider = IncrementalPackagePartProvider(
      projectEnvironment.getPackagePartProvider(sourceScope),
      targetIds.map(incrementalComponents::getIncrementalCache)
    )
    return IncrementalCompilationContext(emptyList(), packagePartProvider, incrementalCompilationScope)
  }

  private class CompilationContext(
    val module: Module,
    val allSources: List<KtFile>,
    val projectEnvironment: AbstractProjectEnvironment,
    val messageCollector: MessageCollector,
    val renderDiagnosticName: Boolean,
    val moduleConfiguration: CompilerConfiguration,
    val performanceManager: CommonCompilerPerformanceManager?,
    val targetIds: List<TargetId>?,
    val incrementalComponents: IncrementalCompilationComponents?,
    val extendedAnalysisMode: Boolean,
    val firExtensionRegistrars: List<FirExtensionRegistrar>,
    val irGenerationExtensions: Collection<IrGenerationExtension>
  )
}
