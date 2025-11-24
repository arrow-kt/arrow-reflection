package arrow.meta.module.impl.arrow.meta.module.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.properties
import arrow.meta.module.impl.arrow.meta.macro.compilation.transform
import arrow.meta.module.impl.arrow.meta.macro.compilation.transformclassfactory.function
import arrow.meta.module.impl.arrow.meta.macro.compilation.unaryPlus
import arrow.meta.samples.Product
import org.jetbrains.kotlin.fir.declarations.FirClass

@Macro(target = Product::class)
context(_: MacroContext)
fun product(firClass: FirClass): MacroCompilation {
  return firClass.transform {
    //language=kotlin
    """
      fun product(): List<Pair<String, *>> {               
        return listOf(${firClass.properties { """"${+it.name}" to this.${+it.name}""" }})
      }
    """.function().create()
  }
}
