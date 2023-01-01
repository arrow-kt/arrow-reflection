package foo.bar

import arrow.meta.samples.Increment

fun box(): String {
  val x = @Increment <!IncrementNotInConstantInt!>0.0<!>
  return if (<!EQUALITY_NOT_APPLICABLE_WARNING!>x == 1<!>) {
    "OK"
  } else {
    "Fail"
  }
}
