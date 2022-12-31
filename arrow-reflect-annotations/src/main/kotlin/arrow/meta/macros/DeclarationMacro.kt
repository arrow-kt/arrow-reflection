package arrow.meta.macros

import arrow.meta.Meta
import arrow.meta.plugins.*
import org.jetbrains.kotlin.KtInMemoryTextSourceFile
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.toRegularClassSymbol
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.extensions.AnnotationFqn
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.pipeline.buildFirViaLightTree
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals

typealias ClassMacro = DeclarationMacro<FirRegularClass>

@OptIn(SymbolInternals::class)
abstract class DeclarationMacro<D : FirDeclaration>(annotation: Annotation) : FrontendPlugin.Builder() {

  override val plugins: Sequence<(FirSession) -> FrontendPlugin>
    get() {
      return super.plugins + sequence {
        yield(
          Generation {
            register(macroPredicate)
            session.transformations.forEach { transformation ->
              when (transformation) {
                is Transform.Generate.Member.Function -> {
                  callableNamesForClass = { classSymbol ->
                    val kotlin = transformation.invoke(classSymbol.fir, null)
                    val fir = session.buildFirViaLightTree(
                      listOf(
                        KtInMemoryTextSourceFile(
                          "topLevelCallableIds.map.kt", null, kotlin.value
                        )
                      )
                    )
                    val decls = fir.firstOrNull()?.declarations?.filterIsInstance<FirCallableDeclaration>()
                      ?.map { it.symbol.callableId.callableName }
                    decls?.toSet().orEmpty()
                  }
                }

                is Transform.Generate.Member.NestedClass -> TODO()
                is Transform.Generate.Member.Property -> TODO()
                is Transform.Generate.TopLevel.Class -> TODO()
                is Transform.Generate.TopLevel.Function -> TODO()
                is Transform.Generate.TopLevel.Property -> TODO()
                is Transform.Replace -> TODO()
              }
            }
          }
        )
      }
    }
//  init {
//    +Supertypes {
//      registerPredicates = register(macroPredicate)
//    }
//    +StatusTransformer {
//      registerPredicates = register(macroPredicate)
//    }
//    +Transformer<String> {
//      registerPredicates = register(macroPredicate)
//    }
//    +Generation {
//      registerPredicates = register(macroPredicate)
//      session.transformations.forEach { t ->
//        when (t) {
//          is Transform.Generate.Member.Function -> {
//            callableNamesForClass = { classSymbol ->
//              val kotlin = t.invoke(classSymbol.fir, null)
//
//              TODO()
//            }
//          }
//          is Transform.Generate.Member.NestedClass -> TODO()
//          is Transform.Generate.Member.Property -> TODO()
//          is Transform.Generate.TopLevel.Class -> TODO()
//          is Transform.Generate.TopLevel.Function -> TODO()
//          is Transform.Generate.TopLevel.Property -> TODO()
//          is Transform.Replace -> TODO()
//        }
//      }
//    }
//  }

  val annotationFqName = AnnotationFqn(annotation.annotationClass.java.canonicalName)

  val macroPredicate: DeclarationPredicate = DeclarationPredicate.create {
    metaAnnotated(annotationFqName)
  }

  fun FirSession.macroSymbols(): List<FirBasedSymbol<*>> =
    predicateBasedProvider.getSymbolsByPredicate(LookupPredicate.create { annotated(annotationFqName) })

  fun FirSession.macroInstances(): List<DeclarationMacro<FirDeclaration>> = macroSymbols().mapNotNull {
    val macroAnnotation = it.annotations.firstOrNull { it.fqName(this) == annotationFqName }
    val companionFqName =
      macroAnnotation?.annotationTypeRef?.toRegularClassSymbol(this)?.fir?.companionObjectSymbol?.classId?.asFqNameString()
    val companionInstance = Class.forName(companionFqName).kotlin.objectInstance as? DeclarationMacro<*>
    companionInstance as? DeclarationMacro<FirDeclaration>
  }

  val FirSession.transformations: List<Transform<FirDeclaration, FirDeclaration>>
    get() =
      macroInstances().map { it.run { transform() } }


  //  transformations.forEach { transform ->
//    when (transform) {
//      is Transform.Generate -> +Generation {
//        registerPredicates = register(macroPredicate)
//        when (transform) {
//          is Transform.Generate.Member -> TODO()
//          is Transform.Generate.TopLevel -> {
//            when (transform) {
//              is Transform.Generate.TopLevel.Class -> {
//
//              }
//
//              is Transform.Generate.TopLevel.Function -> {
//                topLevelCallableIds = {
////                  val source = transform.invoke(null)
////                  val fir = session.buildFirViaLightTree(
////                    listOf(
////                      KtInMemoryTextSourceFile(
////                        "topLevelCallableIds.map.kt", null, source.value
////                      )
////                    )
////                  )
////                  val decls = fir.firstOrNull()?.declarations?.filterIsInstance<FirCallableDeclaration>()
////                    ?.map { it.symbol.callableId }
////                  decls?.toSet().orEmpty()
//                  TODO()
//                }
//                functions = { callableId, memberGenerationContext ->
//                  TODO()
//                }
//              }
//
//              is Transform.Generate.TopLevel.Property -> TODO()
//              is Transform.Generate.Member.Function -> TODO()
//              is Transform.Generate.Member.NestedClass -> TODO()
//              is Transform.Generate.Member.Property -> TODO()
//            }
//
//            classLikeDeclaration = {
//              TODO()
//            }
//          }
//        }
//      }
//
//      is Transform.Replace -> TODO()
//    }
//  }
  abstract fun transform(): Transform<D, FirDeclaration>

}

@Meta
annotation class MyMacro {

  companion object : ClassMacro(MyMacro()) {
    override fun transform(): Transform.Generate.Member.Function<FirClass, FirSimpleFunction> =
      Transform.Generate.Member.Function { context, callableId ->
        Kotlin(
          """
          fun foo(): Unit = println("Hello, World!")
          """
        )
      }
  }
}

@MyMacro
class Sample
