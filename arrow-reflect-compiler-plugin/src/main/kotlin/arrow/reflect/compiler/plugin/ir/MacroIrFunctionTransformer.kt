package arrow.reflect.compiler.plugin.ir

import arrow.meta.module.impl.arrow.meta.macro.compilation.transformclassfactory.TransformClassState
import arrow.reflect.compiler.plugin.fir.codegen.FirMacroCodegenExtension
import arrow.reflect.compiler.plugin.ir.generation.ArrowReflectFir2IrVisitor
import arrow.reflect.compiler.plugin.ir.generation.toIr
import arrow.reflect.compiler.plugin.targets.macro.MacroInvoke
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

class MacroIrFunctionTransformer(
  private val macro: MacroInvoke
) : IrVisitorVoid() {

  override fun visitElement(element: IrElement) {
    when (element) {
      is IrDeclaration,
      is IrFile,
      is IrModuleFragment -> element.acceptChildrenVoid(this)
      else -> {}
    }
  }

  @OptIn(DirectDeclarationsAccess::class)
  override fun visitSimpleFunction(declaration: IrSimpleFunction) {
    val origin = declaration.origin as? IrDeclarationOrigin.GeneratedByPlugin ?: return
    val key = origin.pluginKey as? FirMacroCodegenExtension.MacroGeneratedFunctionKey ?: return
    val transformation = macro.classTransformation()[key] as? TransformClassState.Function ?: return
    macro.irActualizedResult()?.run {
      transformation.resolveFunctionBody(original = declaration)
    }
  }

  context(_: ArrowReflectFir2IrVisitor)
  private fun TransformClassState.Function.resolveFunctionBody(original: IrSimpleFunction) {
    val function = firSimpleFunction.toIr(
      original = original,
      firParent = context as FirRegularClass
    )
    original.body = function.body
  }
}
