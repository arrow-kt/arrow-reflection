package arrow.reflect.compiler.plugin.fir.transformers

import arrow.meta.TemplateCompiler
import arrow.reflect.compiler.plugin.targets.MetaTarget
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.extensions.FirStatusTransformerExtension

class FirMetaStatusTransformer(
  session: FirSession,
  val templateCompiler: TemplateCompiler,
  val metaTargets: List<MetaTarget>
) : FirStatusTransformerExtension(session) {
  override fun needTransformStatus(declaration: FirDeclaration): Boolean {
    return false
  }
}
