package arrow.meta

//import org.jetbrains.kotlin.fir.session.FirSessionFactoryHelper
import com.intellij.openapi.Disposable
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.jvm.JvmIrDeserializerImpl
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.CommonCompilerPerformanceManager
import org.jetbrains.kotlin.cli.common.checkKotlinPackageUsage
import org.jetbrains.kotlin.cli.common.fir.FirDiagnosticsCompilerResultsReporter
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.*
import org.jetbrains.kotlin.cli.common.modules.ModuleBuilder
import org.jetbrains.kotlin.cli.jvm.compiler.*
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.analysis.collectors.FirDiagnosticsCollector
import org.jetbrains.kotlin.fir.backend.Fir2IrResult
import org.jetbrains.kotlin.fir.backend.jvm.JvmFir2IrExtensions
import org.jetbrains.kotlin.fir.builder.BodyBuildingMode
import org.jetbrains.kotlin.fir.builder.RawFirBuilder
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.buildSimpleFunctionCopy
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildConstExpression
import org.jetbrains.kotlin.fir.expressions.impl.FirAnnotationArgumentMappingImpl
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.pipeline.convertToIr
import org.jetbrains.kotlin.fir.resolve.*
import org.jetbrains.kotlin.fir.resolve.calls.ImplicitDispatchReceiverValue
import org.jetbrains.kotlin.fir.resolve.dfa.DataFlowAnalyzerContext
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
import org.jetbrains.kotlin.fir.resolve.providers.impl.FirProviderImpl
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.transformers.*
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.BodyResolveContext
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.FirBodyResolveTransformer
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.FirImplicitTypeBodyResolveProcessor
import org.jetbrains.kotlin.fir.resolve.transformers.contracts.FirContractResolveProcessor
import org.jetbrains.kotlin.fir.resolve.transformers.mpp.FirExpectActualMatcherProcessor
import org.jetbrains.kotlin.fir.resolve.transformers.plugin.FirAnnotationArgumentsMappingProcessor
import org.jetbrains.kotlin.fir.resolve.transformers.plugin.FirAnnotationArgumentsResolveProcessor
import org.jetbrains.kotlin.fir.resolve.transformers.plugin.FirCompanionGenerationProcessor
import org.jetbrains.kotlin.fir.resolve.transformers.plugin.FirCompilerRequiredAnnotationsResolveProcessor
import org.jetbrains.kotlin.fir.scopes.impl.FirLocalScope
import org.jetbrains.kotlin.fir.scopes.impl.FirPackageMemberScope
import org.jetbrains.kotlin.fir.scopes.kotlinScopeProvider
import org.jetbrains.kotlin.fir.session.environment.AbstractProjectEnvironment
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeLookupTagBasedType
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.incremental.createDirectory
import org.jetbrains.kotlin.ir.backend.jvm.serialization.JvmIrMangler
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCompilationComponents
import org.jetbrains.kotlin.modules.Module
import org.jetbrains.kotlin.modules.TargetId
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.isCommon
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.platform.konan.isNative
import org.jetbrains.kotlin.progress.ProgressIndicatorAndCompilationCanceledStatus
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.types.ConstantValueKind
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class FirResult(
  val session: FirSession,
  val scopeSession: ScopeSession,
  val files: List<FirFile>,
  val scopeDeclarations: List<FirDeclaration>
)

