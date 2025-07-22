import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.arrowGradleConfig.kotlin)
}

dependencies {
  runtimeOnly(libs.kotlin.stdlib)
  runtimeOnly(project(":arrow-reflect-annotations"))
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}

tasks {
  named<Delete>("clean") {
    delete("$rootDir/docs/docs/apidocs")
  }

  withType<KotlinCompile>().configureEach {
    compilerOptions {
      freeCompilerArgs.add("-Xskip-runtime-version-check")
    }
  }
}
