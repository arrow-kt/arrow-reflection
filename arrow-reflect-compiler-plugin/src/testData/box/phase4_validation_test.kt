package foo.bar

import arrow.meta.quotes.samples.WithValidation

@WithValidation
class ValidationTest {
    fun processData(x: Int): Int = x * 2
}

fun box(): String {
    val test = ValidationTest()
    
    try {
        // The generated code should validate quote levels
        // and generate proper staged expressions
        val result = test.validatedProcess(21)
        
        return if (result == 42) {
            "OK"
        } else {
            "FAIL: Expected 42 but got $result"
        }
    } catch (e: Exception) {
        return "FAIL: ${e.message}"
    }
}