import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.9.20-Beta2"

plugins {
  id("org.jetbrains.kotlin.jvm") version "1.9.20-Beta2"
  id("io.arrow-kt.reflect") version "0.1.0"
  application
}

application {
  mainClass.set("example.SampleKt")
}

tasks {
  withType<JavaCompile>().configureEach {
    sourceCompatibility = "${JavaVersion.VERSION_1_8}"
    targetCompatibility = "${JavaVersion.VERSION_1_8}"
  }

  withType<KotlinCompile>().configureEach {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_1_8)
      languageVersion.set(KotlinVersion.KOTLIN_2_0)
      freeCompilerArgs.add("-Xcontext-receivers")
    }
  }
}

dependencies {
  // implementation(project(":arrow-reflect-annotations"))
  implementation("io.arrow-kt:arrow-reflect-annotations:0.1.0")
  kotlinCompilerPluginClasspath("org.jetbrains.kotlin:kotlin-compiler:$kotlinVersion")
}
