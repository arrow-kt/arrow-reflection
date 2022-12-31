package arrow.meta.plugins

import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.contracts.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.diagnostics.FirDiagnosticHolder
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.*
import org.jetbrains.kotlin.fir.types.*

interface Transformer : FrontendPlugin {

  fun transformAnnotationContainer(annotationContainer: FirAnnotationContainer, parent: FirElement): FirAnnotationContainer

  fun transformTypeRef(typeRef: FirTypeRef, parent: FirElement): FirTypeRef

  fun transformReference(reference: FirReference, parent: FirElement): FirReference

  fun transformLabel(label: FirLabel, parent: FirElement): FirLabel

  fun transformResolvable(resolvable: FirResolvable, parent: FirElement): FirResolvable

  fun transformTargetElement(targetElement: FirTargetElement, parent: FirElement): FirTargetElement

  fun transformDeclarationStatus(declarationStatus: FirDeclarationStatus, parent: FirElement): FirDeclarationStatus

  fun transformResolvedDeclarationStatus(
      resolvedDeclarationStatus: FirResolvedDeclarationStatus,
      parent: FirElement
  ): FirDeclarationStatus

  fun transformControlFlowGraphOwner(controlFlowGraphOwner: FirControlFlowGraphOwner, parent: FirElement): FirControlFlowGraphOwner

  fun transformStatement(statement: FirStatement, parent: FirElement): FirStatement

  fun transformExpression(expression: FirExpression, parent: FirElement): FirStatement

  fun transformContextReceiver(contextReceiver: FirContextReceiver, parent: FirElement): FirContextReceiver

  fun transformElementWithResolvePhase(
      elementWithResolvePhase: FirElementWithResolvePhase,
      parent: FirElement
  ): FirElementWithResolvePhase

  fun transformFileAnnotationsContainer(
      fileAnnotationsContainer: FirFileAnnotationsContainer,
      parent: FirElement
  ): FirFileAnnotationsContainer

  fun transformDeclaration(declaration: FirDeclaration, parent: FirElement): FirDeclaration

  fun transformTypeParameterRefsOwner(
      typeParameterRefsOwner: FirTypeParameterRefsOwner,
      parent: FirElement
  ): FirTypeParameterRefsOwner

  fun transformTypeParametersOwner(typeParametersOwner: FirTypeParametersOwner, parent: FirElement): FirTypeParametersOwner

  fun transformMemberDeclaration(memberDeclaration: FirMemberDeclaration, parent: FirElement): FirMemberDeclaration

  fun transformAnonymousInitializer(anonymousInitializer: FirAnonymousInitializer, parent: FirElement): FirAnonymousInitializer

  fun transformCallableDeclaration(callableDeclaration: FirCallableDeclaration, parent: FirElement): FirCallableDeclaration

  fun transformTypeParameterRef(typeParameterRef: FirTypeParameterRef, parent: FirElement): FirTypeParameterRef

  fun transformTypeParameter(typeParameter: FirTypeParameter, parent: FirElement): FirTypeParameterRef

  fun transformVariable(variable: FirVariable, parent: FirElement): FirStatement

  fun transformValueParameter(valueParameter: FirValueParameter, parent: FirElement): FirStatement

  fun transformReceiverParameter(receiverParameter: FirReceiverParameter, parent: FirElement): FirReceiverParameter

  fun transformProperty(property: FirProperty, parent: FirElement): FirStatement

  fun transformField(field: FirField, parent: FirElement): FirStatement

  fun transformEnumEntry(enumEntry: FirEnumEntry, parent: FirElement): FirStatement

  fun transformFunctionTypeParameter(functionTypeParameter: FirFunctionTypeParameter, parent: FirElement): FirFunctionTypeParameter

  fun transformClassLikeDeclaration(classLikeDeclaration: FirClassLikeDeclaration, parent: FirElement): FirStatement

  fun transformClass(klass: FirClass, parent: FirElement): FirStatement

  fun transformRegularClass(regularClass: FirRegularClass, parent: FirElement): FirStatement

  fun transformTypeAlias(typeAlias: FirTypeAlias, parent: FirElement): FirStatement

  fun transformFunction(function: FirFunction, parent: FirElement): FirStatement

  fun transformContractDescriptionOwner(
      contractDescriptionOwner: FirContractDescriptionOwner,
      parent: FirElement
  ): FirContractDescriptionOwner

  fun transformSimpleFunction(simpleFunction: FirSimpleFunction, parent: FirElement): FirStatement

  fun transformPropertyAccessor(propertyAccessor: FirPropertyAccessor, parent: FirElement): FirStatement

  fun transformBackingField(backingField: FirBackingField, parent: FirElement): FirStatement

  fun transformConstructor(constructor: FirConstructor, parent: FirElement): FirStatement

  fun transformFile(file: FirFile, parent: FirElement): FirFile

  fun transformScript(script: FirScript, parent: FirElement): FirScript

  fun transformPackageDirective(packageDirective: FirPackageDirective, parent: FirElement): FirPackageDirective

  fun transformAnonymousFunction(anonymousFunction: FirAnonymousFunction, parent: FirElement): FirStatement

  fun transformAnonymousFunctionExpression(
      anonymousFunctionExpression: FirAnonymousFunctionExpression,
      parent: FirElement
  ): FirStatement

  fun transformAnonymousObject(anonymousObject: FirAnonymousObject, parent: FirElement): FirStatement

  fun transformAnonymousObjectExpression(anonymousObjectExpression: FirAnonymousObjectExpression, parent: FirElement): FirStatement

  fun transformDiagnosticHolder(diagnosticHolder: FirDiagnosticHolder, parent: FirElement): FirDiagnosticHolder

  fun transformImport(import: FirImport, parent: FirElement): FirImport

  fun transformResolvedImport(resolvedImport: FirResolvedImport, parent: FirElement): FirImport

  fun transformErrorImport(errorImport: FirErrorImport, parent: FirElement): FirImport

  fun transformLoop(loop: FirLoop, parent: FirElement): FirStatement

  fun transformErrorLoop(errorLoop: FirErrorLoop, parent: FirElement): FirStatement

  fun transformDoWhileLoop(doWhileLoop: FirDoWhileLoop, parent: FirElement): FirStatement

  fun transformWhileLoop(whileLoop: FirWhileLoop, parent: FirElement): FirStatement

  fun transformBlock(block: FirBlock, parent: FirElement): FirStatement

  fun transformBinaryLogicExpression(binaryLogicExpression: FirBinaryLogicExpression, parent: FirElement): FirStatement

  fun <E : FirTargetElement> transformJump(jump: FirJump<E>, parent: FirElement): FirStatement

  fun transformLoopJump(loopJump: FirLoopJump, parent: FirElement): FirStatement

  fun transformBreakExpression(breakExpression: FirBreakExpression, parent: FirElement): FirStatement

  fun transformContinueExpression(continueExpression: FirContinueExpression, parent: FirElement): FirStatement

  fun transformCatch(catch: FirCatch, parent: FirElement): FirCatch

  fun transformTryExpression(tryExpression: FirTryExpression, parent: FirElement): FirStatement

  fun <T> transformConstExpression(constExpression: FirConstExpression<T>, parent: FirElement): FirStatement

