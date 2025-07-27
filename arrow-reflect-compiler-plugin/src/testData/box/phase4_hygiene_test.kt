package foo.bar

import arrow.meta.quotes.samples.WithHygiene

@WithHygiene
class HygieneTest {
    fun getValue(): Int = 42
}

fun box(): String {
    val test = HygieneTest()
    
    try {
        // The generated code should use fresh variable names
        // and not capture existing 'value' variable
        val value = 10
        val result = test.processWithFreshBinding(value)
        
        return if (result == 52) { // 42 + 10
            "OK"
        } else {
            "FAIL: Expected 52 but got $result"
        }
    } catch (e: Exception) {
        return "FAIL: ${e.message}"
    }
}