// WITH_STDLIB
// MODULE: main
package foo.bar

import arrow.meta.samples.Decorator

@Decorator
fun foo(value: Int): Int =
  value + 41

fun box(): String {
  val x = foo(0)
  return if (x == 42) {
    "OK"
  } else {
    "Fail"
  }
}
