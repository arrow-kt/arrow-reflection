import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin.jvm)
  //  alias(libs.plugins.arrowGradleConfig.kotlin)
  alias(libs.plugins.arrowGradleConfig.publish)
}

kotlin {
  sourceSets.all {
    languageSettings {
      optIn("kotlin.RequiresOptIn")
      optIn("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
      optIn("org.jetbrains.kotlin.diagnostics.InternalDiagnosticFactoryMethod")
      optIn("org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI")
      optIn("org.jetbrains.kotlin.fir.PrivateForInline")
      optIn("org.jetbrains.kotlin.fir.resolve.dfa.DfaInternals")
      optIn("org.jetbrains.kotlin.fir.symbols.SymbolInternals")
    }
  }
}

tasks {
  dokkaGfm { enabled = false }
  dokkaHtml { enabled = false }
  dokkaJavadoc { enabled = false }
  dokkaJekyll { enabled = false }

  withType<KotlinCompile>().configureEach {
    compilerOptions {
      useK2.set(true)
      freeCompilerArgs.add("-Xcontext-receivers")
    }
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
  kotlinCompilerPluginClasspath(libs.kotlin.compiler)
}
