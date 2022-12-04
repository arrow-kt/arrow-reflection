package arrow.meta.samples

import arrow.meta.Meta

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Meta
annotation class Decorator {

  companion object : Meta.CallInterceptor {

    fun <In, Out> decorate(args: List<In>, func: (List<In>) -> Out): Out {
      println("Arguments: $args")
      val newArgs = args.map {
        when (it) {
          is Int -> (it + 1) as In
          else -> it
        }
      }
      val result = func(newArgs)
      println("Return value: $result")
      return result
    }

  }

}

