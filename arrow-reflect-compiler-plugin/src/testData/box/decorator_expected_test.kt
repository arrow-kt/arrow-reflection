package foo.bar

import arrow.meta.samples.Decorator

fun foo(value: Int): Int =
  value + 41

fun box(): String {
  val x = Decorator.intercept(listOf(0)) { args: List<Any?> ->
    foo(args[0] as Int)
  }
  return if (x == 42) {
    "OK"
  } else {
    "Fail"
  }
}
