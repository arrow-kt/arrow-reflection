import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.9.25"

plugins {
  id("org.jetbrains.kotlin.jvm") version "1.9.25"
  id("io.arrow-kt.reflect") version "0.1.0"
  application
}

application {
  mainClass.set("example.SampleKt")
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}

tasks {
  withType<JavaCompile>().configureEach {
    sourceCompatibility = "${JavaVersion.VERSION_11}"
    targetCompatibility = "${JavaVersion.VERSION_11}"
  }

  withType<KotlinCompile>().configureEach {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
      useK2.set(true)
      freeCompilerArgs.add("-Xcontext-receivers")
    }
  }
}

dependencies {
  // implementation(project(":arrow-reflect-annotations"))
  implementation("io.arrow-kt:arrow-reflect-annotations:0.1.0")
  kotlinCompilerPluginClasspath("org.jetbrains.kotlin:kotlin-compiler:$kotlinVersion")
}
