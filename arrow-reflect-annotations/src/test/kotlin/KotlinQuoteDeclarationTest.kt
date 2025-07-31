import arrow.meta.module.impl.arrow.meta.quote.Kotlin
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSourceModuleData
import org.jetbrains.kotlin.fir.PrivateSessionConstructor
import org.jetbrains.kotlin.fir.SessionConfiguration
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.java.FirCliSession
import org.jetbrains.kotlin.fir.java.FirProjectSessionProvider
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.session.registerModuleData
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.text
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

@OptIn(PrivateSessionConstructor::class, SessionConfiguration::class)
class KotlinQuoteDeclarationTest {

  @Test
  fun testClassDeclaration() {
    assertTrue {
      val code = "class Test {}"
      val kotlin = Kotlin(session()) { code }
      val klass = kotlin.filterIsInstance<FirClass>()
      klass.size == 1 && klass.first().source.text == code
    }
  }

  @Test
  fun testObjectDeclaration() {
    assertTrue {
      val code = "object Test {}"
      val kotlin = Kotlin(session()) { code }
      val klass = kotlin.filterIsInstance<FirClass>()
      klass.size == 1 && klass.first().source.text == code
    }
  }

  @Test
  fun testInterfaceDeclaration() {
    assertTrue {
      val code = "interface Test {}"
      val kotlin = Kotlin(session()) { code }
      val klass = kotlin.filterIsInstance<FirClass>()
      klass.size == 1 && klass.first().source.text == code
    }
  }

  @Test
  fun testCompanionObjectDeclaration() {
    assertTrue {
      val code = "companion object { fun x() {} }"
      val kotlin = Kotlin(session()) { code }
      val klass = kotlin.filterIsInstance<FirClass>()
      klass.size == 1 && klass.first().source.text == code
    }
  }

  @Test
  fun testSealedClassDeclaration() {
    assertTrue {
      val code = "sealed class Test {}"
      val kotlin = Kotlin(session()) { code }
      val klass = kotlin.filterIsInstance<FirClass>()
      klass.size == 1 && klass.first().source.text == code
    }
  }

  @Test
  fun testEnumDeclaration() {
    assertTrue {
      val code = "enum class Test { TEST, TEST2 }"
      val kotlin = Kotlin(session()) { code }
      val klass = kotlin.filterIsInstance<FirClass>()
      klass.size == 1 && klass.first().source.text == code
    }
  }

  @Test
  fun testFunctionDeclaration() {
    assertTrue {
      val code = "fun test() {}"
      val kotlin = Kotlin(session()) { code }
      val klass = kotlin.filterIsInstance<FirFunction>()
      klass.size == 1 && klass.first().source.text == code
    }
  }

  @Test
  fun testValueDeclaration() {
    assertTrue {
      val code = "val test = 2"
      val kotlin = Kotlin(session()) { code }
      val klass = kotlin.filterIsInstance<FirProperty>()
      klass.size == 1 && klass.first().source.text == code
    }
  }

  @Test
  fun testVariableDeclaration() {
    assertTrue {
      val code = "var test = 2"
      val kotlin = Kotlin(session()) { code }
      val klass = kotlin.filterIsInstance<FirProperty>()
      klass.size == 1 && klass.first().source.text == code
    }
  }

  @Test
  fun testTypeAliasDeclaration() {
    assertTrue {
      val code = "typealias Test = String"
      val kotlin = Kotlin(session()) { code }
      val klass = kotlin.filterIsInstance<FirTypeAlias>()
      klass.size == 1 && klass.first().source.text == code
    }
  }

  @Test
  fun testAnnotationClassDeclaration() {
    assertTrue {
      val code = "annotation class Test(val x: String)"
      val kotlin = Kotlin(session()) { code }
      val klass = kotlin.filterIsInstance<FirClass>()
      klass.size == 1 && klass.first().source.text == code
    }
  }

  @Test
  fun testFileDeclaration() {
    assertTrue {
      val code = """
          package example.sample

          import kotlin.annotation.AnnotationTarget.*

          @Target(CLASS, PROPERTY, CONSTRUCTOR, FUNCTION)
          @Retention(AnnotationRetention.SOURCE)
          annotation class Test
      """.trimIndent()
      val kotlin = Kotlin(session()) { code }
      val klass = kotlin.filterIsInstance<FirFile>()
      klass.size == 1 && klass.first().source.text == code
    }
  }

  @Test
  fun testClassDeclarationInsideFile() {
    assertTrue {
      val testClass = "annotation class Test"
      val code: () -> String = {
        """
          package example.sample

          import kotlin.annotation.AnnotationTarget.*

          @Target(CLASS, PROPERTY, CONSTRUCTOR, FUNCTION)
          @Retention(AnnotationRetention.SOURCE)
          $testClass
        """.trimIndent()
      }
      val kotlin = Kotlin(session()) { code() }
      val klass = kotlin.filterIsInstance<FirClass>()
      klass.size == 1 && klass.first().source.text?.contains(testClass) == true
    }
  }

  @Test
  fun testClassExplicitDeclarationInsideFileWithPredicate() {
    assertTrue {
      val fooClass = "class Foo"
      val barClass = "class Bar"
      val code: () -> String = {
        """
        package foo.bar
        
        $fooClass
        $barClass
      """.trimIndent()
      }
      val kotlin = Kotlin(session()) { code() }
      kotlin.find { it.source.text == fooClass } != null && kotlin.find { it.source.text == barClass } != null
    }
  }

  private fun session(): FirSession {
    val applicationEnvironment = KotlinCoreEnvironment.createForProduction(
      projectDisposable = Disposer.newDisposable(),
      configuration = CompilerConfiguration(),
      configFiles = EnvironmentConfigFiles.METADATA_CONFIG_FILES
    )
    val moduleData = FirSourceModuleData(
      name = Name.special("<TestFirModuleData>"),
      dependencies = emptyList(),
      dependsOnDependencies = emptyList(),
      friendDependencies = emptyList(),
      platform = JvmPlatforms.defaultJvmPlatform
    )
    val project = applicationEnvironment.project
    PsiFileFactory.getInstance(project)
    return FirCliSession(FirProjectSessionProvider(), FirSession.Kind.Library).apply {
      register(FirKotlinScopeProvider::class, FirKotlinScopeProvider())
      registerModuleData(moduleData)
    }
  }
}
