@file:OptIn(PrivateForInline::class)

package arrow.meta

import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.KtInMemoryTextSourceFile
import org.jetbrains.kotlin.KtIoFileSourceFile
import org.jetbrains.kotlin.KtSourceFile
import org.jetbrains.kotlin.backend.jvm.JvmIrDeserializerImpl
import org.jetbrains.kotlin.backend.jvm.JvmIrTypeSystemContext
import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.*
import org.jetbrains.kotlin.cli.common.modules.ModuleBuilder
import org.jetbrains.kotlin.cli.jvm.compiler.*
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.constant.EvaluatedConstTracker
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.analysis.collectors.FirDiagnosticsCollector
import org.jetbrains.kotlin.fir.backend.Fir2IrConfiguration
import org.jetbrains.kotlin.fir.backend.Fir2IrResult
import org.jetbrains.kotlin.fir.backend.jvm.FirJvmVisibilityConverter
import org.jetbrains.kotlin.fir.backend.jvm.JvmFir2IrExtensions
import org.jetbrains.kotlin.fir.builder.AbstractRawFirBuilder
import org.jetbrains.kotlin.fir.builder.BodyBuildingMode
import org.jetbrains.kotlin.fir.builder.PsiRawFirBuilder
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.lightTree.LightTree2Fir
import org.jetbrains.kotlin.fir.pipeline.Fir2IrActualizedResult
import org.jetbrains.kotlin.fir.pipeline.ModuleCompilerAnalyzedOutput
import org.jetbrains.kotlin.fir.resolve.*
import org.jetbrains.kotlin.fir.resolve.calls.ImplicitDispatchReceiverValue
import org.jetbrains.kotlin.fir.resolve.dfa.DataFlowAnalyzerContext
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
import org.jetbrains.kotlin.fir.resolve.providers.impl.FirProviderImpl
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
import org.jetbrains.kotlin.fir.session.sourcesToPathsMapper
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.fir.visitors.FirVisitor
import org.jetbrains.kotlin.incremental.createDirectory
import org.jetbrains.kotlin.ir.backend.jvm.serialization.JvmIrMangler
import org.jetbrains.kotlin.modules.Module
import org.jetbrains.kotlin.progress.ProgressIndicatorAndCompilationCanceledStatus
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.readSourceFileWithMapping
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import org.jetbrains.kotlin.fir.pipeline.convertToIrAndActualize
import org.jetbrains.kotlin.fir.pipeline.convertToIrAndActualizeForJvm
import org.jetbrains.kotlin.util.PrivateForInline
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JvmBuiltIns

class FirResult(
  val session: FirSession,
  val scopeSession: ScopeSession,
  val files: List<FirFile>,
  val scopeDeclarations: List<FirDeclaration>
)

