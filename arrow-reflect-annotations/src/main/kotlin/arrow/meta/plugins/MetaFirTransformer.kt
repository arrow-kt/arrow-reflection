package arrow.meta.plugins

import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.contracts.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.diagnostics.FirDiagnosticHolder
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visitors.FirTransformer

class MetaFirTransformer(val builder: Transformer) : FirTransformer<FirElement>() {
  override fun <E : FirElement> transformElement(element: E, data: FirElement): E {
    element.transformChildren(this, data)
    return element
  }

  override fun transformAnnotation(annotation: FirAnnotation, data: FirElement): FirStatement {
    return builder.transformAnnotation(annotation, data)
  }

  override fun transformAnnotationArgumentMapping(
      annotationArgumentMapping: FirAnnotationArgumentMapping,
      data: FirElement
  ): FirAnnotationArgumentMapping {
    return builder.transformAnnotationArgumentMapping(annotationArgumentMapping, data)
  }

  override fun transformAnnotationCall(annotationCall: FirAnnotationCall, data: FirElement): FirStatement {
    return builder.transformAnnotationCall(annotationCall, data)
  }

  override fun transformAnnotationContainer(
      annotationContainer: FirAnnotationContainer,
      data: FirElement
  ): FirAnnotationContainer {
    return builder.transformAnnotationContainer(annotationContainer, data)
  }

  override fun transformAnonymousFunction(anonymousFunction: FirAnonymousFunction, data: FirElement): FirStatement {
    return builder.transformAnonymousFunction(anonymousFunction, data)
  }

  override fun transformAnonymousFunctionExpression(
      anonymousFunctionExpression: FirAnonymousFunctionExpression,
      data: FirElement
  ): FirStatement {
    return builder.transformAnonymousFunctionExpression(anonymousFunctionExpression, data)
  }

  override fun transformAnonymousInitializer(
      anonymousInitializer: FirAnonymousInitializer,
      data: FirElement
  ): FirAnonymousInitializer {
    return builder.transformAnonymousInitializer(anonymousInitializer, data)
  }

  override fun transformAnonymousObject(anonymousObject: FirAnonymousObject, data: FirElement): FirStatement {
    return builder.transformAnonymousObject(anonymousObject, data)
  }

  override fun transformAnonymousObjectExpression(
      anonymousObjectExpression: FirAnonymousObjectExpression,
      data: FirElement
  ): FirStatement {
    return builder.transformAnonymousObjectExpression(anonymousObjectExpression, data)
  }

  override fun transformArgumentList(argumentList: FirArgumentList, data: FirElement): FirArgumentList {
    return builder.transformArgumentList(argumentList, data)
  }

  override fun transformArrayOfCall(arrayOfCall: FirArrayOfCall, data: FirElement): FirStatement {
    return builder.transformArrayOfCall(arrayOfCall, data)
  }

  override fun transformAssignmentOperatorStatement(
      assignmentOperatorStatement: FirAssignmentOperatorStatement,
      data: FirElement
  ): FirStatement {
    return builder.transformAssignmentOperatorStatement(assignmentOperatorStatement, data)
  }

  override fun transformAugmentedArraySetCall(
      augmentedArraySetCall: FirAugmentedArraySetCall,
      data: FirElement
  ): FirStatement {
    return builder.transformAugmentedArraySetCall(augmentedArraySetCall, data)
  }

  override fun transformBackingField(backingField: FirBackingField, data: FirElement): FirStatement {
    return builder.transformBackingField(backingField, data)
  }

  override fun transformBackingFieldReference(
      backingFieldReference: FirBackingFieldReference,
      data: FirElement
  ): FirReference {
    return builder.transformBackingFieldReference(backingFieldReference, data)
  }

  override fun transformBinaryLogicExpression(
      binaryLogicExpression: FirBinaryLogicExpression,
      data: FirElement
  ): FirStatement {
    return builder.transformBinaryLogicExpression(binaryLogicExpression, data)
  }

  override fun transformBlock(block: FirBlock, data: FirElement): FirStatement {
    return builder.transformBlock(block, data)
  }

