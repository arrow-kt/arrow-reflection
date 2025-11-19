package arrow.meta.module.impl

import arrow.meta.samples.Decorator

object MetaModuleImpl : MetaModule {
  override val decorator: Decorator.Companion get() = Decorator
}
