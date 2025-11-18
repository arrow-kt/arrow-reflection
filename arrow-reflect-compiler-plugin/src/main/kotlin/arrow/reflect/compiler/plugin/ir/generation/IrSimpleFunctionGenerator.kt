package arrow.reflect.compiler.plugin.ir.generation

import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.properties

@OptIn(DirectDeclarationsAccess::class, UnsafeDuringIrConstructionAPI::class)
fun ArrowReflectFir2IrVisitor.createIrFrom(
  function: FirSimpleFunction,
  symbol: IrSimpleFunctionSymbol,
  parent: IrClass,
  firParent: FirRegularClass
): IrSimpleFunction {
  val firProperties = firParent.declarations.filterIsInstance<FirProperty>()
  storage.classCache[firParent] = parent.symbol
  storage.functionCache[function] = symbol
  parent.properties.forEach { irProperty ->
    firProperties.firstOrNull {
      it.name.identifier == irProperty.name.identifier
    }?.let { property ->
      storage.propertyCache[property] = irProperty.symbol
    }
    storage.getterForPropertyCache[irProperty.symbol] = irProperty.getter!!.symbol
  }
  return withParent(parent) {
    visitor.visitSimpleFunction(simpleFunction = function, null) as IrSimpleFunction
  }
}
