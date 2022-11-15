package foo.bar

data class Sample(val foo: Int)

fun box(): String {
  return if (true) {
    "OK"
  } else {
    "Fail"
  }
}
