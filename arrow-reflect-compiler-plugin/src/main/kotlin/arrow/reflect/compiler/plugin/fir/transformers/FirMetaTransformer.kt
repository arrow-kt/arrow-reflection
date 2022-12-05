package arrow.reflect.compiler.plugin.fir.transformers

import arrow.meta.FirMetaContext
import arrow.meta.Meta
import arrow.meta.TemplateCompiler
import arrow.reflect.compiler.plugin.fir.checkers.metaAnnotations
import arrow.reflect.compiler.plugin.targets.MetaTarget
import arrow.reflect.compiler.plugin.targets.MetagenerationTarget
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirTargetElement
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.resolve.fqName
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import kotlin.reflect.KClass

class FirMetaTransformer(
  private val session: FirSession, val templateCompiler: TemplateCompiler, val metaTargets: List<MetaTarget>,
  val checkerContext: CheckerContext,
) : FirTransformer<Unit>() {

  val metaContext = FirMetaContext(templateCompiler, session)

  internal inline fun <reified In : FirAnnotationContainer, reified Out> invokeMeta(
    superType: KClass<*>, methodName: String, arg: In
  ): Out? {
    if (templateCompiler.compiling) return null
    val args = listOf(In::class, CheckerContext::class)
    val retType = Out::class
    val metaAnnotations = arg.metaAnnotations(session)
    return MetaTarget.find(
      metaAnnotations.mapNotNull { it.fqName(session)?.asString() }.toSet(),
      methodName,
      superType,
      MetagenerationTarget.Fir,
      args,
      retType,
      metaTargets
    )?.let { target ->
      val result = target.method.invoke(target.companion.objectInstance, metaContext, arg, checkerContext)
      result as? Out
    }
  }

  override fun <E : FirElement> transformElement(element: E, data: Unit): E {
    element.transformChildren(this, data)
    return element
  }

  override fun transformAnnotation(annotation: FirAnnotation, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.Annotation::class, "annotation", annotation) ?: super.transformAnnotation(
      annotation, data
    )

  override fun transformAnnotationCall(annotationCall: FirAnnotationCall, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.AnnotationCall::class, "annotationCall", annotationCall)
      ?: super.transformAnnotationCall(annotationCall, data)

  override fun transformAnnotationContainer(
    annotationContainer: FirAnnotationContainer, data: Unit
  ): FirAnnotationContainer =
    invokeMeta(Meta.FrontendTransformer.AnnotationContainer::class, "annotationContainer", annotationContainer)
      ?: super.transformAnnotationContainer(
        annotationContainer, data
      )

  override fun transformAnonymousFunction(anonymousFunction: FirAnonymousFunction, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.AnonymousFunction::class, "anonymousFunction", anonymousFunction)
      ?: super.transformAnonymousFunction(anonymousFunction, data)

  override fun transformAnonymousFunctionExpression(
    anonymousFunctionExpression: FirAnonymousFunctionExpression, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.AnonymousFunctionExpression::class,
    "anonymousFunctionExpression",
    anonymousFunctionExpression
  ) ?: super.transformAnonymousFunctionExpression(anonymousFunctionExpression, data)

  override fun transformAnonymousInitializer(
    anonymousInitializer: FirAnonymousInitializer, data: Unit
  ): FirAnonymousInitializer =
    invokeMeta(Meta.FrontendTransformer.AnonymousInitializer::class, "anonymousInitializer", anonymousInitializer)
      ?: super.transformAnonymousInitializer(anonymousInitializer, data)

  override fun transformAnonymousObject(anonymousObject: FirAnonymousObject, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.AnonymousObject::class, "anonymousObject", anonymousObject)
      ?: super.transformAnonymousObject(anonymousObject, data)

  override fun transformAnonymousObjectExpression(
    anonymousObjectExpression: FirAnonymousObjectExpression, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.AnonymousObjectExpression::class, "anonymousObjectExpression", anonymousObjectExpression
  ) ?: super.transformAnonymousObjectExpression(
    anonymousObjectExpression, data
  )

  override fun transformArrayOfCall(arrayOfCall: FirArrayOfCall, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ArrayOfCall::class, "arrayOfCall", arrayOfCall) ?: super.transformArrayOfCall(
      arrayOfCall, data
    )

  override fun transformAssignmentOperatorStatement(
    assignmentOperatorStatement: FirAssignmentOperatorStatement, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.AssignmentOperatorStatement::class,
    "assignmentOperatorStatement",
    assignmentOperatorStatement
  ) ?: super.transformAssignmentOperatorStatement(assignmentOperatorStatement, data)

  override fun transformAugmentedArraySetCall(
    augmentedArraySetCall: FirAugmentedArraySetCall, data: Unit
  ): FirStatement =
    invokeMeta(Meta.FrontendTransformer.AugmentedArraySetCall::class, "augmentedArraySetCall", augmentedArraySetCall)
      ?: super.transformAugmentedArraySetCall(
        augmentedArraySetCall, data
      )

  override fun transformBackingField(backingField: FirBackingField, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.BackingField::class, "backingField", backingField)
      ?: super.transformBackingField(backingField, data)

  override fun transformBinaryLogicExpression(
    binaryLogicExpression: FirBinaryLogicExpression, data: Unit
  ): FirStatement =
    invokeMeta(Meta.FrontendTransformer.BinaryLogicExpression::class, "binaryLogicExpression", binaryLogicExpression)
      ?: super.transformBinaryLogicExpression(
        binaryLogicExpression, data
      )

  override fun transformBlock(block: FirBlock, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.Block::class, "block", block) ?: super.transformBlock(block, data)

  override fun transformBreakExpression(breakExpression: FirBreakExpression, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.BreakExpression::class, "breakExpression", breakExpression)
      ?: super.transformBreakExpression(breakExpression, data)

  override fun transformCall(call: FirCall, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.Call::class, "call", call) ?: super.transformCall(call, data)

  override fun transformCallableDeclaration(
    callableDeclaration: FirCallableDeclaration, data: Unit
  ): FirCallableDeclaration =
    invokeMeta(Meta.FrontendTransformer.CallableDeclaration::class, "callableDeclaration", callableDeclaration)
      ?: super.transformCallableDeclaration(
        callableDeclaration, data
      )

  override fun transformCallableReferenceAccess(
    callableReferenceAccess: FirCallableReferenceAccess, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.CallableReferenceAccess::class, "callableReferenceAccess", callableReferenceAccess
  ) ?: super.transformCallableReferenceAccess(
    callableReferenceAccess, data
  )

  override fun transformCheckNotNullCall(checkNotNullCall: FirCheckNotNullCall, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.CheckNotNullCall::class, "checkNotNullCall", checkNotNullCall)
      ?: super.transformCheckNotNullCall(checkNotNullCall, data)

  override fun transformCheckedSafeCallSubject(
    checkedSafeCallSubject: FirCheckedSafeCallSubject, data: Unit
  ): FirStatement =
    invokeMeta(Meta.FrontendTransformer.CheckedSafeCallSubject::class, "checkedSafeCallSubject", checkedSafeCallSubject)
      ?: super.transformCheckedSafeCallSubject(
        checkedSafeCallSubject, data
      )

  override fun transformClass(klass: FirClass, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.Class::class, "klass", klass) ?: super.transformClass(klass, data)

  override fun transformClassLikeDeclaration(classLikeDeclaration: FirClassLikeDeclaration, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ClassLikeDeclaration::class, "classLikeDeclaration", classLikeDeclaration)
      ?: super.transformClassLikeDeclaration(classLikeDeclaration, data)

  override fun transformClassReferenceExpression(
    classReferenceExpression: FirClassReferenceExpression, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.ClassReferenceExpression::class, "classReferenceExpression", classReferenceExpression
  ) ?: super.transformClassReferenceExpression(
    classReferenceExpression, data
  )

  override fun transformComparisonExpression(comparisonExpression: FirComparisonExpression, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ComparisonExpression::class, "comparisonExpression", comparisonExpression)
      ?: super.transformComparisonExpression(comparisonExpression, data)

  override fun transformComponentCall(componentCall: FirComponentCall, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ComponentCall::class, "componentCall", componentCall)
      ?: super.transformComponentCall(componentCall, data)

  override fun <T> transformConstExpression(constExpression: FirConstExpression<T>, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ConstExpression::class, "constExpression", constExpression)
      ?: super.transformConstExpression(constExpression, data)

  override fun transformConstructor(constructor: FirConstructor, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.Constructor::class, "constructor", constructor) ?: super.transformConstructor(
      constructor, data
    )

  override fun transformDeclaration(declaration: FirDeclaration, data: Unit): FirDeclaration =
    invokeMeta(Meta.FrontendTransformer.Declaration::class, "declaration", declaration) ?: super.transformDeclaration(
      declaration, data
    )

  override fun transformDelegatedConstructorCall(
    delegatedConstructorCall: FirDelegatedConstructorCall, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.DelegatedConstructorCall::class, "delegatedConstructorCall", delegatedConstructorCall
  ) ?: super.transformDelegatedConstructorCall(
    delegatedConstructorCall, data
  )

  override fun transformDoWhileLoop(doWhileLoop: FirDoWhileLoop, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.DoWhileLoop::class, "doWhileLoop", doWhileLoop) ?: super.transformDoWhileLoop(
      doWhileLoop, data
    )

  override fun transformDynamicTypeRef(dynamicTypeRef: FirDynamicTypeRef, data: Unit): FirTypeRef =
    invokeMeta(Meta.FrontendTransformer.DynamicTypeRef::class, "dynamicTypeRef", dynamicTypeRef)
      ?: super.transformDynamicTypeRef(dynamicTypeRef, data)

  override fun transformElvisExpression(elvisExpression: FirElvisExpression, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ElvisExpression::class, "elvisExpression", elvisExpression)
      ?: super.transformElvisExpression(elvisExpression, data)

  override fun transformEnumEntry(enumEntry: FirEnumEntry, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.EnumEntry::class, "enumEntry", enumEntry) ?: super.transformEnumEntry(
      enumEntry, data
    )

  override fun transformEqualityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.EqualityOperatorCall::class, "equalityOperatorCall", equalityOperatorCall)
      ?: super.transformEqualityOperatorCall(equalityOperatorCall, data)

  override fun transformErrorAnnotationCall(errorAnnotationCall: FirErrorAnnotationCall, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ErrorAnnotationCall::class, "errorAnnotationCall", errorAnnotationCall)
      ?: super.transformErrorAnnotationCall(
        errorAnnotationCall, data
      )

  override fun transformErrorExpression(errorExpression: FirErrorExpression, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ErrorExpression::class, "errorExpression", errorExpression)
      ?: super.transformErrorExpression(errorExpression, data)

  override fun transformErrorFunction(errorFunction: FirErrorFunction, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ErrorFunction::class, "errorFunction", errorFunction)
      ?: super.transformErrorFunction(errorFunction, data)

  override fun transformErrorLoop(errorLoop: FirErrorLoop, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ErrorLoop::class, "errorLoop", errorLoop) ?: super.transformErrorLoop(
      errorLoop, data
    )

  override fun transformErrorProperty(errorProperty: FirErrorProperty, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ErrorProperty::class, "errorProperty", errorProperty)
      ?: super.transformErrorProperty(errorProperty, data)

  override fun transformErrorResolvedQualifier(
    errorResolvedQualifier: FirErrorResolvedQualifier, data: Unit
  ): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ErrorResolvedQualifier::class, "errorResolvedQualifier", errorResolvedQualifier)
      ?: super.transformErrorResolvedQualifier(
        errorResolvedQualifier, data
      )

  override fun transformErrorTypeRef(errorTypeRef: FirErrorTypeRef, data: Unit): FirTypeRef =
    invokeMeta(Meta.FrontendTransformer.ErrorTypeRef::class, "errorTypeRef", errorTypeRef)
      ?: super.transformErrorTypeRef(errorTypeRef, data)

  override fun transformExpression(expression: FirExpression, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.Expression::class, "expression", expression) ?: super.transformExpression(
      expression, data
    )

  override fun transformField(field: FirField, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.Field::class, "field", field) ?: super.transformField(field, data)

  override fun transformFile(file: FirFile, data: Unit): FirFile =
    invokeMeta(Meta.FrontendTransformer.File::class, "file", file) ?: super.transformFile(file, data)

  override fun transformFunction(function: FirFunction, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.Function::class, "function", function) ?: super.transformFunction(
      function, data
    )

  override fun transformFunctionCall(functionCall: FirFunctionCall, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.FunctionCall::class, "functionCall", functionCall)
      ?: super.transformFunctionCall(functionCall, data)

  override fun transformFunctionTypeRef(functionTypeRef: FirFunctionTypeRef, data: Unit): FirTypeRef =
    invokeMeta(Meta.FrontendTransformer.FunctionTypeRef::class, "functionTypeRef", functionTypeRef)
      ?: super.transformFunctionTypeRef(functionTypeRef, data)

  override fun transformGetClassCall(getClassCall: FirGetClassCall, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.GetClassCall::class, "getClassCall", getClassCall)
      ?: super.transformGetClassCall(getClassCall, data)

  override fun transformImplicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ImplicitInvokeCall::class, "implicitInvokeCall", implicitInvokeCall)
      ?: super.transformImplicitInvokeCall(implicitInvokeCall, data)

  override fun transformImplicitTypeRef(implicitTypeRef: FirImplicitTypeRef, data: Unit): FirTypeRef =
    invokeMeta(Meta.FrontendTransformer.ImplicitTypeRef::class, "implicitTypeRef", implicitTypeRef)
      ?: super.transformImplicitTypeRef(implicitTypeRef, data)

  override fun transformIntegerLiteralOperatorCall(
    integerLiteralOperatorCall: FirIntegerLiteralOperatorCall, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.IntegerLiteralOperatorCall::class, "integerLiteralOperatorCall", integerLiteralOperatorCall
  ) ?: super.transformIntegerLiteralOperatorCall(
    integerLiteralOperatorCall, data
  )

  override fun transformIntersectionTypeRef(intersectionTypeRef: FirIntersectionTypeRef, data: Unit): FirTypeRef =
    invokeMeta(Meta.FrontendTransformer.IntersectionTypeRef::class, "intersectionTypeRef", intersectionTypeRef)
      ?: super.transformIntersectionTypeRef(
        intersectionTypeRef, data
      )

  override fun <E : FirTargetElement> transformJump(jump: FirJump<E>, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.Jump::class, "jump", jump) ?: super.transformJump(jump, data)

  override fun transformLambdaArgumentExpression(
    lambdaArgumentExpression: FirLambdaArgumentExpression, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.LambdaArgumentExpression::class, "lambdaArgumentExpression", lambdaArgumentExpression
  ) ?: super.transformLambdaArgumentExpression(
    lambdaArgumentExpression, data
  )

  override fun transformLoop(loop: FirLoop, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.Loop::class, "loop", loop) ?: super.transformLoop(loop, data)

  override fun transformLoopJump(loopJump: FirLoopJump, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.LoopJump::class, "loopJump", loopJump) ?: super.transformLoopJump(
      loopJump, data
    )

  override fun transformMemberDeclaration(memberDeclaration: FirMemberDeclaration, data: Unit): FirMemberDeclaration =
    invokeMeta(Meta.FrontendTransformer.MemberDeclaration::class, "memberDeclaration", memberDeclaration)
      ?: super.transformMemberDeclaration(memberDeclaration, data)

  override fun transformNamedArgumentExpression(
    namedArgumentExpression: FirNamedArgumentExpression, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.NamedArgumentExpression::class, "namedArgumentExpression", namedArgumentExpression
  ) ?: super.transformNamedArgumentExpression(
    namedArgumentExpression, data
  )

  override fun transformProperty(property: FirProperty, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.Property::class, "property", property) ?: super.transformProperty(
      property, data
    )

  override fun transformPropertyAccessExpression(
    propertyAccessExpression: FirPropertyAccessExpression, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.PropertyAccessExpression::class, "propertyAccessExpression", propertyAccessExpression
  ) ?: super.transformPropertyAccessExpression(
    propertyAccessExpression, data
  )

  override fun transformPropertyAccessor(propertyAccessor: FirPropertyAccessor, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.PropertyAccessor::class, "propertyAccessor", propertyAccessor)
      ?: super.transformPropertyAccessor(propertyAccessor, data)

  override fun transformQualifiedAccess(qualifiedAccess: FirQualifiedAccess, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.QualifiedAccess::class, "qualifiedAccess", qualifiedAccess)
      ?: super.transformQualifiedAccess(qualifiedAccess, data)

  override fun transformQualifiedAccessExpression(
    qualifiedAccessExpression: FirQualifiedAccessExpression, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.QualifiedAccessExpression::class, "qualifiedAccessExpression", qualifiedAccessExpression
  ) ?: super.transformQualifiedAccessExpression(
    qualifiedAccessExpression, data
  )

  override fun transformRegularClass(regularClass: FirRegularClass, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.RegularClass::class, "regularClass", regularClass)
      ?: super.transformRegularClass(regularClass, data)

  override fun transformResolvedQualifier(resolvedQualifier: FirResolvedQualifier, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ResolvedQualifier::class, "resolvedQualifier", resolvedQualifier)
      ?: super.transformResolvedQualifier(resolvedQualifier, data)

  override fun transformResolvedReifiedParameterReference(
    resolvedReifiedParameterReference: FirResolvedReifiedParameterReference, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.ResolvedReifiedParameterReference::class,
    "resolvedReifiedParameterReference",
    resolvedReifiedParameterReference
  ) ?: super.transformResolvedReifiedParameterReference(resolvedReifiedParameterReference, data)

  override fun transformResolvedTypeRef(resolvedTypeRef: FirResolvedTypeRef, data: Unit): FirTypeRef =
    invokeMeta(Meta.FrontendTransformer.ResolvedTypeRef::class, "resolvedTypeRef", resolvedTypeRef)
      ?: super.transformResolvedTypeRef(resolvedTypeRef, data)

  override fun transformReturnExpression(returnExpression: FirReturnExpression, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ReturnExpression::class, "returnExpression", returnExpression)
      ?: super.transformReturnExpression(returnExpression, data)

  override fun transformSafeCallExpression(safeCallExpression: FirSafeCallExpression, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.SafeCallExpression::class, "safeCallExpression", safeCallExpression)
      ?: super.transformSafeCallExpression(safeCallExpression, data)

  override fun transformSimpleFunction(simpleFunction: FirSimpleFunction, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.SimpleFunction::class, "simpleFunction", simpleFunction)
      ?: super.transformSimpleFunction(simpleFunction, data)

  override fun transformSmartCastExpression(smartCastExpression: FirSmartCastExpression, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.SmartCastExpression::class, "smartCastExpression", smartCastExpression)
      ?: super.transformSmartCastExpression(
        smartCastExpression, data
      )

  override fun transformSpreadArgumentExpression(
    spreadArgumentExpression: FirSpreadArgumentExpression, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.SpreadArgumentExpression::class, "spreadArgumentExpression", spreadArgumentExpression
  ) ?: super.transformSpreadArgumentExpression(
    spreadArgumentExpression, data
  )

  override fun transformStatement(statement: FirStatement, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.Statement::class, "statement", statement) ?: super.transformStatement(
      statement, data
    )

  override fun transformStringConcatenationCall(
    stringConcatenationCall: FirStringConcatenationCall, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.StringConcatenationCall::class, "stringConcatenationCall", stringConcatenationCall
  ) ?: super.transformStringConcatenationCall(
    stringConcatenationCall, data
  )

  override fun transformThisReceiverExpression(
    thisReceiverExpression: FirThisReceiverExpression, data: Unit
  ): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ThisReceiverExpression::class, "thisReceiverExpression", thisReceiverExpression)
      ?: super.transformThisReceiverExpression(
        thisReceiverExpression, data
      )

  override fun transformThrowExpression(throwExpression: FirThrowExpression, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ThrowExpression::class, "throwExpression", throwExpression)
      ?: super.transformThrowExpression(throwExpression, data)

  override fun transformTryExpression(tryExpression: FirTryExpression, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.TryExpression::class, "tryExpression", tryExpression)
      ?: super.transformTryExpression(tryExpression, data)

  override fun transformTypeAlias(typeAlias: FirTypeAlias, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.TypeAlias::class, "typeAlias", typeAlias) ?: super.transformTypeAlias(
      typeAlias, data
    )

  override fun transformTypeOperatorCall(typeOperatorCall: FirTypeOperatorCall, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.TypeOperatorCall::class, "typeOperatorCall", typeOperatorCall)
      ?: super.transformTypeOperatorCall(typeOperatorCall, data)

  override fun transformTypeParameter(typeParameter: FirTypeParameter, data: Unit): FirTypeParameterRef =
    invokeMeta(Meta.FrontendTransformer.TypeParameter::class, "typeParameter", typeParameter)
      ?: super.transformTypeParameter(typeParameter, data)

  override fun transformTypeRef(typeRef: FirTypeRef, data: Unit): FirTypeRef =
    invokeMeta(Meta.FrontendTransformer.TypeRef::class, "typeRef", typeRef) ?: super.transformTypeRef(typeRef, data)

  override fun transformTypeRefWithNullability(
    typeRefWithNullability: FirTypeRefWithNullability, data: Unit
  ): FirTypeRef =
    invokeMeta(Meta.FrontendTransformer.TypeRefWithNullability::class, "typeRefWithNullability", typeRefWithNullability)
      ?: super.transformTypeRefWithNullability(
        typeRefWithNullability, data
      )

  override fun transformUserTypeRef(userTypeRef: FirUserTypeRef, data: Unit): FirTypeRef =
    invokeMeta(Meta.FrontendTransformer.UserTypeRef::class, "userTypeRef", userTypeRef) ?: super.transformUserTypeRef(
      userTypeRef, data
    )

  override fun transformValueParameter(valueParameter: FirValueParameter, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.ValueParameter::class, "valueParameter", valueParameter)
      ?: super.transformValueParameter(valueParameter, data)

  override fun transformVarargArgumentsExpression(
    varargArgumentsExpression: FirVarargArgumentsExpression, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.VarargArgumentsExpression::class, "varargArgumentsExpression", varargArgumentsExpression
  ) ?: super.transformVarargArgumentsExpression(
    varargArgumentsExpression, data
  )

  override fun transformVariable(variable: FirVariable, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.Variable::class, "variable", variable) ?: super.transformVariable(
      variable, data
    )

  override fun transformVariableAssignment(variableAssignment: FirVariableAssignment, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.VariableAssignment::class, "variableAssignment", variableAssignment)
      ?: super.transformVariableAssignment(variableAssignment, data)

  override fun transformWhenExpression(whenExpression: FirWhenExpression, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.WhenExpression::class, "whenExpression", whenExpression)
      ?: super.transformWhenExpression(whenExpression, data)

  override fun transformWhenSubjectExpression(
    whenSubjectExpression: FirWhenSubjectExpression, data: Unit
  ): FirStatement =
    invokeMeta(Meta.FrontendTransformer.WhenSubjectExpression::class, "whenSubjectExpression", whenSubjectExpression)
      ?: super.transformWhenSubjectExpression(
        whenSubjectExpression, data
      )

  override fun transformWhileLoop(whileLoop: FirWhileLoop, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.WhileLoop::class, "whileLoop", whileLoop) ?: super.transformWhileLoop(
      whileLoop, data
    )

  override fun transformWrappedArgumentExpression(
    wrappedArgumentExpression: FirWrappedArgumentExpression, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.WrappedArgumentExpression::class, "wrappedArgumentExpression", wrappedArgumentExpression
  ) ?: super.transformWrappedArgumentExpression(
    wrappedArgumentExpression, data
  )

  override fun transformWrappedDelegateExpression(
    wrappedDelegateExpression: FirWrappedDelegateExpression, data: Unit
  ): FirStatement = invokeMeta(
    Meta.FrontendTransformer.WrappedDelegateExpression::class, "wrappedDelegateExpression", wrappedDelegateExpression
  ) ?: super.transformWrappedDelegateExpression(
    wrappedDelegateExpression, data
  )

  override fun transformWrappedExpression(wrappedExpression: FirWrappedExpression, data: Unit): FirStatement =
    invokeMeta(Meta.FrontendTransformer.WrappedExpression::class, "wrappedExpression", wrappedExpression)
      ?: super.transformWrappedExpression(wrappedExpression, data)
}

