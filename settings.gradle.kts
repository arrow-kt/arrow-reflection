pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
  }
}

include(
  ":arrow-reflect-annotations",
  ":arrow-reflect-compiler-plugin",
  ":arrow-reflect-gradle-plugin",
)

// Docs
include(":arrow-reflect-docs")
project(":arrow-reflect-docs").projectDir = File("docs")

val localProperties =
  java.util.Properties().apply {
    val localPropertiesFile = file("local.properties").apply { createNewFile() }
    load(localPropertiesFile.inputStream())
  }

val isSandboxEnabled = localProperties.getProperty("sandbox.enabled", "false").toBoolean()

if (isSandboxEnabled) include(":sandbox")
