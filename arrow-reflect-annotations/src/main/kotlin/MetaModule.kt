package arrow.meta.module.impl

import arrow.meta.module.Module
import arrow.meta.samples.Increment
import arrow.meta.samples.Log
import arrow.meta.samples.Product

interface MetaModule: Module {
  val increment: Increment
  val product: Product
  val log: Log
}
