package foo.bar

import arrow.meta.samples.DisallowLambdaCapture

interface Raise<in E> {
  @DisallowLambdaCapture("It's unsafe to capture `raise` inside non-inline anonymous functions")
  fun raise(e: E): Nothing
}

context(Raise<String>)
fun shouldNotCature(): () -> Unit {
  return { raise("boom") }
}

context(Raise<String>)
fun inlineCaptureOk(): Unit {
  listOf(1, 2, 3).map { raise("boom") }
}

context(Raise<String>)
fun ok(): Unit {
  raise("boom")
}
