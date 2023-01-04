@file:Immutable

package foo.bar

import arrow.meta.samples.Immutable

class Sample(<!FoundMutableVar!>var foo: Int<!>)

fun foo() {
  <!FoundMutableVar!>var x = 0<!>
  val l = <!FoundMutableIterable!>mutableListOf<Int>()<!>
}
