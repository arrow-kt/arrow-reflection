// WITH_STDLIB
// MODULE: main
package foo.bar

import arrow.meta.quotes.samples.IncrementWithQuotes

fun box(): String {
  // Test the IncrementWithQuotes annotation which uses quasiquotes
  val x = @IncrementWithQuotes 41
  
  // The annotation should increment 41 to 42
  return if (x == 42) "OK" else "FAIL: Expected 42 but got $x"
}