  override fun transformBreakExpression(breakExpression: FirBreakExpression, data: FirElement): FirStatement {
    return builder.transformBreakExpression(breakExpression, data)
  }

  override fun transformCall(call: FirCall, data: FirElement): FirStatement {
    return builder.transformCall(call, data)
  }

  override fun transformCallableDeclaration(
      callableDeclaration: FirCallableDeclaration,
      data: FirElement
  ): FirCallableDeclaration {
    return builder.transformCallableDeclaration(callableDeclaration, data)
  }

  override fun transformCallableReferenceAccess(
      callableReferenceAccess: FirCallableReferenceAccess,
      data: FirElement
  ): FirStatement {
    return builder.transformCallableReferenceAccess(callableReferenceAccess, data)
  }

  override fun transformCatch(catch: FirCatch, data: FirElement): FirCatch {
    return builder.transformCatch(catch, data)
  }

  override fun transformCheckNotNullCall(checkNotNullCall: FirCheckNotNullCall, data: FirElement): FirStatement {
    return builder.transformCheckNotNullCall(checkNotNullCall, data)
  }

  override fun transformCheckedSafeCallSubject(
      checkedSafeCallSubject: FirCheckedSafeCallSubject,
      data: FirElement
  ): FirStatement {
    return builder.transformCheckedSafeCallSubject(checkedSafeCallSubject, data)
  }

  override fun transformClass(klass: FirClass, data: FirElement): FirStatement {
    return builder.transformClass(klass, data)
  }

  override fun transformClassLikeDeclaration(classLikeDeclaration: FirClassLikeDeclaration, data: FirElement): FirStatement {
    return builder.transformClassLikeDeclaration(classLikeDeclaration, data)
  }

  override fun transformClassReferenceExpression(
      classReferenceExpression: FirClassReferenceExpression,
      data: FirElement
  ): FirStatement {
    return builder.transformClassReferenceExpression(classReferenceExpression, data)
  }

  override fun transformComparisonExpression(comparisonExpression: FirComparisonExpression, data: FirElement): FirStatement {
    return builder.transformComparisonExpression(comparisonExpression, data)
  }

  override fun transformComponentCall(componentCall: FirComponentCall, data: FirElement): FirStatement {
    return builder.transformComponentCall(componentCall, data)
  }

  override fun <T> transformConstExpression(constExpression: FirConstExpression<T>, data: FirElement): FirStatement {
    return builder.transformConstExpression(constExpression, data)
  }

  override fun transformConstructor(constructor: FirConstructor, data: FirElement): FirStatement {
    return builder.transformConstructor(constructor, data)
  }

  override fun transformContextReceiver(contextReceiver: FirContextReceiver, data: FirElement): FirContextReceiver {
    return builder.transformContextReceiver(contextReceiver, data)
  }

  override fun transformContextReceiverArgumentListOwner(
      contextReceiverArgumentListOwner: FirContextReceiverArgumentListOwner,
      data: FirElement
  ): FirContextReceiverArgumentListOwner {
    return builder.transformContextReceiverArgumentListOwner(contextReceiverArgumentListOwner, data)
  }

  override fun transformContinueExpression(continueExpression: FirContinueExpression, data: FirElement): FirStatement {
    return builder.transformContinueExpression(continueExpression, data)
  }

  override fun transformContractDescription(
      contractDescription: FirContractDescription,
      data: FirElement
  ): FirContractDescription {
    return builder.transformContractDescription(contractDescription, data)
  }

  override fun transformContractDescriptionOwner(
      contractDescriptionOwner: FirContractDescriptionOwner,
      data: FirElement
  ): FirContractDescriptionOwner {
    return builder.transformContractDescriptionOwner(contractDescriptionOwner, data)
  }

  override fun transformControlFlowGraphOwner(
      controlFlowGraphOwner: FirControlFlowGraphOwner,
      data: FirElement
  ): FirControlFlowGraphOwner {
    return builder.transformControlFlowGraphOwner(controlFlowGraphOwner, data)
  }

  override fun transformControlFlowGraphReference(
      controlFlowGraphReference: FirControlFlowGraphReference,
      data: FirElement
  ): FirReference {
    return builder.transformControlFlowGraphReference(controlFlowGraphReference, data)
  }

