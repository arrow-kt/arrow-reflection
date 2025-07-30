import arrow.meta.module.impl.arrow.meta.quote.Declr
import arrow.meta.module.impl.arrow.meta.quote.EvaluatedFirDeclr
import arrow.meta.module.impl.arrow.meta.quote.FirDeclrQuote
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
import org.jetbrains.kotlin.fir.declarations.utils.nameOrSpecialName
import org.jetbrains.kotlin.fir.java.FirCliSession
import org.jetbrains.kotlin.fir.java.FirProjectSessionProvider
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.session.registerModuleData
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

@OptIn(PrivateSessionConstructor::class, SessionConfiguration::class)
class QuoteDeclarationTest {

  @Test
  fun testClassDeclaration() {
    assertTrue {
      val declr = Declr { "class Test {}" }
      val fir = declr.fir(session = session())
      fir.unbox() is FirClass
    }
  }

  @Test
  fun testObjectDeclaration() {
    assertTrue {
      val declr = Declr { "object Test {}" }
      val fir = declr.fir(session = session())
      fir.unbox() is FirClass
    }
  }

  @Test
  fun testInterfaceDeclaration() {
    assertTrue {
      val declr = Declr { "interface Test {}" }
      val fir = declr.fir(session = session())
      fir.unbox() is FirClass
    }
  }

  @Test
  fun testCompanionObjectDeclaration() {
    assertTrue {
      val declr = Declr { "companion object { fun x() {} }" }
      val fir = declr.fir(session = session())
      fir.unbox() is FirClass
    }
  }

  @Test
  fun testSealedClassDeclaration() {
    assertTrue {
      val declr = Declr { "sealed class Test {}" }
      val fir = declr.fir(session = session())
      fir.unbox() is FirClass
    }
  }

  @Test
  fun testEnumDeclaration() {
    assertTrue {
      val declr = Declr { "enum class Test { TEST, TEST2 }" }
      val fir = declr.fir(session = session())
      fir.unbox() is FirClass
    }
  }

  @Test
  fun testFunctionDeclaration() {
    assertTrue {
      val declr = Declr { "fun test() {}" }
      val fir = declr.fir(session = session())
      fir.unbox() is FirFunction
    }
  }

  @Test
  fun testValueDeclaration() {
    assertTrue {
      val declr = Declr { "val test = 2" }
      val fir = declr.fir(session = session())
      fir.unbox() is FirProperty
    }
  }

  @Test
  fun testVariableDeclaration() {
    assertTrue {
      val declr = Declr { "var test = 2" }
      val fir = declr.fir(session = session())
      fir.unbox() is FirProperty
    }
  }

  @Test
  fun testTypeAliasDeclaration() {
    assertTrue {
      val declr = Declr { "typealias Test = String" }
      val fir = declr.fir(session = session())
      fir.unbox() is FirTypeAlias
    }
  }

  @Test
  fun testAnnotationClassDeclaration() {
    assertTrue {
      val declr = Declr { "annotation class Test(val x: String)" }
      val fir = declr.fir(session = session())
      fir.unbox() is FirClass
    }
  }

  @Test
  fun testClassExplicitDeclaration() {
    assertTrue {
      val declr = Declr { "class Test {}" }
      val fir = declr.findFir<FirClass>(session = session())
      fir.unbox() is FirClass
    }
  }

  @Test
  fun testFileExplicitDeclaration() {
    assertTrue {
      val declr = Declr {
        """
          package example.sample

          import kotlin.annotation.AnnotationTarget.*

          @Target(CLASS, PROPERTY, CONSTRUCTOR, FUNCTION)
          @Retention(AnnotationRetention.SOURCE)
          annotation class Test
        """.trimIndent()
      }
      val fir = declr.findFir<FirFile>(session = session())
      fir.unbox() is FirFile
    }
  }

  @Test
  fun testClassExplicitDeclarationInsideFile() {
    assertTrue {
      val declr = Declr {
        """
          package example.sample

          import kotlin.annotation.AnnotationTarget.*

          @Target(CLASS, PROPERTY, CONSTRUCTOR, FUNCTION)
          @Retention(AnnotationRetention.SOURCE)
          annotation class Test
        """.trimIndent()
      }
      val fir = declr.findFir<FirClass>(session = session())
      val unboxedFir = fir.unbox()
      unboxedFir is FirClass && unboxedFir.nameOrSpecialName.asString() == "Test"
    }
  }

  @Test
  fun testClassExplicitDeclarationInsideFileWithPredicate() {
    assertTrue {
      val declr = Declr {
        """
          package example.sample

          class Foo
          class Bar
        """.trimIndent()
      }
      val fir = declr.findFir<FirClass>(session = session()) { firClass ->
        firClass.nameOrSpecialName.asString() == "Bar"
      }
      val unboxedFir = fir.unbox()
      unboxedFir is FirClass && unboxedFir.nameOrSpecialName.asString() == "Bar"
    }
  }

  inline fun <reified T : FirDeclaration> FirDeclrQuote<T>.unbox(): T? = (this as? EvaluatedFirDeclr<T>)?.fir

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
