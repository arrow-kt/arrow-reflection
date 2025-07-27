package arrow.reflect.compiler.plugin.services

import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.RuntimeClasspathProvider
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import java.io.File
import java.io.FilenameFilter

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
    val libDir: File
      get() = File(ANNOTATIONS_JAR_DIR)

    private val failMessage = {
      "Jar with annotations does not exist. Please run :arrow-reflect-annotations:jar"
    }

    fun jar(testServices: TestServices) =
      libDir.listFiles(ANNOTATIONS_JAR_FILTER)?.firstOrNull()
        ?: testServices.assertions.fail(failMessage)
  }


  override fun configureCompilerConfiguration(
    configuration: CompilerConfiguration,
    module: TestModule
  ) {

    testServices.assertions.assertTrue(libDir.exists() && libDir.isDirectory, failMessage)
    val jar = jar(testServices)
    println("found jar: ${jar}")
    configuration.addJvmClasspathRoot(jar)
  }


}

class MetaRuntimeClasspathProvider(testServices: TestServices) : RuntimeClasspathProvider(testServices) {
  override fun runtimeClassPaths(module: TestModule): List<File> {
    val annotationsJar = PluginAnnotationsConfigurator.jar(testServices)
    val quotesJarDir = File("../arrow-reflection-quotes/build/libs/")
    val quotesJar = quotesJarDir.listFiles { _, name ->
      name.startsWith("arrow-reflection-quotes") &&
        name.endsWith(".jar") &&
        !name.endsWith("-javadoc.jar") &&
        !name.endsWith("-sources.jar")
    }?.firstOrNull()
    
    return if (quotesJar != null) {
      listOf(annotationsJar, quotesJar)
    } else {
      listOf(annotationsJar)
    }
  }
}

fun TestConfigurationBuilder.configureForRuntimeAnnotationLibrary() {
  useConfigurators(::PluginAnnotationsConfigurator)
  useCustomRuntimeClasspathProviders(::MetaRuntimeClasspathProvider)
}

