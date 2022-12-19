pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
    mavenLocal {
      content {
        includeGroup("io.arrow-kt")
        includeGroup("io.arrow-kt.reflect")
      }
    }
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
    mavenLocal {
      content {
        includeGroup("io.arrow-kt")
        includeGroup("io.arrow-kt.reflect")
      }
    }
  }
}

//sourceControl {
//  gitRepository(uri("https://github.com/JetBrains/kotlin.git")) {
//    rootDir = "/tree/master/compiler/fir/tree/tree-generator"
//    producesModule("compiler:tree-generator")
//  }
//}

include(":sandbox")
