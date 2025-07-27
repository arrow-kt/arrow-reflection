package foo.bar

import arrow.meta.quotes.samples.WithPatternOptimization

@WithPatternOptimization
class PatternTest {
    fun compute(x: Int, y: Int): Int = x + y
}

fun box(): String {
    val test = PatternTest()
    
    try {
        // The generated code should pattern match on expressions
        // and optimize x + 0 to just x
        val result = test.optimizedCompute(5, 0)
        
        return if (result == 5) {
            "OK"
        } else {
            "FAIL: Expected 5 but got $result"
        }
    } catch (e: Exception) {
        return "FAIL: ${e.message}"
    }
}