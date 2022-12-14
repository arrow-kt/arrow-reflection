package arrow.meta.samples

//import arrow.meta.Diagnostics
//import arrow.meta.FirMetaCheckerContext
//import arrow.meta.Meta
//import arrow.meta.samples.OpticsErrors.DisallowedApi
//import arrow.meta.samples.OpticsErrors.EmptyPath
//import arrow.meta.samples.OpticsErrors.PathExpressionIncludesForeignReferences
//import org.jetbrains.kotlin.fir.FirElement
//import org.jetbrains.kotlin.fir.expressions.*
//import org.jetbrains.kotlin.fir.types.*
//import org.jetbrains.kotlin.fir.visitors.FirVisitor
//import org.jetbrains.kotlin.text
//import kotlin.contracts.ExperimentalContracts
//import kotlin.contracts.InvocationKind
//import kotlin.contracts.contract
//
//interface Path<Root : Any, Out> {
//  fun get(instance: Root): Out
//  fun set(instance: Root, value: Out): Root
//}
//
//interface NullablePath<Root : Any, Out> {
//  fun get(instance: Root): Out?
//  fun set(instance: Root, value: Out): Root
//}
//
//interface IterablePath<Root : Any, Out> {
//  fun get(instance: Root): List<Out>
//  fun set(instance: Root, value: Out): Root
//}
//
//object OpticsErrors : Diagnostics.Error {
//  val EmptyPath by error1()
//  val PathExpressionIncludesForeignReferences by error1()
//  val DisallowedApi by error1()
//}
//
//@Target(AnnotationTarget.FUNCTION)
//@Retention(AnnotationRetention.RUNTIME)
//@Meta
//annotation class Optics {
//  companion object : Meta.FrontendTransformer.Expression,
//    Diagnostics(PathExpressionIncludesForeignReferences, DisallowedApi, EmptyPath) {
//
//    override fun FirMetaCheckerContext.expression(expression: FirExpression): FirStatement {
//      println(expression.tree())
//      return expression
//    }
//  }
//}
//
//fun FirElement.tree(): String {
//  val printer = ExpressionPrinter()
//  accept(printer, 0)
//  return printer.tree()
//}
//
//class ExpressionPrinter(val builder: StringBuilder = StringBuilder()) : FirVisitor<Unit, Int>() {
//  override fun visitElement(element: FirElement, data: Int) {
//    val parent = data
//    builder.append((0..data).joinToString("") { "\t" } + element::class.java.simpleName + " { ${element.source.text?.replace("\n".toRegex(), " ") ?: "" } }")
//    builder.append("\n")
//    element.acceptChildren(this, data + 1)
//  } //├
//  fun tree(): String = builder.toString()
//}
//
//
//data class Street(val number: Int, val name: String)
//
//data class Address(val city: String, val street: Street)
//
//data class Company(val name: String, val address: Address, val employees: List<Employee>)
//
//data class Employee(val name: String, val company: Company?)
//
//@Optics
//inline fun <A> A.copy(f: A.() -> Unit): A = TODO()
//
//inline fun <A, B> A.copyReplaced(f: A.() -> A): A = f(this)
//
//inline val <A> Iterable<A>.all: A get() = TODO()
//
//inline fun <A> Iterable<A>.filter(f: (A) -> Boolean): A = TODO()
//
//fun box() {
//  val employee =
//    Employee("John Doe", Company("Arrow", Address("Functional city", Street(23, "lambda street")), emptyList()))
//
//  val path: Employee = employee.copy {
//    company?.employees?.all?.company
//    company?.employees?.filter { it.name == "" }?.company
//  }
//  return
//}
//
//@DslMarker
//annotation class EffectDsl
//
//@EffectDsl
//class Raise<E> {
//
//  @RestrictsSuspension
//  inner class raise(value: E) {
//    @EffectDsl
//    suspend fun boom(): Nothing = TODO()
//  }
//
//}
//
//@RestrictsSuspension
//@EffectDsl
//class EagerRaise<E> {
//  @EffectDsl fun raise(value: E): Nothing = TODO()
//}
//
//typealias Effect<E, A> = suspend Raise<E>.() -> A
//typealias EagerEffect<E, A> = suspend EagerRaise<E>.() -> A
//
//inline fun <E, A> Effect<E, A>.raise(value: E): Nothing = TODO()
//
//fun <E, A> effect(f: Effect<E, A>): Effect<E, A> = f
//fun <E, A> eagerEffect(f: EagerEffect<E, A>): EagerEffect<E, A> = f
//
//suspend fun foo()  {}
//
//fun main() {
//  effect {
//    raise("").boom() //compiles
//    foo()
//    val captured = suspend {
//      raise("").boom() // does not compile
//    }
//  }
//  eagerEffect {
//    raise("") //compiles
//    val captured = {
//      raise("") // does not compile
//    }
//  }
//}
