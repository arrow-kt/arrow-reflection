package arrow.meta.quotes.validation

/**
 * Represents the staging level of quotes and splices.
 * Level 0 = runtime code
 * Level 1 = inside one quote
 * Level 2 = inside nested quotes
 * etc.
 */
@JvmInline
value class QuoteLevel(val level: Int) {
    
    companion object {
        val RUNTIME = QuoteLevel(0)
        val QUOTED = QuoteLevel(1)
    }
    
    /**
     * Enter a quote (increase level by 1)
     */
    fun enterQuote(): QuoteLevel = QuoteLevel(level + 1)
    
    /**
     * Enter a splice (decrease level by 1)
     */
    fun enterSplice(): QuoteLevel = QuoteLevel(level - 1)
    
    /**
     * Check if we can splice at this level
     */
    fun canSplice(): Boolean = level > 0
    
    /**
     * Check if we're at runtime level
     */
    fun isRuntime(): Boolean = level == 0
    
    /**
     * Check if we're inside a quote
     */
    fun isQuoted(): Boolean = level > 0
    
    override fun toString(): String = "Level($level)"
}

/**
 * Exception thrown when quote/splice operations are used at invalid levels
 */
sealed class QuoteLevelException(message: String) : Exception(message) {
    
    class InvalidSpliceLevel(currentLevel: QuoteLevel) : 
        QuoteLevelException("Cannot splice at level $currentLevel. Splices require level > 0.")
    
    class NegativeLevel(attemptedLevel: QuoteLevel) :
        QuoteLevelException("Quote level cannot be negative. Attempted level: $attemptedLevel")
    
    class UnbalancedQuoteSplice(currentLevel: QuoteLevel, operation: String) :
        QuoteLevelException("Unbalanced quote/splice operations. Current level: $currentLevel, attempted: $operation")
}