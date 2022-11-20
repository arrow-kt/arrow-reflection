package arrow.reflect.compiler.plugin.fir.transformer

import arrow.meta.TemplateCompiler
import arrow.meta.module.impl.arrow.meta.FirMetaContext
import arrow.reflect.compiler.plugin.targets.MetaTarget
import arrow.reflect.compiler.plugin.targets.MetagenerationTarget
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.contracts.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.diagnostics.FirDiagnosticHolder
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.references.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visitors.FirTransformer

class Transformer(
  private val session: FirSession, val templateCompiler: TemplateCompiler, val metaTargets: List<MetaTarget>
) : FirTransformer<Unit>() {

  val metaContext = FirMetaContext(templateCompiler, session)

  internal inline fun <reified In, reified Out> invokeMeta(methodName: String, arg: In): Out? {
    val args = listOf(In::class)
    val retType = Out::class
    return MetaTarget.find(methodName, MetagenerationTarget.Fir, args, retType, metaTargets)?.let { target ->
      val result = target.method.invoke(target.companion.objectInstance, metaContext, arg)
      result as? Out
    }
  }

  override fun <E : FirElement> transformElement(element: E, data: Unit): E {
    element.transformChildren(this, data)
    return element
  }

  override fun transformAnnotation(annotation: FirAnnotation, data: Unit): FirStatement =
    invokeMeta("annotation", annotation) ?: super.transformAnnotation(annotation, data)

  override fun transformAnnotationArgumentMapping(
    annotationArgumentMapping: FirAnnotationArgumentMapping, data: Unit
  ): FirAnnotationArgumentMapping =
    invokeMeta("annotationArgumentMapping", annotationArgumentMapping) ?: super.transformAnnotationArgumentMapping(
      annotationArgumentMapping, data
    )

  override fun transformAnnotationCall(annotationCall: FirAnnotationCall, data: Unit): FirStatement =
    invokeMeta("annotationCall", annotationCall) ?: super.transformAnnotationCall(annotationCall, data)

  override fun transformAnnotationContainer(
    annotationContainer: FirAnnotationContainer, data: Unit
  ): FirAnnotationContainer =
    invokeMeta("annotationContainer", annotationContainer) ?: super.transformAnnotationContainer(
      annotationContainer, data
    )

  override fun transformAnonymousFunction(anonymousFunction: FirAnonymousFunction, data: Unit): FirStatement =
    invokeMeta("anonymousFunction", anonymousFunction) ?: super.transformAnonymousFunction(anonymousFunction, data)

  override fun transformAnonymousFunctionExpression(
    anonymousFunctionExpression: FirAnonymousFunctionExpression, data: Unit
  ): FirStatement = invokeMeta("anonymousFunctionExpression", anonymousFunctionExpression)
    ?: super.transformAnonymousFunctionExpression(anonymousFunctionExpression, data)

  override fun transformAnonymousInitializer(
    anonymousInitializer: FirAnonymousInitializer, data: Unit
  ): FirAnonymousInitializer = invokeMeta("anonymousInitializer", anonymousInitializer)
    ?: super.transformAnonymousInitializer(anonymousInitializer, data)

  override fun transformAnonymousObject(anonymousObject: FirAnonymousObject, data: Unit): FirStatement =
    invokeMeta("anonymousObject", anonymousObject) ?: super.transformAnonymousObject(anonymousObject, data)

  override fun transformAnonymousObjectExpression(
    anonymousObjectExpression: FirAnonymousObjectExpression, data: Unit
  ): FirStatement =
    invokeMeta("anonymousObjectExpression", anonymousObjectExpression) ?: super.transformAnonymousObjectExpression(
      anonymousObjectExpression, data
    )

  override fun transformArgumentList(argumentList: FirArgumentList, data: Unit): FirArgumentList =
    invokeMeta("argumentList", argumentList) ?: super.transformArgumentList(argumentList, data)

  override fun transformArrayOfCall(arrayOfCall: FirArrayOfCall, data: Unit): FirStatement =
    invokeMeta("arrayOfCall", arrayOfCall) ?: super.transformArrayOfCall(arrayOfCall, data)

  override fun transformAssignmentOperatorStatement(
    assignmentOperatorStatement: FirAssignmentOperatorStatement, data: Unit
  ): FirStatement = invokeMeta("assignmentOperatorStatement", assignmentOperatorStatement)
    ?: super.transformAssignmentOperatorStatement(assignmentOperatorStatement, data)

  override fun transformAugmentedArraySetCall(
    augmentedArraySetCall: FirAugmentedArraySetCall, data: Unit
  ): FirStatement = invokeMeta("augmentedArraySetCall", augmentedArraySetCall) ?: super.transformAugmentedArraySetCall(
    augmentedArraySetCall, data
  )

  override fun transformBackingField(backingField: FirBackingField, data: Unit): FirStatement =
    invokeMeta("backingField", backingField) ?: super.transformBackingField(backingField, data)

  override fun transformBackingFieldReference(
    backingFieldReference: FirBackingFieldReference, data: Unit
  ): FirReference = invokeMeta("backingFieldReference", backingFieldReference) ?: super.transformBackingFieldReference(
    backingFieldReference, data
  )

  override fun transformBinaryLogicExpression(
    binaryLogicExpression: FirBinaryLogicExpression, data: Unit
  ): FirStatement = invokeMeta("binaryLogicExpression", binaryLogicExpression) ?: super.transformBinaryLogicExpression(
    binaryLogicExpression, data
  )

  override fun transformBlock(block: FirBlock, data: Unit): FirStatement =
    invokeMeta("block", block) ?: super.transformBlock(block, data)

  override fun transformBreakExpression(breakExpression: FirBreakExpression, data: Unit): FirStatement =
    invokeMeta("breakExpression", breakExpression) ?: super.transformBreakExpression(breakExpression, data)

  override fun transformCall(call: FirCall, data: Unit): FirStatement =
    invokeMeta("call", call) ?: super.transformCall(call, data)

  override fun transformCallableDeclaration(
    callableDeclaration: FirCallableDeclaration, data: Unit
  ): FirCallableDeclaration =
    invokeMeta("callableDeclaration", callableDeclaration) ?: super.transformCallableDeclaration(
      callableDeclaration, data
    )

  override fun transformCallableReferenceAccess(
    callableReferenceAccess: FirCallableReferenceAccess, data: Unit
  ): FirStatement =
    invokeMeta("callableReferenceAccess", callableReferenceAccess) ?: super.transformCallableReferenceAccess(
      callableReferenceAccess, data
    )

  override fun transformCatch(catch: FirCatch, data: Unit): FirCatch =
    invokeMeta("catch", catch) ?: super.transformCatch(catch, data)

  override fun transformCheckNotNullCall(checkNotNullCall: FirCheckNotNullCall, data: Unit): FirStatement =
    invokeMeta("checkNotNullCall", checkNotNullCall) ?: super.transformCheckNotNullCall(checkNotNullCall, data)

  override fun transformCheckedSafeCallSubject(
    checkedSafeCallSubject: FirCheckedSafeCallSubject, data: Unit
  ): FirStatement =
    invokeMeta("checkedSafeCallSubject", checkedSafeCallSubject) ?: super.transformCheckedSafeCallSubject(
      checkedSafeCallSubject, data
    )

  override fun transformClass(klass: FirClass, data: Unit): FirStatement =
    invokeMeta("klass", klass) ?: super.transformClass(klass, data)

  override fun transformClassLikeDeclaration(classLikeDeclaration: FirClassLikeDeclaration, data: Unit): FirStatement =
    invokeMeta("classLikeDeclaration", classLikeDeclaration)
      ?: super.transformClassLikeDeclaration(classLikeDeclaration, data)

  override fun transformClassReferenceExpression(
    classReferenceExpression: FirClassReferenceExpression, data: Unit
  ): FirStatement =
    invokeMeta("classReferenceExpression", classReferenceExpression) ?: super.transformClassReferenceExpression(
      classReferenceExpression, data
    )

  override fun transformComparisonExpression(comparisonExpression: FirComparisonExpression, data: Unit): FirStatement =
    invokeMeta("comparisonExpression", comparisonExpression)
      ?: super.transformComparisonExpression(comparisonExpression, data)

  override fun transformComponentCall(componentCall: FirComponentCall, data: Unit): FirStatement =
    invokeMeta("componentCall", componentCall) ?: super.transformComponentCall(componentCall, data)

  override fun <T> transformConstExpression(constExpression: FirConstExpression<T>, data: Unit): FirStatement =
    invokeMeta("constExpression", constExpression) ?: super.transformConstExpression(constExpression, data)

  override fun transformConstructor(constructor: FirConstructor, data: Unit): FirStatement =
    invokeMeta("constructor", constructor) ?: super.transformConstructor(constructor, data)

  override fun transformContextReceiver(contextReceiver: FirContextReceiver, data: Unit): FirContextReceiver =
    invokeMeta("contextReceiver", contextReceiver) ?: super.transformContextReceiver(contextReceiver, data)

  override fun transformContextReceiverArgumentListOwner(
    contextReceiverArgumentListOwner: FirContextReceiverArgumentListOwner, data: Unit
  ): FirContextReceiverArgumentListOwner =
    invokeMeta("contextReceiverArgumentListOwner", contextReceiverArgumentListOwner)
      ?: super.transformContextReceiverArgumentListOwner(contextReceiverArgumentListOwner, data)

  override fun transformContinueExpression(continueExpression: FirContinueExpression, data: Unit): FirStatement =
    invokeMeta("continueExpression", continueExpression) ?: super.transformContinueExpression(continueExpression, data)

  override fun transformContractDescription(
    contractDescription: FirContractDescription, data: Unit
  ): FirContractDescription =
    invokeMeta("contractDescription", contractDescription) ?: super.transformContractDescription(
      contractDescription, data
    )

  override fun transformContractDescriptionOwner(
    contractDescriptionOwner: FirContractDescriptionOwner, data: Unit
  ): FirContractDescriptionOwner =
    invokeMeta("contractDescriptionOwner", contractDescriptionOwner) ?: super.transformContractDescriptionOwner(
      contractDescriptionOwner, data
    )

  override fun transformControlFlowGraphOwner(
    controlFlowGraphOwner: FirControlFlowGraphOwner, data: Unit
  ): FirControlFlowGraphOwner =
    invokeMeta("controlFlowGraphOwner", controlFlowGraphOwner) ?: super.transformControlFlowGraphOwner(
      controlFlowGraphOwner, data
    )

  override fun transformControlFlowGraphReference(
    controlFlowGraphReference: FirControlFlowGraphReference, data: Unit
  ): FirReference =
    invokeMeta("controlFlowGraphReference", controlFlowGraphReference) ?: super.transformControlFlowGraphReference(
      controlFlowGraphReference, data
    )

  override fun transformDeclaration(declaration: FirDeclaration, data: Unit): FirDeclaration =
    invokeMeta("declaration", declaration) ?: super.transformDeclaration(declaration, data)

  override fun transformDeclarationStatus(declarationStatus: FirDeclarationStatus, data: Unit): FirDeclarationStatus =
    invokeMeta("declarationStatus", declarationStatus) ?: super.transformDeclarationStatus(declarationStatus, data)

  override fun transformDelegateFieldReference(
    delegateFieldReference: FirDelegateFieldReference, data: Unit
  ): FirReference =
    invokeMeta("delegateFieldReference", delegateFieldReference) ?: super.transformDelegateFieldReference(
      delegateFieldReference, data
    )

  override fun transformDelegatedConstructorCall(
    delegatedConstructorCall: FirDelegatedConstructorCall, data: Unit
  ): FirStatement =
    invokeMeta("delegatedConstructorCall", delegatedConstructorCall) ?: super.transformDelegatedConstructorCall(
      delegatedConstructorCall, data
    )

  override fun transformDiagnosticHolder(diagnosticHolder: FirDiagnosticHolder, data: Unit): FirDiagnosticHolder =
    invokeMeta("diagnosticHolder", diagnosticHolder) ?: super.transformDiagnosticHolder(diagnosticHolder, data)

  override fun transformDoWhileLoop(doWhileLoop: FirDoWhileLoop, data: Unit): FirStatement =
    invokeMeta("doWhileLoop", doWhileLoop) ?: super.transformDoWhileLoop(doWhileLoop, data)

  override fun transformDynamicTypeRef(dynamicTypeRef: FirDynamicTypeRef, data: Unit): FirTypeRef =
    invokeMeta("dynamicTypeRef", dynamicTypeRef) ?: super.transformDynamicTypeRef(dynamicTypeRef, data)

  override fun transformEffectDeclaration(effectDeclaration: FirEffectDeclaration, data: Unit): FirEffectDeclaration =
    invokeMeta("effectDeclaration", effectDeclaration) ?: super.transformEffectDeclaration(effectDeclaration, data)

  override fun transformElvisExpression(elvisExpression: FirElvisExpression, data: Unit): FirStatement =
    invokeMeta("elvisExpression", elvisExpression) ?: super.transformElvisExpression(elvisExpression, data)

  override fun transformEnumEntry(enumEntry: FirEnumEntry, data: Unit): FirStatement =
    invokeMeta("enumEntry", enumEntry) ?: super.transformEnumEntry(enumEntry, data)

  override fun transformEqualityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall, data: Unit): FirStatement =
    invokeMeta("equalityOperatorCall", equalityOperatorCall)
      ?: super.transformEqualityOperatorCall(equalityOperatorCall, data)

  override fun transformErrorAnnotationCall(errorAnnotationCall: FirErrorAnnotationCall, data: Unit): FirStatement =
    invokeMeta("errorAnnotationCall", errorAnnotationCall) ?: super.transformErrorAnnotationCall(
      errorAnnotationCall, data
    )

  override fun transformErrorExpression(errorExpression: FirErrorExpression, data: Unit): FirStatement =
    invokeMeta("errorExpression", errorExpression) ?: super.transformErrorExpression(errorExpression, data)

  override fun transformErrorFunction(errorFunction: FirErrorFunction, data: Unit): FirStatement =
    invokeMeta("errorFunction", errorFunction) ?: super.transformErrorFunction(errorFunction, data)

  override fun transformErrorImport(errorImport: FirErrorImport, data: Unit): FirImport =
    invokeMeta("errorImport", errorImport) ?: super.transformErrorImport(errorImport, data)

  override fun transformErrorLoop(errorLoop: FirErrorLoop, data: Unit): FirStatement =
    invokeMeta("errorLoop", errorLoop) ?: super.transformErrorLoop(errorLoop, data)

  override fun transformErrorNamedReference(errorNamedReference: FirErrorNamedReference, data: Unit): FirReference =
    invokeMeta("errorNamedReference", errorNamedReference) ?: super.transformErrorNamedReference(
      errorNamedReference, data
    )

  override fun transformErrorProperty(errorProperty: FirErrorProperty, data: Unit): FirStatement =
    invokeMeta("errorProperty", errorProperty) ?: super.transformErrorProperty(errorProperty, data)

  override fun transformErrorResolvedQualifier(
    errorResolvedQualifier: FirErrorResolvedQualifier, data: Unit
  ): FirStatement =
    invokeMeta("errorResolvedQualifier", errorResolvedQualifier) ?: super.transformErrorResolvedQualifier(
      errorResolvedQualifier, data
    )

  override fun transformErrorTypeRef(errorTypeRef: FirErrorTypeRef, data: Unit): FirTypeRef =
    invokeMeta("errorTypeRef", errorTypeRef) ?: super.transformErrorTypeRef(errorTypeRef, data)

  override fun transformExpression(expression: FirExpression, data: Unit): FirStatement =
    invokeMeta("expression", expression) ?: super.transformExpression(expression, data)

  override fun transformField(field: FirField, data: Unit): FirStatement =
    invokeMeta("field", field) ?: super.transformField(field, data)

  override fun transformFile(file: FirFile, data: Unit): FirFile =
    invokeMeta("file", file) ?: super.transformFile(file, data)

  override fun transformFunction(function: FirFunction, data: Unit): FirStatement =
    invokeMeta("function", function) ?: super.transformFunction(function, data)

  override fun transformFunctionCall(functionCall: FirFunctionCall, data: Unit): FirStatement =
    invokeMeta("functionCall", functionCall) ?: super.transformFunctionCall(functionCall, data)

  override fun transformFunctionTypeRef(functionTypeRef: FirFunctionTypeRef, data: Unit): FirTypeRef =
    invokeMeta("functionTypeRef", functionTypeRef) ?: super.transformFunctionTypeRef(functionTypeRef, data)

  override fun transformGetClassCall(getClassCall: FirGetClassCall, data: Unit): FirStatement =
    invokeMeta("getClassCall", getClassCall) ?: super.transformGetClassCall(getClassCall, data)

  override fun transformImplicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall, data: Unit): FirStatement =
    invokeMeta("implicitInvokeCall", implicitInvokeCall) ?: super.transformImplicitInvokeCall(implicitInvokeCall, data)

  override fun transformImplicitTypeRef(implicitTypeRef: FirImplicitTypeRef, data: Unit): FirTypeRef =
    invokeMeta("implicitTypeRef", implicitTypeRef) ?: super.transformImplicitTypeRef(implicitTypeRef, data)

  override fun transformImport(import: FirImport, data: Unit): FirImport =
    invokeMeta("import", import) ?: super.transformImport(import, data)

  override fun transformIntegerLiteralOperatorCall(
    integerLiteralOperatorCall: FirIntegerLiteralOperatorCall, data: Unit
  ): FirStatement =
    invokeMeta("integerLiteralOperatorCall", integerLiteralOperatorCall) ?: super.transformIntegerLiteralOperatorCall(
      integerLiteralOperatorCall, data
    )

  override fun transformIntersectionTypeRef(intersectionTypeRef: FirIntersectionTypeRef, data: Unit): FirTypeRef =
    invokeMeta("intersectionTypeRef", intersectionTypeRef) ?: super.transformIntersectionTypeRef(
      intersectionTypeRef, data
    )

  override fun <E : FirTargetElement> transformJump(jump: FirJump<E>, data: Unit): FirStatement =
    invokeMeta("jump", jump) ?: super.transformJump(jump, data)

  override fun transformLabel(label: FirLabel, data: Unit): FirLabel =
    invokeMeta("label", label) ?: super.transformLabel(label, data)

  override fun transformLambdaArgumentExpression(
    lambdaArgumentExpression: FirLambdaArgumentExpression, data: Unit
  ): FirStatement =
    invokeMeta("lambdaArgumentExpression", lambdaArgumentExpression) ?: super.transformLambdaArgumentExpression(
      lambdaArgumentExpression, data
    )

  override fun transformLegacyRawContractDescription(
    legacyRawContractDescription: FirLegacyRawContractDescription, data: Unit
  ): FirContractDescription = invokeMeta("legacyRawContractDescription", legacyRawContractDescription)
    ?: super.transformLegacyRawContractDescription(legacyRawContractDescription, data)

  override fun transformLoop(loop: FirLoop, data: Unit): FirStatement =
    invokeMeta("loop", loop) ?: super.transformLoop(loop, data)

  override fun transformLoopJump(loopJump: FirLoopJump, data: Unit): FirStatement =
    invokeMeta("loopJump", loopJump) ?: super.transformLoopJump(loopJump, data)

  override fun transformMemberDeclaration(memberDeclaration: FirMemberDeclaration, data: Unit): FirMemberDeclaration =
    invokeMeta("memberDeclaration", memberDeclaration) ?: super.transformMemberDeclaration(memberDeclaration, data)

  override fun transformNamedArgumentExpression(
    namedArgumentExpression: FirNamedArgumentExpression, data: Unit
  ): FirStatement =
    invokeMeta("namedArgumentExpression", namedArgumentExpression) ?: super.transformNamedArgumentExpression(
      namedArgumentExpression, data
    )

  override fun transformNamedReference(namedReference: FirNamedReference, data: Unit): FirReference =
    invokeMeta("namedReference", namedReference) ?: super.transformNamedReference(namedReference, data)

  override fun transformPackageDirective(packageDirective: FirPackageDirective, data: Unit): FirPackageDirective =
    invokeMeta("packageDirective", packageDirective) ?: super.transformPackageDirective(packageDirective, data)

  override fun transformPlaceholderProjection(
    placeholderProjection: FirPlaceholderProjection, data: Unit
  ): FirTypeProjection =
    invokeMeta("placeholderProjection", placeholderProjection) ?: super.transformPlaceholderProjection(
      placeholderProjection, data
    )

  override fun transformProperty(property: FirProperty, data: Unit): FirStatement =
    invokeMeta("property", property) ?: super.transformProperty(property, data)

  override fun transformPropertyAccessExpression(
    propertyAccessExpression: FirPropertyAccessExpression, data: Unit
  ): FirStatement =
    invokeMeta("propertyAccessExpression", propertyAccessExpression) ?: super.transformPropertyAccessExpression(
      propertyAccessExpression, data
    )

  override fun transformPropertyAccessor(propertyAccessor: FirPropertyAccessor, data: Unit): FirStatement =
    invokeMeta("propertyAccessor", propertyAccessor) ?: super.transformPropertyAccessor(propertyAccessor, data)

  override fun transformQualifiedAccess(qualifiedAccess: FirQualifiedAccess, data: Unit): FirStatement =
    invokeMeta("qualifiedAccess", qualifiedAccess) ?: super.transformQualifiedAccess(qualifiedAccess, data)

  override fun transformQualifiedAccessExpression(
    qualifiedAccessExpression: FirQualifiedAccessExpression, data: Unit
  ): FirStatement =
    invokeMeta("qualifiedAccessExpression", qualifiedAccessExpression) ?: super.transformQualifiedAccessExpression(
      qualifiedAccessExpression, data
    )

  override fun transformRawContractDescription(
    rawContractDescription: FirRawContractDescription, data: Unit
  ): FirContractDescription =
    invokeMeta("rawContractDescription", rawContractDescription) ?: super.transformRawContractDescription(
      rawContractDescription, data
    )

  override fun transformReference(reference: FirReference, data: Unit): FirReference =
    invokeMeta("reference", reference) ?: super.transformReference(reference, data)

  override fun transformRegularClass(regularClass: FirRegularClass, data: Unit): FirStatement =
    invokeMeta("regularClass", regularClass) ?: super.transformRegularClass(regularClass, data)

  override fun transformResolvable(resolvable: FirResolvable, data: Unit): FirResolvable =
    invokeMeta("resolvable", resolvable) ?: super.transformResolvable(resolvable, data)

  override fun transformResolvedCallableReference(
    resolvedCallableReference: FirResolvedCallableReference, data: Unit
  ): FirReference =
    invokeMeta("resolvedCallableReference", resolvedCallableReference) ?: super.transformResolvedCallableReference(
      resolvedCallableReference, data
    )

  override fun transformResolvedContractDescription(
    resolvedContractDescription: FirResolvedContractDescription, data: Unit
  ): FirContractDescription = invokeMeta("resolvedContractDescription", resolvedContractDescription)
    ?: super.transformResolvedContractDescription(resolvedContractDescription, data)

  override fun transformResolvedDeclarationStatus(
    resolvedDeclarationStatus: FirResolvedDeclarationStatus, data: Unit
  ): FirDeclarationStatus =
    invokeMeta("resolvedDeclarationStatus", resolvedDeclarationStatus) ?: super.transformResolvedDeclarationStatus(
      resolvedDeclarationStatus, data
    )

  override fun transformResolvedImport(resolvedImport: FirResolvedImport, data: Unit): FirImport =
    invokeMeta("resolvedImport", resolvedImport) ?: super.transformResolvedImport(resolvedImport, data)

  override fun transformResolvedNamedReference(
    resolvedNamedReference: FirResolvedNamedReference, data: Unit
  ): FirReference =
    invokeMeta("resolvedNamedReference", resolvedNamedReference) ?: super.transformResolvedNamedReference(
      resolvedNamedReference, data
    )

  override fun transformResolvedQualifier(resolvedQualifier: FirResolvedQualifier, data: Unit): FirStatement =
    invokeMeta("resolvedQualifier", resolvedQualifier) ?: super.transformResolvedQualifier(resolvedQualifier, data)

  override fun transformResolvedReifiedParameterReference(
    resolvedReifiedParameterReference: FirResolvedReifiedParameterReference, data: Unit
  ): FirStatement = invokeMeta("resolvedReifiedParameterReference", resolvedReifiedParameterReference)
    ?: super.transformResolvedReifiedParameterReference(resolvedReifiedParameterReference, data)

  override fun transformResolvedTypeRef(resolvedTypeRef: FirResolvedTypeRef, data: Unit): FirTypeRef =
    invokeMeta("resolvedTypeRef", resolvedTypeRef) ?: super.transformResolvedTypeRef(resolvedTypeRef, data)

  override fun transformReturnExpression(returnExpression: FirReturnExpression, data: Unit): FirStatement =
    invokeMeta("returnExpression", returnExpression) ?: super.transformReturnExpression(returnExpression, data)

  override fun transformSafeCallExpression(safeCallExpression: FirSafeCallExpression, data: Unit): FirStatement =
    invokeMeta("safeCallExpression", safeCallExpression) ?: super.transformSafeCallExpression(safeCallExpression, data)

  override fun transformSimpleFunction(simpleFunction: FirSimpleFunction, data: Unit): FirStatement =
    invokeMeta("simpleFunction", simpleFunction) ?: super.transformSimpleFunction(simpleFunction, data)

  override fun transformSmartCastExpression(smartCastExpression: FirSmartCastExpression, data: Unit): FirStatement =
    invokeMeta("smartCastExpression", smartCastExpression) ?: super.transformSmartCastExpression(
      smartCastExpression, data
    )

  override fun transformSpreadArgumentExpression(
    spreadArgumentExpression: FirSpreadArgumentExpression, data: Unit
  ): FirStatement =
    invokeMeta("spreadArgumentExpression", spreadArgumentExpression) ?: super.transformSpreadArgumentExpression(
      spreadArgumentExpression, data
    )

  override fun transformStarProjection(starProjection: FirStarProjection, data: Unit): FirTypeProjection =
    invokeMeta("starProjection", starProjection) ?: super.transformStarProjection(starProjection, data)

  override fun transformStatement(statement: FirStatement, data: Unit): FirStatement =
    invokeMeta("statement", statement) ?: super.transformStatement(statement, data)

  override fun transformStringConcatenationCall(
    stringConcatenationCall: FirStringConcatenationCall, data: Unit
  ): FirStatement =
    invokeMeta("stringConcatenationCall", stringConcatenationCall) ?: super.transformStringConcatenationCall(
      stringConcatenationCall, data
    )

  override fun transformSuperReference(superReference: FirSuperReference, data: Unit): FirReference =
    invokeMeta("superReference", superReference) ?: super.transformSuperReference(superReference, data)

  override fun transformTargetElement(targetElement: FirTargetElement, data: Unit): FirTargetElement =
    invokeMeta("targetElement", targetElement) ?: super.transformTargetElement(targetElement, data)

  override fun transformThisReceiverExpression(
    thisReceiverExpression: FirThisReceiverExpression, data: Unit
  ): FirStatement =
    invokeMeta("thisReceiverExpression", thisReceiverExpression) ?: super.transformThisReceiverExpression(
      thisReceiverExpression, data
    )

  override fun transformThisReference(thisReference: FirThisReference, data: Unit): FirReference =
    invokeMeta("thisReference", thisReference) ?: super.transformThisReference(thisReference, data)

  override fun transformThrowExpression(throwExpression: FirThrowExpression, data: Unit): FirStatement =
    invokeMeta("throwExpression", throwExpression) ?: super.transformThrowExpression(throwExpression, data)

  override fun transformTryExpression(tryExpression: FirTryExpression, data: Unit): FirStatement =
    invokeMeta("tryExpression", tryExpression) ?: super.transformTryExpression(tryExpression, data)

  override fun transformTypeAlias(typeAlias: FirTypeAlias, data: Unit): FirStatement =
    invokeMeta("typeAlias", typeAlias) ?: super.transformTypeAlias(typeAlias, data)

  override fun transformTypeOperatorCall(typeOperatorCall: FirTypeOperatorCall, data: Unit): FirStatement =
    invokeMeta("typeOperatorCall", typeOperatorCall) ?: super.transformTypeOperatorCall(typeOperatorCall, data)

  override fun transformTypeParameter(typeParameter: FirTypeParameter, data: Unit): FirTypeParameterRef =
    invokeMeta("typeParameter", typeParameter) ?: super.transformTypeParameter(typeParameter, data)

  override fun transformTypeParameterRef(typeParameterRef: FirTypeParameterRef, data: Unit): FirTypeParameterRef =
    invokeMeta("typeParameterRef", typeParameterRef) ?: super.transformTypeParameterRef(typeParameterRef, data)

  override fun transformTypeParameterRefsOwner(
    typeParameterRefsOwner: FirTypeParameterRefsOwner, data: Unit
  ): FirTypeParameterRefsOwner =
    invokeMeta("typeParameterRefsOwner", typeParameterRefsOwner) ?: super.transformTypeParameterRefsOwner(
      typeParameterRefsOwner, data
    )

  override fun transformTypeParametersOwner(
    typeParametersOwner: FirTypeParametersOwner, data: Unit
  ): FirTypeParametersOwner =
    invokeMeta("typeParametersOwner", typeParametersOwner) ?: super.transformTypeParametersOwner(
      typeParametersOwner, data
    )

  override fun transformTypeProjection(typeProjection: FirTypeProjection, data: Unit): FirTypeProjection =
    invokeMeta("typeProjection", typeProjection) ?: super.transformTypeProjection(typeProjection, data)

  override fun transformTypeProjectionWithVariance(
    typeProjectionWithVariance: FirTypeProjectionWithVariance, data: Unit
  ): FirTypeProjection =
    invokeMeta("typeProjectionWithVariance", typeProjectionWithVariance) ?: super.transformTypeProjectionWithVariance(
      typeProjectionWithVariance, data
    )

  override fun transformTypeRef(typeRef: FirTypeRef, data: Unit): FirTypeRef =
    invokeMeta("typeRef", typeRef) ?: super.transformTypeRef(typeRef, data)

  override fun transformTypeRefWithNullability(
    typeRefWithNullability: FirTypeRefWithNullability, data: Unit
  ): FirTypeRef = invokeMeta("typeRefWithNullability", typeRefWithNullability) ?: super.transformTypeRefWithNullability(
    typeRefWithNullability, data
  )

  override fun transformUserTypeRef(userTypeRef: FirUserTypeRef, data: Unit): FirTypeRef =
    invokeMeta("userTypeRef", userTypeRef) ?: super.transformUserTypeRef(userTypeRef, data)

  override fun transformValueParameter(valueParameter: FirValueParameter, data: Unit): FirStatement =
    invokeMeta("valueParameter", valueParameter) ?: super.transformValueParameter(valueParameter, data)

  override fun transformVarargArgumentsExpression(
    varargArgumentsExpression: FirVarargArgumentsExpression, data: Unit
  ): FirStatement =
    invokeMeta("varargArgumentsExpression", varargArgumentsExpression) ?: super.transformVarargArgumentsExpression(
      varargArgumentsExpression, data
    )

  override fun transformVariable(variable: FirVariable, data: Unit): FirStatement =
    invokeMeta("variable", variable) ?: super.transformVariable(variable, data)

  override fun transformVariableAssignment(variableAssignment: FirVariableAssignment, data: Unit): FirStatement =
    invokeMeta("variableAssignment", variableAssignment) ?: super.transformVariableAssignment(variableAssignment, data)

  override fun transformWhenBranch(whenBranch: FirWhenBranch, data: Unit): FirWhenBranch =
    invokeMeta("whenBranch", whenBranch) ?: super.transformWhenBranch(whenBranch, data)

  override fun transformWhenExpression(whenExpression: FirWhenExpression, data: Unit): FirStatement =
    invokeMeta("whenExpression", whenExpression) ?: super.transformWhenExpression(whenExpression, data)

  override fun transformWhenSubjectExpression(
    whenSubjectExpression: FirWhenSubjectExpression, data: Unit
  ): FirStatement = invokeMeta("whenSubjectExpression", whenSubjectExpression) ?: super.transformWhenSubjectExpression(
    whenSubjectExpression, data
  )

  override fun transformWhileLoop(whileLoop: FirWhileLoop, data: Unit): FirStatement =
    invokeMeta("whileLoop", whileLoop) ?: super.transformWhileLoop(whileLoop, data)

  override fun transformWrappedArgumentExpression(
    wrappedArgumentExpression: FirWrappedArgumentExpression, data: Unit
  ): FirStatement =
    invokeMeta("wrappedArgumentExpression", wrappedArgumentExpression) ?: super.transformWrappedArgumentExpression(
      wrappedArgumentExpression, data
    )

  override fun transformWrappedDelegateExpression(
    wrappedDelegateExpression: FirWrappedDelegateExpression, data: Unit
  ): FirStatement =
    invokeMeta("wrappedDelegateExpression", wrappedDelegateExpression) ?: super.transformWrappedDelegateExpression(
      wrappedDelegateExpression, data
    )

  override fun transformWrappedExpression(wrappedExpression: FirWrappedExpression, data: Unit): FirStatement =
    invokeMeta("wrappedExpression", wrappedExpression) ?: super.transformWrappedExpression(wrappedExpression, data)
}
