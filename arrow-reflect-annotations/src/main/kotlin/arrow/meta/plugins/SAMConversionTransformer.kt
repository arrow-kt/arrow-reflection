package arrow.meta.plugins

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.types.ConeLookupTagBasedType

interface SAMConversionTransformer : FrontendPlugin {
  fun getCustomFunctionalTypeForSamConversion(function: FirSimpleFunction): ConeLookupTagBasedType?
  class Builder(override val session: FirSession) : SAMConversionTransformer, FrontendPlugin.Builder() {

    var getCustomFunctionalTypeForSamConversion: (FirSimpleFunction) -> ConeLookupTagBasedType? = { null }

    override fun getCustomFunctionalTypeForSamConversion(function: FirSimpleFunction): ConeLookupTagBasedType? =
      this.getCustomFunctionalTypeForSamConversion(function)
  }

  companion object {
    operator fun invoke(init: Builder.() -> Unit): (FirSession) -> SAMConversionTransformer = { Builder(it).apply(init) }
  }
}
