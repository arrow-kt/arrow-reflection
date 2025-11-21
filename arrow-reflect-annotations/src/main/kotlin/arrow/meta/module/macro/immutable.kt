package arrow.meta.module.impl.arrow.meta.module.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import arrow.meta.module.impl.arrow.meta.macro.compilation.DiagnosticsContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.diagnosticError
import arrow.meta.module.impl.arrow.meta.macro.compilation.diagnostics
import arrow.meta.module.impl.arrow.meta.macro.compilation.report
import arrow.meta.samples.Immutable
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.isKMutableProperty
import org.jetbrains.kotlin.fir.types.renderReadableWithFqNames
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import kotlin.reflect.KType
import kotlin.reflect.typeOf

private val FoundMutableVar by diagnosticError()
private val FoundMutableKProperty by diagnosticError()
private val FoundMutableIterable by diagnosticError()

@Macro(target = Immutable::class)
context(_: MacroContext)
fun immutable(declaration: FirDeclaration): MacroCompilation {
  return diagnostics {
    declaration.accept(object : FirVisitorVoid() {
      override fun visitElement(element: FirElement) {
        element.acceptChildren(this)
      }
      override fun visitProperty(property: FirProperty) {
        property.check()
        visitElement(property)
      }

      override fun visitFunctionCall(functionCall: FirFunctionCall) {
        functionCall.check()
        super.visitFunctionCall(functionCall)
      }
    })
  }
}

context(_: DiagnosticsContext)
private fun FirProperty.check() {
  if (isVar) {
    report(FoundMutableVar, "mutable vars are forbidden in `${Immutable::class.java.simpleName}` container")
  }
}

context(context: DiagnosticsContext)
private fun FirFunctionCall.check() {
  val resolvedType = toResolvedCallableSymbol()?.resolvedReturnType
  if (resolvedType != null) {
    if (context.isMutableKProperty(resolvedType)) {
      report(
        FoundMutableKProperty,
        "mutable KProperties are forbidden in `${Immutable::class.java.simpleName}` container"
      )
    }
    if (context.extends(typeOf<MutableIterable<*>>(), resolvedType)) {
      report(
        FoundMutableIterable,
        "mutable iterables are forbidden in `${Immutable::class.java.simpleName}` container"
      )
    }
  }
}

private fun DiagnosticsContext.extends(type: KType, coneType: ConeKotlinType): Boolean {
  val rawType = coneType.renderReadableWithFqNames().replace("/", ".").substringBefore("<")
  val selectedType = type.toString().substringBefore("<")
  return rawType == selectedType || coneType.toRegularClassSymbol(session)?.resolvedSuperTypes?.any {
    extends(type, it)
  } == true
}

private fun DiagnosticsContext.isMutableKProperty(coneType: ConeKotlinType) =
  coneType.isKMutableProperty(session)
