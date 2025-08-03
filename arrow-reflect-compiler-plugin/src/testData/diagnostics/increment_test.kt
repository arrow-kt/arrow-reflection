package foo.bar

import arrow.meta.samples.Increment

fun foo() {
  val x = @Increment <!IncrementNotInConstantInt!>"0"<!>
}
