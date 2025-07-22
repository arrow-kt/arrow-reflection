// WITH_STDLIB
// MODULE: main
package test

import arrow.meta.samples.Product
import arrow.meta.FromTemplate

data class Sample(val foo: Int, val bar: String) {
  fun product(): List<Pair<String, *>> =
    listOf("foo" to this.foo, "bar" to this.bar)
}

fun box(): String {
  val x = Sample(foo = 0, bar = "abc").product()
  return if (x == listOf("foo" to 0, "bar" to "abc")) {
    "OK"
  } else {
    "Fail"
  }
}
