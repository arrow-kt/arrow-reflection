package arrow.meta.quotes.validation

import arrow.meta.quotes.Expr
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.FirReference
import org.jetbrains.kotlin.fir.visitors.FirDefaultVisitor

/**
 * Validates quote/splice usage and staging levels
 */
class QuoteValidator {
    
    /**
     * Validate that an expression has proper quote/splice staging
     */
    fun validateQuoteStaging(expr: Expr<*>, initialLevel: QuoteLevel = QuoteLevel.RUNTIME): ValidationResult {
        val visitor = QuoteLevelVisitor(initialLevel)
        return try {
            expr.fir.accept(visitor, null)
            ValidationResult.Success
        } catch (e: QuoteLevelException) {
            ValidationResult.Error(e.message ?: "Unknown quote level error")
        }
    }
    
    /**
     * Check if a splice operation is valid at the current level
     */
    fun validateSplice(currentLevel: QuoteLevel): ValidationResult {
        return if (currentLevel.canSplice()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Cannot splice at level $currentLevel")
        }
    }
    
    /**
     * Check if quote nesting is within reasonable bounds
     */
    fun validateQuoteDepth(level: QuoteLevel, maxDepth: Int = 10): ValidationResult {
        return if (level.level <= maxDepth) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Quote nesting too deep: ${level.level} > $maxDepth")
        }
    }
}

/**
 * Result of quote validation
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
    
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    
    inline fun onError(action: (String) -> Unit): ValidationResult {
        if (this is Error) action(message)
        return this
    }
    
    inline fun onSuccess(action: () -> Unit): ValidationResult {
        if (this is Success) action()
        return this
    }
}

/**
 * FIR visitor that tracks quote levels during traversal
 */
private class QuoteLevelVisitor(
    private var currentLevel: QuoteLevel
) : FirDefaultVisitor<Unit, Nothing?>() {
    
    override fun visitElement(element: FirElement, data: Nothing?) {
        // For now, we'll track quote/splice through function calls
        // In a real implementation, this would recognize specific quote/splice markers
        element.acceptChildren(this, data)
    }
    
    override fun visitFunctionCall(functionCall: FirFunctionCall, data: Nothing?) {
        val calleeReference = functionCall.calleeReference
        
        when {
            // Recognize quote operations (this would be more sophisticated in practice)
            isQuoteCall(calleeReference) -> {
                val newLevel = currentLevel.enterQuote()
                validateLevel(newLevel)
                
                val previousLevel = currentLevel
                currentLevel = newLevel
                functionCall.acceptChildren(this, data)
                currentLevel = previousLevel
            }
            
            // Recognize splice operations
            isSpliceCall(calleeReference) -> {
                if (!currentLevel.canSplice()) {
                    throw QuoteLevelException.InvalidSpliceLevel(currentLevel)
                }
                
                val newLevel = currentLevel.enterSplice()
                val previousLevel = currentLevel
                currentLevel = newLevel
                functionCall.acceptChildren(this, data)
                currentLevel = previousLevel
            }
            
            else -> functionCall.acceptChildren(this, data)
        }
    }
    
    private fun validateLevel(level: QuoteLevel) {
        if (level.level < 0) {
            throw QuoteLevelException.NegativeLevel(level)
        }
    }
    
    private fun isQuoteCall(calleeReference: FirReference): Boolean {
        // In practice, this would check for specific quote function names
        // For now, simplified implementation
        return false
    }
    
    private fun isSpliceCall(calleeReference: FirReference): Boolean {
        // In practice, this would check for specific splice function names/operators
        // For now, simplified implementation  
        return false
    }
}