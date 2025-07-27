pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
    mavenLocal {
      content {
        includeGroup("io.arrow-kt")
        includeGroup("io.arrow-kt.reflect")
      }
    }
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
    mavenLocal {
      content {
        includeGroup("io.arrow-kt")
        includeGroup("io.arrow-kt.reflect")
      }
    }
  }
}

//sourceControl {
//  gitRepository(uri("https://github.com/JetBrains/kotlin.git")) {
//    rootDir = "/tree/master/compiler/fir/tree/tree-generator"
//    producesModule("compiler:tree-generator")
//  }
//}

include(
  ":arrow-reflect-annotations",
  ":arrow-reflect-compiler-plugin",
  ":arrow-reflect-gradle-plugin",
  ":arrow-reflection-quotes",
)

// Docs
//include(":arrow-reflect-docs")
//project(":arrow-reflect-docs").projectDir = File("docs")

val localProperties =
  java.util.Properties().apply {
    val localPropertiesFile = file("local.properties").apply { createNewFile() }
    load(localPropertiesFile.inputStream())
  }

val isSandboxEnabled = localProperties.getProperty("sandbox.enabled", "false").toBoolean()

if (isSandboxEnabled) include(":sandbox")
