package arrow.meta.samples

import arrow.meta.Meta
import arrow.meta.module.impl.arrow.meta.FirMetaContext
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.expressions.FirCall
import org.jetbrains.kotlin.ir.util.*

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Meta
annotation class Decorator {

  companion object : Meta.Checker.Declaration<FirDeclaration> {

    override fun FirMetaContext.check(
      declaration: FirDeclaration, context: CheckerContext, reporter: DiagnosticReporter
    ) {
        declaration.transform<FirCall, Unit>(
          CallDecoratorTransformer(this, context), Unit
        )
    }

    fun <In, Out> decorate(args: List<In>, func: (List<In>) -> Out): Out {
      println("Arguments: $args")
      val newArgs = args.map {
        when (it) {
          is Int -> (it + 1) as In
          else -> it
        }
      }
      val result = func(newArgs)
      println("Return value: $result")
      return result
    }

  }

}

