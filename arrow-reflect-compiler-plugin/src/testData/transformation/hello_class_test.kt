// WITH_STDLIB
// MODULE: main
package foo.bar

import arrow.meta.samples.HelloClassTransform

@HelloClassTransform
class Hello

fun box(): String {
  val hello = Hello()
  val x = "${hello.hello()} ${hello.world()}"
  return if (x == "Hello World!") {
    "OK"
  } else {
    "Fail"
  }
}
