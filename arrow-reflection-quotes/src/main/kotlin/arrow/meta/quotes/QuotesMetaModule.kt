package arrow.meta.quotes

import arrow.meta.module.Module
import arrow.meta.quotes.samples.QuasiquoteConditional
import arrow.meta.quotes.samples.IncrementWithQuotes
import arrow.meta.quotes.samples.DoubleExpression
import arrow.meta.quotes.samples.WithHygiene
import arrow.meta.quotes.samples.WithPatternOptimization
import arrow.meta.quotes.samples.WithValidation
import arrow.meta.quotes.samples.WithTransformOptimization

interface QuotesMetaModule : Module {
  val quasiquoteConditional: QuasiquoteConditional.Companion
  val incrementWithQuotes: IncrementWithQuotes.Companion
  val doubleExpression: DoubleExpression.Companion
  val withHygiene: WithHygiene.Companion
  val withPatternOptimization: WithPatternOptimization.Companion
  val withValidation: WithValidation.Companion
  val withTransformOptimization: WithTransformOptimization.Companion
}