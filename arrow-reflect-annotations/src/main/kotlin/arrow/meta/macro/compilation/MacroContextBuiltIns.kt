package arrow.meta.module.impl.arrow.meta.macro.compilation

import arrow.meta.module.impl.arrow.meta.quote.Kotlin
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.impl.FirPrimaryConstructor
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

@OptIn(SymbolInternals::class, DirectDeclarationsAccess::class)
context(_: MacroContext)
fun FirClass.properties(f: (FirValueParameter) -> String): String =
  +declarations.firstIsInstanceOrNull<FirPrimaryConstructor>()?.valueParameters.orEmpty().filter { it.isVal }.map {
    f(it)
  }

context(_: MacroContext)
operator fun List<String>.unaryPlus(): String =
  joinToString()

context(_: MacroContext)
operator fun Sequence<String>.unaryPlus(): String =
  joinToString()

context(_: MacroContext)
operator fun Name?.unaryPlus(): String =
  this?.asString() ?: ""

context(_: MacroContext)
operator fun FirElement.unaryPlus(): String =
  (this as? FirTypeRef)?.coneType?.renderReadableWithFqNames()?.replace("/", ".")
    ?: source?.text?.toString()
    ?: error("$this has no source psi text element")

context(_: MacroContext)
fun String.function(session: FirSession, scope: List<FirDeclaration>): FirSimpleFunction? {
  return Kotlin(session = session, scope = scope, code = { this }).firstIsInstanceOrNull()
}
