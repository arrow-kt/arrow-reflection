package arrow.reflect.compiler.plugin.runners

import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.backend.BlackBoxCodegenSuppressor
import org.jetbrains.kotlin.test.backend.handlers.IrTextDumpHandler
import org.jetbrains.kotlin.test.backend.handlers.IrTreeVerifierHandler
import org.jetbrains.kotlin.test.backend.handlers.JvmBoxRunner
import org.jetbrains.kotlin.test.backend.ir.JvmIrBackendFacade
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.irHandlersStep
import org.jetbrains.kotlin.test.builders.jvmArtifactsHandlersStep
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.DUMP_IR
import org.jetbrains.kotlin.test.runners.RunnerWithTargetBackendForTestGeneratorMarker

open class AbstractBoxTest : BaseTestRunner(), RunnerWithTargetBackendForTestGeneratorMarker {
  override val targetBackend: TargetBackend = TargetBackend.JVM_IR

  override fun configure(builder: TestConfigurationBuilder) = with(builder) {
    commonFirWithPluginFrontendConfiguration()
    
    defaultDirectives { 
      +DUMP_IR 
    }
    
    irHandlersStep {
      useHandlers(
        ::IrTextDumpHandler,
        ::IrTreeVerifierHandler,
      )
    }
    
    facadeStep(::JvmIrBackendFacade)
    
    jvmArtifactsHandlersStep { 
      useHandlers(::JvmBoxRunner) 
    }

    useAfterAnalysisCheckers(::BlackBoxCodegenSuppressor)
  }
}
