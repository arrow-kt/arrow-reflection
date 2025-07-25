package arrow.meta.module.impl

import arrow.meta.module.Module
import arrow.meta.samples.*

object MetaModuleImpl : MetaModule {
  override val increment: Increment.Companion get() = Increment
  override val product: Product.Companion get() = Product
  override val decorator: Decorator.Companion get() = Decorator
  override val immutable: Immutable.Companion get() = Immutable
}
