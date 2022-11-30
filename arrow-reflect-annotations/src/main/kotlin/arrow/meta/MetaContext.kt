package arrow.meta.module.impl.arrow.meta

import arrow.meta.TemplateCompiler
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.FirCall
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.name.Name
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

  val synthetic = """TODO("synthetic body must be filled in IR Transformation")"""

  @OptIn(SymbolInternals::class)
  fun FirMetaContext.properties(firClass: FirClass, f: (FirValueParameter) -> String): String =
    +firClass.primaryConstructorIfAny(session)?.fir?.valueParameters.orEmpty().filter { it.isVal }.map {
      f(it)
    }

  fun String.functionIn(firClass: FirClass): FirSimpleFunction {
    val results = templateCompiler.compileSource(this, extendedAnalysisMode = false, firClass)
    val firFiles = results.firResults.flatMap { it.files }
    val currentElement: FirSimpleFunction? = findSelectedFirElement(FirSimpleFunction::class, firFiles)
    return currentElement ?: error("Could not find a ${FirSimpleFunction::class}")
  }

  operator fun FirElement.unaryPlus(): String =
    psi?.text ?: error("$this has no source psi text element")

  inline fun <reified Fir : FirElement> String.frontend(): Fir {
    val results = templateCompiler.compileSource(this, extendedAnalysisMode = false, null)
    val firFiles = results.firResults.flatMap { it.files }
    val currentElement: Fir? = findSelectedFirElement(Fir::class, firFiles)
    return currentElement ?: error("Could not find a ${Fir::class}")
  }

  fun <Fir : FirElement> findSelectedFirElement(firElementClass: KClass<Fir>, firFiles: List<FirFile>): Fir? {
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

  val String.call: FirCall
    get() =
      """
      val x = $this
    """.frontend()


}

class IrMetaContext(
  override val templateCompiler: TemplateCompiler,
  val irPluginContext: IrPluginContext
) : MetaContext(templateCompiler) {


  val String.expressionBody: IrExpressionBody
    get() =
      IrExpressionBodyImpl(
        UNDEFINED_OFFSET, UNDEFINED_OFFSET, backend<IrExpression>()
      )

  inline fun <reified Ir : IrElement> String.backend(): Ir {
    val results = templateCompiler.compileSource(this, extendedAnalysisMode = true, null, produceIr = true)
    val irModuleFragments = results.irResults.map { it.irModuleFragment }
    val currentElement: Ir? = findSelectedIrElement(Ir::class, irModuleFragments)
    return currentElement ?: error("Could not find a ${Ir::class}")
  }

  fun <Ir : IrElement> findSelectedIrElement(
    irElementClass: KClass<Ir>,
    irModuleFragments: List<IrModuleFragment>
  ): Ir? {
    var currentElement: Ir? = null
    irModuleFragments.forEach { irModuleFragment ->
      irModuleFragment.accept(object : IrElementVisitor<Unit, Unit> {
        override fun visitElement(element: IrElement, data: Unit) {
          if (irElementClass.isInstance(element)) {
            currentElement = element as Ir
          } else
            element.acceptChildren(this, Unit)
        }
      }, Unit)
    }
    return currentElement
  }

  operator fun IrElement.unaryPlus(): String =
    templateCompiler.sourceCache[startOffset to endOffset] ?: dumpKotlinLike(
      options = KotlinLikeDumpOptions(
        printRegionsPerFile = false,
        printFileName = false,
        printFilePath = false,
        useNamedArguments = false,
        labelPrintingStrategy = LabelPrintingStrategy.SMART,
        printFakeOverridesStrategy = FakeOverridesStrategy.NONE,
        printElseAsTrue = false,
      )
    )

  operator fun Iterable<IrElement>.unaryPlus(): String =
    source()

  @JvmName("printSeq")
  operator fun Sequence<IrElement>.unaryPlus(): String =
    toList().source()

  fun Iterable<IrElement>.source(separator: String = ", "): String =
    joinToString(", ") { +it }

  val String.const: IrConst<*>
    get() =
      """
      val x = $this
    """.backend<IrConst<*>>()

}

