package arrow.reflect.compiler.plugin.targets.macro

import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroCompilation
import arrow.meta.module.impl.arrow.meta.macro.compilation.MacroContext
import arrow.meta.module.impl.arrow.meta.macro.compilation.transformclassfactory.TransformClassState
import arrow.reflect.compiler.plugin.ir.generation.ArrowReflectFir2IrVisitor
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.fir.FirElement
import kotlin.reflect.full.isSubclassOf

data class MacroRegistry(
  val firMacroRegistry: FirMacroRegistry,
  val classTransformationRegistry: ClassTransformationRegistry,
  var irActualizedResult: ArrowReflectFir2IrVisitor?
)

@ConsistentCopyVisibility
data class ClassTransformationRegistry private constructor(
  private val transformation: MutableMap<GeneratedDeclarationKey, TransformClassState>
) {

  companion object {
    fun empty(): ClassTransformationRegistry = ClassTransformationRegistry(
      transformation = mutableMapOf()
    )
  }

  fun register(key: GeneratedDeclarationKey, classTransformation: TransformClassState) {
    transformation[key] = classTransformation
  }

  operator fun set(key: GeneratedDeclarationKey, classTransformation: TransformClassState) {
    transformation[key] = classTransformation
  }

  operator fun get(key: GeneratedDeclarationKey): TransformClassState? {
    return transformation[key]
  }
}

@ConsistentCopyVisibility
data class FirMacroRegistry private constructor(
  private val macros: List<MacroTarget>
) {

  companion object {
    fun buildRegistry(macros: List<MacroTarget>): FirMacroRegistry {
      return FirMacroRegistry(macros = macros.filterFirMacros())
    }

    private fun List<MacroTarget>.filterFirMacros(): List<MacroTarget> {
      return filter {
        it.params.size == 2 && it.params[1].isSubclassOf(FirElement::class)
      }
    }
  }

  internal operator fun <T : FirElement> invoke(
    context: MacroContext,
    element: T,
    annotations: Set<String>
  ): List<MacroCompilation> {
    return macros.filter { macro ->
      if (macro.targetClass != null) {
        macro.nodeParameterIsSupertypeOf(node = element) && macro.targetClass.java.canonicalName in annotations
      } else {
        macro.nodeParameterIsSupertypeOf(node = element)
      }
    }.mapNotNull {
      it.method.invoke(this, context, element) as? MacroCompilation
    }
  }

  private fun <T : FirElement> MacroTarget.nodeParameterIsSupertypeOf(node: T): Boolean = node::class.isSubclassOf(params[1])
}
