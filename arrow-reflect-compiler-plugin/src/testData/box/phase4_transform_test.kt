package foo.bar

import arrow.meta.quotes.samples.WithTransformOptimization

@WithTransformOptimization  
class TransformTest {
    fun calculate(a: Int, b: Int): Int = a + b + 1
}

fun box(): String {
    val test = TransformTest()
    
    try {
        // The generated code should apply expression transformations
        // optimizing arithmetic expressions
        val result = test.optimizedCalculate(10, 5)
        
        return if (result == 16) { // 10 + 5 + 1
            "OK"
        } else {
            "FAIL: Expected 16 but got $result"
        }
    } catch (e: Exception) {
        return "FAIL: ${e.message}"
    }
}