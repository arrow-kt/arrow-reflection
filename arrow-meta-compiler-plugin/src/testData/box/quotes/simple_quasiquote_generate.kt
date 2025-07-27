// WITH_RUNTIME
// MODULE: arrow-reflection-quotes

// FILE: Test.kt
package test

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