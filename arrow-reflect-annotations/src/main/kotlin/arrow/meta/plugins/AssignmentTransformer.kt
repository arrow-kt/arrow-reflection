package arrow.meta.plugins

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.expressions.FirVariableAssignment


interface AssignmentTransformer : FrontendPlugin {
  fun transformVariableAssignment(variableAssignment: FirVariableAssignment): FirStatement?
  class Builder(override val session: FirSession) : FrontendPlugin.Builder(), AssignmentTransformer {
    var transformVariableAssignment: (FirVariableAssignment) -> FirStatement? = { it }

    override fun transformVariableAssignment(variableAssignment: FirVariableAssignment): FirStatement? =
      this.transformVariableAssignment(variableAssignment)
  }

  companion object {
    operator fun invoke(init: Builder.() -> Unit): (FirSession) -> AssignmentTransformer = { Builder(it).apply(init) }
  }
}
