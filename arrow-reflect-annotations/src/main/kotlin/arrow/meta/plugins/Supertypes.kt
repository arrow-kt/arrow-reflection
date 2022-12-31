package arrow.meta.plugins

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.extensions.FirSupertypeGenerationExtension
import org.jetbrains.kotlin.fir.types.FirResolvedTypeRef

interface Supertypes : FrontendPlugin {
  context(FirSupertypeGenerationExtension.TypeResolveServiceContainer) fun computeAdditionalSupertypes(
      classLikeDeclaration: FirClassLikeDeclaration, resolvedSupertypes: List<FirResolvedTypeRef>
  ): List<FirResolvedTypeRef>

  fun needTransformSupertypes(declaration: FirClassLikeDeclaration): Boolean

  class Builder(override val session: FirSession) : Supertypes, FrontendPlugin.Builder() {
    var additionalSupertypes: (FirClassLikeDeclaration, List<FirResolvedTypeRef>) -> List<FirResolvedTypeRef> =
      { _, _ -> emptyList() }
    var shouldTransformSupertypes: (FirClassLikeDeclaration) -> Boolean = { false }
    override fun computeAdditionalSupertypes(
        classLikeDeclaration: FirClassLikeDeclaration,
        resolvedSupertypes: List<FirResolvedTypeRef>
    ): List<FirResolvedTypeRef> =
      additionalSupertypes(classLikeDeclaration, resolvedSupertypes)

    override fun needTransformSupertypes(declaration: FirClassLikeDeclaration): Boolean =
      shouldTransformSupertypes(declaration)
  }

  companion object {
    operator fun invoke(init: Builder.() -> Unit): (FirSession) -> Supertypes = { Builder(it).apply(init) }
  }
}
