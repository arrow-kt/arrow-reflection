package arrow.meta.quotes.hygiene

import org.jetbrains.kotlin.name.Name
import java.util.concurrent.atomic.AtomicLong

/**
 * Context for managing hygienic name generation in quotes.
 * Ensures that generated variable names don't capture existing variables.
 */
class HygienicContext private constructor(
    private val counter: AtomicLong = AtomicLong(0),
    private val usedNames: MutableSet<String> = mutableSetOf(),
    private val prefix: String = "hygienic"
) {
    
    companion object {
        fun create(prefix: String = "hygienic"): HygienicContext = 
            HygienicContext(prefix = prefix)
    }
    
    /**
     * Generate a fresh variable name that doesn't conflict with existing names
     */
    fun freshName(hint: String = "var"): Name {
        val baseName = "${prefix}_${hint}"
        var candidateName = baseName
        var attempts = 0
        
        while (candidateName in usedNames) {
            candidateName = "${baseName}_${attempts++}"
        }
        
        usedNames.add(candidateName)
        return Name.identifier(candidateName)
    }
    
    /**
     * Generate a fresh name with a specific counter
     */
    fun freshNameWithId(hint: String = "var"): Name {
        val id = counter.incrementAndGet()
        val candidateName = "${prefix}_${hint}_$id"
        usedNames.add(candidateName)
        return Name.identifier(candidateName)
    }
    
    /**
     * Mark a name as used to prevent conflicts
     */
    fun markAsUsed(name: String) {
        usedNames.add(name)
    }
    
    /**
     * Check if a name is already used
     */
    fun isUsed(name: String): Boolean = name in usedNames
    
    /**
     * Create a child context that inherits used names but has independent counter
     */
    fun createChild(childPrefix: String = prefix): HygienicContext =
        HygienicContext(
            counter = AtomicLong(0),
            usedNames = usedNames.toMutableSet(),
            prefix = childPrefix
        )
}