package arrow.meta.module.impl.arrow.meta.module.macro

import arrow.meta.module.impl.arrow.meta.macro.Macro
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.samples.Errors.error1
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.utils.nameOrSpecialName

private val MACRO_SAMPLE by error1()

@Macro
fun MacroContext.macroSample(firClass: FirClass): MacroCompilation {
  return diagnostics {
    if (firClass.nameOrSpecialName.identifier == "MacroSample") {
      firClass.report(MACRO_SAMPLE, "macro sample")
    }
  }
}
