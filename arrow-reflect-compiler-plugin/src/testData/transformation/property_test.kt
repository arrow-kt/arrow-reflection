// WITH_STDLIB
// MODULE: main
package foo.bar

import arrow.meta.samples.Property

@Property
class Sample

fun box(): String {
  val sample = Sample()
  return if (sample.x + sample.y == 5) {
    "OK"
  } else {
    "Fail"
  }
}
