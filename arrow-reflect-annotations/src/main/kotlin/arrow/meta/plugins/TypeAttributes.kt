package arrow.meta.plugins

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.types.ConeAttribute

interface TypeAttributes : FrontendPlugin {
  fun convertAttributeToAnnotation(attribute: ConeAttribute<*>): FirAnnotation?
  fun extractAttributeFromAnnotation(annotation: FirAnnotation): ConeAttribute<*>?
  class Builder(override val session: FirSession) : TypeAttributes, FrontendPlugin.Builder() {
    var attributeToAnnotation: (ConeAttribute<*>) -> FirAnnotation? = { null }
    var attributeFromAnnotation: (FirAnnotation) -> ConeAttribute<*>? = { null }
    override fun convertAttributeToAnnotation(attribute: ConeAttribute<*>): FirAnnotation? =
      attributeToAnnotation(attribute)

    override fun extractAttributeFromAnnotation(annotation: FirAnnotation): ConeAttribute<*>? =
      attributeFromAnnotation(annotation)
  }

  companion object {
    operator fun invoke(init: Builder.() -> Unit): (FirSession) -> TypeAttributes =
      { Builder(it).apply(init) }
  }
}
