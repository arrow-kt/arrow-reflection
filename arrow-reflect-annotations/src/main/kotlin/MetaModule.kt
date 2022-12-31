package arrow.meta.module.impl

import arrow.meta.macros.MyMacro
import arrow.meta.module.Compiler
import arrow.meta.module.Module
import arrow.meta.module.increment
import arrow.meta.samples.*

interface MetaModule: Module {
  val reflect: Reflect
  val increment: Increment
  val product: Product
  val log: Log
  val decorator: Decorator
  val pure: Pure
  val myMacro : MyMacro
  //val optics: Optics
}

interface MetaModule2: Module {
  val increment get() = Compiler::increment
}
