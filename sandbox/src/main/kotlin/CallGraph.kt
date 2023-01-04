package sample

import arrow.meta.samples.Pure
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

@Pure
fun foo() : Int {
  bar()
  return 0
}

fun bar() {
  println("io")
}
