// MODULE: main
// FILE: module_main_simple_declaration_builder_test.kt
package foo.bar

import arrow.meta.quotes.samples.SimpleGenerateFactory

@SimpleGenerateFactory
class SimpleProduct

fun box(): String {
    // Test that companion object was generated
    val companion = SimpleProduct.Companion
    
    // Test that factory method was generated  
    val product = SimpleProduct.Companion.create()
    
    return "OK"
}