  override fun transformDanglingModifierList(
      danglingModifierList: FirDanglingModifierList,
      data: FirElement
  ): FirDanglingModifierList {
    return builder.transformDanglingModifierList(danglingModifierList, data)
  }

  override fun transformDeclaration(declaration: FirDeclaration, data: FirElement): FirDeclaration {
    return builder.transformDeclaration(declaration, data)
  }

  override fun transformDeclarationStatus(declarationStatus: FirDeclarationStatus, data: FirElement): FirDeclarationStatus {
    return builder.transformDeclarationStatus(declarationStatus, data)
  }

  override fun transformDelegateFieldReference(
      delegateFieldReference: FirDelegateFieldReference,
      data: FirElement
  ): FirReference {
    return builder.transformDelegateFieldReference(delegateFieldReference, data)
  }

  override fun transformDelegatedConstructorCall(
      delegatedConstructorCall: FirDelegatedConstructorCall,
      data: FirElement
  ): FirStatement {
    return builder.transformDelegatedConstructorCall(delegatedConstructorCall, data)
  }

  override fun transformDiagnosticHolder(diagnosticHolder: FirDiagnosticHolder, data: FirElement): FirDiagnosticHolder {
    return builder.transformDiagnosticHolder(diagnosticHolder, data)
  }

  override fun transformDoWhileLoop(doWhileLoop: FirDoWhileLoop, data: FirElement): FirStatement {
    return builder.transformDoWhileLoop(doWhileLoop, data)
  }

  override fun transformDynamicTypeRef(dynamicTypeRef: FirDynamicTypeRef, data: FirElement): FirTypeRef {
    return builder.transformDynamicTypeRef(dynamicTypeRef, data)
  }

  override fun transformEffectDeclaration(effectDeclaration: FirEffectDeclaration, data: FirElement): FirEffectDeclaration {
    return builder.transformEffectDeclaration(effectDeclaration, data)
  }

  override fun transformElementWithResolvePhase(
      elementWithResolvePhase: FirElementWithResolvePhase,
      data: FirElement
  ): FirElementWithResolvePhase {
    return builder.transformElementWithResolvePhase(elementWithResolvePhase, data)
  }

  override fun transformElvisExpression(elvisExpression: FirElvisExpression, data: FirElement): FirStatement {
    return builder.transformElvisExpression(elvisExpression, data)
  }

  override fun transformEnumEntry(enumEntry: FirEnumEntry, data: FirElement): FirStatement {
    return builder.transformEnumEntry(enumEntry, data)
  }

