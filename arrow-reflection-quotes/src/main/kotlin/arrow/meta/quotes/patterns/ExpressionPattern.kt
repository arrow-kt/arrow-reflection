package arrow.meta.quotes.patterns

import arrow.meta.quotes.Expr
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.name.Name

/**
 * Pattern matching for FIR expressions to enable decomposition and analysis
 */
sealed class ExpressionPattern<T> {
    
    /**
     * Attempt to match this pattern against an expression
     */
    abstract fun match(expr: Expr<*>): MatchResult<T>
    
    /**
     * Combine this pattern with another using AND logic
     */
    infix fun <U> and(other: ExpressionPattern<U>): ExpressionPattern<Pair<T, U>> =
        AndPattern(this, other)
    
    /**
     * Combine this pattern with another using OR logic  
     */
    infix fun or(other: ExpressionPattern<T>): ExpressionPattern<T> =
        OrPattern(this, other)
    
    /**
     * Transform the matched result
     */
    fun <U> map(transform: (T) -> U): ExpressionPattern<U> =
        MappedPattern(this, transform)
}

/**
 * Result of pattern matching
 */
sealed class MatchResult<out T> {
    data class Success<T>(val value: T) : MatchResult<T>()
    data object Failure : MatchResult<Nothing>()
    
    fun isSuccess(): Boolean = this is Success
    fun isFailure(): Boolean = this is Failure
    
    inline fun <U> map(transform: (T) -> U): MatchResult<U> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> Failure
    }
    
    inline fun onSuccess(action: (T) -> Unit): MatchResult<T> {
        if (this is Success) action(value)
        return this
    }
}

// Core Pattern Implementations

/**
 * Matches any expression
 */
object AnyPattern : ExpressionPattern<Expr<*>>() {
    override fun match(expr: Expr<*>): MatchResult<Expr<*>> = MatchResult.Success(expr)
}

/**
 * Matches literal values
 */
data class LiteralPattern<T>(val expectedValue: T) : ExpressionPattern<T>() {
    override fun match(expr: Expr<*>): MatchResult<T> {
        return when (val fir = expr.fir) {
            is FirLiteralExpression -> {
                @Suppress("UNCHECKED_CAST")
                if (fir.value == expectedValue) {
                    MatchResult.Success(expectedValue)
                } else {
                    MatchResult.Failure
                }
            }
            else -> MatchResult.Failure
        }
    }
}

/**
 * Matches function calls by name
 */
data class FunctionCallPattern(
    val functionName: Name,
    val argumentPatterns: List<ExpressionPattern<*>> = emptyList()
) : ExpressionPattern<FunctionCallMatch>() {
    
    override fun match(expr: Expr<*>): MatchResult<FunctionCallMatch> {
        return when (val fir = expr.fir) {
            is FirFunctionCall -> {
                val calleeReference = fir.calleeReference
                if (calleeReference is FirNamedReference && calleeReference.name == functionName) {
                    val argumentMatches = fir.arguments.zip(argumentPatterns).map { (arg, pattern) ->
                        pattern.match(Expr<Any?>(arg))
                    }
                    
                    if (argumentMatches.all { it.isSuccess() }) {
                        MatchResult.Success(
                            FunctionCallMatch(
                                functionName = functionName,
                                receiver = fir.explicitReceiver?.let { Expr<Any?>(it) },
                                arguments = fir.arguments.map { Expr<Any?>(it) }
                            )
                        )
                    } else {
                        MatchResult.Failure
                    }
                } else {
                    MatchResult.Failure
                }
            }
            else -> MatchResult.Failure
        }
    }
}

/**
 * Matches binary operations
 */
data class BinaryOpPattern(
    val operatorName: Name,
    val leftPattern: ExpressionPattern<*> = AnyPattern,
    val rightPattern: ExpressionPattern<*> = AnyPattern
) : ExpressionPattern<BinaryOpMatch>() {
    
    override fun match(expr: Expr<*>): MatchResult<BinaryOpMatch> {
        return when (val fir = expr.fir) {
            is FirFunctionCall -> {
                // Binary operations are represented as function calls
                val calleeReference = fir.calleeReference
                if (calleeReference is FirNamedReference && calleeReference.name == operatorName) {
                    val receiver = fir.explicitReceiver
                    val argument = fir.arguments.firstOrNull()
                    
                    if (receiver != null && argument != null) {
                        val leftMatch = leftPattern.match(Expr<Any?>(receiver))
                        val rightMatch = rightPattern.match(Expr<Any?>(argument))
                        
                        if (leftMatch.isSuccess() && rightMatch.isSuccess()) {
                            MatchResult.Success(
                                BinaryOpMatch(
                                    operator = operatorName,
                                    left = Expr<Any?>(receiver),
                                    right = Expr<Any?>(argument)
                                )
                            )
                        } else {
                            MatchResult.Failure
                        }
                    } else {
                        MatchResult.Failure
                    }
                } else {
                    MatchResult.Failure
                }
            }
            else -> MatchResult.Failure
        }
    }
}

/**
 * Matches variable references
 */
data class VariablePattern(val variableName: Name) : ExpressionPattern<Name>() {
    override fun match(expr: Expr<*>): MatchResult<Name> {
        return when (val fir = expr.fir) {
            is FirPropertyAccessExpression -> {
                val calleeReference = fir.calleeReference
                if (calleeReference is FirNamedReference && calleeReference.name == variableName) {
                    MatchResult.Success(variableName)
                } else {
                    MatchResult.Failure
                }
            }
            else -> MatchResult.Failure
        }
    }
}

// Combinator Patterns

private data class AndPattern<T, U>(
    val first: ExpressionPattern<T>,
    val second: ExpressionPattern<U>
) : ExpressionPattern<Pair<T, U>>() {
    override fun match(expr: Expr<*>): MatchResult<Pair<T, U>> {
        val firstMatch = first.match(expr)
        val secondMatch = second.match(expr)
        
        return if (firstMatch is MatchResult.Success && secondMatch is MatchResult.Success) {
            MatchResult.Success(firstMatch.value to secondMatch.value)
        } else {
            MatchResult.Failure
        }
    }
}

private data class OrPattern<T>(
    val first: ExpressionPattern<T>,
    val second: ExpressionPattern<T>
) : ExpressionPattern<T>() {
    override fun match(expr: Expr<*>): MatchResult<T> {
        return first.match(expr).let { result ->
            if (result.isSuccess()) result else second.match(expr)
        }
    }
}

private data class MappedPattern<T, U>(
    val pattern: ExpressionPattern<T>,
    val transform: (T) -> U
) : ExpressionPattern<U>() {
    override fun match(expr: Expr<*>): MatchResult<U> {
        return pattern.match(expr).map(transform)
    }
}

// Match Result Types

data class FunctionCallMatch(
    val functionName: Name,
    val receiver: Expr<*>?,
    val arguments: List<Expr<*>>
)

data class BinaryOpMatch(
    val operator: Name,
    val left: Expr<*>,
    val right: Expr<*>
)