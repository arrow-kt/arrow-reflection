// MODULE: main
// FILE: module_main_quasiquote_conditional_test.kt
package foo.bar

import arrow.meta.quotes.samples.QuasiquoteConditional

fun box(): String {
    val nullable: Int? = null
    val notNull: String = "hello"
    
    val result1 = @QuasiquoteConditional nullable
    val result2 = @QuasiquoteConditional notNull
    
    return if (result1 == "default" && result2 == "hello") {
        "OK"
    } else {
        "FAIL: result1=$result1, result2=$result2"
    }
}