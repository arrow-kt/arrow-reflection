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
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.ir.backend.jvm.serialization.JvmIrMangler
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.SymbolRemapper
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.utils.getSafe
import kotlin.sequences.forEach

class ArrowReflectFir2IrVisitor private constructor(
  val visitor: Fir2IrVisitor,
  val storage: Fir2IrCommonMemberStorage,
  private val conversionScope: Fir2IrConversionScope
) {

  fun <T : IrDeclarationParent, R> withParent(parent: T, f: T.() -> R): R {
    return conversionScope.withParent(parent, f)
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
      return Triple(componentsStorage.fir2IrVisitor, storage, field.getSafe(componentsStorage) as Fir2IrConversionScope)
    }
  }
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
context(visitor: ArrowReflectFir2IrVisitor)
fun IrClass.cacheFunctions(
  firFunctions: List<FirSimpleFunction>
) {
  functions.forEach { irFunction ->
    firFunctions.firstOrNull {
      it.name.identifier == irFunction.name.identifier
    }?.let { function ->
      visitor.storage.functionCache[function] = irFunction.symbol
    }
  }
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
context(visitor: ArrowReflectFir2IrVisitor)
fun IrClass.cacheProperties(
  firProperties: List<FirProperty>
) {
  properties.forEach { irProperty ->
    firProperties.firstOrNull {
      it.name.identifier == irProperty.name.identifier
    }?.let { property ->
      visitor.storage.propertyCache[property] = irProperty.symbol
    }
    irProperty.getter?.let { getter -> visitor.storage.getterForPropertyCache[irProperty.symbol] = getter.symbol }
  }
}
