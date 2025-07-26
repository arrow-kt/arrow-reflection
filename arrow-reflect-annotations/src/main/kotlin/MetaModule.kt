package arrow.meta.module.impl

import arrow.meta.module.Module
import arrow.meta.samples.Decorator
import arrow.meta.samples.Increment
import arrow.meta.samples.Product

interface MetaModule: Module {
  val increment: Increment.Companion
  val product: Product.Companion
  val decorator: Decorator.Companion
}

