package arrow.meta.quotes.patterns

import arrow.meta.quotes.Expr
import org.jetbrains.kotlin.name.Name

/**
 * DSL for creating expression patterns
 */
object Patterns {
    
    /**
     * Pattern that matches any expression
     */
    val any: ExpressionPattern<Expr<*>> = AnyPattern
    
    /**
     * Pattern that matches a specific literal value
     */
    fun <T> literal(value: T): ExpressionPattern<T> = LiteralPattern(value)
    
    /**
     * Pattern that matches a function call by name
     */
    fun functionCall(
        name: String,
        vararg argumentPatterns: ExpressionPattern<*>
    ): ExpressionPattern<FunctionCallMatch> =
        FunctionCallPattern(Name.identifier(name), argumentPatterns.toList())
    
    /**
     * Pattern that matches a binary operation
     */
    fun binaryOp(
        operator: String,
        left: ExpressionPattern<*> = any,
        right: ExpressionPattern<*> = any
    ): ExpressionPattern<BinaryOpMatch> =
        BinaryOpPattern(Name.identifier(operator), left, right)
    
    /**
     * Pattern that matches a variable reference
     */
    fun variable(name: String): ExpressionPattern<Name> =
        VariablePattern(Name.identifier(name))
    
    /**
     * Convenience patterns for common operations
     */
    object CommonOps {
        val plus = binaryOp("plus")
        val minus = binaryOp("minus") 
        val times = binaryOp("times")
        val div = binaryOp("div")
        val equals = binaryOp("equals")
        val notEquals = binaryOp("notEquals")
        val compareTo = binaryOp("compareTo")
    }
    
    /**
     * Convenience patterns for common literals
     */
    object CommonLiterals {
        val intZero = literal(0)
        val intOne = literal(1)
        val stringEmpty = literal("")
        val booleanTrue = literal(true)
        val booleanFalse = literal(false)
    }
}

/**
 * Extension functions for pattern matching on expressions
 */
fun <T> Expr<*>.match(pattern: ExpressionPattern<T>): MatchResult<T> =
    pattern.match(this)

/**
 * Convenient infix function for pattern matching
 */
infix fun <T> Expr<*>.matches(pattern: ExpressionPattern<T>): Boolean =
    pattern.match(this).isSuccess()

/**
 * Pattern matching with when-like syntax
 */
inline fun <T, R> Expr<*>.matchWith(
    pattern: ExpressionPattern<T>,
    onMatch: (T) -> R,
    onNoMatch: () -> R
): R {
    return when (val result = pattern.match(this)) {
        is MatchResult.Success -> onMatch(result.value)
        is MatchResult.Failure -> onNoMatch()
    }
}

/**
 * Multi-pattern matching
 */
class PatternMatcher<R>(private val expr: Expr<*>) {
    private val cases = mutableListOf<Pair<ExpressionPattern<*>, (Any?) -> R>>()
    private var defaultCase: (() -> R)? = null
    
    fun <T> case(pattern: ExpressionPattern<T>, handler: (T) -> R): PatternMatcher<R> {
        @Suppress("UNCHECKED_CAST")
        cases.add(pattern to { value -> handler(value as T) })
        return this
    }
    
    fun default(handler: () -> R): PatternMatcher<R> {
        defaultCase = handler
        return this
    }
    
    fun execute(): R {
        for ((pattern, handler) in cases) {
            val result = pattern.match(expr)
            if (result is MatchResult.Success) {
                return handler(result.value)
            }
        }
        
        return defaultCase?.invoke() 
            ?: throw IllegalStateException("No pattern matched and no default case provided")
    }
}

/**
 * Start a pattern matching expression
 */
fun <R> Expr<*>.patternMatch(): PatternMatcher<R> = PatternMatcher(this)