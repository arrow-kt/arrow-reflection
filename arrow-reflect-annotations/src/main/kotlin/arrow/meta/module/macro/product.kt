package arrow.meta.module.impl.arrow.meta.module.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.samples.Product
import org.jetbrains.kotlin.fir.declarations.FirClass

@Macro(target = Product::class)
fun MacroContext.product(firClass: FirClass): MacroCompilation {
  return firClass.transform {
    function {
      //language=kotlin
      """
        fun product(): List<Pair<String, *>> {               
            return listOf(${propertiesOf(session, firClass) { """"${+it.name}" to this.${+it.name}""" }})
        }
      """
    }
  }
}
