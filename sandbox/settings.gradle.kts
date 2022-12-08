pluginManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
  }
}

dependencyResolutionManagement {
  repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
  }
}

//sourceControl {
//  gitRepository(uri("https://github.com/JetBrains/kotlin.git")) {
//    rootDir = "/tree/master/compiler/fir/tree/tree-generator"
//    producesModule("compiler:tree-generator")
//  }
//}

include(":sandbox")