class TemplateCompiler(
  disposable: Disposable,
  targetPlatform: TargetPlatform,
  val projectConfiguration: CompilerConfiguration,
  val frontEndScopeCache: FrontendScopeCache
) {

  private var counter = AtomicInteger(0)

  private val projectEnvironment: AbstractProjectEnvironment
  private val messageCollector: MessageCollector
  val ktPsiFactory: KtPsiFactory
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

  lateinit var session: FirSession

  data class TemplateResult(
    val firResults: List<FirResult>,
    val irResults: List<Fir2IrResult>
  )

  fun compileSource(
    source: String,
    extendedAnalysisMode: Boolean,
    scopeDeclarations : List<FirDeclaration>,
    produceIr: Boolean = false
  ): TemplateResult {
    val performanceManager = projectConfiguration.get(CLIConfigurationKeys.PERF_MANAGER)

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
    val outputs: ArrayList<FirResult> = arrayListOf()
    val irOutput: ArrayList<Fir2IrResult> = arrayListOf()
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
      val result = context.compileModule(scopeDeclarations)
//      val scopedClassSymbol: FirClassSymbol<out FirClass>? = scopeDeclarations.firstIsInstanceOrNull<FirClass>()?.symbol
//      if (result != null && scopedClassSymbol != null) {
//        result.files.forEach { file ->
//          file.transform<FirSimpleFunction, FirClassSymbol<*>>(
//            object : FirTransformer<FirClassSymbol<*>>() {
//              override fun <E : FirElement> transformElement(element: E, data: FirClassSymbol<*>): E {
//                element.transformChildren(this, data)
//                return if (element is FirSimpleFunction) {
//                  patchFunction(data, element) as E
//                } else {
//                  element
//                }
//              }
//            }
//          , scopedClassSymbol)
//        }
//      }

      val templateResult = result ?: return TemplateResult(emptyList(), emptyList())
      outputs += templateResult

      if (produceIr) {
        outputs.forEach {
          irOutput.add(convertToIR(it, moduleConfiguration))
        }
      }
    }
    return TemplateResult(outputs, irOutput)
  }

  private fun CompilationContext.compileModule(scopeDeclarations: List<FirDeclaration>): FirResult? {
    performanceManager?.notifyAnalysisStarted()
    ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

    if (!checkKotlinPackageUsage(moduleConfiguration, allSources)) return null

    val renderDiagnosticNames = moduleConfiguration.getBoolean(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME)
    val diagnosticsReporter = DiagnosticReporterFactory.createPendingReporter()
    val firResult = runFrontend(allSources, diagnosticsReporter, scopeDeclarations).also {
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

    return firResult
  }

  fun convertToIR(firResult: FirResult, moduleConfiguration: CompilerConfiguration): Fir2IrResult {
    val fir2IrExtensions = JvmFir2IrExtensions(moduleConfiguration, JvmIrDeserializerImpl(), JvmIrMangler)
    val linkViaSignatures = moduleConfiguration.getBoolean(JVMConfigurationKeys.LINK_VIA_SIGNATURES)
    val scopeFiles = firResult.scopeDeclarations.filterIsInstance<FirFile>()
    val files =  firResult.files + scopeFiles
    val fir2IrResult = firResult.session.convertToIr(
      firResult.scopeSession, files, fir2IrExtensions, emptyList(), linkViaSignatures
    )
    ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()
    return fir2IrResult
  }

  private val fromTemplateAnnotationClassId: ClassId
    get() = ClassId.fromString(FromTemplate::class.java.canonicalName.replace(".", "/"))

  private val fromTemplateClassLikeSymbol: FirClassLikeSymbol<*>
    get() =
      checkNotNull(
        session.symbolProvider.getClassLikeSymbolByClassId(fromTemplateAnnotationClassId)
      ) {
        // TODO: rename this artifact if it is wrong before publishing the final release
        "@CompileTime annotation is missing, add io.arrow-kt.arrow-inject-annotations"
      }

  @OptIn(SymbolInternals::class)
  private val fromTemplateAnnotationType: ConeLookupTagBasedType
    get() = fromTemplateClassLikeSymbol.fir.symbol.constructType(emptyArray(), false)

  @OptIn(SymbolInternals::class)
  fun patchFunction(owner: FirClassSymbol<*>, simpleFunction: FirSimpleFunction): FirSimpleFunction {
    val callableId = CallableId(owner.classId, simpleFunction.name)
    return buildSimpleFunctionCopy(simpleFunction) {
      annotations += buildAnnotation {
        annotationTypeRef = buildResolvedTypeRef { type = fromTemplateAnnotationType }
        argumentMapping =
          FirAnnotationArgumentMappingImpl(
            null,
            mapOf(
              Name.identifier("parent") to
                buildConstExpression(
                  null,
                  ConstantValueKind.String,
                  callableId.classId?.asString() ?: error("expected class name in callable"),
                  mutableListOf(),
                  true
                )
            )
          )
      }
      symbol =
        FirNamedFunctionSymbol(callableId).also {
          // it.bind(simpleFunction)
        }
      dispatchReceiverType = owner.fir.defaultType()
    }
  }

  private fun CompilationContext.runFrontend(
    ktFiles: List<KtFile>,
    diagnosticsReporter: BaseDiagnosticsCollector,
    scopeDeclarations: List<FirDeclaration>,
  ): FirResult? {
    val syntaxErrors = ktFiles.fold(false) { errorsFound, ktFile ->
      AnalyzerWithCompilerReport.reportSyntaxErrors(ktFile, messageCollector).isHasErrors or errorsFound
    }
    val scope = ScopeSession()
    val rawFir = session.buildFirFromKtFiles(ktFiles)

    val (scopeSession, fir) = session.runResolution(rawFir, scope, scopeDeclarations)
    session.runCheckers(scopeSession, fir, diagnosticsReporter)

    return if (syntaxErrors || diagnosticsReporter.hasErrors) null else FirResult(session, scopeSession, fir, scopeDeclarations)
    //return if (syntaxErrors) null else FirResult(session, scopeSession, fir)
  }

  fun FirSession.runResolution(firFiles: List<FirFile>, scopeSession: ScopeSession, scopeDeclarations: List<FirDeclaration>): Pair<ScopeSession, List<FirFile>> {
    val resolveProcessor = FirTotalResolveProcessor(this, scopeSession, scopeDeclarations)
    resolveProcessor.process(firFiles)
    return resolveProcessor.scopeSession to firFiles
  }

  fun FirSession.runCheckers(scopeSession: ScopeSession, firFiles: List<FirFile>, reporter: DiagnosticReporter) {
    val collector = FirDiagnosticsCollector.create(this, scopeSession)
    for (file in firFiles) {
      collector.collectDiagnostics(file, reporter)
    }
  }

  class FirTotalResolveProcessor(session: FirSession, val scopeSession: ScopeSession, scopeDeclarations: List<FirDeclaration>) {

    private val processors: List<FirResolveProcessor> = createAllCompilerResolveProcessors(
      session,
      scopeSession,
      scopeDeclarations
    )

    fun process(files: List<FirFile>) {
      for (processor in processors) {
        processor.beforePhase()
        when (processor) {
          is FirTransformerBasedResolveProcessor -> {
            for (file in files) {
              processor.processFile(file)
            }
          }
          is FirGlobalResolveProcessor -> {
            processor.process(files)
          }
        }
        processor.afterPhase()
      }
    }
  }

  fun FirSession.buildFirFromKtFiles(ktFiles: Collection<KtFile>): List<FirFile> {
    val firProvider = (firProvider as FirProviderImpl)
    val builder = RawFirBuilder(this, kotlinScopeProvider, BodyBuildingMode.NORMAL)
    return ktFiles.map {
      builder.buildFirFile(it).also { firFile ->
        firProvider.recordFile(firFile)
      }
    }
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

fun createAllCompilerResolveProcessors(
  session: FirSession,
  scopeSession: ScopeSession? = null,
  scopeDeclarations: List<FirDeclaration>
): List<FirResolveProcessor> {
  return createAllResolveProcessors(scopeSession) {
    createCompilerProcessorByPhase(session, it, scopeDeclarations)
  }
}

private inline fun <T : FirResolveProcessor> createAllResolveProcessors(
  scopeSession: ScopeSession? = null,
  creator: FirResolvePhase.(ScopeSession) -> T
): List<T> {
  @Suppress("NAME_SHADOWING")
  val scopeSession = scopeSession ?: ScopeSession()
  val phases = FirResolvePhase.values().filter {
    !it.noProcessor
  }
  return phases.map { it.creator(scopeSession) }
}

fun FirResolvePhase.createCompilerProcessorByPhase(
  session: FirSession,
  scopeSession: ScopeSession,
  scopeDeclarations: List<FirDeclaration>
): FirResolveProcessor {
  return when (this) {
    FirResolvePhase.RAW_FIR -> throw IllegalArgumentException("Raw FIR building phase does not have a transformer")
    FirResolvePhase.COMPILER_REQUIRED_ANNOTATIONS -> FirCompilerRequiredAnnotationsResolveProcessor(session, scopeSession)
    FirResolvePhase.COMPANION_GENERATION -> FirCompanionGenerationProcessor(session, scopeSession)
    FirResolvePhase.IMPORTS -> FirImportResolveProcessor(session, scopeSession)
    FirResolvePhase.SUPER_TYPES -> FirSupertypeResolverProcessor(session, scopeSession)
    FirResolvePhase.SEALED_CLASS_INHERITORS -> FirSealedClassInheritorsProcessor(session, scopeSession)
    FirResolvePhase.TYPES -> FirTypeResolveProcessor(session, scopeSession)
    FirResolvePhase.STATUS -> FirStatusResolveProcessor(session, scopeSession)
    FirResolvePhase.ARGUMENTS_OF_ANNOTATIONS -> FirAnnotationArgumentsResolveProcessor(session, scopeSession)
    FirResolvePhase.CONTRACTS -> FirContractResolveProcessor(session, scopeSession)
    FirResolvePhase.IMPLICIT_TYPES_BODY_RESOLVE -> FirImplicitTypeBodyResolveProcessor(session, scopeSession)
    FirResolvePhase.ANNOTATIONS_ARGUMENTS_MAPPING -> FirAnnotationArgumentsMappingProcessor(session, scopeSession)
    FirResolvePhase.BODY_RESOLVE -> FirBodyResolveProcessor(session, scopeSession, scopeDeclarations)
    FirResolvePhase.EXPECT_ACTUAL_MATCHING -> FirExpectActualMatcherProcessor(session, scopeSession)
  }
}

@OptIn(AdapterForResolveProcessor::class)
class FirBodyResolveProcessor(session: FirSession, scopeSession: ScopeSession, scopeDeclarations: List<FirDeclaration>) : FirTransformerBasedResolveProcessor(session, scopeSession) {
  override val transformer = FirBodyResolveTransformerAdapter(session, scopeSession, scopeDeclarations)
}

@AdapterForResolveProcessor
class FirBodyResolveTransformerAdapter(session: FirSession, scopeSession: ScopeSession, scopeDeclarations: List<FirDeclaration>) : FirTransformer<Any?>() {
  @OptIn(PrivateForInline::class)
  private val transformer = FirBodyResolveTransformer(
    session,
    phase = FirResolvePhase.BODY_RESOLVE,
    implicitTypeOnly = false,
    scopeSession = scopeSession,
    outerBodyResolveContext = BodyResolveContext(
      ReturnTypeCalculatorForFullBodyResolve,
      DataFlowAnalyzerContext.empty(session),
      scopeDeclarations.filterIsInstance<FirClassLikeDeclaration>().toSet()
    )
  ).also { bodyResolveTransformer ->
    val ctx = bodyResolveTransformer.context
    scopeDeclarations.forEach { analysisContext ->
      when (analysisContext) {
        is FirRegularClass -> {
          ctx.addReceiver(null, ImplicitDispatchReceiverValue(analysisContext.symbol, session, scopeSession))
          //ctx.addInaccessibleImplicitReceiverValue(analysisContext, SessionHolderImpl(session, scopeSession))
        }
        is FirFile -> {
          val filePackageScope = FirPackageMemberScope(analysisContext.packageFqName, session)
          ctx.addNonLocalTowerDataElement(filePackageScope.asTowerDataElement(false))
          //ctx.addLocalScope(FirLocalScope(session))
        }
        else -> {
          val localScope = FirLocalScope(session)
          ctx.addLocalScope(localScope)
        } //error("unsupported declaration: $analysisContext")
      }
    }
  }

  override fun <E : FirElement> transformElement(element: E, data: Any?): E {
    return element
  }


  override fun transformFile(file: FirFile, data: Any?): FirFile {
    return file.transform(transformer, ResolutionMode.ContextIndependent)
  }
}
