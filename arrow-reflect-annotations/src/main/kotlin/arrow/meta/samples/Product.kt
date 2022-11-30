package arrow.meta.samples

import arrow.meta.Meta
import arrow.meta.module.impl.arrow.meta.FirMetaContext
import org.jetbrains.kotlin.fir.declarations.*

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Meta
annotation class Product {
  companion object : Meta.Generate.Members.Functions {

    override fun FirMetaContext.newFunctions(firClass: FirClass): Map<String, () -> String> =
      mapOf(
        "product" to {
          //language=kotlin
          """|
             |fun product(): List<Pair<String, *>> = 
             |  listOf(${properties(firClass) { """"${+it.name}" to this.${+it.name}""" }})
          """.trimMargin()
        }
      )

  }
}
