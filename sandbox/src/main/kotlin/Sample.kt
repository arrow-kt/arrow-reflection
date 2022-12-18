package example

import arrow.meta.samples.Log
import arrow.meta.samples.Product
import arrow.meta.samples.Pure

@Product
data class Sample(val name: String, val age: Int)


fun main() {
  val properties = Sample("j", 12).product()
  println(properties)
}
