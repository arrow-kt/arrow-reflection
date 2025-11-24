package arrow.meta.module.impl.arrow.meta.module.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.transform
import arrow.meta.module.impl.arrow.meta.macro.compilation.transformclassfactory.property
import arrow.meta.samples.Property
import org.jetbrains.kotlin.fir.declarations.FirClass

@Macro(target = Property::class)
context(_: MacroContext)
fun property(firClass: FirClass): MacroCompilation {
  return firClass.transform {
    "val x: Int = 2".property().create()
    "val y: Int = 3".property().create()
  }
}
