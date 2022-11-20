package foo.bar

import arrow.meta.samples.Increment

fun box(): String {
  val x = @Increment 0
  return if (x == 1) {
    "OK"
  } else {
    "Fail"
  }
}