  fun transformTypeProjection(typeProjection: FirTypeProjection, parent: FirElement): FirTypeProjection

  fun transformStarProjection(starProjection: FirStarProjection, parent: FirElement): FirTypeProjection

  fun transformPlaceholderProjection(placeholderProjection: FirPlaceholderProjection, parent: FirElement): FirTypeProjection

  fun transformTypeProjectionWithVariance(
      typeProjectionWithVariance: FirTypeProjectionWithVariance,
      parent: FirElement
  ): FirTypeProjection

  fun transformArgumentList(argumentList: FirArgumentList, parent: FirElement): FirArgumentList

  fun transformCall(call: FirCall, parent: FirElement): FirStatement

  fun transformAnnotation(annotation: FirAnnotation, parent: FirElement): FirStatement

  fun transformAnnotationCall(annotationCall: FirAnnotationCall, parent: FirElement): FirStatement

  fun transformAnnotationArgumentMapping(
      annotationArgumentMapping: FirAnnotationArgumentMapping,
      parent: FirElement
  ): FirAnnotationArgumentMapping

  fun transformErrorAnnotationCall(errorAnnotationCall: FirErrorAnnotationCall, parent: FirElement): FirStatement

  fun transformComparisonExpression(comparisonExpression: FirComparisonExpression, parent: FirElement): FirStatement

  fun transformTypeOperatorCall(typeOperatorCall: FirTypeOperatorCall, parent: FirElement): FirStatement

  fun transformAssignmentOperatorStatement(
      assignmentOperatorStatement: FirAssignmentOperatorStatement,
      parent: FirElement
  ): FirStatement

