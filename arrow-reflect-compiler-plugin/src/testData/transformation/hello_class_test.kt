// WITH_STDLIB
// MODULE: main
package foo.bar

import arrow.meta.samples.HelloClassTransform

@HelloClassTransform
class Hello

fun box(): String {
  val x = Hello().hello()
  return if (x == "Hello!") {
    "OK"
  } else {
    "Fail"
  }
}
