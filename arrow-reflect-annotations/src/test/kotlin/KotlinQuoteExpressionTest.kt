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
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.impl.FirElseIfTrueCondition
import org.jetbrains.kotlin.fir.java.FirCliSession
import org.jetbrains.kotlin.fir.java.FirProjectSessionProvider
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.session.registerModuleData
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

@OptIn(PrivateSessionConstructor::class, SessionConfiguration::class)
class KotlinQuoteExpressionTest {

  @Test
  fun testReturnExpression() {
    assertTrue {
      val kotlin = Kotlin(session(), runResolution = false) { "return 2 + 2" }
      kotlin.filterIsInstance<FirReturnExpression>().isNotEmpty()
    }
  }

  @Test
  fun testIfElseExpression() {
    assertTrue {
      val kotlin = Kotlin(session(), runResolution = false) { "if(true) { 2 } else { 3 }" }
      kotlin.filterIsInstance<FirElseIfTrueCondition>().isNotEmpty()
    }
  }

  @Test
  fun testWhenExpression() {
    assertTrue {
      val kotlin = Kotlin(session(), runResolution = false) {
        """
          when {
            2 + 2 == 4 -> true
            else -> false
          }
        """.trimIndent()
      }
      kotlin.filterIsInstance<FirWhenExpression>().isNotEmpty()
    }
  }

  @Test
  fun testThrowExpression() {
    assertTrue {
      val kotlin = Kotlin(session(), runResolution = false) { "throw Exception()" }
      kotlin.filterIsInstance<FirThrowExpression>().isNotEmpty()
    }
  }

  @Test
  fun testBreakExpression() {
    assertTrue {
      val kotlin = Kotlin(session(), runResolution = false) { "break" }
      kotlin.filterIsInstance<FirBreakExpression>().isNotEmpty()
    }
  }

  @Test
  fun testContinueExpression() {
    assertTrue {
      val kotlin = Kotlin(session(), runResolution = false) { "continue" }
      kotlin.filterIsInstance<FirContinueExpression>().isNotEmpty()
    }
  }

  @Test
  fun testTryExpression() {
    assertTrue {
      val kotlin = Kotlin(session(), runResolution = false) {
        """
          try {
            2 + 2
          } catch (e: Exception) {
            3 + 3
          } finally {
            println()
          }
        """.trimIndent()
      }
      kotlin.filterIsInstance<FirTryExpression>().isNotEmpty()
    }
  }

  @Test
  fun testWhileLoopStatement() {
    assertTrue {
      val kotlin = Kotlin(session(), runResolution = false) { "while (true) { println() }" }
      kotlin.filterIsInstance<FirWhileLoop>().isNotEmpty()
    }
  }

  @Test
  fun testForLoopStatement() {
    assertTrue {
      val kotlin = Kotlin(session(), runResolution = false) { "for (i in 1..10) { println(i) }" }
      kotlin.filterIsInstance<FirWhileLoop>().isNotEmpty()
    }
  }

  @Test
  fun testDoWhileLoopStatement() {
    assertTrue {
      val kotlin = Kotlin(session(), runResolution = false) { "do { println() } while (true)" }
      kotlin.filterIsInstance<FirDoWhileLoop>().isNotEmpty()
    }
  }

  @Test
  fun testMultipleWhileLoopStatementPredicate() {
    assertTrue {
      val kotlin = Kotlin(session(), runResolution = false) {
        """
          while (true) { println() }
          while (false) { println() }
        """.trimIndent()
      }
      kotlin.filterIsInstance<FirWhileLoop>().size == 2
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
