package arrow.reflect.compiler.plugin.fir.transformers

import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.TransformCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.TransformContext
import arrow.reflect.compiler.plugin.targets.macro.MacroInvoke
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.visitors.FirTransformer

class FirMacroTransformer(
  private val session: FirSession,
  private val macro: MacroInvoke,
  private val diagnosticReporter: DiagnosticReporter,
  private val checkerContext: CheckerContext
) : FirTransformer<FirDeclaration>() {

  @Suppress("UNCHECKED_CAST")
  private fun <E : FirElement> invokeMacro(element: E, scope: FirDeclaration): E {
    val annotations = (element as? FirAnnotationContainer)?.annotations ?: emptyList()
    val compilations = macro(
      session = session,
      context = object : MacroContext {},
      element = element,
      annotations = annotations
    )
    var result: E? = null
    compilations.forEach {
      result = (it as? TransformCompilation<E>)?.run {
        val context = TransformContext(
          session = session,
          diagnosticReporter = diagnosticReporter,
          checkerContext = checkerContext,
          scope = listOf(scope)
        )
        transform(context)
      } ?: result
    }
    return result ?: element
  }

  override fun <E : FirElement> transformElement(element: E, data: FirDeclaration): E {
    element.transformChildren(this, data)
    return invokeMacro(element = element, scope = data)
  }
}
