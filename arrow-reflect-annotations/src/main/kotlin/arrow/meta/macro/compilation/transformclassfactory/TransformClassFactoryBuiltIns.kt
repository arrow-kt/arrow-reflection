package arrow.meta.module.impl.arrow.meta.macro.compilation.transformclassfactory

import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.function
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction

context(_: MacroContext, factory: TransformClassFactory)
fun String.function(): FirSimpleFunction? = function(session = factory.context.session, scope = listOf(factory.firClass))
