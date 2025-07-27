package arrow.meta.quotes.samples

import arrow.meta.Meta
import arrow.meta.quotes.QuotesPluginKey
import arrow.meta.quotes.builders.*
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.plugin.createDefaultPrivateConstructor
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.constructClassType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Generates a companion object with factory method
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class SimpleGenerateFactory

/**
 * Simple Meta implementation using declaration builders
 */
@Meta
annotation class SimpleQuasiquoteGenerate

@OptIn(ExperimentalTopLevelDeclarationsGenerationApi::class)
class SimpleQuasiquoteGenerateExtension(session: FirSession) : FirDeclarationGenerationExtension(session) {
    
    companion object {
        private val FACTORY_FQN = FqName("arrow.meta.quotes.samples.SimpleGenerateFactory")
        private val COMPANION_NAME = Name.special("<companion>")
        private val FACTORY_METHOD_NAME = Name.identifier("create")
        
        private val PREDICATE = LookupPredicate.create {
            annotated(FACTORY_FQN)
        }
    }
    
    private val predicateBasedProvider = session.predicateBasedProvider
    private val matchedClasses by lazy {
        predicateBasedProvider.getSymbolsByPredicate(PREDICATE).filterIsInstance<FirRegularClassSymbol>()
    }
    
    override fun generateNestedClassLikeDeclaration(
        owner: FirClassSymbol<*>,
        name: Name,
        context: NestedClassGenerationContext
    ): FirClassLikeSymbol<*>? {
        if (name != COMPANION_NAME) return null
        if (owner !in matchedClasses) return null
        
        // Generate companion object
        val companionObject = createClass(owner, "Companion", ClassKind.OBJECT)
        
        return companionObject.symbol
    }
    
    override fun generateFunctions(callableId: CallableId, context: MemberGenerationContext?): List<FirNamedFunctionSymbol> {
        if (callableId.callableName != FACTORY_METHOD_NAME) return emptyList()
        if (context == null) return emptyList()
        
        // Check if this is the companion object of a matched class
        val ownerClass = matchedClasses.find { 
            it.classId.createNestedClassId(COMPANION_NAME) == context.owner.classId
        } ?: return emptyList()
        
        // Generate factory method
        val factoryMethod = createFunction(
            context.owner,
            "create",
            ownerClass.toLookupTag().constructClassType(emptyArray(), false)
        )
        
        return listOf(factoryMethod.symbol)
    }
    
    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        return if (matchedClasses.any { it.classId.createNestedClassId(COMPANION_NAME) == classSymbol.classId }) {
            setOf(FACTORY_METHOD_NAME)
        } else {
            emptySet()
        }
    }
    
    override fun getNestedClassifiersNames(classSymbol: FirClassSymbol<*>, context: NestedClassGenerationContext): Set<Name> {
        return if (classSymbol in matchedClasses) {
            setOf(COMPANION_NAME)
        } else {
            emptySet()
        }
    }
    
    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(PREDICATE)
    }
}