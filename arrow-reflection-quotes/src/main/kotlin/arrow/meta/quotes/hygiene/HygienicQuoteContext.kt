package arrow.meta.quotes.hygiene

import arrow.meta.FirMetaContext
import arrow.meta.quotes.Expr
import arrow.meta.quotes.QuoteContext
import org.jetbrains.kotlin.name.Name

/**
 * Enhanced QuoteContext with hygiene support for variable name generation
 */
class HygienicQuoteContext(
    override val metaContext: FirMetaContext,
    private val hygienicContext: HygienicContext = HygienicContext.create()
) : QuoteContext {
    
    /**
     * Create a fresh variable reference that won't capture existing variables
     */
    fun freshVariable(hint: String = "var"): Name =
        hygienicContext.freshName(hint)
    
    /**
     * Create a fresh variable with a unique ID
     */
    fun freshVariableWithId(hint: String = "var"): Name =
        hygienicContext.freshNameWithId(hint)
    
    /**
     * Create a let-like binding with a fresh variable name
     * let freshVar = value in body(freshVar)
     */
    fun <T> withFreshBinding(
        value: Expr<T>,
        hint: String = "binding",
        body: (Name) -> Expr<T>
    ): Expr<T> {
        val freshVar = freshVariable(hint)
        hygienicContext.markAsUsed(freshVar.identifier)
        
        // For now, return a simplified implementation
        // In a full implementation, this would create proper block expressions
        return body(freshVar)
    }
    
    /**
     * Create multiple fresh bindings
     */
    fun <T> withFreshBindings(
        values: List<Pair<Expr<*>, String>>,
        body: (List<Name>) -> Expr<T>
    ): Expr<T> {
        val freshVars = values.map { (_, hint) -> 
            freshVariable(hint).also { hygienicContext.markAsUsed(it.identifier) }
        }
        
        // For now, return a simplified implementation
        // In a full implementation, this would create proper block expressions
        return body(freshVars)
    }
    
    /**
     * Mark existing variable names as used to prevent capture
     */
    fun markUsedNames(vararg names: String) {
        names.forEach { hygienicContext.markAsUsed(it) }
    }
    
    /**
     * Create a child context for nested scopes
     */
    fun createChildContext(prefix: String = "nested"): HygienicQuoteContext =
        HygienicQuoteContext(metaContext, hygienicContext.createChild(prefix))
}