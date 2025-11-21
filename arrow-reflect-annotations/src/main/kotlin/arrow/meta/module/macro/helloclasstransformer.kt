package arrow.meta.module.impl.arrow.meta.module.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.transform
import arrow.meta.module.impl.arrow.meta.macro.compilation.transformclassfactory.function
import arrow.meta.samples.HelloClassTransform
import org.jetbrains.kotlin.fir.declarations.FirClass

@Macro(target = HelloClassTransform::class)
context(_: MacroContext)
fun hello(firClass: FirClass): MacroCompilation {
  return firClass.transform {
    //language=kotlin
    """
      fun hello(): String = "Hello"
    """.function().create()
  }
}

@Macro(target = HelloClassTransform::class)
context(_: MacroContext)
fun world(firClass: FirClass): MacroCompilation {
  return firClass.transform {
    //language=kotlin
    """
      fun world(): String = "World!"
    """.function().create()
  }
}
