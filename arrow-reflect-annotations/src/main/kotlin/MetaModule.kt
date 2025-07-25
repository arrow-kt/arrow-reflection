package arrow.meta.module.impl

import arrow.meta.module.Module
import arrow.meta.samples.*

interface MetaModule: Module {
  val increment: Increment.Companion
  val product: Product.Companion
  val decorator: Decorator.Companion
  val pure: Pure.Companion
  val immutable: Immutable.Companion
}

