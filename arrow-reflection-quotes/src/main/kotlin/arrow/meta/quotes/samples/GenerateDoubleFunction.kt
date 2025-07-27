package arrow.meta.quotes.samples

import arrow.meta.Meta
import arrow.meta.quotes.QuotesPluginKey
import arrow.meta.quotes.builders.*
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Generates a doubled function for classes annotated with @GenerateDoubleFunction
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class GenerateDoubleFunction

/**
 * Meta implementation using declaration builders
 */
@Meta
annotation class GenerateDoubleFunctionMeta

class GenerateDoubleFunctionExtension(session: FirSession) : FirDeclarationGenerationExtension(session) {
    
    companion object {
        private val GENERATE_DOUBLE_FQN = FqName("arrow.meta.quotes.samples.GenerateDoubleFunction")
        private val DOUBLED_NAME = Name.identifier("doubled")
        
        private val PREDICATE = LookupPredicate.create {
            annotated(GENERATE_DOUBLE_FQN)
        }
    }
    
    private val predicateBasedProvider = session.predicateBasedProvider
    private val matchedClasses by lazy {
        predicateBasedProvider.getSymbolsByPredicate(PREDICATE).filterIsInstance<FirRegularClassSymbol>()
    }
    
    override fun generateFunctions(callableId: CallableId, context: MemberGenerationContext?): List<FirNamedFunctionSymbol> {
        if (callableId.callableName != DOUBLED_NAME) return emptyList()
        if (context == null) return emptyList()
        if (context.owner !in matchedClasses) return emptyList()
        
        // Generate doubled function using simple builder
        val doubledFunction = createFunction(
            owner = context.owner,
            name = "doubled",
            returnType = session.builtinTypes.intType.coneType,
            parameters = listOf("x" to session.builtinTypes.intType.coneType)
        )
        
        return listOf(doubledFunction.symbol)
    }
    
    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        return if (classSymbol in matchedClasses) {
            setOf(DOUBLED_NAME)
        } else {
            emptySet()
        }
    }
    
    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(PREDICATE)
    }
}