package arrow.meta

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

data class ScopedSource(
  val metaAnnotations: List<FqName>,
  val scopes: List<FirDeclaration>,
  val source: String?,
  val startOffset: Int?,
  val endOffset: Int?,
  val path: String?
)

class FrontendScopeCache {
  private val classes: MutableMap<ClassId, ScopedSource> = mutableMapOf()
  private val callables: MutableMap<CallableId, ScopedSource> = mutableMapOf()
  private val expressions: MutableSet<ScopedSource> = mutableSetOf()

  fun addDeclaration(context: CheckerContext, firDeclaration: FirDeclaration): Unit {
//    when (firDeclaration) {
//      is FirCallableDeclaration -> addCallable(context, firDeclaration)
//      is FirClassLikeDeclaration -> addClass(context, firDeclaration)
//      else -> addElement(context, firDeclaration)
//    }
  }

  private fun addClass(context: CheckerContext, firClassLikeDeclaration: FirClassLikeDeclaration): Unit {
//    classes[firClassLikeDeclaration.classId] = ScopedSource(
//      firClassLikeDeclaration.annotations.mapNotNull { it.fqName(context.session) },
//      context.containingDeclarations.toList(),
//      firClassLikeDeclaration.psi?.text,
//      firClassLikeDeclaration.psi?.startOffset,
//      firClassLikeDeclaration.psi?.endOffset,
//      firClassLikeDeclaration.psi?.containingFile?.name
//    )
  }

  private fun addCallable(context: CheckerContext, firCallable: FirCallableDeclaration): Unit {
//    callables[firCallable.symbol.callableId] = ScopedSource(
//      firCallable.annotations.mapNotNull { it.fqName(context.session) },
//      context.containingDeclarations.toList(),
//      firCallable.psi?.text,
//      firCallable.psi?.startOffset,
//      firCallable.psi?.endOffset,
//      firCallable.psi?.containingFile?.name
//    )
  }

  fun getClassScope(classId: ClassId): ScopedSource? = classes[classId]

  fun getCallableScope(callableId: CallableId): ScopedSource? = callables[callableId]

  fun getScope(irFile: IrFile?, ir: IrElement): ScopedSource? =
    when (ir) {
      is IrClass -> classes[ir.classId]
      is IrSimpleFunction -> {
        val classId = (ir.parent as? IrClass)?.classId
        if (classId != null) {
          val callableId = CallableId(classId, ir.name)
          callables[callableId]
        } else {
          val callableId = CallableId(ir.getPackageFragment().fqName, ir.name)
          callables[callableId]
        }
      }

      else -> {
        (classes.values + callables.values + expressions).firstOrNull { source ->
          "/" + source.path == irFile?.path &&
            source.startOffset == ir.startOffset &&
            source.endOffset == ir.endOffset
        }
      }
    }

  fun addElement(context: CheckerContext, firElement: FirElement): Unit {
//    expressions.add(
//      ScopedSource(
//        (firElement as? FirAnnotationContainer)?.annotations.orEmpty().mapNotNull { it.fqName(context.session) },
//        context.containingDeclarations.toList(),
//        firElement.source.text?.toString(),
//        firElement.source?.startOffset,
//        firElement.source?.endOffset,
//        firElement.psi?.containingFile?.name
//      )
//    )
  }
}
