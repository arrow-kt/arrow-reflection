// WITH_STDLIB
// MODULE: main
package foo.bar

import arrow.meta.quotes.samples.DoubleExpression

fun box(): String {
  // Test the DoubleExpression annotation which uses quasiquotes
  val x = @DoubleExpression 21
  
  // The annotation should double 21 to 42
  return if (x == 42) "OK" else "FAIL: Expected 42 but got $x"
}