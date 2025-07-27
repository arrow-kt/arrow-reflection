// MODULE: main
// FILE: module_main_quasiquote_declare_function_test.kt
package foo.bar

import arrow.meta.quotes.samples.GenerateDoubleFunction

@GenerateDoubleFunction
class Calculator

fun box(): String {
    val calc = Calculator()
    
    // Test if doubled function was generated
    return try {
        val result = calc.doubled(21)
        if (result == 42) {
            "OK"
        } else {
            "FAIL: Expected 42 but got $result"
        }
    } catch (e: Exception) {
        "FAIL: ${e.message}"
    }
}