import org.jetbrains.kotlin.gradle.dsl.JvmTarget
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

  withType<JavaCompile>().configureEach {
    sourceCompatibility = "${JavaVersion.VERSION_1_8}"
    targetCompatibility = "${JavaVersion.VERSION_1_8}"
  }

  withType<KotlinCompile>().configureEach {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_1_8)
      useK2.set(true)
      freeCompilerArgs.add("-Xcontext-receivers")
    }
  }

  create<JavaExec>("generateTests") {
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("arrow.reflect.compiler.plugin.GenerateTestsKt")

    dependsOn(":arrow-reflect-annotations:jar")
  }

  test {
    testLogging { showStandardStreams = true }

    useJUnitPlatform()
    doFirst {
      setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib", "kotlin-stdlib")
      setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-jdk8", "kotlin-stdlib-jdk8")
      setLibraryProperty("org.jetbrains.kotlin.test.kotlin-reflect", "kotlin-reflect")
      setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test", "kotlin-test")
      setLibraryProperty("org.jetbrains.kotlin.test.kotlin-script-runtime", "kotlin-script-runtime")
      setLibraryProperty("org.jetbrains.kotlin.test.kotlin-annotations-jvm", "kotlin-annotations-jvm")
    }

    dependsOn("generateTests")
    dependsOn(":arrow-reflect-annotations:jar")
  }
}

sourceSets {
  test {
    java.srcDirs("src/testGenerated")
  }
}

dependencies {
  implementation(project(":arrow-reflect-annotations"))
  implementation(libs.classgraph)
  implementation(libs.kotlin.compiler)

  testCompileOnly(libs.kotlin.compiler)
  testImplementation(libs.kotlin.compiler)

  testRuntimeOnly(libs.kotlin.test)
  testRuntimeOnly(libs.kotlin.scriptRuntime)
  testRuntimeOnly(libs.kotlin.annotationsJvm)

  testImplementation(libs.kotlin.compilerInternalTestFramework)
  testImplementation(libs.kotlin.reflect)
  testImplementation(libs.kotlin.stdlib)
  testImplementation("junit:junit:4.13.2")

  testImplementation(platform("org.junit:junit-bom:5.13.1"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("org.junit.platform:junit-platform-commons")
  testImplementation("org.junit.platform:junit-platform-launcher")
  testImplementation("org.junit.platform:junit-platform-runner")
  testImplementation("org.junit.platform:junit-platform-suite-api")
}

fun Test.setLibraryProperty(propName: String, jarName: String) {
  val path =
    project.configurations.testRuntimeClasspath
      .get()
      .files
      .find { """$jarName-\d.*jar""".toRegex().matches(it.name) }
      ?.absolutePath
      ?: return
  systemProperty(propName, path)
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(8)) // "8"
  }
}
