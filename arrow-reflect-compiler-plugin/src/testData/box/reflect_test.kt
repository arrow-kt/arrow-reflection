package foo.bar

import arrow.meta.samples.Reflect
import arrow.meta.samples.CompileTimeReflected
import kotlin.reflect.KClass

annotation class Test

@Reflect
data class Sample(val name: String, val age: Int)

fun box(): String {
  val x = @Reflect Sample::class
  return if (x is CompileTimeReflected<*>) {
    "OK"
  } else {
    "Fail"
  }
}
