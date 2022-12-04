package arrow.meta.samples

import arrow.meta.module.impl.arrow.meta.FirMetaContext
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.analysis.checkers.PsiSourceNavigator.getRawName
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.expressions.FirCall
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.dfa.DfaInternals
import org.jetbrains.kotlin.fir.resolve.dfa.symbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class CallDecoratorTransformer(
    private val metaContext: FirMetaContext,
    private val context: CheckerContext
) : FirTransformer<Unit>() {

  @OptIn(DfaInternals::class, SymbolInternals::class)
  private fun declarationName(newElement: FirCall): Name? =
    when (val callDeclaration = newElement.symbol?.fir) {
      is FirClass -> callDeclaration.classId.shortClassName
      is FirSimpleFunction -> callDeclaration.name
      else -> callDeclaration?.getRawName()?.let { Name.identifier(it) }
    }

  @OptIn(SymbolInternals::class)
  private fun isDecorated(newElement: FirFunctionCall): Boolean =
    newElement.toResolvedCallableSymbol()?.fir?.annotations?.hasAnnotation(
        ClassId.topLevel(
            FqName(
                Decorator::class.java.canonicalName
            )
        )
    ) == true

  private fun FirMetaContext.decoratedCall(
      newElement: FirFunctionCall,
      context: CheckerContext?
  ): FirCall {
    val args = newElement.arguments
    val argsApplied = args.mapIndexed { n, expr -> "args[$n] as ${+expr.typeRef}" }
    val name = declarationName(newElement)
    //language=kotlin
    return """| import arrow.meta.samples.Decorator
                | val x = Decorator.decorate<Any?, ${+newElement.typeRef}>(listOf(${+args})) { args: List<Any?> -> ${+name}(${+argsApplied}) }
                |""".trimMargin().frontend<FirCall>(context?.containingDeclarations.orEmpty())
  }

  override fun <E : FirElement> transformElement(element: E, data: Unit): E =
    metaContext.run {
    val newElement = element.transformChildren(this@CallDecoratorTransformer, data) as E
    val newCall = if (newElement is FirFunctionCall && isDecorated(newElement)) {
      //language=kotlin
      val call: FirCall = decoratedCall(newElement, context)
      call
    } else newElement
    newCall as E
  }
}
