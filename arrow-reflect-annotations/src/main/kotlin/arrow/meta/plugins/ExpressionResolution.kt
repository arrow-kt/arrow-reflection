package arrow.meta.plugins

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.types.ConeKotlinType

interface ExpressionResolution : FrontendPlugin {
  fun addNewImplicitReceivers(functionCall: FirFunctionCall): List<ConeKotlinType>

  class Builder(override val session: FirSession) : ExpressionResolution, FrontendPlugin.Builder() {

    var newImplicitReceivers: (FirFunctionCall) -> List<ConeKotlinType> = { emptyList() }

    override fun addNewImplicitReceivers(functionCall: FirFunctionCall): List<ConeKotlinType> =
      newImplicitReceivers(functionCall)
  }

  companion object {
    operator fun invoke(init: Builder.() -> Unit): (FirSession) -> ExpressionResolution = { Builder(it).apply(init) }
  }
}
