package arrow.meta.quotes.builders

import arrow.meta.quotes.QuotesPluginKey
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.extensions.FirExtension
import org.jetbrains.kotlin.fir.plugin.*
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.name.Name

/**
 * Simplified declaration builders that work with the current implementation
 */

// Create a member function
fun FirExtension.createFunction(
    owner: FirClassSymbol<*>,
    name: String,
    returnType: ConeKotlinType,
    parameters: List<Pair<String, ConeKotlinType>> = emptyList()
): FirSimpleFunction = createMemberFunction(
    owner = owner,
    key = QuotesPluginKey,
    name = Name.identifier(name),
    returnType = returnType
) {
    visibility = Visibilities.Public
    modality = Modality.FINAL
    
    // Add parameters
    parameters.forEach { (paramName, paramType) ->
        valueParameter(
            name = Name.identifier(paramName),
            type = paramType
        )
    }
}

// Create a nested class  
fun FirExtension.createClass(
    owner: FirClassSymbol<*>,
    name: String,
    classKind: ClassKind = ClassKind.CLASS
): FirRegularClass = createNestedClass(
    owner = owner,
    name = Name.identifier(name),
    key = QuotesPluginKey,
    classKind = classKind
) {
    visibility = Visibilities.Public
    modality = Modality.FINAL
}

// Create a property
fun FirExtension.createProperty(
    owner: FirClassSymbol<*>,
    name: String,
    type: ConeKotlinType,
    isVar: Boolean = false
): FirProperty = createMemberProperty(
    owner = owner,
    key = QuotesPluginKey,
    name = Name.identifier(name),
    returnType = type,
    isVal = !isVar
) {
    visibility = Visibilities.Public
    modality = Modality.FINAL
}