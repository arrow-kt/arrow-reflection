package arrow.meta.module.impl

import arrow.meta.module.Module
import arrow.meta.samples.Decorator

interface MetaModule: Module {
  val decorator: Decorator.Companion
}

