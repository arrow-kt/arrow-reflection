import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
}

plugins {
  alias(libs.plugins.kotlin.jvm)
  //  alias(libs.plugins.arrowGradleConfig.kotlin)
  alias(libs.plugins.arrowGradleConfig.publish)
}

tasks {
  dokkaGfm { enabled = false }
  dokkaHtml { enabled = false }
  dokkaJavadoc { enabled = false }
  dokkaJekyll { enabled = false }
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    useK2.set(true)
    freeCompilerArgs.set(listOf("-Xcontext-receivers"))
  }
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(8)) // "8"
  }
}


sourceSets {
  main {
    kotlin {
      srcDirs(
        "$projectDir/src/main/kotlin",
        "$projectDir/src/main/generated"
      )
      include("**/*.kt")
    }
  }
}

dependencies {
  compileOnly(libs.kotlin.compiler)
}
