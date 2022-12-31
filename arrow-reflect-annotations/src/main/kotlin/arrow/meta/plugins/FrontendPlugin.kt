package arrow.meta.plugins

import arrow.meta.Meta
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.AnnotationFqn
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate

sealed interface FrontendPlugin : CompilerPlugin {

  val session : FirSession

  val metaAnnotatedPredicate: DeclarationPredicate
    get() = DeclarationPredicate.create { metaAnnotated(AnnotationFqn(Meta::class.java.canonicalName)) }

  fun FirDeclarationPredicateRegistrar.registerPredicates() {
    register(metaAnnotatedPredicate)
  }

  open class Builder {
    private var compilerPlugins: Sequence<(FirSession) -> FrontendPlugin> = sequenceOf()
    operator fun ((FirSession) -> FrontendPlugin).unaryPlus() {
      compilerPlugins += this
    }
    open val plugins: Sequence<(FirSession) -> FrontendPlugin> get() = compilerPlugins

    fun register(macroPredicate: DeclarationPredicate): FirDeclarationPredicateRegistrar.() -> Unit = {
      register(macroPredicate)
    }
  }
  companion object {
    operator fun invoke(init: Builder.() -> Unit): Builder = Builder().apply(init)
  }
}
