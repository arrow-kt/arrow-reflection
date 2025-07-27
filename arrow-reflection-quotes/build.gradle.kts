import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.arrowGradleConfig.publish)
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }
  
  sourceSets.all {
    languageSettings {
      optIn("kotlin.RequiresOptIn")
      optIn("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
      optIn("org.jetbrains.kotlin.diagnostics.InternalDiagnosticFactoryMethod")
      optIn("org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI")
      optIn("org.jetbrains.kotlin.fir.PrivateForInline")
      optIn("org.jetbrains.kotlin.fir.resolve.dfa.DfaInternals")
      optIn("org.jetbrains.kotlin.fir.resolve.transformers.AdapterForResolveProcessor")
      optIn("org.jetbrains.kotlin.fir.symbols.SymbolInternals")
    }
  }
}

tasks {
  dokkaGfm { enabled = false }
  dokkaHtml { enabled = false }
  dokkaJavadoc { enabled = false }
  dokkaJekyll { enabled = false }
}

dependencies {
  // Core dependencies - need access to FIR and compiler APIs
  implementation(project(":arrow-reflect-annotations"))
  implementation(libs.kotlin.compiler)
  implementation(libs.kotlin.reflect)
  
  // Test dependencies
  testImplementation(libs.kotlin.test)
  testImplementation(libs.kotlin.compilerInternalTestFramework)
  testRuntimeOnly(libs.kotlin.scriptRuntime)
  testRuntimeOnly(libs.kotlin.annotationsJvm)
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    freeCompilerArgs.addAll(
      "-Xcontext-receivers",
      "-Xskip-prerelease-check"
    )
  }
}

// Configure test task
tasks.test {
  useJUnitPlatform()
}