package arrow.meta.module

import arrow.meta.FirMetaCheckerContext
import arrow.meta.FirMetaContext
import arrow.meta.Meta
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.FirConstExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.types.ConstantValueKind

interface Module

inline fun <reified A> classId(): ClassId {
  val klass = A::class
  val packageName = klass.java.`package`.name
  val shortName = klass.java.simpleName
  val isLocal = klass.java.isLocalClass
  return ClassId(FqName(packageName), FqName(shortName), isLocal)
}

inline fun <reified Annotation> FirAnnotationContainer.annotatedWith(): Boolean =
  annotations.hasAnnotation(classId<Annotation>())

fun interface Transform<in In : FirElement, Out : FirElement> {
  fun FirMetaContext.transform(element: In): Out
}

@Target(AnnotationTarget.EXPRESSION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Increment

annotation class AnnotatedWith<T>

typealias Compiler = FirMetaContext

//transform macro
@Meta
fun Compiler.increment(@AnnotatedWith<Increment> constant: FirConstExpression<*>): FirStatement {
  check(constant.kind == ConstantValueKind.Int) {
    "@Increment only works in constant of type `Int`. found: ${constant.kind.asString}"
  }
  return compile("${constant} + 1")
}

object IncrementMacroImpl : Module, Meta.FrontendTransformer.ConstExpression {
  override fun FirMetaCheckerContext.constExpression(constExpression: FirConstExpression<*>): FirStatement =
    increment(constExpression)
}

annotation class Test

@Test
data class Sample(val name: String)

//generation macro
@Meta
fun Compiler.generationMacro(@Test firClassSymbol: FirClassSymbol<*>): List<FirSimpleFunction> =
  listOf(
    compile("""
      fun test(): Unit {}
    """.trimIndent())
  )

fun main() {
  //val x = Sample("f").test()
  println(@Increment 0)
}




