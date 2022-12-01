package arrow.reflect.compiler.plugin.services

import java.io.File
import java.io.FilenameFilter
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions

class PluginAnnotationsConfigurator(testServices: TestServices) :
  EnvironmentConfigurator(testServices) {

  companion object {
    private const val ANNOTATIONS_JAR_DIR = "../arrow-reflect-annotations/build/libs/"
    private val ANNOTATIONS_JAR_FILTER = FilenameFilter { _, name ->
      name.startsWith("arrow-reflect-annotations") &&
        name.endsWith(".jar") &&
        !name.endsWith("-javadoc.jar") &&
        !name.endsWith("-sources.jar")
    }
  }

  override fun configureCompilerConfiguration(
    configuration: CompilerConfiguration,
    module: TestModule
  ) {
    val libDir = File(ANNOTATIONS_JAR_DIR)
    testServices.assertions.assertTrue(libDir.exists() && libDir.isDirectory, failMessage)
    val jar =
      libDir.listFiles(ANNOTATIONS_JAR_FILTER)?.firstOrNull()
        ?: testServices.assertions.fail(failMessage)
    println("found jar: $jar")
    configuration.addJvmClasspathRoot(jar)
  }

  private val failMessage = {
    "Jar with annotations does not exist. Please run :arrow-reflect-annotations:jar"
  }
}
