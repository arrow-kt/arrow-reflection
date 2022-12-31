package arrow.meta.plugins

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus

interface StatusTransformer : FrontendPlugin {
  abstract fun needTransformStatus(declaration: FirDeclaration): Boolean

  fun transformStatus(status: FirDeclarationStatus, declaration: FirDeclaration): FirDeclarationStatus = status

  class Builder(override val session: FirSession) : StatusTransformer, FrontendPlugin.Builder() {
    var shouldTransformStatus: (FirDeclaration) -> Boolean = { false }
    var transformStatus: (FirDeclarationStatus) -> FirDeclarationStatus = { it }

    override fun needTransformStatus(declaration: FirDeclaration): Boolean {
      return shouldTransformStatus(declaration)
    }
  }

  companion object {
    operator fun invoke(init: Builder.() -> Unit): (FirSession) -> StatusTransformer = { Builder(it).apply(init) }
  }
}
