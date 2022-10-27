package arrow.reflect.compiler.plugin

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor

class ArrowReflectCliProcessor : CommandLineProcessor {
  override val pluginId: String = "arrow.reflect.compiler.plugin.reflect"
  override val pluginOptions: Collection<AbstractCliOption> = emptyList()
}
