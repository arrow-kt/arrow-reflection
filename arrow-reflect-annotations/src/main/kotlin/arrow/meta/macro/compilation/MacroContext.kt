package arrow.meta.module.impl.arrow.meta.macro.compilation

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.impl.FirPrimaryConstructor
import org.jetbrains.kotlin.fir.declarations.primaryConstructorIfAny
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.renderReadableWithFqNames
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.text
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.orEmpty

interface MacroContext {

  fun diagnostics(scope: DiagnosticsContext.() -> Unit): DiagnosticsCompilation {
    return object : DiagnosticsCompilation {
      override fun runCompilation(context: DiagnosticsContext) {
        context.scope()
      }
    }
  }

  fun <T : FirElement> transform(scope: TransformContext.() -> T): TransformCompilation<T> {
    return object : TransformCompilation<T> {
      override fun transform(context: TransformContext): T {
        return context.scope()
      }
    }
  }

  fun FirClass.transform(scope: TransformClassFactory.() -> Unit): TransformClassCompilation {
    return object : TransformClassCompilation {
      override fun transform(context: TransformClassContext): TransformClassFactory {
        return TransformClassFactory(
          firClass = this@transform,
          context = context
        ).also { scope(it) }
      }
    }
  }

  @OptIn(SymbolInternals::class, DirectDeclarationsAccess::class)
  fun propertiesOf(session: FirSession, firClass: FirClass, f: (FirValueParameter) -> String): String =
    +firClass.declarations.firstIsInstanceOrNull<FirPrimaryConstructor>()?.valueParameters.orEmpty().filter { it.isVal }.map {
      f(it)
    }

  operator fun List<String>.unaryPlus(): String =
    joinToString()

  operator fun Sequence<String>.unaryPlus(): String =
    joinToString()

  operator fun Name?.unaryPlus(): String =
    this?.asString() ?: ""

  operator fun FirElement.unaryPlus(): String =
    (this as? FirTypeRef)?.coneType?.renderReadableWithFqNames()?.replace("/", ".")
      ?: source?.text?.toString()
      ?: error("$this has no source psi text element")
}
