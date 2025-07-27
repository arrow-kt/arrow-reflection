package arrow.meta.quotes

import arrow.meta.FirMetaContext
import arrow.meta.quotes.internal.ToExprInstances
import org.jetbrains.kotlin.fir.expressions.FirExpression

/**
 * Context for building quoted expressions.
 * 
 * Provides the necessary operations for creating and manipulating
 * quoted expressions within a quote block.
 */
interface QuoteContext {
    /**
     * The FIR meta context providing access to session and utilities.
     */
    val metaContext: FirMetaContext
    
    /**
     * Converts a FIR expression to a typed Expr.
     */
    fun <T> FirExpression.toExpr(): Expr<T> = Expr(this)
    
    /**
     * Splices an expression, extracting its FIR representation.
     */
    fun <T> Expr<T>.splice(): FirExpression = this.fir
    
    /**
     * Converts a value to an expression using the ToExpr typeclass.
     */
    fun <T : Any> T.toExpr(): Expr<T> = with(metaContext) {
        val toExprInstance = ToExprInstances.findInstance<T>(this@toExpr::class)
            ?: error("No ToExpr instance found for ${this@toExpr::class}")
        
        with(toExprInstance) {
            this@QuoteContext.toExpr(this@toExpr)
        }
    }
    
    /**
     * Shorthand for converting literals to expressions.
     * Example: 42.q() instead of 42.toExpr()
     */
    fun <T : Any> T.q(): Expr<T> = toExpr()
}

/**
 * Default implementation of QuoteContext.
 */
class QuoteContextImpl(
    override val metaContext: FirMetaContext
) : QuoteContext {
    companion object {
        /**
         * Thread-local storage for the current quote context.
         * Used by expression builders to access the context.
         */
        internal val currentContext = ThreadLocal<QuoteContext>()
        
        /**
         * Gets the current quote context.
         */
        val current: QuoteContext
            get() = currentContext.get() 
                ?: error("No QuoteContext available. Expression builders must be used within a quote { } block.")
    }
}

/**
 * Entry point for creating quoted expressions.
 * 
 * @param this@quote The FIR meta context
 * @param block The quote building block
 * @return The resulting quoted expression
 */
fun <T> FirMetaContext.quote(
  block: QuoteContext.() -> Expr<T>
): Expr<T> {
    val context = QuoteContextImpl(this)
    // Set the current context for expression builders
    val previousContext = QuoteContextImpl.currentContext.get()
    try {
        QuoteContextImpl.currentContext.set(context)
        return context.block()
    } finally {
        // Restore previous context (in case of nested quotes)
        QuoteContextImpl.currentContext.set(previousContext)
    }
}

/**
 * Splice operator - extracts the FIR expression from an Expr.
 * 
 * Usage: +expr
 */
operator fun <T> Expr<T>.unaryPlus(): FirExpression = this.fir

/**
 * Alternative splice function for contexts where operator overloading
 * is not preferred.
 */
fun <T> splice(expr: Expr<T>): FirExpression = expr.fir
