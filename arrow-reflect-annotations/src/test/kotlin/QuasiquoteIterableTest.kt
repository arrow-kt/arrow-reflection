@file:OptIn(PrivateSessionConstructor::class, SessionConfiguration::class)

import arrow.meta.module.impl.arrow.meta.quote.FirKotlinCodeTransformer
import arrow.meta.module.impl.arrow.meta.quote.QuasiquoteIterable
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSourceModuleData
import org.jetbrains.kotlin.fir.PrivateSessionConstructor
import org.jetbrains.kotlin.fir.SessionConfiguration
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.expressions.FirReturnExpression
import org.jetbrains.kotlin.fir.expressions.FirWhileLoop
import org.jetbrains.kotlin.fir.java.FirCliSession
import org.jetbrains.kotlin.fir.java.FirProjectSessionProvider
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.session.registerModuleData
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.text
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class QuasiquoteIterableTest {

  @Test
  fun testQuasiquoteIterableIterateOverDeclaration() {
    val code: () -> String = {
      """
        class X {}
      """.trimIndent()
    }
    val iterable = iterable(code = code(), isExpression = false)
    assertTrue {
      iterable.firstOrNull { it is FirClass }?.source.text == code()
    }
  }

  @Test
  fun testQuasiquoteIterableIterateOverMultipleDeclarations() {
    val fooClass = "class Foo"
    val barClass = "class Bar"
    val code: () -> String = {
      """
        package foo.bar
        
        $fooClass
        $barClass
      """.trimIndent()
    }
    val iterable = iterable(code = code(), isExpression = false)
    assertTrue {
      val classes = iterable.filterIsInstance<FirClass>()
      classes.find { it.source.text == fooClass } != null && classes.find { it.source.text == barClass } != null
    }
  }

  @Test
  fun testQuasiquoteIterableIterateOverExpression() {
    val code: () -> String = {
      """
        for (i in 1..10) { println(i) }
      """.trimIndent()
    }
    val iterable = iterable(code = code(), isExpression = true)
    assertTrue {
      iterable.firstOrNull { it is FirWhileLoop }?.source.text == code()
    }
  }

  @Test
  fun testQuasiquoteIterableIterateOverMultipleExpressions() {
    val retrn = "return 1 + 1"
    val whle = "while(true) {}"
    val code: () -> String = {
      """
        $retrn
        $whle
      """.trimIndent()
    }
    val iterable = iterable(code = code(), isExpression = true)
    assertTrue {
      iterable.firstOrNull { it is FirReturnExpression }?.source.text == retrn
      iterable.firstOrNull { it is FirWhileLoop }?.source.text == whle
    }
  }

  private fun iterable(code: String, isExpression: Boolean): QuasiquoteIterable {
    val file = FirKotlinCodeTransformer.transform(session = session(), code = code, isExpression = isExpression)
    return QuasiquoteIterable(file)
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
