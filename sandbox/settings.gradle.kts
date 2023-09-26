pluginManagement {
  repositories {
    mavenLocal {
      content {
        includeGroup("io.arrow-kt")
        includeGroup("io.arrow-kt.reflect")
      }
    }
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
  }
}

dependencyResolutionManagement {
  repositories {
    mavenLocal {
      content {
        includeGroup("io.arrow-kt")
        includeGroup("io.arrow-kt.reflect")
      }
    }
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
  }
}

//sourceControl {
//  gitRepository(uri("https://github.com/JetBrains/kotlin.git")) {
//    rootDir = "/tree/master/compiler/fir/tree/tree-generator"
//    producesModule("compiler:tree-generator")
//  }
//}

include(":sandbox")
