package arrow.meta.module.impl

import arrow.meta.module.Module
import arrow.meta.samples.*

interface MetaModule: Module {
  val increment: Increment
  val product: Product
  val log: Log
  val decorator: Decorator
  val pure: Pure
  val immutable: Immutable
}

