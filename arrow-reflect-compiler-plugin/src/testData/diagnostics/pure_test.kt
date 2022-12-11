package foo.bar

import arrow.meta.samples.Pure

<!CallGraphIncludesIO!>@Pure
fun foo() {
  <!CallGraphIncludesIO!>bar()<!>
}<!>

fun bar() {
  <!CallGraphIncludesIO, CallGraphIncludesIO, CallGraphIncludesIO!>println("maybe boom!")<!>
  bar()
}
