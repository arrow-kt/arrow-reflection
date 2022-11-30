import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin.jvm)
  //  id("io.arrow-kt.reflect") version "0.1.0"
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    //    useK2 = true
    freeCompilerArgs +=
      listOf(
        "-Xcontext-receivers"
        // "-Xplugin=$rootDir/arrow-inject-compiler-plugin/build/libs/arrow-reflect-compiler-plugin-0.1.0.jar"
        )
  }
}

dependencies {
  implementation(project(":arrow-reflect-annotations"))
//  implementation("io.arrow-kt:arrow-reflect-annotations:0.1.0")
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(8)) // "8"
  }
}
