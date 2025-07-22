import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlin.jvm)
  //  alias(libs.plugins.arrowGradleConfig.kotlin)
  alias(libs.plugins.arrowGradleConfig.publish)
}

dependencies {
  compileOnly(libs.kotlin.gradlePluginApi)

  implementation(gradleKotlinDsl())
  implementation(libs.classgraph)
  implementation(libs.kotlin.gradlePluginApi)
}

kotlin {
  explicitApi()
  
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}

tasks {
  dokkaGfm { enabled = false }
  dokkaHtml { enabled = false }
  dokkaJavadoc { enabled = false }
  dokkaJekyll { enabled = false }

  withType<JavaCompile>().configureEach {
    sourceCompatibility = "${JavaVersion.VERSION_11}"
    targetCompatibility = "${JavaVersion.VERSION_11}"
  }

  withType<KotlinCompile>().configureEach {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
    }
  }
}

gradlePlugin {
  plugins {
    create("arrow-reflect") {
      id = "io.arrow-kt.reflect"
      displayName = "Arrow Reflect Gradle Plugin"
      implementationClass = "arrow.reflect.gradle.plugin.ArrowReflectGradlePlugin"
    }
  }
}

pluginBundle {
  website = "https://arrow-kt.io/docs/reflect"
  vcsUrl = "https://github.com/arrow-kt/arrow-reflect"
  description = "Compile time reflection"
  tags =
    listOf(
      "kotlin",
      "compiler",
      "arrow",
      "plugin",
      "meta",
      "reflect",
      "reflection",
    )
}

generateArrowReflectVersionFile()

fun generateArrowReflectVersionFile() {
  val generatedDir =
    File("$buildDir/generated/main/kotlin/").apply {
      mkdirs()
      File("$this/arrow/reflect/gradle/plugin/ArrowReflectVersion.kt").apply {
        ensureParentDirsCreated()
        createNewFile()
        writeText(
          """
            |package arrow.reflect.gradle.plugin
            |
            |internal val ArrowReflectVersion = "${project.version}"
            |
          """.trimMargin()
        )
      }
    }

  kotlin.sourceSets.map { it.kotlin.srcDirs(generatedDir) }
}
