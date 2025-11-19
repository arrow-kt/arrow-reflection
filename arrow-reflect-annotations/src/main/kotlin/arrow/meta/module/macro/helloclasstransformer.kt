package arrow.meta.module.impl.arrow.meta.module.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.samples.HelloClassTransform
import org.jetbrains.kotlin.fir.declarations.FirClass

@Macro(target = HelloClassTransform::class)
fun MacroContext.hello(firClass: FirClass): MacroCompilation {
  return firClass.transform {
    function {
      //language=kotlin
      """
        fun hello(): String = "Hello"
      """
    }
  }
}

@Macro(target = HelloClassTransform::class)
fun MacroContext.world(firClass: FirClass): MacroCompilation {
  return firClass.transform {
    function {
      //language=kotlin
      """
        fun world(): String = "World!"
      """
    }
  }
}