class TemplateCompiler(
  val projectConfiguration: CompilerConfiguration
) {

  private var counter = AtomicInteger(0)

  private val chunk: List<Module>
  val templatesFolder = File(File("."), "/build/meta/templates")

  init {

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
    val irResults: List<Fir2IrActualizedResult>
  )

  var compiling: Boolean = false

  fun compileSource(
    metaCheckerContext: FirMetaCheckerContext?,
    source: String,
    scopeDeclarations: List<FirDeclaration>,
    produceIr: Boolean = false
  ): TemplateResult {
    compiling = true
    try {
      val next = counter.incrementAndGet()
      //val fileName = "meta.template_$next.kt"
      println("parsing source:\n$source")
      println("session: ${session::class}")
      val outputs: ArrayList<FirResult> = arrayListOf()
      val irOutput: ArrayList<Fir2IrActualizedResult> = arrayListOf()
      val messageCollector: MessageCollector = MessageCollector.NONE
      for (module in chunk) {
        val moduleConfiguration = projectConfiguration//.applyModuleProperties(module, buildFile)
        val context = CompilationContext(
          source,
          messageCollector,
          moduleConfiguration
        )
        val result = context.compileModule(metaCheckerContext, scopeDeclarations)

        val templateResult = result ?: return TemplateResult(emptyList(), emptyList())
        outputs += templateResult

        if (produceIr) {
          outputs.forEach {
            irOutput.add(convertToIR(metaCheckerContext, it, moduleConfiguration))
          }
        }
      }
      return TemplateResult(outputs, irOutput)
    } finally {
      compiling = false
    }
  }

  private fun CompilationContext.compileModule(metaCheckerContext: FirMetaCheckerContext?, scopeDeclarations: List<FirDeclaration>): FirResult? {
    ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()
    val renderDiagnosticNames = true
    val diagnosticsReporter = DiagnosticReporterFactory.createPendingReporter()
    val firResult = runFrontend(source, diagnosticsReporter, scopeDeclarations)
    val diagnosticsContext = metaCheckerContext?.checkerContext
    if (firResult == null && diagnosticsContext != null) {
      diagnosticsReporter.diagnostics.forEach {
        metaCheckerContext.diagnosticReporter.report(it, diagnosticsContext)
        println("error: [" + it.factory.name + "] " + it.factory.ktRenderer.render(it))
      }
      return null
    }
    return firResult
  }

  fun convertToIR(metaCheckerContext: FirMetaCheckerContext?, firResult: FirResult, moduleConfiguration: CompilerConfiguration): Fir2IrActualizedResult {
    val diagnosticReporter = DiagnosticReporterFactory.createPendingReporter()
    val fir2IrExtensions = JvmFir2IrExtensions(moduleConfiguration, JvmIrDeserializerImpl(), JvmIrMangler)
    val linkViaSignatures = moduleConfiguration.getBoolean(JVMConfigurationKeys.LINK_VIA_SIGNATURES)
    val scopeFiles = firResult.scopeDeclarations.filterIsInstance<FirFile>()
    val files = firResult.files + scopeFiles
    val validatedFirResult = with(firResult) {
      org.jetbrains.kotlin.fir.pipeline.FirResult(listOf(
        ModuleCompilerAnalyzedOutput(session = session, scopeSession = scopeSession, fir = files)
      ))
    }

    val fir2IrConfiguration = Fir2IrConfiguration(
      languageVersionSettings = moduleConfiguration.languageVersionSettings,
      diagnosticReporter = diagnosticReporter,
      linkViaSignatures = linkViaSignatures,
      evaluatedConstTracker = moduleConfiguration
        .putIfAbsent(CommonConfigurationKeys.EVALUATED_CONST_TRACKER, EvaluatedConstTracker.create()),
      inlineConstTracker = moduleConfiguration[CommonConfigurationKeys.INLINE_CONST_TRACKER],
      // TODO: read notes on this flag
      allowNonCachedDeclarations = false
    )

    val fir2IrResult = validatedFirResult.convertToIrAndActualizeForJvm(
      fir2IrExtensions= fir2IrExtensions,
      irGeneratorExtensions = emptyList(),
      fir2IrConfiguration = fir2IrConfiguration,
    )
    ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()
    val diagnosticsContext = metaCheckerContext?.checkerContext
    if (diagnosticsContext != null) {
      diagnosticReporter.diagnostics.forEach {
        metaCheckerContext.diagnosticReporter.report(it, diagnosticsContext)
        println("error: [" + it.factory.name + "] " + it.factory.ktRenderer.render(it))
      }
    }

    return fir2IrResult
  }

  private fun runFrontend(
    source: String,
    diagnosticsReporter: BaseDiagnosticsCollector,
    scopeDeclarations: List<FirDeclaration>,
  ): FirResult? {
    val syntaxErrors = false
    val scope = ScopeSession()
    val next = counter.incrementAndGet()
    val fileName = "meta.template_$next.kt"
    val rawFir = session.buildFirViaLightTree(listOf(KtInMemoryTextSourceFile(fileName, null, source))) // ,.buildFirFromKtFiles(ktFiles)
    val (scopeSession, fir) = session.runResolution(rawFir, scope, scopeDeclarations)
    session.runCheckers(scopeSession, fir, diagnosticsReporter)
    return if (syntaxErrors || diagnosticsReporter.hasErrors) null else FirResult(
      session,
      scopeSession,
      fir,
      scopeDeclarations
    )
  }

  fun FirSession.runResolution(
    firFiles: List<FirFile>,
    scopeSession: ScopeSession,
    scopeDeclarations: List<FirDeclaration>
  ): Pair<ScopeSession, List<FirFile>> {
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

  class FirTotalResolveProcessor(
    session: FirSession,
    val scopeSession: ScopeSession,
    scopeDeclarations: List<FirDeclaration>
  ) {
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
              withFileAnalysisExceptionWrapping(file) {
                processor.processFile(file)
              }
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

  fun FirSession.buildFirViaLightTree(
    files: Collection<KtSourceFile>,
    diagnosticsReporter: DiagnosticReporter? = null,
    reportFilesAndLines: ((Int, Int) -> Unit)? = null
  ): List<FirFile> {
    val firProvider = (firProvider as? FirProviderImpl)
    val sourcesToPathsMapper = sourcesToPathsMapper
    val builder = LightTree2Fir(this, kotlinScopeProvider, diagnosticsReporter)
    val shouldCountLines = (reportFilesAndLines != null)
    var linesCount = 0
    val firFiles = files.map { file ->
      val (code, linesMapping) = file.getContentsAsStream().reader(Charsets.UTF_8).use {
        it.readSourceFileWithMapping()
      }
      if (shouldCountLines) {
        linesCount += linesMapping.lastOffset
      }
      builder.buildFirFile(code, file, linesMapping).also { firFile ->
        firProvider?.recordFile(firFile)
        sourcesToPathsMapper.registerFileSource(firFile.source!!, file.path ?: file.name)
      }
    }
    reportFilesAndLines?.invoke(files.count(), linesCount)
    return firFiles
  }

  fun FirSession.buildFirFromKtFiles(ktFiles: Collection<KtFile>): List<FirFile> {
    val firProvider = (firProvider as? FirProviderImpl)
    val builder = PsiRawFirBuilder(this, kotlinScopeProvider, BodyBuildingMode.NORMAL)
    return ktFiles.map {
      builder.buildFirFile(it).also { firFile ->
        firProvider?.recordFile(firFile)
      }
    }
  }

  private class CompilationContext(
    val source: String,
    val messageCollector: MessageCollector,
    val moduleConfiguration: CompilerConfiguration
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
    FirResolvePhase.COMPILER_REQUIRED_ANNOTATIONS -> FirCompilerRequiredAnnotationsResolveProcessor(
      session,
      scopeSession
    )

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

class FirBodyResolveProcessor(
  session: FirSession,
  scopeSession: ScopeSession,
  scopeDeclarations: List<FirDeclaration>
) : FirTransformerBasedResolveProcessor(session, scopeSession, FirResolvePhase.BODY_RESOLVE) {

  override val transformer = FirBodyResolveTransformerAdapter(session, scopeSession, scopeDeclarations)
}


class FirBodyResolveTransformerAdapter(
  session: FirSession,
  scopeSession: ScopeSession,
  scopeDeclarations: List<FirDeclaration>
) : FirTransformer<Any?>() {

  @OptIn(PrivateForInline::class)
  private val transformer = FirBodyResolveTransformer(
    session,
    phase = FirResolvePhase.BODY_RESOLVE,
    implicitTypeOnly = false,
    scopeSession = scopeSession,
    outerBodyResolveContext = BodyResolveContext(
      ReturnTypeCalculatorForFullBodyResolve.Default,
      DataFlowAnalyzerContext(session),
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