  override fun transformEqualityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall, data: FirElement): FirStatement {
    return builder.transformEqualityOperatorCall(equalityOperatorCall, data)
  }

  override fun transformErrorAnnotationCall(errorAnnotationCall: FirErrorAnnotationCall, data: FirElement): FirStatement {
    return builder.transformErrorAnnotationCall(errorAnnotationCall, data)
  }

  override fun transformErrorExpression(errorExpression: FirErrorExpression, data: FirElement): FirStatement {
    return builder.transformErrorExpression(errorExpression, data)
  }

  override fun transformErrorFunction(errorFunction: FirErrorFunction, data: FirElement): FirStatement {
    return builder.transformErrorFunction(errorFunction, data)
  }

  override fun transformErrorImport(errorImport: FirErrorImport, data: FirElement): FirImport {
    return builder.transformErrorImport(errorImport, data)
  }

  override fun transformErrorLoop(errorLoop: FirErrorLoop, data: FirElement): FirStatement {
    return builder.transformErrorLoop(errorLoop, data)
  }

  override fun transformErrorNamedReference(errorNamedReference: FirErrorNamedReference, data: FirElement): FirReference {
    return builder.transformErrorNamedReference(errorNamedReference, data)
  }

  override fun transformErrorProperty(errorProperty: FirErrorProperty, data: FirElement): FirStatement {
    return builder.transformErrorProperty(errorProperty, data)
  }

  override fun transformErrorResolvedQualifier(
      errorResolvedQualifier: FirErrorResolvedQualifier,
      data: FirElement
  ): FirStatement {
    return builder.transformErrorResolvedQualifier(errorResolvedQualifier, data)
  }

  override fun transformErrorTypeRef(errorTypeRef: FirErrorTypeRef, data: FirElement): FirTypeRef {
    return builder.transformErrorTypeRef(errorTypeRef, data)
  }

  override fun transformExpression(expression: FirExpression, data: FirElement): FirStatement {
    return builder.transformExpression(expression, data)
  }

  override fun transformField(field: FirField, data: FirElement): FirStatement {
    return builder.transformField(field, data)
  }

  override fun transformFile(file: FirFile, data: FirElement): FirFile {
    return builder.transformFile(file, data)
  }

  override fun transformFileAnnotationsContainer(
      fileAnnotationsContainer: FirFileAnnotationsContainer,
      data: FirElement
  ): FirFileAnnotationsContainer {
    return builder.transformFileAnnotationsContainer(fileAnnotationsContainer, data)
  }

  override fun transformFunction(function: FirFunction, data: FirElement): FirStatement {
    return builder.transformFunction(function, data)
  }

  override fun transformFunctionCall(functionCall: FirFunctionCall, data: FirElement): FirStatement {
    return builder.transformFunctionCall(functionCall, data)
  }

  override fun transformFunctionTypeParameter(
      functionTypeParameter: FirFunctionTypeParameter,
      data: FirElement
  ): FirFunctionTypeParameter {
    return builder.transformFunctionTypeParameter(functionTypeParameter, data)
  }

  override fun transformFunctionTypeRef(functionTypeRef: FirFunctionTypeRef, data: FirElement): FirTypeRef {
    return builder.transformFunctionTypeRef(functionTypeRef, data)
  }

  override fun transformGetClassCall(getClassCall: FirGetClassCall, data: FirElement): FirStatement {
    return builder.transformGetClassCall(getClassCall, data)
  }

  override fun transformImplicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall, data: FirElement): FirStatement {
    return builder.transformImplicitInvokeCall(implicitInvokeCall, data)
  }

  override fun transformImplicitTypeRef(implicitTypeRef: FirImplicitTypeRef, data: FirElement): FirTypeRef {
    return builder.transformImplicitTypeRef(implicitTypeRef, data)
  }

  override fun transformImport(import: FirImport, data: FirElement): FirImport {
    return builder.transformImport(import, data)
  }

  override fun transformIntegerLiteralOperatorCall(
      integerLiteralOperatorCall: FirIntegerLiteralOperatorCall,
      data: FirElement
  ): FirStatement {
    return builder.transformIntegerLiteralOperatorCall(integerLiteralOperatorCall, data)
  }

  override fun transformIntersectionTypeRef(intersectionTypeRef: FirIntersectionTypeRef, data: FirElement): FirTypeRef {
    return builder.transformIntersectionTypeRef(intersectionTypeRef, data)
  }

  override fun <E : FirTargetElement> transformJump(jump: FirJump<E>, data: FirElement): FirStatement {
    return builder.transformJump(jump, data)
  }

  override fun transformLabel(label: FirLabel, data: FirElement): FirLabel {
    return builder.transformLabel(label, data)
  }

  override fun transformLambdaArgumentExpression(
      lambdaArgumentExpression: FirLambdaArgumentExpression,
      data: FirElement
  ): FirStatement {
    return builder.transformLambdaArgumentExpression(lambdaArgumentExpression, data)
  }

  override fun transformLegacyRawContractDescription(
      legacyRawContractDescription: FirLegacyRawContractDescription,
      data: FirElement
  ): FirContractDescription {
    return builder.transformLegacyRawContractDescription(legacyRawContractDescription, data)
  }

  override fun transformLoop(loop: FirLoop, data: FirElement): FirStatement {
    return builder.transformLoop(loop, data)
  }

  override fun transformLoopJump(loopJump: FirLoopJump, data: FirElement): FirStatement {
    return builder.transformLoopJump(loopJump, data)
  }

  override fun transformMemberDeclaration(memberDeclaration: FirMemberDeclaration, data: FirElement): FirMemberDeclaration {
    return builder.transformMemberDeclaration(memberDeclaration, data)
  }

  override fun transformNamedArgumentExpression(
      namedArgumentExpression: FirNamedArgumentExpression,
      data: FirElement
  ): FirStatement {
    return builder.transformNamedArgumentExpression(namedArgumentExpression, data)
  }

  override fun transformNamedReference(namedReference: FirNamedReference, data: FirElement): FirReference {
    return builder.transformNamedReference(namedReference, data)
  }

  override fun transformNamedReferenceWithCandidateBase(
      namedReferenceWithCandidateBase: FirNamedReferenceWithCandidateBase,
      data: FirElement
  ): FirReference {
    return builder.transformNamedReferenceWithCandidateBase(namedReferenceWithCandidateBase, data)
  }

  override fun transformPackageDirective(packageDirective: FirPackageDirective, data: FirElement): FirPackageDirective {
    return builder.transformPackageDirective(packageDirective, data)
  }

  override fun transformPlaceholderProjection(
      placeholderProjection: FirPlaceholderProjection,
      data: FirElement
  ): FirTypeProjection {
    return builder.transformPlaceholderProjection(placeholderProjection, data)
  }

  override fun transformProperty(property: FirProperty, data: FirElement): FirStatement {
    return builder.transformProperty(property, data)
  }

  override fun transformPropertyAccessExpression(
      propertyAccessExpression: FirPropertyAccessExpression,
      data: FirElement
  ): FirStatement {
    return builder.transformPropertyAccessExpression(propertyAccessExpression, data)
  }

  override fun transformPropertyAccessor(propertyAccessor: FirPropertyAccessor, data: FirElement): FirStatement {
    return builder.transformPropertyAccessor(propertyAccessor, data)
  }

  override fun transformQualifiedAccess(qualifiedAccess: FirQualifiedAccess, data: FirElement): FirStatement {
    return builder.transformQualifiedAccess(qualifiedAccess, data)
  }

  override fun transformQualifiedAccessExpression(
      qualifiedAccessExpression: FirQualifiedAccessExpression,
      data: FirElement
  ): FirStatement {
    return builder.transformQualifiedAccessExpression(qualifiedAccessExpression, data)
  }

  override fun transformQualifiedErrorAccessExpression(
      qualifiedErrorAccessExpression: FirQualifiedErrorAccessExpression,
      data: FirElement
  ): FirStatement {
    return builder.transformQualifiedErrorAccessExpression(qualifiedErrorAccessExpression, data)
  }

  override fun transformRawContractDescription(
      rawContractDescription: FirRawContractDescription,
      data: FirElement
  ): FirContractDescription {
    return builder.transformRawContractDescription(rawContractDescription, data)
  }

  override fun transformReceiverParameter(receiverParameter: FirReceiverParameter, data: FirElement): FirReceiverParameter {
    return builder.transformReceiverParameter(receiverParameter, data)
  }

  override fun transformReference(reference: FirReference, data: FirElement): FirReference {
    return builder.transformReference(reference, data)
  }

  override fun transformRegularClass(regularClass: FirRegularClass, data: FirElement): FirStatement {
    return builder.transformRegularClass(regularClass, data)
  }

  override fun transformResolvable(resolvable: FirResolvable, data: FirElement): FirResolvable {
    return builder.transformResolvable(resolvable, data)
  }

  override fun transformResolvedCallableReference(
      resolvedCallableReference: FirResolvedCallableReference,
      data: FirElement
  ): FirReference {
    return builder.transformResolvedCallableReference(resolvedCallableReference, data)
  }

  override fun transformResolvedContractDescription(
      resolvedContractDescription: FirResolvedContractDescription,
      data: FirElement
  ): FirContractDescription {
    return builder.transformResolvedContractDescription(resolvedContractDescription, data)
  }

  override fun transformResolvedDeclarationStatus(
      resolvedDeclarationStatus: FirResolvedDeclarationStatus,
      data: FirElement
  ): FirDeclarationStatus {
    return builder.transformResolvedDeclarationStatus(resolvedDeclarationStatus, data)
  }

  override fun transformResolvedErrorReference(
      resolvedErrorReference: FirResolvedErrorReference,
      data: FirElement
  ): FirReference {
    return builder.transformResolvedErrorReference(resolvedErrorReference, data)
  }

  override fun transformResolvedImport(resolvedImport: FirResolvedImport, data: FirElement): FirImport {
    return builder.transformResolvedImport(resolvedImport, data)
  }

  override fun transformResolvedNamedReference(
      resolvedNamedReference: FirResolvedNamedReference,
      data: FirElement
  ): FirReference {
    return builder.transformResolvedNamedReference(resolvedNamedReference, data)
  }

  override fun transformResolvedQualifier(resolvedQualifier: FirResolvedQualifier, data: FirElement): FirStatement {
    return builder.transformResolvedQualifier(resolvedQualifier, data)
  }

  override fun transformResolvedReifiedParameterReference(
      resolvedReifiedParameterReference: FirResolvedReifiedParameterReference,
      data: FirElement
  ): FirStatement {
    return builder.transformResolvedReifiedParameterReference(resolvedReifiedParameterReference, data)
  }

  override fun transformResolvedTypeRef(resolvedTypeRef: FirResolvedTypeRef, data: FirElement): FirTypeRef {
    return builder.transformResolvedTypeRef(resolvedTypeRef, data)
  }

  override fun transformReturnExpression(returnExpression: FirReturnExpression, data: FirElement): FirStatement {
    return builder.transformReturnExpression(returnExpression, data)
  }

  override fun transformSafeCallExpression(safeCallExpression: FirSafeCallExpression, data: FirElement): FirStatement {
    return builder.transformSafeCallExpression(safeCallExpression, data)
  }

  override fun transformScript(script: FirScript, data: FirElement): FirScript {
    return builder.transformScript(script, data)
  }

  override fun transformSimpleFunction(simpleFunction: FirSimpleFunction, data: FirElement): FirStatement {
    return builder.transformSimpleFunction(simpleFunction, data)
  }

  override fun transformSmartCastExpression(smartCastExpression: FirSmartCastExpression, data: FirElement): FirStatement {
    return builder.transformSmartCastExpression(smartCastExpression, data)
  }

  override fun transformSpreadArgumentExpression(
      spreadArgumentExpression: FirSpreadArgumentExpression,
      data: FirElement
  ): FirStatement {
    return builder.transformSpreadArgumentExpression(spreadArgumentExpression, data)
  }

  override fun transformStarProjection(starProjection: FirStarProjection, data: FirElement): FirTypeProjection {
    return builder.transformStarProjection(starProjection, data)
  }

  override fun transformStatement(statement: FirStatement, data: FirElement): FirStatement {
    return builder.transformStatement(statement, data)
  }

  override fun transformStringConcatenationCall(
      stringConcatenationCall: FirStringConcatenationCall,
      data: FirElement
  ): FirStatement {
    return builder.transformStringConcatenationCall(stringConcatenationCall, data)
  }

  override fun transformSuperReference(superReference: FirSuperReference, data: FirElement): FirReference {
    return builder.transformSuperReference(superReference, data)
  }

  override fun transformTargetElement(targetElement: FirTargetElement, data: FirElement): FirTargetElement {
    return builder.transformTargetElement(targetElement, data)
  }

  override fun transformThisReceiverExpression(
      thisReceiverExpression: FirThisReceiverExpression,
      data: FirElement
  ): FirStatement {
    return builder.transformThisReceiverExpression(thisReceiverExpression, data)
  }

  override fun transformThisReference(thisReference: FirThisReference, data: FirElement): FirReference {
    return builder.transformThisReference(thisReference, data)
  }

  override fun transformThrowExpression(throwExpression: FirThrowExpression, data: FirElement): FirStatement {
    return builder.transformThrowExpression(throwExpression, data)
  }

  override fun transformTryExpression(tryExpression: FirTryExpression, data: FirElement): FirStatement {
    return builder.transformTryExpression(tryExpression, data)
  }

  override fun transformTypeAlias(typeAlias: FirTypeAlias, data: FirElement): FirStatement {
    return builder.transformTypeAlias(typeAlias, data)
  }

  override fun transformTypeOperatorCall(typeOperatorCall: FirTypeOperatorCall, data: FirElement): FirStatement {
    return builder.transformTypeOperatorCall(typeOperatorCall, data)
  }

  override fun transformTypeParameter(typeParameter: FirTypeParameter, data: FirElement): FirTypeParameterRef {
    return builder.transformTypeParameter(typeParameter, data)
  }

  override fun transformTypeParameterRef(typeParameterRef: FirTypeParameterRef, data: FirElement): FirTypeParameterRef {
    return builder.transformTypeParameterRef(typeParameterRef, data)
  }

  override fun transformTypeParameterRefsOwner(
      typeParameterRefsOwner: FirTypeParameterRefsOwner,
      data: FirElement
  ): FirTypeParameterRefsOwner {
    return builder.transformTypeParameterRefsOwner(typeParameterRefsOwner, data)
  }

  override fun transformTypeParametersOwner(
      typeParametersOwner: FirTypeParametersOwner,
      data: FirElement
  ): FirTypeParametersOwner {
    return builder.transformTypeParametersOwner(typeParametersOwner, data)
  }

  override fun transformTypeProjection(typeProjection: FirTypeProjection, data: FirElement): FirTypeProjection {
    return builder.transformTypeProjection(typeProjection, data)
  }

  override fun transformTypeProjectionWithVariance(
      typeProjectionWithVariance: FirTypeProjectionWithVariance,
      data: FirElement
  ): FirTypeProjection {
    return builder.transformTypeProjectionWithVariance(typeProjectionWithVariance, data)
  }

  override fun transformTypeRef(typeRef: FirTypeRef, data: FirElement): FirTypeRef {
    return builder.transformTypeRef(typeRef, data)
  }

  override fun transformTypeRefWithNullability(
      typeRefWithNullability: FirTypeRefWithNullability,
      data: FirElement
  ): FirTypeRef {
    return builder.transformTypeRefWithNullability(typeRefWithNullability, data)
  }

  override fun transformUserTypeRef(userTypeRef: FirUserTypeRef, data: FirElement): FirTypeRef {
    return builder.transformUserTypeRef(userTypeRef, data)
  }

  override fun transformValueParameter(valueParameter: FirValueParameter, data: FirElement): FirStatement {
    return builder.transformValueParameter(valueParameter, data)
  }

  override fun transformVarargArgumentsExpression(
      varargArgumentsExpression: FirVarargArgumentsExpression,
      data: FirElement
  ): FirStatement {
    return builder.transformVarargArgumentsExpression(varargArgumentsExpression, data)
  }

  override fun transformVariable(variable: FirVariable, data: FirElement): FirStatement {
    return builder.transformVariable(variable, data)
  }

  override fun transformVariableAssignment(variableAssignment: FirVariableAssignment, data: FirElement): FirStatement {
    return builder.transformVariableAssignment(variableAssignment, data)
  }

  override fun transformWhenBranch(whenBranch: FirWhenBranch, data: FirElement): FirWhenBranch {
    return builder.transformWhenBranch(whenBranch, data)
  }

  override fun transformWhenExpression(whenExpression: FirWhenExpression, data: FirElement): FirStatement {
    return builder.transformWhenExpression(whenExpression, data)
  }

  override fun transformWhenSubjectExpression(
      whenSubjectExpression: FirWhenSubjectExpression,
      data: FirElement
  ): FirStatement {
    return builder.transformWhenSubjectExpression(whenSubjectExpression, data)
  }

  override fun transformWhileLoop(whileLoop: FirWhileLoop, data: FirElement): FirStatement {
    return builder.transformWhileLoop(whileLoop, data)
  }

  override fun transformWrappedArgumentExpression(
      wrappedArgumentExpression: FirWrappedArgumentExpression,
      data: FirElement
  ): FirStatement {
    return builder.transformWrappedArgumentExpression(wrappedArgumentExpression, data)
  }

  override fun transformWrappedDelegateExpression(
      wrappedDelegateExpression: FirWrappedDelegateExpression,
      data: FirElement
  ): FirStatement {
    return builder.transformWrappedDelegateExpression(wrappedDelegateExpression, data)
  }

  override fun transformWrappedExpression(wrappedExpression: FirWrappedExpression, data: FirElement): FirStatement {
    return builder.transformWrappedExpression(wrappedExpression, data)
  }
}
