package arrow.meta.quotes.module.impl

import arrow.meta.quotes.QuotesMetaModule
import arrow.meta.quotes.samples.*

object QuotesModuleImpl : QuotesMetaModule {
  override val quasiquoteConditional: QuasiquoteConditional.Companion 
    get() = QuasiquoteConditional
  
  override val incrementWithQuotes: IncrementWithQuotes.Companion 
    get() = IncrementWithQuotes
  
  override val doubleExpression: DoubleExpression.Companion 
    get() = DoubleExpression
    
  override val withHygiene: WithHygiene.Companion
    get() = WithHygiene
    
  override val withPatternOptimization: WithPatternOptimization.Companion
    get() = WithPatternOptimization
    
  override val withValidation: WithValidation.Companion
    get() = WithValidation
    
  override val withTransformOptimization: WithTransformOptimization.Companion
    get() = WithTransformOptimization
}