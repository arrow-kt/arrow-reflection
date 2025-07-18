package arrow.meta.samples

import arrow.meta.Diagnostics
import arrow.meta.FirMetaCheckerContext
import arrow.meta.Meta
import arrow.meta.samples.ImmutableErrors.FoundMutableIterable
import arrow.meta.samples.ImmutableErrors.FoundMutableKProperty
import arrow.meta.samples.ImmutableErrors.FoundMutableVar
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.visitors.FirVisitor
import kotlin.reflect.KType
import kotlin.reflect.typeOf

object ImmutableErrors : Diagnostics.Error {
  val FoundMutableVar by error1()
  val FoundMutableKProperty by error1()
  val FoundMutableIterable by error1()
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Immutable {
  companion object : Meta.Checker.Declaration<FirDeclaration>,
    Diagnostics(FoundMutableVar, FoundMutableKProperty, FoundMutableIterable) {

    override fun FirMetaCheckerContext.check(declaration: FirDeclaration) {
      declaration.accept(object : FirVisitor<Unit, Unit>() {
        override fun visitElement(element: FirElement, data: Unit) {
          element.acceptChildren(this, data)
        }

        override fun visitProperty(property: FirProperty, data: Unit) {
          if (property.isVar) {
            property.report(FoundMutableVar, "mutable vars are forbidden in `${Immutable::class.java.simpleName}` container")
          }
          visitElement(property, data)
        }

        override fun visitFunctionCall(functionCall: FirFunctionCall, data: Unit) {
          val resolvedType = functionCall.toResolvedCallableSymbol()?.resolvedReturnType
          if (resolvedType != null) {
            if (isMutableKProperty(resolvedType)) {
              functionCall.report(
                FoundMutableKProperty,
                "mutable KProperties are forbidden in `${Immutable::class.java.simpleName}` container"
              )
            }
            if (extends(typeOf<MutableIterable<*>>(), resolvedType)) {
              functionCall.report(
                FoundMutableIterable,
                "mutable iterables are forbidden in `${Immutable::class.java.simpleName}` container"
              )
            }
          }
          super.visitFunctionCall(functionCall, data)
        }

      }, Unit)
    }

    private fun FirMetaCheckerContext.extends(type: KType, coneType: ConeKotlinType): Boolean {
      val rawType = coneType.renderReadableWithFqNames().replace("/", ".").substringBefore("<")
      val selectedType = type.toString().substringBefore("<")
      return rawType == selectedType || coneType.toRegularClassSymbol(session)?.resolvedSuperTypes?.any {
        extends(type, it)
      } == true
    }

    private fun FirMetaCheckerContext.isMutableKProperty(coneType: ConeKotlinType) =
      coneType.isKMutableProperty(session)
  }
}
