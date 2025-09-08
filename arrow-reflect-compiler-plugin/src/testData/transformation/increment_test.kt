// WITH_STDLIB
// MODULE: main
package test

import arrow.meta.samples.Product

@Product
data class Sample(val foo: Int, val bar: String)

fun box(): String {
  val x = Sample(foo = 0, bar = "abc").product()
  return if (x == listOf("foo" to 0, "bar" to "abc")) {
    "OK"
  } else {
    "Fail"
  }
}