  fun transformEqualityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall, parent: FirElement): FirStatement

  fun transformWhenExpression(whenExpression: FirWhenExpression, parent: FirElement): FirStatement

  fun transformWhenBranch(whenBranch: FirWhenBranch, parent: FirElement): FirWhenBranch

  fun transformContextReceiverArgumentListOwner(
      contextReceiverArgumentListOwner: FirContextReceiverArgumentListOwner,
      parent: FirElement
  ): FirContextReceiverArgumentListOwner

  fun transformQualifiedAccess(qualifiedAccess: FirQualifiedAccess, parent: FirElement): FirStatement

  fun transformCheckNotNullCall(checkNotNullCall: FirCheckNotNullCall, parent: FirElement): FirStatement

  fun transformElvisExpression(elvisExpression: FirElvisExpression, parent: FirElement): FirStatement

  fun transformArrayOfCall(arrayOfCall: FirArrayOfCall, parent: FirElement): FirStatement

  fun transformAugmentedArraySetCall(augmentedArraySetCall: FirAugmentedArraySetCall, parent: FirElement): FirStatement

  fun transformClassReferenceExpression(classReferenceExpression: FirClassReferenceExpression, parent: FirElement): FirStatement

  fun transformErrorExpression(errorExpression: FirErrorExpression, parent: FirElement): FirStatement

  fun transformErrorFunction(errorFunction: FirErrorFunction, parent: FirElement): FirStatement

  fun transformErrorProperty(errorProperty: FirErrorProperty, parent: FirElement): FirStatement

  fun transformDanglingModifierList(danglingModifierList: FirDanglingModifierList, parent: FirElement): FirDanglingModifierList

  fun transformQualifiedAccessExpression(qualifiedAccessExpression: FirQualifiedAccessExpression, parent: FirElement): FirStatement

  fun transformQualifiedErrorAccessExpression(
      qualifiedErrorAccessExpression: FirQualifiedErrorAccessExpression,
      parent: FirElement
  ): FirStatement

  fun transformPropertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression, parent: FirElement): FirStatement

  fun transformFunctionCall(functionCall: FirFunctionCall, parent: FirElement): FirStatement

  fun transformIntegerLiteralOperatorCall(
      integerLiteralOperatorCall: FirIntegerLiteralOperatorCall,
      parent: FirElement
  ): FirStatement

  fun transformImplicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall, parent: FirElement): FirStatement

  fun transformDelegatedConstructorCall(delegatedConstructorCall: FirDelegatedConstructorCall, parent: FirElement): FirStatement

  fun transformComponentCall(componentCall: FirComponentCall, parent: FirElement): FirStatement

  fun transformCallableReferenceAccess(callableReferenceAccess: FirCallableReferenceAccess, parent: FirElement): FirStatement

  fun transformThisReceiverExpression(thisReceiverExpression: FirThisReceiverExpression, parent: FirElement): FirStatement

  fun transformSmartCastExpression(smartCastExpression: FirSmartCastExpression, parent: FirElement): FirStatement

  fun transformSafeCallExpression(safeCallExpression: FirSafeCallExpression, parent: FirElement): FirStatement

  fun transformCheckedSafeCallSubject(checkedSafeCallSubject: FirCheckedSafeCallSubject, parent: FirElement): FirStatement

  fun transformGetClassCall(getClassCall: FirGetClassCall, parent: FirElement): FirStatement

  fun transformWrappedExpression(wrappedExpression: FirWrappedExpression, parent: FirElement): FirStatement

  fun transformWrappedArgumentExpression(wrappedArgumentExpression: FirWrappedArgumentExpression, parent: FirElement): FirStatement

  fun transformLambdaArgumentExpression(lambdaArgumentExpression: FirLambdaArgumentExpression, parent: FirElement): FirStatement

  fun transformSpreadArgumentExpression(spreadArgumentExpression: FirSpreadArgumentExpression, parent: FirElement): FirStatement

  fun transformNamedArgumentExpression(namedArgumentExpression: FirNamedArgumentExpression, parent: FirElement): FirStatement

  fun transformVarargArgumentsExpression(varargArgumentsExpression: FirVarargArgumentsExpression, parent: FirElement): FirStatement

  fun transformResolvedQualifier(resolvedQualifier: FirResolvedQualifier, parent: FirElement): FirStatement

  fun transformErrorResolvedQualifier(errorResolvedQualifier: FirErrorResolvedQualifier, parent: FirElement): FirStatement

  fun transformResolvedReifiedParameterReference(
      resolvedReifiedParameterReference: FirResolvedReifiedParameterReference,
      parent: FirElement
  ): FirStatement

  fun transformReturnExpression(returnExpression: FirReturnExpression, parent: FirElement): FirStatement

  fun transformStringConcatenationCall(stringConcatenationCall: FirStringConcatenationCall, parent: FirElement): FirStatement

  fun transformThrowExpression(throwExpression: FirThrowExpression, parent: FirElement): FirStatement

  fun transformVariableAssignment(variableAssignment: FirVariableAssignment, parent: FirElement): FirStatement

  fun transformWhenSubjectExpression(whenSubjectExpression: FirWhenSubjectExpression, parent: FirElement): FirStatement

  fun transformWrappedDelegateExpression(wrappedDelegateExpression: FirWrappedDelegateExpression, parent: FirElement): FirStatement

  fun transformNamedReference(namedReference: FirNamedReference, parent: FirElement): FirReference

  fun transformNamedReferenceWithCandidateBase(
      namedReferenceWithCandidateBase: FirNamedReferenceWithCandidateBase,
      parent: FirElement
  ): FirReference

  fun transformErrorNamedReference(errorNamedReference: FirErrorNamedReference, parent: FirElement): FirReference

  fun transformSuperReference(superReference: FirSuperReference, parent: FirElement): FirReference

  fun transformThisReference(thisReference: FirThisReference, parent: FirElement): FirReference

  fun transformControlFlowGraphReference(controlFlowGraphReference: FirControlFlowGraphReference, parent: FirElement): FirReference

  fun transformResolvedNamedReference(resolvedNamedReference: FirResolvedNamedReference, parent: FirElement): FirReference

  fun transformResolvedErrorReference(resolvedErrorReference: FirResolvedErrorReference, parent: FirElement): FirReference

  fun transformDelegateFieldReference(delegateFieldReference: FirDelegateFieldReference, parent: FirElement): FirReference

  fun transformBackingFieldReference(backingFieldReference: FirBackingFieldReference, parent: FirElement): FirReference

  fun transformResolvedCallableReference(resolvedCallableReference: FirResolvedCallableReference, parent: FirElement): FirReference

  fun transformResolvedTypeRef(resolvedTypeRef: FirResolvedTypeRef, parent: FirElement): FirTypeRef

  fun transformErrorTypeRef(errorTypeRef: FirErrorTypeRef, parent: FirElement): FirTypeRef

  fun transformTypeRefWithNullability(typeRefWithNullability: FirTypeRefWithNullability, parent: FirElement): FirTypeRef

  fun transformUserTypeRef(userTypeRef: FirUserTypeRef, parent: FirElement): FirTypeRef

  fun transformDynamicTypeRef(dynamicTypeRef: FirDynamicTypeRef, parent: FirElement): FirTypeRef

  fun transformFunctionTypeRef(functionTypeRef: FirFunctionTypeRef, parent: FirElement): FirTypeRef

  fun transformIntersectionTypeRef(intersectionTypeRef: FirIntersectionTypeRef, parent: FirElement): FirTypeRef

  fun transformImplicitTypeRef(implicitTypeRef: FirImplicitTypeRef, parent: FirElement): FirTypeRef

  fun transformEffectDeclaration(effectDeclaration: FirEffectDeclaration, parent: FirElement): FirEffectDeclaration

  fun transformContractDescription(contractDescription: FirContractDescription, parent: FirElement): FirContractDescription

  fun transformLegacyRawContractDescription(
      legacyRawContractDescription: FirLegacyRawContractDescription,
      parent: FirElement
  ): FirContractDescription

  fun transformRawContractDescription(
      rawContractDescription: FirRawContractDescription,
      parent: FirElement
  ): FirContractDescription

  fun transformResolvedContractDescription(
      resolvedContractDescription: FirResolvedContractDescription,
      parent: FirElement
  ): FirContractDescription

  class Builder(override val session: FirSession) : Transformer, FrontendPlugin.Builder() {
    var annotationContainer: (FirAnnotationContainer, parent: FirElement) -> FirAnnotationContainer = { el, _ -> el }

    override fun transformAnnotationContainer(
        annotationContainer: FirAnnotationContainer,
        parent: FirElement
    ): FirAnnotationContainer =
      annotationContainer(annotationContainer, parent)

    var typeRef: (typeRef: FirTypeRef, parent: FirElement) -> FirTypeRef = { el, _ -> el }

    override fun transformTypeRef(typeRef: FirTypeRef, parent: FirElement): FirTypeRef =
      typeRef(typeRef, parent)

    var typeProjection: (typeProjection: FirTypeProjection, parent: FirElement) -> FirTypeProjection = { el, _ -> el }

    override fun transformTypeProjection(typeProjection: FirTypeProjection, parent: FirElement): FirTypeProjection =
      typeProjection(typeProjection, parent)

    var typeProjectionWithVariance: (typeProjectionWithVariance: FirTypeProjectionWithVariance, parent: FirElement) -> FirTypeProjectionWithVariance =
      { el, _ -> el }

    override fun transformTypeProjectionWithVariance(
        typeProjectionWithVariance: FirTypeProjectionWithVariance,
        parent: FirElement
    ): FirTypeProjectionWithVariance =
      typeProjectionWithVariance(typeProjectionWithVariance, parent)

    var starProjection: (starProjection: FirStarProjection, parent: FirElement) -> FirStarProjection = { el, _ -> el }

    override fun transformStarProjection(starProjection: FirStarProjection, parent: FirElement): FirStarProjection =
      starProjection(starProjection, parent)

    var reference: (reference: FirReference, parent: FirElement) -> FirReference = { el, _ -> el }
    override fun transformReference(reference: FirReference, parent: FirElement): FirReference =
      reference(reference, parent)

    var label: (label: FirLabel, parent: FirElement) -> FirLabel = { el, _ -> el }
    override fun transformLabel(label: FirLabel, parent: FirElement): FirLabel =
      label(label, parent)

    var resolvable: (resolvable: FirResolvable, parent: FirElement) -> FirResolvable = { el, _ -> el }
    override fun transformResolvable(resolvable: FirResolvable, parent: FirElement): FirResolvable =
      resolvable(resolvable, parent)

    var targetElement: (targetElement: FirTargetElement, parent: FirElement) -> FirTargetElement = { el, _ -> el }
    override fun transformTargetElement(targetElement: FirTargetElement, parent: FirElement): FirTargetElement =
      targetElement(targetElement, parent)

    var declarationStatus: (declarationStatus: FirDeclarationStatus, parent: FirElement) -> FirDeclarationStatus = { el, _ -> el }
    override fun transformDeclarationStatus(declarationStatus: FirDeclarationStatus, parent: FirElement): FirDeclarationStatus =
      declarationStatus(declarationStatus, parent)

    var resolvedDeclarationStatus: (resolvedDeclarationStatus: FirResolvedDeclarationStatus, parent: FirElement) -> FirDeclarationStatus =
      { el, _ -> el }

    override fun transformResolvedDeclarationStatus(
        resolvedDeclarationStatus: FirResolvedDeclarationStatus,
        parent: FirElement
    ): FirDeclarationStatus =
      resolvedDeclarationStatus(resolvedDeclarationStatus, parent)

    var controlFlowGraphOwner: (controlFlowGraphOwner: FirControlFlowGraphOwner, parent: FirElement) -> FirControlFlowGraphOwner =
      { el, _ -> el }

    override fun transformControlFlowGraphOwner(
        controlFlowGraphOwner: FirControlFlowGraphOwner,
        parent: FirElement
    ): FirControlFlowGraphOwner =
      controlFlowGraphOwner(controlFlowGraphOwner, parent)

    var statement: (statement: FirStatement, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformStatement(statement: FirStatement, parent: FirElement): FirStatement =
      statement(statement, parent)

    var expression: (expression: FirExpression, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformExpression(expression: FirExpression, parent: FirElement): FirStatement =
      expression(expression, parent)

    var contextReceiver: (contextReceiver: FirContextReceiver, parent: FirElement) -> FirContextReceiver = { el, _ -> el }
    override fun transformContextReceiver(contextReceiver: FirContextReceiver, parent: FirElement): FirContextReceiver =
      contextReceiver(contextReceiver, parent)

    var elementWithResolvePhase: (elementWithResolvePhase: FirElementWithResolvePhase, parent: FirElement) -> FirElementWithResolvePhase =
      { el, _ -> el }

    override fun transformElementWithResolvePhase(
        elementWithResolvePhase: FirElementWithResolvePhase,
        parent: FirElement
    ): FirElementWithResolvePhase =
      elementWithResolvePhase(elementWithResolvePhase, parent)

    var fileAnnotationsContainer: (fileAnnotationsContainer: FirFileAnnotationsContainer, parent: FirElement) -> FirFileAnnotationsContainer =
      { el, _ -> el }

    override fun transformFileAnnotationsContainer(
        fileAnnotationsContainer: FirFileAnnotationsContainer,
        parent: FirElement
    ): FirFileAnnotationsContainer =
      fileAnnotationsContainer(fileAnnotationsContainer, parent)

    var declaration: (declaration: FirDeclaration, parent: FirElement) -> FirDeclaration = { el, _ -> el }
    override fun transformDeclaration(declaration: FirDeclaration, parent: FirElement): FirDeclaration =
      declaration(declaration, parent)

    var typeParameterRefsOwner: (typeParameterRefsOwner: FirTypeParameterRefsOwner, parent: FirElement) -> FirTypeParameterRefsOwner =
      { el, _ -> el }

    override fun transformTypeParameterRefsOwner(
        typeParameterRefsOwner: FirTypeParameterRefsOwner,
        parent: FirElement
    ): FirTypeParameterRefsOwner =
      typeParameterRefsOwner(typeParameterRefsOwner, parent)

    var typeParametersOwner: (typeParametersOwner: FirTypeParametersOwner, parent: FirElement) -> FirTypeParametersOwner =
      { el, _ -> el }

    override fun transformTypeParametersOwner(
        typeParametersOwner: FirTypeParametersOwner,
        parent: FirElement
    ): FirTypeParametersOwner =
      typeParametersOwner(typeParametersOwner, parent)

    var memberDeclaration: (memberDeclaration: FirMemberDeclaration, parent: FirElement) -> FirMemberDeclaration = { el, _ -> el }
    override fun transformMemberDeclaration(memberDeclaration: FirMemberDeclaration, parent: FirElement): FirMemberDeclaration =
      memberDeclaration(memberDeclaration, parent)

    var anonymousInitializer: (anonymousInitializer: FirAnonymousInitializer, parent: FirElement) -> FirAnonymousInitializer =
      { el, _ -> el }

    override fun transformAnonymousInitializer(
        anonymousInitializer: FirAnonymousInitializer,
        parent: FirElement
    ): FirAnonymousInitializer =
      anonymousInitializer(anonymousInitializer, parent)

    var callableDeclaration: (callableDeclaration: FirCallableDeclaration, parent: FirElement) -> FirCallableDeclaration =
      { el, _ -> el }

    override fun transformCallableDeclaration(
        callableDeclaration: FirCallableDeclaration,
        parent: FirElement
    ): FirCallableDeclaration =
      callableDeclaration(callableDeclaration, parent)

    var typeParameterRef: (typeParameterRef: FirTypeParameterRef, parent: FirElement) -> FirTypeParameterRef = { el, _ -> el }
    override fun transformTypeParameterRef(typeParameterRef: FirTypeParameterRef, parent: FirElement): FirTypeParameterRef =
      typeParameterRef(typeParameterRef, parent)

    var typeParameter: (typeParameter: FirTypeParameter, parent: FirElement) -> FirTypeParameterRef = { el, _ -> el }
    override fun transformTypeParameter(typeParameter: FirTypeParameter, parent: FirElement): FirTypeParameterRef =
      typeParameter(typeParameter, parent)

    var variable: (variable: FirVariable, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformVariable(variable: FirVariable, parent: FirElement): FirStatement =
      variable(variable, parent)

    var valueParameter: (valueParameter: FirValueParameter, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformValueParameter(valueParameter: FirValueParameter, parent: FirElement): FirStatement =
      valueParameter(valueParameter, parent)

    var receiverParameter: (receiverParameter: FirReceiverParameter, parent: FirElement) -> FirReceiverParameter = { el, _ -> el }
    override fun transformReceiverParameter(receiverParameter: FirReceiverParameter, parent: FirElement): FirReceiverParameter =
      receiverParameter(receiverParameter, parent)

    var property: (property: FirProperty, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformProperty(property: FirProperty, parent: FirElement): FirStatement =
      property(property, parent)

    var field: (field: FirField, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformField(field: FirField, parent: FirElement): FirStatement =
      field(field, parent)

    var enumEntry: (enumEntry: FirEnumEntry, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformEnumEntry(enumEntry: FirEnumEntry, parent: FirElement): FirStatement =
      enumEntry(enumEntry, parent)

    var functionTypeParameter: (functionTypeParameter: FirFunctionTypeParameter, parent: FirElement) -> FirFunctionTypeParameter =
      { el, _ -> el }

    override fun transformFunctionTypeParameter(
        functionTypeParameter: FirFunctionTypeParameter,
        parent: FirElement
    ): FirFunctionTypeParameter =
      functionTypeParameter(functionTypeParameter, parent)

    var classLikeDeclaration: (classLikeDeclaration: FirClassLikeDeclaration, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformClassLikeDeclaration(classLikeDeclaration: FirClassLikeDeclaration, parent: FirElement): FirStatement =
      classLikeDeclaration(classLikeDeclaration, parent)

    var klass: (klass: FirClass, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformClass(klass: FirClass, parent: FirElement): FirStatement =
      klass(klass, parent)

    var regularClass: (regularClass: FirRegularClass, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformRegularClass(regularClass: FirRegularClass, parent: FirElement): FirStatement =
      regularClass(regularClass, parent)

    var typeAlias: (typeAlias: FirTypeAlias, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformTypeAlias(typeAlias: FirTypeAlias, parent: FirElement): FirStatement =
      typeAlias(typeAlias, parent)

    var function: (function: FirFunction, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformFunction(function: FirFunction, parent: FirElement): FirStatement =
      function(function, parent)

    var contractDescriptionOwner: (contractDescriptionOwner: FirContractDescriptionOwner, parent: FirElement) -> FirContractDescriptionOwner =
      { el, _ -> el }

    override fun transformContractDescriptionOwner(
        contractDescriptionOwner: FirContractDescriptionOwner,
        parent: FirElement
    ): FirContractDescriptionOwner =
      contractDescriptionOwner(contractDescriptionOwner, parent)

    var simpleFunction: (simpleFunction: FirSimpleFunction, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformSimpleFunction(simpleFunction: FirSimpleFunction, parent: FirElement): FirStatement =
      simpleFunction(simpleFunction, parent)

    var propertyAccessor: (propertyAccessor: FirPropertyAccessor, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformPropertyAccessor(propertyAccessor: FirPropertyAccessor, parent: FirElement): FirStatement =
      propertyAccessor(propertyAccessor, parent)

    var backingField: (backingField: FirBackingField, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformBackingField(backingField: FirBackingField, parent: FirElement): FirStatement =
      backingField(backingField, parent)

    var constructor: (constructor: FirConstructor, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformConstructor(constructor: FirConstructor, parent: FirElement): FirStatement =
      constructor(constructor, parent)

    var file: (file: FirFile, parent: FirElement) -> FirFile = { el, _ -> el }
    override fun transformFile(file: FirFile, parent: FirElement): FirFile =
      file(file, parent)

    var script: (script: FirScript, parent: FirElement) -> FirScript = { el, _ -> el }
    override fun transformScript(script: FirScript, parent: FirElement): FirScript =
      script(script, parent)

    var packageDirective: (packageDirective: FirPackageDirective, parent: FirElement) -> FirPackageDirective = { el, _ -> el }
    override fun transformPackageDirective(packageDirective: FirPackageDirective, parent: FirElement): FirPackageDirective =
      packageDirective(packageDirective, parent)

    var anonymousFunction: (anonymousFunction: FirAnonymousFunction, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformAnonymousFunction(anonymousFunction: FirAnonymousFunction, parent: FirElement): FirStatement =
      anonymousFunction(anonymousFunction, parent)

    var anonymousFunctionExpression: (anonymousFunctionExpression: FirAnonymousFunctionExpression, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformAnonymousFunctionExpression(
        anonymousFunctionExpression: FirAnonymousFunctionExpression,
        parent: FirElement
    ): FirStatement =
      anonymousFunctionExpression(anonymousFunctionExpression, parent)

    var anonymousObject: (anonymousObject: FirAnonymousObject, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformAnonymousObject(anonymousObject: FirAnonymousObject, parent: FirElement): FirStatement =
      anonymousObject(anonymousObject, parent)

    var anonymousObjectExpression: (anonymousObjectExpression: FirAnonymousObjectExpression, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformAnonymousObjectExpression(
        anonymousObjectExpression: FirAnonymousObjectExpression,
        parent: FirElement
    ): FirStatement =
      anonymousObjectExpression(anonymousObjectExpression, parent)

    var diagnosticHolder: (diagnosticHolder: FirDiagnosticHolder, parent: FirElement) -> FirDiagnosticHolder = { el, _ -> el }
    override fun transformDiagnosticHolder(diagnosticHolder: FirDiagnosticHolder, parent: FirElement): FirDiagnosticHolder =
      diagnosticHolder(diagnosticHolder, parent)

    var import: (import: FirImport, parent: FirElement) -> FirImport = { el, _ -> el }
    override fun transformImport(import: FirImport, parent: FirElement): FirImport =
      import(import, parent)

    var resolvedImport: (resolvedImport: FirResolvedImport, parent: FirElement) -> FirImport = { el, _ -> el }
    override fun transformResolvedImport(resolvedImport: FirResolvedImport, parent: FirElement): FirImport =
      resolvedImport(resolvedImport, parent)

    var errorImport: (errorImport: FirErrorImport, parent: FirElement) -> FirImport = { el, _ -> el }
    override fun transformErrorImport(errorImport: FirErrorImport, parent: FirElement): FirImport =
      errorImport(errorImport, parent)

    var loop: (loop: FirLoop, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformLoop(loop: FirLoop, parent: FirElement): FirStatement =
      loop(loop, parent)

    var errorLoop: (errorLoop: FirErrorLoop, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformErrorLoop(errorLoop: FirErrorLoop, parent: FirElement): FirStatement =
      errorLoop(errorLoop, parent)

    var doWhileLoop: (doWhileLoop: FirDoWhileLoop, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformDoWhileLoop(doWhileLoop: FirDoWhileLoop, parent: FirElement): FirStatement =
      doWhileLoop(doWhileLoop, parent)

    var whileLoop: (whileLoop: FirWhileLoop, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformWhileLoop(whileLoop: FirWhileLoop, parent: FirElement): FirStatement =
      whileLoop(whileLoop, parent)

    var block: (block: FirBlock, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformBlock(block: FirBlock, parent: FirElement): FirStatement =
      block(block, parent)

    var binaryLogicExpression: (binaryLogicExpression: FirBinaryLogicExpression, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformBinaryLogicExpression(
        binaryLogicExpression: FirBinaryLogicExpression,
        parent: FirElement
    ): FirStatement =
      binaryLogicExpression(binaryLogicExpression, parent)

    var jump: (jump: FirJump<*>, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun <E : FirTargetElement> transformJump(jump: FirJump<E>, parent: FirElement): FirStatement =
      jump(jump, parent)

    var loopJump: (loopJump: FirLoopJump, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformLoopJump(loopJump: FirLoopJump, parent: FirElement): FirStatement =
      loopJump(loopJump, parent)

    var breakExpression: (breakExpression: FirBreakExpression, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformBreakExpression(breakExpression: FirBreakExpression, parent: FirElement): FirStatement =
      breakExpression(breakExpression, parent)

    var continueExpression: (continueExpression: FirContinueExpression, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformContinueExpression(continueExpression: FirContinueExpression, parent: FirElement): FirStatement =
      continueExpression(continueExpression, parent)

    var catch: (catch: FirCatch, parent: FirElement) -> FirCatch = { el, _ -> el }
    override fun transformCatch(catch: FirCatch, parent: FirElement): FirCatch =
      catch(catch, parent)

    var tryExpression: (tryExpression: FirTryExpression, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformTryExpression(tryExpression: FirTryExpression, parent: FirElement): FirStatement =
      tryExpression(tryExpression, parent)

    var constExpression: (constExpression: FirConstExpression<*>, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun <T> transformConstExpression(constExpression: FirConstExpression<T>, parent: FirElement): FirStatement =
      constExpression(constExpression, parent)

    var placeholderProjection: (placeholderProjection: FirPlaceholderProjection, parent: FirElement) -> FirTypeProjection =
      { el, _ -> el }

    override fun transformPlaceholderProjection(
        placeholderProjection: FirPlaceholderProjection,
        parent: FirElement
    ): FirTypeProjection =
      placeholderProjection(placeholderProjection, parent)

    var argumentList: (argumentList: FirArgumentList, parent: FirElement) -> FirArgumentList = { el, _ -> el }
    override fun transformArgumentList(argumentList: FirArgumentList, parent: FirElement): FirArgumentList =
      argumentList(argumentList, parent)

    var call: (call: FirCall, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformCall(call: FirCall, parent: FirElement): FirStatement =
      call(call, parent)

    var annotation: (annotation: FirAnnotation, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformAnnotation(annotation: FirAnnotation, parent: FirElement): FirStatement =
      annotation(annotation, parent)

    var annotationCall: (annotationCall: FirAnnotationCall, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformAnnotationCall(annotationCall: FirAnnotationCall, parent: FirElement): FirStatement =
      annotationCall(annotationCall, parent)

    var annotationArgumentMapping: (annotationArgumentMapping: FirAnnotationArgumentMapping, parent: FirElement) -> FirAnnotationArgumentMapping =
      { el, _ -> el }

    override fun transformAnnotationArgumentMapping(
        annotationArgumentMapping: FirAnnotationArgumentMapping,
        parent: FirElement
    ): FirAnnotationArgumentMapping =
      annotationArgumentMapping(annotationArgumentMapping, parent)

    var errorAnnotationCall: (errorAnnotationCall: FirErrorAnnotationCall, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformErrorAnnotationCall(errorAnnotationCall: FirErrorAnnotationCall, parent: FirElement): FirStatement =
      errorAnnotationCall(errorAnnotationCall, parent)

    var comparisonExpression: (comparisonExpression: FirComparisonExpression, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformComparisonExpression(comparisonExpression: FirComparisonExpression, parent: FirElement): FirStatement =
      comparisonExpression(comparisonExpression, parent)

    var typeOperatorCall: (typeOperatorCall: FirTypeOperatorCall, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformTypeOperatorCall(typeOperatorCall: FirTypeOperatorCall, parent: FirElement): FirStatement =
      typeOperatorCall(typeOperatorCall, parent)

    var assignmentOperatorStatement: (assignmentOperatorStatement: FirAssignmentOperatorStatement, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformAssignmentOperatorStatement(
        assignmentOperatorStatement: FirAssignmentOperatorStatement,
        parent: FirElement
    ): FirStatement =
      assignmentOperatorStatement(assignmentOperatorStatement, parent)

    var equalityOperatorCall: (equalityOperatorCall: FirEqualityOperatorCall, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformEqualityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall, parent: FirElement): FirStatement =
      equalityOperatorCall(equalityOperatorCall, parent)

    var whenExpression: (whenExpression: FirWhenExpression, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformWhenExpression(whenExpression: FirWhenExpression, parent: FirElement): FirStatement =
      whenExpression(whenExpression, parent)

    var whenBranch: (whenBranch: FirWhenBranch, parent: FirElement) -> FirWhenBranch = { el, _ -> el }
    override fun transformWhenBranch(whenBranch: FirWhenBranch, parent: FirElement): FirWhenBranch =
      whenBranch(whenBranch, parent)

    var contextReceiverArgumentListOwner: (contextReceiverArgumentListOwner: FirContextReceiverArgumentListOwner, parent: FirElement) -> FirContextReceiverArgumentListOwner =
      { el, _ -> el }

    override fun transformContextReceiverArgumentListOwner(
        contextReceiverArgumentListOwner: FirContextReceiverArgumentListOwner,
        parent: FirElement
    ): FirContextReceiverArgumentListOwner =
      contextReceiverArgumentListOwner(contextReceiverArgumentListOwner, parent)

    var qualifiedAccess: (qualifiedAccess: FirQualifiedAccess, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformQualifiedAccess(qualifiedAccess: FirQualifiedAccess, parent: FirElement): FirStatement =
      qualifiedAccess(qualifiedAccess, parent)

    var checkNotNullCall: (checkNotNullCall: FirCheckNotNullCall, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformCheckNotNullCall(checkNotNullCall: FirCheckNotNullCall, parent: FirElement): FirStatement =
      checkNotNullCall(checkNotNullCall, parent)

    var elvisExpression: (elvisExpression: FirElvisExpression, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformElvisExpression(elvisExpression: FirElvisExpression, parent: FirElement): FirStatement =
      elvisExpression(elvisExpression, parent)

    var arrayOfCall: (arrayOfCall: FirArrayOfCall, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformArrayOfCall(arrayOfCall: FirArrayOfCall, parent: FirElement): FirStatement =
      arrayOfCall(arrayOfCall, parent)

    var augmentedArraySetCall: (augmentedArraySetCall: FirAugmentedArraySetCall, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformAugmentedArraySetCall(
        augmentedArraySetCall: FirAugmentedArraySetCall,
        parent: FirElement
    ): FirStatement =
      augmentedArraySetCall(augmentedArraySetCall, parent)

    var classReferenceExpression: (classReferenceExpression: FirClassReferenceExpression, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformClassReferenceExpression(
        classReferenceExpression: FirClassReferenceExpression,
        parent: FirElement
    ): FirStatement =
      classReferenceExpression(classReferenceExpression, parent)

    var errorExpression: (errorExpression: FirErrorExpression, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformErrorExpression(errorExpression: FirErrorExpression, parent: FirElement): FirStatement =
      errorExpression(errorExpression, parent)

    var errorFunction: (errorFunction: FirErrorFunction, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformErrorFunction(errorFunction: FirErrorFunction, parent: FirElement): FirStatement =
      errorFunction(errorFunction, parent)

    var errorProperty: (errorProperty: FirErrorProperty, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformErrorProperty(errorProperty: FirErrorProperty, parent: FirElement): FirStatement =
      errorProperty(errorProperty, parent)

    var danglingModifierList: (danglingModifierList: FirDanglingModifierList, parent: FirElement) -> FirDanglingModifierList =
      { el, _ -> el }

    override fun transformDanglingModifierList(
        danglingModifierList: FirDanglingModifierList,
        parent: FirElement
    ): FirDanglingModifierList =
      danglingModifierList(danglingModifierList, parent)

    var qualifiedAccessExpression: (qualifiedAccessExpression: FirQualifiedAccessExpression, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformQualifiedAccessExpression(
        qualifiedAccessExpression: FirQualifiedAccessExpression,
        parent: FirElement
    ): FirStatement =
      qualifiedAccessExpression(qualifiedAccessExpression, parent)

    var qualifiedErrorAccessExpression: (qualifiedErrorAccessExpression: FirQualifiedErrorAccessExpression, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformQualifiedErrorAccessExpression(
        qualifiedErrorAccessExpression: FirQualifiedErrorAccessExpression,
        parent: FirElement
    ): FirStatement =
      qualifiedErrorAccessExpression(qualifiedErrorAccessExpression, parent)

    var propertyAccessExpression: (propertyAccessExpression: FirPropertyAccessExpression, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformPropertyAccessExpression(
        propertyAccessExpression: FirPropertyAccessExpression,
        parent: FirElement
    ): FirStatement =
      propertyAccessExpression(propertyAccessExpression, parent)

    var functionCall: (functionCall: FirFunctionCall, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformFunctionCall(functionCall: FirFunctionCall, parent: FirElement): FirStatement =
      functionCall(functionCall, parent)

    var integerLiteralOperatorCall: (integerLiteralOperatorCall: FirIntegerLiteralOperatorCall, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformIntegerLiteralOperatorCall(
        integerLiteralOperatorCall: FirIntegerLiteralOperatorCall,
        parent: FirElement
    ): FirStatement =
      integerLiteralOperatorCall(integerLiteralOperatorCall, parent)

    var implicitInvokeCall: (implicitInvokeCall: FirImplicitInvokeCall, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformImplicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall, parent: FirElement): FirStatement =
      implicitInvokeCall(implicitInvokeCall, parent)

    var delegatedConstructorCall: (delegatedConstructorCall: FirDelegatedConstructorCall, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformDelegatedConstructorCall(
        delegatedConstructorCall: FirDelegatedConstructorCall,
        parent: FirElement
    ): FirStatement =
      delegatedConstructorCall(delegatedConstructorCall, parent)

    var componentCall: (componentCall: FirComponentCall, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformComponentCall(componentCall: FirComponentCall, parent: FirElement): FirStatement =
      componentCall(componentCall, parent)

    var callableReferenceAccess: (callableReferenceAccess: FirCallableReferenceAccess, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformCallableReferenceAccess(
        callableReferenceAccess: FirCallableReferenceAccess,
        parent: FirElement
    ): FirStatement =
      callableReferenceAccess(callableReferenceAccess, parent)

    var thisReceiverExpression: (thisReceiverExpression: FirThisReceiverExpression, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformThisReceiverExpression(
        thisReceiverExpression: FirThisReceiverExpression,
        parent: FirElement
    ): FirStatement =
      thisReceiverExpression(thisReceiverExpression, parent)

    var smartCastExpression: (smartCastExpression: FirSmartCastExpression, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformSmartCastExpression(smartCastExpression: FirSmartCastExpression, parent: FirElement): FirStatement =
      smartCastExpression(smartCastExpression, parent)

    var safeCallExpression: (safeCallExpression: FirSafeCallExpression, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformSafeCallExpression(safeCallExpression: FirSafeCallExpression, parent: FirElement): FirStatement =
      safeCallExpression(safeCallExpression, parent)

    var checkedSafeCallSubject: (checkedSafeCallSubject: FirCheckedSafeCallSubject, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformCheckedSafeCallSubject(
        checkedSafeCallSubject: FirCheckedSafeCallSubject,
        parent: FirElement
    ): FirStatement =
      checkedSafeCallSubject(checkedSafeCallSubject, parent)

    var getClassCall: (getClassCall: FirGetClassCall, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformGetClassCall(getClassCall: FirGetClassCall, parent: FirElement): FirStatement =
      getClassCall(getClassCall, parent)

    var wrappedExpression: (wrappedExpression: FirWrappedExpression, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformWrappedExpression(wrappedExpression: FirWrappedExpression, parent: FirElement): FirStatement =
      wrappedExpression(wrappedExpression, parent)

    var wrappedArgumentExpression: (wrappedArgumentExpression: FirWrappedArgumentExpression, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformWrappedArgumentExpression(
        wrappedArgumentExpression: FirWrappedArgumentExpression,
        parent: FirElement
    ): FirStatement =
      wrappedArgumentExpression(wrappedArgumentExpression, parent)

    var lambdaArgumentExpression: (lambdaArgumentExpression: FirLambdaArgumentExpression, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformLambdaArgumentExpression(
        lambdaArgumentExpression: FirLambdaArgumentExpression,
        parent: FirElement
    ): FirStatement =
      lambdaArgumentExpression(lambdaArgumentExpression, parent)

    var spreadArgumentExpression: (spreadArgumentExpression: FirSpreadArgumentExpression, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformSpreadArgumentExpression(
        spreadArgumentExpression: FirSpreadArgumentExpression,
        parent: FirElement
    ): FirStatement =
      spreadArgumentExpression(spreadArgumentExpression, parent)

    var namedArgumentExpression: (namedArgumentExpression: FirNamedArgumentExpression, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformNamedArgumentExpression(
        namedArgumentExpression: FirNamedArgumentExpression,
        parent: FirElement
    ): FirStatement =
      namedArgumentExpression(namedArgumentExpression, parent)

    var varargArgumentsExpression: (varargArgumentsExpression: FirVarargArgumentsExpression, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformVarargArgumentsExpression(
        varargArgumentsExpression: FirVarargArgumentsExpression,
        parent: FirElement
    ): FirStatement =
      varargArgumentsExpression(varargArgumentsExpression, parent)

    var resolvedQualifier: (resolvedQualifier: FirResolvedQualifier, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformResolvedQualifier(resolvedQualifier: FirResolvedQualifier, parent: FirElement): FirStatement =
      resolvedQualifier(resolvedQualifier, parent)

    var errorResolvedQualifier: (errorResolvedQualifier: FirErrorResolvedQualifier, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformErrorResolvedQualifier(
        errorResolvedQualifier: FirErrorResolvedQualifier,
        parent: FirElement
    ): FirStatement =
      errorResolvedQualifier(errorResolvedQualifier, parent)

    var resolvedReifiedParameterReference: (resolvedReifiedParameterReference: FirResolvedReifiedParameterReference, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformResolvedReifiedParameterReference(
        resolvedReifiedParameterReference: FirResolvedReifiedParameterReference,
        parent: FirElement
    ): FirStatement =
      resolvedReifiedParameterReference(resolvedReifiedParameterReference, parent)

    var returnExpression: (returnExpression: FirReturnExpression, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformReturnExpression(returnExpression: FirReturnExpression, parent: FirElement): FirStatement =
      returnExpression(returnExpression, parent)

    var stringConcatenationCall: (stringConcatenationCall: FirStringConcatenationCall, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformStringConcatenationCall(
        stringConcatenationCall: FirStringConcatenationCall,
        parent: FirElement
    ): FirStatement =
      stringConcatenationCall(stringConcatenationCall, parent)

    var throwExpression: (throwExpression: FirThrowExpression, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformThrowExpression(throwExpression: FirThrowExpression, parent: FirElement): FirStatement =
      throwExpression(throwExpression, parent)

    var variableAssignment: (variableAssignment: FirVariableAssignment, parent: FirElement) -> FirStatement = { el, _ -> el }
    override fun transformVariableAssignment(variableAssignment: FirVariableAssignment, parent: FirElement): FirStatement =
      variableAssignment(variableAssignment, parent)

    var whenSubjectExpression: (whenSubjectExpression: FirWhenSubjectExpression, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformWhenSubjectExpression(
        whenSubjectExpression: FirWhenSubjectExpression,
        parent: FirElement
    ): FirStatement =
      whenSubjectExpression(whenSubjectExpression, parent)

    var wrappedDelegateExpression: (wrappedDelegateExpression: FirWrappedDelegateExpression, parent: FirElement) -> FirStatement =
      { el, _ -> el }

    override fun transformWrappedDelegateExpression(
        wrappedDelegateExpression: FirWrappedDelegateExpression,
        parent: FirElement
    ): FirStatement =
      wrappedDelegateExpression(wrappedDelegateExpression, parent)

    var namedReference: (namedReference: FirNamedReference, parent: FirElement) -> FirReference = { el, _ -> el }
    override fun transformNamedReference(namedReference: FirNamedReference, parent: FirElement): FirReference =
      namedReference(namedReference, parent)

    var namedReferenceWithCandidateBase: (namedReferenceWithCandidateBase: FirNamedReferenceWithCandidateBase, parent: FirElement) -> FirReference =
      { el, _ -> el }

    override fun transformNamedReferenceWithCandidateBase(
        namedReferenceWithCandidateBase: FirNamedReferenceWithCandidateBase,
        parent: FirElement
    ): FirReference =
      namedReferenceWithCandidateBase(namedReferenceWithCandidateBase, parent)

    var errorNamedReference: (errorNamedReference: FirErrorNamedReference, parent: FirElement) -> FirReference = { el, _ -> el }
    override fun transformErrorNamedReference(errorNamedReference: FirErrorNamedReference, parent: FirElement): FirReference =
      errorNamedReference(errorNamedReference, parent)

    var superReference: (superReference: FirSuperReference, parent: FirElement) -> FirReference = { el, _ -> el }
    override fun transformSuperReference(superReference: FirSuperReference, parent: FirElement): FirReference =
      superReference(superReference, parent)

    var thisReference: (thisReference: FirThisReference, parent: FirElement) -> FirReference = { el, _ -> el }
    override fun transformThisReference(thisReference: FirThisReference, parent: FirElement): FirReference =
      thisReference(thisReference, parent)

    var controlFlowGraphReference: (controlFlowGraphReference: FirControlFlowGraphReference, parent: FirElement) -> FirReference =
      { el, _ -> el }

    override fun transformControlFlowGraphReference(
        controlFlowGraphReference: FirControlFlowGraphReference,
        parent: FirElement
    ): FirReference =
      controlFlowGraphReference(controlFlowGraphReference, parent)

    var resolvedNamedReference: (resolvedNamedReference: FirResolvedNamedReference, parent: FirElement) -> FirReference =
      { el, _ -> el }

    override fun transformResolvedNamedReference(
        resolvedNamedReference: FirResolvedNamedReference,
        parent: FirElement
    ): FirReference =
      resolvedNamedReference(resolvedNamedReference, parent)

    var resolvedErrorReference: (resolvedErrorReference: FirResolvedErrorReference, parent: FirElement) -> FirReference =
      { el, _ -> el }

    override fun transformResolvedErrorReference(
        resolvedErrorReference: FirResolvedErrorReference,
        parent: FirElement
    ): FirReference =
      resolvedErrorReference(resolvedErrorReference, parent)

    var delegateFieldReference: (delegateFieldReference: FirDelegateFieldReference, parent: FirElement) -> FirReference =
      { el, _ -> el }

    override fun transformDelegateFieldReference(
        delegateFieldReference: FirDelegateFieldReference,
        parent: FirElement
    ): FirReference =
      delegateFieldReference(delegateFieldReference, parent)

    var backingFieldReference: (backingFieldReference: FirBackingFieldReference, parent: FirElement) -> FirReference =
      { el, _ -> el }

    override fun transformBackingFieldReference(
        backingFieldReference: FirBackingFieldReference,
        parent: FirElement
    ): FirReference =
      backingFieldReference(backingFieldReference, parent)

    var resolvedCallableReference: (resolvedCallableReference: FirResolvedCallableReference, parent: FirElement) -> FirReference =
      { el, _ -> el }

    override fun transformResolvedCallableReference(
        resolvedCallableReference: FirResolvedCallableReference,
        parent: FirElement
    ): FirReference =
      resolvedCallableReference(resolvedCallableReference, parent)

    var resolvedTypeRef: (resolvedTypeRef: FirResolvedTypeRef, parent: FirElement) -> FirTypeRef = { el, _ -> el }
    override fun transformResolvedTypeRef(resolvedTypeRef: FirResolvedTypeRef, parent: FirElement): FirTypeRef =
      resolvedTypeRef(resolvedTypeRef, parent)

    var errorTypeRef: (errorTypeRef: FirErrorTypeRef, parent: FirElement) -> FirTypeRef = { el, _ -> el }
    override fun transformErrorTypeRef(errorTypeRef: FirErrorTypeRef, parent: FirElement): FirTypeRef =
      errorTypeRef(errorTypeRef, parent)

    var typeRefWithNullability: (typeRefWithNullability: FirTypeRefWithNullability, parent: FirElement) -> FirTypeRef =
      { el, _ -> el }

    override fun transformTypeRefWithNullability(
        typeRefWithNullability: FirTypeRefWithNullability,
        parent: FirElement
    ): FirTypeRef =
      typeRefWithNullability(typeRefWithNullability, parent)

    var userTypeRef: (userTypeRef: FirUserTypeRef, parent: FirElement) -> FirTypeRef = { el, _ -> el }
    override fun transformUserTypeRef(userTypeRef: FirUserTypeRef, parent: FirElement): FirTypeRef =
      userTypeRef(userTypeRef, parent)

    var dynamicTypeRef: (dynamicTypeRef: FirDynamicTypeRef, parent: FirElement) -> FirTypeRef = { el, _ -> el }
    override fun transformDynamicTypeRef(dynamicTypeRef: FirDynamicTypeRef, parent: FirElement): FirTypeRef =
      dynamicTypeRef(dynamicTypeRef, parent)

    var functionTypeRef: (functionTypeRef: FirFunctionTypeRef, parent: FirElement) -> FirTypeRef = { el, _ -> el }
    override fun transformFunctionTypeRef(functionTypeRef: FirFunctionTypeRef, parent: FirElement): FirTypeRef =
      functionTypeRef(functionTypeRef, parent)

    var intersectionTypeRef: (intersectionTypeRef: FirIntersectionTypeRef, parent: FirElement) -> FirTypeRef = { el, _ -> el }
    override fun transformIntersectionTypeRef(intersectionTypeRef: FirIntersectionTypeRef, parent: FirElement): FirTypeRef =
      intersectionTypeRef(intersectionTypeRef, parent)

    var implicitTypeRef: (implicitTypeRef: FirImplicitTypeRef, parent: FirElement) -> FirTypeRef = { el, _ -> el }
    override fun transformImplicitTypeRef(implicitTypeRef: FirImplicitTypeRef, parent: FirElement): FirTypeRef =
      implicitTypeRef(implicitTypeRef, parent)

    var effectDeclaration: (effectDeclaration: FirEffectDeclaration, parent: FirElement) -> FirEffectDeclaration = { el, _ -> el }
    override fun transformEffectDeclaration(effectDeclaration: FirEffectDeclaration, parent: FirElement): FirEffectDeclaration =
      effectDeclaration(effectDeclaration, parent)

    var contractDescription: (contractDescription: FirContractDescription, parent: FirElement) -> FirContractDescription =
      { el, _ -> el }

    override fun transformContractDescription(
        contractDescription: FirContractDescription,
        parent: FirElement
    ): FirContractDescription =
      contractDescription(contractDescription, parent)

    var legacyRawContractDescription: (legacyRawContractDescription: FirLegacyRawContractDescription, parent: FirElement) -> FirContractDescription =
      { el, _ -> el }

    override fun transformLegacyRawContractDescription(
        legacyRawContractDescription: FirLegacyRawContractDescription,
        parent: FirElement
    ): FirContractDescription =
      legacyRawContractDescription(legacyRawContractDescription, parent)

    var rawContractDescription: (rawContractDescription: FirRawContractDescription, parent: FirElement) -> FirContractDescription =
      { el, _ -> el }

    override fun transformRawContractDescription(
        rawContractDescription: FirRawContractDescription,
        parent: FirElement
    ): FirContractDescription =
      rawContractDescription(rawContractDescription, parent)

    var resolvedContractDescription: (resolvedContractDescription: FirResolvedContractDescription, parent: FirElement) -> FirContractDescription =
      { el, _ -> el }

    override fun transformResolvedContractDescription(
        resolvedContractDescription: FirResolvedContractDescription,
        parent: FirElement
    ): FirContractDescription =
      resolvedContractDescription(resolvedContractDescription, parent)

  }

  companion object {
    operator fun <D> invoke(init: Builder.() -> Unit): (FirSession) -> Transformer =
      { Builder(it).apply(init) }
  }

}
