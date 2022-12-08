package foo.bar

import arrow.meta.samples.Increment

fun main() {
  val x = @Increment 10.0
  println(x)
}

