package foo.bar

import arrow.meta.samples.Pure

@Pure
fun foo() {
  bar()
}

fun bar() {
  println("maybe boom!")
  bar()
}
