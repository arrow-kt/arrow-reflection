import arrow.meta.module.impl.arrow.meta.quote.EvaluatedFirExpr
import arrow.meta.module.impl.arrow.meta.quote.Expr
import arrow.meta.module.impl.arrow.meta.quote.FirExprQuote
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
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.booleanLiteralValue
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.session.registerModuleData
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

@OptIn(PrivateSessionConstructor::class, SessionConfiguration::class)
class QuoteExpressionTest {

  @Test
  fun testReturnExpression() {
    assertTrue {
      val expr = Expr { "return 2 + 2" }
      val fir = expr.fir(session = session())
      fir.unbox() is FirReturnExpression
    }
  }

  @Test
  fun testIfElseExpression() {
    assertTrue {
      val expr = Expr { "if(true) { 2 } else { 3 }" }
      val fir = expr.fir(session = session())
      fir.unbox() is FirElseIfTrueCondition
    }
  }

  @Test
  fun testWhenExpression() {
    assertTrue {
      val expr = Expr {
        """
          when {
            2 + 2 == 4 -> true
            else -> false
          }
        """.trimIndent()
      }
      val fir = expr.fir(session = session())
      fir.unbox() is FirWhenExpression
    }
  }

  @Test
  fun testThrowExpression() {
    assertTrue {
      val expr = Expr { "throw Exception()" }
      val fir = expr.fir(session = session())
      fir.unbox() is FirThrowExpression
    }
  }

  @Test
  fun testBreakExpression() {
    assertTrue {
      val expr = Expr { "break" }
      val fir = expr.fir(session = session())
      fir.unbox() is FirBreakExpression
    }
  }

  @Test
  fun testContinueExpression() {
    assertTrue {
      val expr = Expr { "continue" }
      val fir = expr.fir(session = session())
      fir.unbox() is FirContinueExpression
    }
  }

  @Test
  fun testTryExpression() {
    assertTrue {
      val expr = Expr {
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
      val fir = expr.fir(session = session())
      fir.unbox() is FirTryExpression
    }
  }

  @Test
  fun testWhileLoopStatement() {
    assertTrue {
      val expr = Expr { "while (true) { println() }" }
      val fir = expr.fir(session = session())
      fir.unbox() is FirWhileLoop
    }
  }

  @Test
  fun testForLoopStatement() {
    assertTrue {
      val expr = Expr { "for (i in 1..10) { println(i) }" }
      val fir = expr.fir(session = session())
      fir.unbox() is FirWhileLoop
    }
  }

  @Test
  fun testDoWhileLoopStatement() {
    assertTrue {
      val expr = Expr { "do { println() } while (true)" }
      val fir = expr.fir(session = session())
      fir.unbox() is FirDoWhileLoop
    }
  }

  @Test
  fun testReturnExplicitExpression() {
    assertTrue {
      val expr = Expr {
        """
          do { println() } while (true)
          return 2 + 2
        """.trimIndent()
      }
      val fir = expr.findFir<FirReturnExpression>(session = session())
      fir.unbox() is FirReturnExpression
    }
  }

  @Test
  fun testWhileLoopExplicitStatementWithPredicate() {
    assertTrue {
      val expr = Expr {
        """
          while (true) { println() }
          while (false) { println() }
        """.trimIndent()
      }
      val fir = expr.findFir<FirWhileLoop>(session = session()) { expression ->
        expression.condition.booleanLiteralValue == false
      }
      val unboxedFir = fir.unbox()
      unboxedFir is FirWhileLoop && unboxedFir.condition.booleanLiteralValue == false
    }
  }

  inline fun <reified T : FirStatement> FirExprQuote<T>.unbox(): T? = (this as? EvaluatedFirExpr<T>)?.fir

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
