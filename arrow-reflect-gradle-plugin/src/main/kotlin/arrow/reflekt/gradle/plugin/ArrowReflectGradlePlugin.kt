package arrow.reflect.gradle.plugin

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

public class ArrowReflectGradlePlugin : KotlinCompilerPluginSupportPlugin {

  private val groupId: String = "io.arrow-kt"

  private val artifactId: String = "arrow-reflect-compiler-plugin"

  private val pluginId: String = "reflect"

  private val version: String = ArrowReflectVersion

  override fun getPluginArtifact(): SubpluginArtifact {
    return SubpluginArtifact(groupId, artifactId, version)
  }

  override fun applyToCompilation(
    kotlinCompilation: KotlinCompilation<*>
  ): Provider<List<SubpluginOption>> {
    val project = kotlinCompilation.target.project
    return project.provider { emptyList() }
  }

  override fun getCompilerPluginId(): String {
    return "arrow.reflect.compiler.plugin.$pluginId"
  }

  override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
    return true
  }
}
