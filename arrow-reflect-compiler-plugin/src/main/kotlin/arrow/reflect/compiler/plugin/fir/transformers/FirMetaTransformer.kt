package arrow.reflect.compiler.plugin.fir.transformers

import arrow.meta.FirMetaCheckerContext
import arrow.meta.TemplateCompiler
import arrow.reflect.compiler.plugin.fir.checkers.metaAnnotations
import arrow.reflect.compiler.plugin.targets.MetaTarget
import arrow.reflect.compiler.plugin.targets.MetagenerationTarget
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.util.capitalizeDecapitalize.decapitalizeAsciiOnly
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

class FirMetaTransformer(
  private val session: FirSession, val templateCompiler: TemplateCompiler, val metaTargets: List<MetaTarget>,
  val checkerContext: CheckerContext,
  val reporter: DiagnosticReporter,
) : FirTransformer<FirDeclaration>() {

  private fun <E : FirAnnotationContainer> invokeMeta(
    context: FirDeclaration,
    arg: E
  ): FirAnnotationContainer? {
    if (templateCompiler.compiling) return null
    val args = listOf(arg::class)
    val metaAnnotations = arg.metaAnnotations(session)
    val metaClasses = arg.metaClasses()
    val dispatchers = metaClasses.mapNotNull {
      val methodName = it.java.simpleName.decapitalizeAsciiOnly()
      MetaTarget.find(
        false,
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
        val metaContext = FirMetaCheckerContext(templateCompiler, session, checkerContext, reporter, context)
        out ?: target.method.invoke(target.companion.objectInstance, metaContext, arg) as? FirAnnotationContainer
      }

    return result
  }

  override fun <E : FirElement> transformElement(element: E, data: FirDeclaration): E {
    val result = if (element is FirAnnotationContainer) {
      invokeMeta(data, element) ?: element
    } else element
    //val context = element as? FirDeclaration ?: data
    result.transformChildren(this, data)
    return result as E
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

