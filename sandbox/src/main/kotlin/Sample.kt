package foo.bar

import arrow.meta.samples.DisallowLambdaCapture
import kotlin.contracts.*

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

@OptIn(ExperimentalContracts::class)
fun exactlyOne(f: () -> Unit): Unit {
  contract {
    callsInPlace(f, InvocationKind.EXACTLY_ONCE)
  }
}

@OptIn(ExperimentalContracts::class)
fun exactlyOnce(f: () -> Unit): Unit {
  contract {
    callsInPlace(f, InvocationKind.EXACTLY_ONCE)
  }
}

context(Raise<String>)
fun ok(): () -> Unit = {
  exactlyOnce { raise("boom") }
}
