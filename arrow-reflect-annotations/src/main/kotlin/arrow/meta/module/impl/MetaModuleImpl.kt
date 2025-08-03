package arrow.meta.module.impl

import arrow.meta.samples.Decorator
import arrow.meta.samples.Increment
import arrow.meta.samples.Product

object MetaModuleImpl : MetaModule {
  override val product: Product.Companion get() = Product
  override val decorator: Decorator.Companion get() = Decorator
}
