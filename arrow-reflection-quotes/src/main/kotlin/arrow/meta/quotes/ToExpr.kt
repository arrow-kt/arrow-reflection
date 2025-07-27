package arrow.meta.quotes

import kotlin.reflect.KClass

/**
 * Typeclass for converting values to quoted expressions.
 * 
 * Implement this interface to enable custom types to be converted
 * to expressions within quote blocks.
 * 
 * @param T The type of value that can be converted to an expression
 */
interface ToExpr<T> {
    /**
     * Converts a value to a quoted expression within a quote context.
     * 
     * @param value The value to convert
     * @return The quoted expression representing the value
     */
    fun QuoteContext.toExpr(value: T): Expr<T>
}