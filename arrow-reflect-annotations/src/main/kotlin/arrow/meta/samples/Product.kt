package arrow.meta.samples

import arrow.meta.Meta
import arrow.meta.module.impl.arrow.meta.FirMetaContext
import arrow.meta.module.impl.arrow.meta.IrMetaContext
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Meta
annotation class Product {
  companion object :
    Meta.FrontendTransformer.Class,
    Meta.Transform.Class {
    override fun FirMetaContext.regularClass(firClass: FirRegularClass): FirStatement {
      return firClass.add("""
        fun product(): List<Pair<String, *>> = $synthetic
      """.function)
    }

    override fun IrMetaContext.transform(declaration: IrClass): IrStatement {
      return declaration
    }
  }
}
