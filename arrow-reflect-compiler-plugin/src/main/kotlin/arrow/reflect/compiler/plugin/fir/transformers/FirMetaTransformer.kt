package arrow.reflect.compiler.plugin.fir.transformers

import arrow.meta.FirMetaContext
import arrow.meta.TemplateCompiler
import arrow.reflect.compiler.plugin.fir.checkers.metaAnnotations
import arrow.reflect.compiler.plugin.targets.MetaTarget
import arrow.reflect.compiler.plugin.targets.MetagenerationTarget
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.util.capitalizeDecapitalize.decapitalizeAsciiOnly
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

class FirMetaTransformer(
  private val session: FirSession, val templateCompiler: TemplateCompiler, val metaTargets: List<MetaTarget>,
  val checkerContext: CheckerContext,
  val reporter: DiagnosticReporter,
) : FirTransformer<Unit>() {

  val metaContext = FirMetaContext(templateCompiler, session)

  private fun <E : FirAnnotationContainer> invokeMeta(
    arg: E
  ): FirAnnotationContainer? {
    if (templateCompiler.compiling) return null
    val args = listOf(arg::class, CheckerContext::class, DiagnosticReporter::class)
    val metaAnnotations = arg.metaAnnotations(session)
    val metaClasses = arg.metaClasses()
    val dispatchers = metaClasses.mapNotNull {
      val methodName = it.java.simpleName.decapitalizeAsciiOnly()
      MetaTarget.find(
        metaAnnotations.mapNotNull { it.fqName(session)?.asString() }.toSet(),
        methodName,
        it,
        MetagenerationTarget.Fir,
        args,
        metaTargets
      )
    }

    val result: FirAnnotationContainer? =
      dispatchers.fold(null) { out: FirAnnotationContainer?, target: MetaTarget ->
        out ?: target.method.invoke(target.companion.objectInstance, metaContext, arg, checkerContext, reporter) as? FirAnnotationContainer
      }

    return result
  }

  override fun <E : FirElement> transformElement(element: E, data: Unit): E {
    element.transformChildren(this, data)
    return if (element is FirAnnotationContainer) {
      invokeMeta(element) as E? ?: element
    } else element
  }

}

private fun String.removeFirPrefix(): String  =
  replace("Fir", "")

private fun FirElement.metaClasses(): List<KClass<*>> {
  val firClass = this::class
  val superTypeClassName = firClass.simpleName?.removeFirPrefix()
  val superType = "arrow.meta.Meta\$FrontendTransformer\$$superTypeClassName"
  val parentsClasses = firClass.allSuperclasses.map { "arrow.meta.Meta\$FrontendTransformer\$${it.simpleName?.removeFirPrefix()}" }
  val result = (listOf(superType) + parentsClasses).mapNotNull {
    try {
      Class.forName(it).kotlin
    } catch (e: ClassNotFoundException) {
      null
    }
  }
  return result
}

