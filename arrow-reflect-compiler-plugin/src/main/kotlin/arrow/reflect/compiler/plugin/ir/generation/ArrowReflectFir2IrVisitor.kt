package arrow.reflect.compiler.plugin.ir.generation

import org.jetbrains.kotlin.backend.jvm.JvmIrDeserializerImpl
import org.jetbrains.kotlin.backend.jvm.JvmIrSpecialAnnotationSymbolProvider
import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.DiagnosticReporterFactory
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.backend.*
import org.jetbrains.kotlin.fir.backend.jvm.FirJvmVisibilityConverter
import org.jetbrains.kotlin.fir.backend.jvm.JvmFir2IrExtensions
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.ir.backend.jvm.serialization.JvmIrMangler
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.util.SymbolRemapper
import org.jetbrains.kotlin.utils.getSafe

class ArrowReflectFir2IrVisitor private constructor(
  val visitor: Fir2IrVisitor,
  val storage: Fir2IrCommonMemberStorage,
  private val conversionScope: Fir2IrConversionScope
) {

  fun <T : IrDeclarationParent, R> withParent(parent: T, f: T.() -> R): R {
    return conversionScope.withParent(parent, f)
  }

  fun containingClass(containingFirClass: FirClass, f: () -> Unit) {
    conversionScope.withContainingFirClass(containingFirClass, f)
  }

  companion object {
    fun create(
      session: FirSession,
      compilerConfiguration: CompilerConfiguration
    ): ArrowReflectFir2IrVisitor {
      val irResult = bindVisitor(session = session, scopeSession = ScopeSession(), compilerConfiguration = compilerConfiguration)
      return ArrowReflectFir2IrVisitor(visitor = irResult.first, storage = irResult.second, conversionScope = irResult.third)
    }

    private fun bindVisitor(
      session: FirSession,
      scopeSession: ScopeSession,
      compilerConfiguration: CompilerConfiguration
    ): Triple<Fir2IrVisitor, Fir2IrCommonMemberStorage, Fir2IrConversionScope> {
      val fir2IrExtensions = JvmFir2IrExtensions(compilerConfiguration, JvmIrDeserializerImpl())
      val messageCollector = compilerConfiguration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
      val diagnosticReporter = DiagnosticReporterFactory.createPendingReporter(messageCollector)
      val fir2IrConfiguration = Fir2IrConfiguration.forJvmCompilation(compilerConfiguration, diagnosticReporter)
      val storage = Fir2IrCommonMemberStorage()
      val componentsStorage = Fir2IrComponentsStorage(
        session,
        scopeSession,
        listOf(),
        fir2IrExtensions,
        fir2IrConfiguration,
        FirJvmVisibilityConverter,
        storage,
        JvmIrMangler,
        DefaultBuiltIns.Instance,
        JvmIrSpecialAnnotationSymbolProvider,
        FirProviderWithGeneratedFiles(session, mapOf()),
        Fir2IrSyntheticIrBuiltinsSymbolsContainer(),
        SymbolRemapper.EMPTY
      )
      val field = componentsStorage::class.java.getDeclaredField("conversionScope")
      field.isAccessible = true
      return Triple(componentsStorage.fir2IrVisitor, storage, field.getSafe(componentsStorage) as Fir2IrConversionScope)
    }
  }
}


