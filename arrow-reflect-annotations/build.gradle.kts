import org.gradle.kotlin.dsl.withType
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

}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(11)) // "11"
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

  testCompileOnly(libs.kotlin.compiler)
  testImplementation(libs.kotlin.compiler)
  testImplementation(libs.kotlin.stdlib)
  testImplementation(libs.kotlin.test)
  testImplementation("junit:junit:4.13.2")
  testImplementation(platform("org.junit:junit-bom:5.13.4"))
  testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    freeCompilerArgs.add("-Xcontext-parameters")
  }
}

tasks.test {
  useJUnitPlatform()
}
