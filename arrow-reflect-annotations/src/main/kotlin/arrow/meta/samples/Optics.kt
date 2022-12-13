package arrow.meta.samples

import arrow.meta.Diagnostics
import arrow.meta.FirMetaCheckerContext
import arrow.meta.Meta
import arrow.meta.samples.OpticsErrors.DisallowedApi
import arrow.meta.samples.OpticsErrors.EmptyPath
import arrow.meta.samples.OpticsErrors.PathExpressionIncludesForeignReferences
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.psi
import kotlin.reflect.KClass

interface Path<Root : Any, Out> {
  fun get(instance: Root): Out
  fun set(instance: Root, value: Out): Root
}

interface NullablePath<Root : Any, Out> {
  fun get(instance: Root): Out?
  fun set(instance: Root, value: Out): Root
}

interface IterablePath<Root : Any, Out> {
  fun get(instance: Root): List<Out>
  fun set(instance: Root, value: Out): Root
}

object OpticsErrors : Diagnostics.Error {
  val EmptyPath by error1()
  val PathExpressionIncludesForeignReferences by error1()
  val DisallowedApi by error1()
}

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Optics {
  companion object : Meta.FrontendTransformer.ImplicitInvokeCall,
    Diagnostics(PathExpressionIncludesForeignReferences, DisallowedApi, EmptyPath) {

    override fun FirMetaCheckerContext.implicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall): FirStatement {
      val lambdaArgument = implicitInvokeCall.argumentList.arguments.firstOrNull() as? FirLambdaArgumentExpression
      val returnExpression =
        (lambdaArgument?.expression as? FirAnonymousFunctionExpression)?.anonymousFunction?.body?.statements?.firstOrNull() as? FirReturnExpression
      val path = returnExpression?.result
      val fromConeType = implicitInvokeCall.typeRef.coneType.typeArguments.firstOrNull()?.type
      val from = if (fromConeType != null) buildResolvedTypeRef { type = fromConeType } else null
      val to = path?.typeRef
      return if (from != null && to != null) {
        path(from, to, path)
      } else {
        implicitInvokeCall
      }
    }

    fun FirMetaCheckerContext.path(
      from: FirTypeRef,
      to: FirTypeRef,
      expression: FirExpression
    ): FirStatement {
      val notNullableTo = to.coneType.makeConeTypeDefinitelyNotNullOrNotNull(session.typeContext).renderReadable()
      val parts = listOf("instance") + expression.source.psi?.text?.split(".").orEmpty()
      expression.report(EmptyPath, "Empty path, expected at least one part in this path")
      return if (parts.isNotEmpty()) {
        compile<FirAnonymousObjectExpression>(
          """
            val x = object : arrow.meta.samples.NullablePath<${+from}, ${notNullableTo}> {
              override fun get(instance: ${+from}): ${+to} =
                instance.${+expression}

              override fun set(instance: ${+from}, value: ${notNullableTo}): ${+from} =
                ${generateCopy(parts)}
            }
            """
        )
      } else expression
    }

    fun generateCopy(parts: List<String>): String {
      // if there are no parts, just return the initial value
      return if (parts.isEmpty()) ""
      else if (parts.size == 1) parts.first()
      else {
        val result = parts.foldIndexed("") { n, acc, part ->
          val propertyName = part.removeSuffix("?")
          val previousWasNullable = n == 0 || parts[n - 1].lastOrNull() == '?'
          if (n == 0) parts[0] + ".copy("
          else if (part == parts.lastOrNull()) {
            "$acc$propertyName = value"
          } else {
            "$acc$propertyName = ${
              (0..n).joinToString(".") {
                if (previousWasNullable) parts[it].removeSuffix("?") // applies smartcast 
                else parts[it]
              }
            }.copy("
          }
        }
        // add closing parentheses for the nested copy calls
        result.padEnd(result.length + parts.size, ')')
      }
    }

  }
}


operator fun <A : Any, B> KClass<A>.invoke(f: A.() -> B): Path<A, B> = TODO("synth")


data class Street(val number: Int, val name: String)

data class Address(val city: String, val street: Street)

data class Company(val name: String, val address: Address, val employees: List<Employee>)

data class Employee(val name: String, val company: Company?)

fun <A, B> A.copy(f: A.() -> B): A = TODO()

fun main() {
  val path = @Optics Employee::class { company?.address?.street?.number }

  val employee =
    Employee("John Doe", Company("Arrow", Address("Functional city", Street(23, "lambda street")), emptyList()))
  employee


}
