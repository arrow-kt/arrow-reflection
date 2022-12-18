import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.8.255-SNAPSHOT"

plugins {
  id("org.jetbrains.kotlin.jvm") version "1.8.255-SNAPSHOT"
  id("io.arrow-kt.reflect") version "0.1.0"
  application
}

application { mainClass.set("example.SampleKt") }

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    useK2.set(true)
    freeCompilerArgs.addAll(
      "-Xcontext-receivers",
      // "-Xplugin=$rootDir/arrow-inject-compiler-plugin/build/libs/arrow-reflect-compiler-plugin-0.1.0.jar"
    )
  }
}

dependencies {
  // implementation(project(":arrow-reflect-annotations"))
  implementation("io.arrow-kt:arrow-reflect-annotations:0.1.0")
  kotlinCompilerPluginClasspath("org.jetbrains.kotlin:kotlin-compiler:$kotlinVersion")
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(8)) // "8"
  }
}
