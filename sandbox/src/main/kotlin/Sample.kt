package foo.bar

import arrow.meta.samples.Product

@Product
data class Sample(val x: Int)


fun main() {
  val x = Sample(1).product()
  println(x)
}

