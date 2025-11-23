package arrow.reflect.compiler.plugin.ir.generation

import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI

context(visitor: ArrowReflectFir2IrVisitor)
fun FirProperty.toIr(
  original: IrProperty,
  firParent: FirRegularClass
): IrProperty {
  val parent = original.parent as? IrClass ?: return original
  return visitor.resolveIrProperty(
    firProperty = this,
    symbol = original.symbol,
    parent = parent,
    firParent = firParent
  )
}

@OptIn(DirectDeclarationsAccess::class, UnsafeDuringIrConstructionAPI::class)
private fun ArrowReflectFir2IrVisitor.resolveIrProperty(
  firProperty: FirProperty,
  symbol: IrPropertySymbol,
  parent: IrClass,
  firParent: FirRegularClass
): IrProperty {
  val firProperties = firParent.declarations.filterIsInstance<FirProperty>()
  val firFunctions = firParent.declarations.filterIsInstance<FirSimpleFunction>()
  storage.classCache[firParent] = parent.symbol
  storage.propertyCache[firProperty] = symbol
  parent.cacheFunctions(firFunctions = firFunctions)
  parent.cacheProperties(firProperties = firProperties)
  return withParent(parent) {
    visitor.visitProperty(firProperty, null) as IrProperty
  }
}

