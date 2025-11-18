package arrow.meta.module.impl.arrow.meta.module.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.samples.Product
import org.jetbrains.kotlin.fir.declarations.FirClass

@Macro(target = Product::class)
fun MacroContext.product(clazz: FirClass): MacroCompilation {
  return clazz.transform {
    function {
      //language=kotlin
      val code = """
        fun product(): List<Pair<String, *>> {               
            return listOf(${propertiesOf(session, firClass) { """"${+it.name}" to this.${+it.name}""" }})
        }
      """.trimIndent()
      Kotlin(scope = clazz) { code }
    }
  }
}
