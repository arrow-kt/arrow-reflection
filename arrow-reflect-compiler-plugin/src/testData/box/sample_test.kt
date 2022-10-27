package foo.bar

data class Sample(val foo: Int)

fun box(): String {
  val sample = Sample(42)
  return if (sample.foo == 42) {
    "OK"
  } else {
    "Fail"
  }
}
