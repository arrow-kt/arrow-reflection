package arrow.reflect.compiler.plugin.services

import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import java.io.File
import java.io.FilenameFilter

class QuotesAnnotationsConfigurator(testServices: TestServices) :
  EnvironmentConfigurator(testServices) {

  companion object {
    private const val QUOTES_JAR_DIR = "../arrow-reflection-quotes/build/libs/"
    private val QUOTES_JAR_FILTER = FilenameFilter { _, name ->
      name.startsWith("arrow-reflection-quotes") &&
        name.endsWith(".jar") &&
        !name.endsWith("-javadoc.jar") &&
        !name.endsWith("-sources.jar")
    }
    val libDir: File
      get() = File(QUOTES_JAR_DIR)

    private val failMessage = {
      "Jar with quotes does not exist. Please run :arrow-reflection-quotes:jar"
    }

    fun jar(testServices: TestServices) =
      libDir.listFiles(QUOTES_JAR_FILTER)?.firstOrNull()
        ?: testServices.assertions.fail(failMessage)
  }

  override fun configureCompilerConfiguration(
    configuration: CompilerConfiguration,
    module: TestModule
  ) {
    testServices.assertions.assertTrue(libDir.exists() && libDir.isDirectory, failMessage)
    val jar = jar(testServices)
    println("found quotes jar: ${jar}")
    configuration.addJvmClasspathRoot(jar)
  }
}