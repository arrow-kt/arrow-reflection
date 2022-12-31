package arrow.meta.plugins

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.types.FirTypeRef

interface Checkers : FrontendPlugin {
  fun interface Declaration<in D : FirDeclaration> {
    fun check(declaration: D, context: CheckerContext, reporter: DiagnosticReporter)
  }

  fun interface Expression<in D : FirStatement> {
    fun check(declaration: D, context: CheckerContext, reporter: DiagnosticReporter)
  }

  fun interface Type<in D : FirTypeRef> {
    fun check(declaration: D, context: CheckerContext, reporter: DiagnosticReporter)
  }

  val declarationCheckers: Set<Declaration<FirDeclaration>>
  val expressionCheckers: Set<Expression<FirStatement>>
  val typeCheckers: Set<Type<FirTypeRef>>

  class Builder(override val session: FirSession) : FrontendPlugin.Builder(), Checkers {
    override val declarationCheckers: MutableSet<Declaration<FirDeclaration>> = mutableSetOf()
    override val expressionCheckers: MutableSet<Expression<FirStatement>> = mutableSetOf()
    override val typeCheckers: MutableSet<Type<FirTypeRef>> = mutableSetOf()
  }

  companion object {
    operator fun invoke(init: Builder.() -> Unit): (FirSession) -> Checkers = { Builder(it).apply(init) }
  }
}
