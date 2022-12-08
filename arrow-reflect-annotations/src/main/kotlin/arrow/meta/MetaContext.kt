package arrow.meta

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.renderReadableWithFqNames
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.text
import kotlin.reflect.KClass

abstract class MetaContext(open val templateCompiler: TemplateCompiler) {

  operator fun List<String>.unaryPlus(): String =
    joinToString()

  operator fun Sequence<String>.unaryPlus(): String =
    joinToString()

  operator fun Name?.unaryPlus(): String =
    this?.asString() ?: ""

  operator fun Visibility?.unaryPlus(): String =
    when (this) {
      Visibilities.Public -> "public"
      Visibilities.Private -> "private"
      Visibilities.PrivateToThis -> "private"
      else -> ""
    }

  operator fun Modality?.unaryPlus(): String =
    when (this) {
      Modality.FINAL -> "final"
      Modality.SEALED -> "sealed"
      Modality.OPEN -> "open"
      Modality.ABSTRACT -> "abstract"
      null -> ""
    }

}

class FirMetaContext(
  override val templateCompiler: TemplateCompiler,
  val session: FirSession
) : MetaContext(templateCompiler) {

  @OptIn(SymbolInternals::class)
  fun propertiesOf(firClass: FirClass, f: (FirValueParameter) -> String): String =
    +firClass.primaryConstructorIfAny(session)?.fir?.valueParameters.orEmpty().filter { it.isVal }.map {
      f(it)
    }

  fun String.functionIn(firClass: FirClass): FirSimpleFunction {
    val results = templateCompiler.compileSource(this, extendedAnalysisMode = false, listOf(firClass))
    val firFiles = results.firResults.flatMap { it.files }
    val currentElement: FirSimpleFunction? = findSelectedFirElement(FirSimpleFunction::class, firFiles)
    return currentElement ?: error("Could not find a ${FirSimpleFunction::class}")
  }

  operator fun FirElement.unaryPlus(): String =
    source?.text?.toString()
      ?: (this as? FirTypeRef)?.coneType?.renderReadableWithFqNames()?.replace("/", ".")
      ?: error("$this has no source psi text element")

  inline fun <reified Fir : FirElement> String.frontend(context: List<FirDeclaration>): Fir {
    val results = templateCompiler.compileSource(this, extendedAnalysisMode = false, context)
    val firFiles = results.firResults.flatMap { it.files }
    val currentElement: Fir? = findSelectedFirElement(Fir::class, firFiles)
    return currentElement ?: error("Could not find a ${Fir::class}")
  }

  @PublishedApi
  internal fun <Fir : FirElement> findSelectedFirElement(firElementClass: KClass<Fir>, firFiles: List<FirFile>): Fir? {
    var currentElement: Fir? = null
    firFiles.forEach { firFile ->
      firFile.accept(object : FirVisitorVoid() {
        override fun visitElement(element: FirElement) {
          if (firElementClass.isInstance(element)) {
            currentElement = element as Fir
          } else
            element.acceptChildren(this)
        }
      })
    }
    return currentElement
  }

  @JvmName("renderFir")
  operator fun Iterable<FirElement>.unaryPlus(): String =
    source()

  fun Iterable<FirElement>.source(separator: String = ", ", unit: Unit = Unit): String =
    joinToString(", ") { +it }

}

