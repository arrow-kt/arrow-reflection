package arrow.meta

import arrow.meta.module.impl.arrow.meta.FirMetaContext
import arrow.meta.module.impl.arrow.meta.IrMetaContext
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirLabel
import org.jetbrains.kotlin.fir.FirPackageDirective
import org.jetbrains.kotlin.fir.FirTargetElement
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.contracts.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.diagnostics.FirDiagnosticHolder
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.references.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

annotation class Meta {

  sealed interface Checker<E> {

    interface Declaration<E : FirDeclaration> : Checker<E> {
      fun check(declaration: E, context: CheckerContext, reporter: DiagnosticReporter)
    }

    interface Expression<E : FirExpression> : Checker<E> {
      fun check(expression: E, context: CheckerContext, reporter: DiagnosticReporter)
    }

    interface Type<E : FirTypeRef> : Checker<E> {
      fun check(typeRef: E, context: CheckerContext, reporter: DiagnosticReporter)
    }

  }

  sealed interface FrontendTransformer {
    interface Annotation : FrontendTransformer {
      fun FirMetaContext.annotation(annotation: FirAnnotation): FirStatement
    }

    interface AnnotationArgumentMapping : FrontendTransformer {
      fun FirMetaContext.annotationArgumentMapping(annotationArgumentMapping: FirAnnotationArgumentMapping): FirAnnotationArgumentMapping
    }

    interface AnnotationCall : FrontendTransformer {
      fun FirMetaContext.annotationCall(annotationCall: FirAnnotationCall): FirStatement
    }

    interface AnnotationContainer: FrontendTransformer {
      fun FirMetaContext.annotationContainer(annotationContainer: FirAnnotationContainer): FirAnnotationContainer
    }

    interface AnonymousFunction : FrontendTransformer {
      fun FirMetaContext.anonymousFunction(anonymousFunction: FirAnonymousFunction): FirStatement
    }

    interface AnonymousFunctionExpression: FrontendTransformer {
      fun FirMetaContext.anonymousFunctionExpression(anonymousFunctionExpression: FirAnonymousFunctionExpression): FirStatement
    }

    interface AnonymousInitializer: FrontendTransformer {
      fun FirMetaContext.anonymousInitializer(anonymousInitializer: FirAnonymousInitializer): FirAnonymousInitializer
    }

    interface AnonymousObject : FrontendTransformer {
      fun FirMetaContext.anonymousObject(anonymousObject: FirAnonymousObject): FirStatement
    }

    interface AnonymousObjectExpression: FrontendTransformer {
      fun FirMetaContext.anonymousObjectExpression(anonymousObjectExpression: FirAnonymousObjectExpression): FirStatement
    }

    interface ArgumentList : FrontendTransformer {
      fun FirMetaContext.argumentList(argumentList: FirArgumentList): FirArgumentList
    }

    interface ArrayOfCall : FrontendTransformer {
      fun FirMetaContext.arrayOfCall(arrayOfCall: FirArrayOfCall): FirStatement
    }

    interface AssignmentOperatorStatement: FrontendTransformer {
      fun FirMetaContext.assignmentOperatorStatement(assignmentOperatorStatement: FirAssignmentOperatorStatement): FirStatement
    }

    interface AugmentedArraySetCall: FrontendTransformer {
      fun FirMetaContext.augmentedArraySetCall(augmentedArraySetCall: FirAugmentedArraySetCall): FirStatement
    }

    interface BackingField : FrontendTransformer {
      fun FirMetaContext.backingField(backingField: FirBackingField): FirStatement
    }

    interface BackingFieldReference: FrontendTransformer {
      fun FirMetaContext.backingFieldReference(backingFieldReference: FirBackingFieldReference): FirReference
    }

    interface BinaryLogicExpression: FrontendTransformer {
      fun FirMetaContext.binaryLogicExpression(binaryLogicExpression: FirBinaryLogicExpression): FirStatement
    }

    interface Block : FrontendTransformer {
      fun FirMetaContext.block(block: FirBlock): FirStatement
    }

    interface BreakExpression : FrontendTransformer {
      fun FirMetaContext.breakExpression(breakExpression: FirBreakExpression): FirStatement
    }

    interface Call : FrontendTransformer {
      fun FirMetaContext.call(call: FirCall): FirStatement
    }

    interface CallableDeclaration: FrontendTransformer {
      fun FirMetaContext.callableDeclaration(callableDeclaration: FirCallableDeclaration): FirCallableDeclaration
    }

    interface CallableReferenceAccess: FrontendTransformer {
      fun FirMetaContext.callableReferenceAccess(callableReferenceAccess: FirCallableReferenceAccess): FirStatement
    }

    interface Catch : FrontendTransformer {
      fun FirMetaContext.catch(catch: FirCatch): FirCatch
    }

    interface CheckNotNullCall : FrontendTransformer {
      fun FirMetaContext.checkNotNullCall(checkNotNullCall: FirCheckNotNullCall): FirStatement
    }

    interface CheckedSafeCallSubject: FrontendTransformer {
      fun FirMetaContext.checkedSafeCallSubject(checkedSafeCallSubject: FirCheckedSafeCallSubject): FirStatement
    }

    interface Class : FrontendTransformer {
      fun FirMetaContext.regularClass(firClass: FirRegularClass): FirStatement
    }

    interface ClassLikeDeclaration: FrontendTransformer {
      fun FirMetaContext.classLikeDeclaration(classLikeDeclaration: FirClassLikeDeclaration): FirStatement
    }

    interface ClassReferenceExpression: FrontendTransformer {
      fun FirMetaContext.classReferenceExpression(classReferenceExpression: FirClassReferenceExpression): FirStatement
    }

    interface ComparisonExpression: FrontendTransformer {
      fun FirMetaContext.comparisonExpression(comparisonExpression: FirComparisonExpression): FirStatement
    }

    interface ComponentCall : FrontendTransformer {
      fun FirMetaContext.componentCall(componentCall: FirComponentCall): FirStatement
    }

    interface ConstExpression : FrontendTransformer {
      fun FirMetaContext.constExpression(constExpression: FirConstExpression<*>): FirStatement
    }

    interface Constructor : FrontendTransformer {
      fun FirMetaContext.constructor(constructor: FirConstructor): FirStatement
    }

    interface ContextReceiver : FrontendTransformer {
      fun FirMetaContext.contextReceiver(contextReceiver: FirContextReceiver): FirContextReceiver
    }

    interface ContextReceiverArgumentListOwner: FrontendTransformer {
      fun FirMetaContext.contextReceiverArgumentListOwner(contextReceiverArgumentListOwner: FirContextReceiverArgumentListOwner): FirContextReceiverArgumentListOwner
    }

    interface ContinueExpression : FrontendTransformer {
      fun FirMetaContext.continueExpression(continueExpression: FirContinueExpression): FirStatement
    }

    interface ContractDescription: FrontendTransformer {
      fun FirMetaContext.contractDescription(contractDescription: FirContractDescription): FirContractDescription
    }

    interface ContractDescriptionOwner: FrontendTransformer {
      fun FirMetaContext.contractDescriptionOwner(contractDescriptionOwner: FirContractDescriptionOwner): FirContractDescriptionOwner
    }

    interface ControlFlowGraphOwner: FrontendTransformer {
      fun FirMetaContext.controlFlowGraphOwner(controlFlowGraphOwner: FirControlFlowGraphOwner): FirControlFlowGraphOwner
    }

    interface ControlFlowGraphReference: FrontendTransformer {
      fun FirMetaContext.controlFlowGraphReference(controlFlowGraphReference: FirControlFlowGraphReference): FirReference
    }

    interface Declaration : FrontendTransformer {
      fun FirMetaContext.declaration(declaration: FirDeclaration): FirDeclaration
    }

    interface DeclarationStatus : FrontendTransformer {
      fun FirMetaContext.declarationStatus(declarationStatus: FirDeclarationStatus): FirDeclarationStatus
    }

    interface DelegateFieldReference: FrontendTransformer {
      fun FirMetaContext.delegateFieldReference(delegateFieldReference: FirDelegateFieldReference): FirReference
    }

    interface DelegatedConstructorCall: FrontendTransformer {
      fun FirMetaContext.delegatedConstructorCall(delegatedConstructorCall: FirDelegatedConstructorCall): FirStatement
    }

    interface DiagnosticHolder : FrontendTransformer {
      fun FirMetaContext.diagnosticHolder(diagnosticHolder: FirDiagnosticHolder): FirDiagnosticHolder
    }

    interface DoWhileLoop : FrontendTransformer {
      fun FirMetaContext.doWhileLoop(doWhileLoop: FirDoWhileLoop): FirStatement
    }

    interface DynamicTypeRef : FrontendTransformer {
      fun FirMetaContext.dynamicTypeRef(dynamicTypeRef: FirDynamicTypeRef): FirTypeRef
    }

    interface EffectDeclaration : FrontendTransformer {
      fun FirMetaContext.effectDeclaration(effectDeclaration: FirEffectDeclaration): FirEffectDeclaration
    }

    interface ElvisExpression : FrontendTransformer {
      fun FirMetaContext.elvisExpression(elvisExpression: FirElvisExpression): FirStatement
    }

    interface EnumEntry : FrontendTransformer {
      fun FirMetaContext.enumEntry(enumEntry: FirEnumEntry): FirStatement
    }

    interface EqualityOperatorCall: FrontendTransformer {
      fun FirMetaContext.equalityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall): FirStatement
    }

    interface ErrorAnnotationCall : FrontendTransformer {
      fun FirMetaContext.errorAnnotationCall(errorAnnotationCall: FirErrorAnnotationCall): FirStatement
    }

    interface ErrorExpression : FrontendTransformer {
      fun FirMetaContext.errorExpression(errorExpression: FirErrorExpression): FirStatement
    }

    interface ErrorFunction : FrontendTransformer {
      fun FirMetaContext.errorFunction(errorFunction: FirErrorFunction): FirStatement
    }

    interface ErrorImport : FrontendTransformer {
      fun FirMetaContext.errorImport(errorImport: FirErrorImport): FirImport
    }

    interface ErrorLoop : FrontendTransformer {
      fun FirMetaContext.errorLoop(errorLoop: FirErrorLoop): FirStatement
    }

    interface ErrorNamedReference : FrontendTransformer {
      fun FirMetaContext.errorNamedReference(errorNamedReference: FirErrorNamedReference): FirReference
    }

    interface ErrorProperty : FrontendTransformer {
      fun FirMetaContext.errorProperty(errorProperty: FirErrorProperty): FirStatement
    }

    interface ErrorResolvedQualifier: FrontendTransformer {
      fun FirMetaContext.errorResolvedQualifier(errorResolvedQualifier: FirErrorResolvedQualifier): FirStatement
    }

    interface ErrorTypeRef : FrontendTransformer {
      fun FirMetaContext.errorTypeRef(errorTypeRef: FirErrorTypeRef): FirTypeRef
    }

    interface Expression : FrontendTransformer {
      fun FirMetaContext.expression(expression: FirExpression): FirStatement
    }

    interface Field : FrontendTransformer {
      fun FirMetaContext.field(field: FirField): FirStatement
    }

    interface File : FrontendTransformer {
      fun FirMetaContext.file(file: FirFile): FirFile
    }

    interface Function : FrontendTransformer {
      fun FirMetaContext.function(function: FirFunction): FirStatement
    }

    interface FunctionCall : FrontendTransformer {
      fun FirMetaContext.functionCall(functionCall: FirFunctionCall): FirStatement
    }

    interface FunctionTypeRef : FrontendTransformer {
      fun FirMetaContext.functionTypeRef(functionTypeRef: FirFunctionTypeRef): FirTypeRef
    }

    interface GetClassCall : FrontendTransformer {
      fun FirMetaContext.getClassCall(getClassCall: FirGetClassCall): FirStatement
    }

    interface ImplicitInvokeCall : FrontendTransformer {
      fun FirMetaContext.implicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall): FirStatement
    }

    interface ImplicitTypeRef : FrontendTransformer {
      fun FirMetaContext.implicitTypeRef(implicitTypeRef: FirImplicitTypeRef): FirTypeRef
    }

    interface Import : FrontendTransformer {
      fun FirMetaContext.import(import: FirImport): FirImport
    }

    interface IntegerLiteralOperatorCall: FrontendTransformer {
      fun FirMetaContext.integerLiteralOperatorCall(integerLiteralOperatorCall: FirIntegerLiteralOperatorCall): FirStatement
    }

    interface IntersectionTypeRef : FrontendTransformer {
      fun FirMetaContext.intersectionTypeRef(intersectionTypeRef: FirIntersectionTypeRef): FirTypeRef
    }

    interface Jump : FrontendTransformer {
      fun FirMetaContext.jump(jump: FirJump<*>): FirStatement
    }

    interface Label : FrontendTransformer {
      fun FirMetaContext.label(label: FirLabel): FirLabel
    }

    interface LambdaArgumentExpression: FrontendTransformer {
      fun FirMetaContext.lambdaArgumentExpression(lambdaArgumentExpression: FirLambdaArgumentExpression): FirStatement
    }

    interface LegacyRawContractDescription: FrontendTransformer {
      fun FirMetaContext.legacyRawContractDescription(legacyRawContractDescription: FirLegacyRawContractDescription): FirContractDescription
    }

    interface Loop : FrontendTransformer {
      fun FirMetaContext.loop(loop: FirLoop): FirStatement
    }

    interface LoopJump : FrontendTransformer {
      fun FirMetaContext.loopJump(loopJump: FirLoopJump): FirStatement
    }

    interface MemberDeclaration : FrontendTransformer {
      fun FirMetaContext.memberDeclaration(memberDeclaration: FirMemberDeclaration): FirMemberDeclaration
    }

    interface NamedArgumentExpression: FrontendTransformer {
      fun FirMetaContext.namedArgumentExpression(namedArgumentExpression: FirNamedArgumentExpression): FirStatement
    }

    interface NamedReference : FrontendTransformer {
      fun FirMetaContext.namedReference(namedReference: FirNamedReference): FirReference
    }

    interface PackageDirective : FrontendTransformer {
      fun FirMetaContext.packageDirective(packageDirective: FirPackageDirective): FirPackageDirective
    }

    interface PlaceholderProjection: FrontendTransformer {
      fun FirMetaContext.placeholderProjection(placeholderProjection: FirPlaceholderProjection): FirTypeProjection
    }

    interface Property : FrontendTransformer {
      fun FirMetaContext.property(property: FirProperty): FirStatement
    }

    interface PropertyAccessExpression: FrontendTransformer {
      fun FirMetaContext.propertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression): FirStatement
    }

    interface PropertyAccessor : FrontendTransformer {
      fun FirMetaContext.propertyAccessor(propertyAccessor: FirPropertyAccessor): FirStatement
    }

    interface QualifiedAccess : FrontendTransformer {
      fun FirMetaContext.qualifiedAccess(qualifiedAccess: FirQualifiedAccess): FirStatement
    }

    interface QualifiedAccessExpression: FrontendTransformer {
      fun FirMetaContext.qualifiedAccessExpression(qualifiedAccessExpression: FirQualifiedAccessExpression): FirStatement
    }

    interface RawContractDescription: FrontendTransformer {
      fun FirMetaContext.rawContractDescription(rawContractDescription: FirRawContractDescription): FirContractDescription
    }

    interface Reference : FrontendTransformer {
      fun FirMetaContext.reference(reference: FirReference): FirReference
    }

    interface RegularClass : FrontendTransformer {
      fun FirMetaContext.regularClass(regularClass: FirRegularClass): FirStatement
    }

    interface Resolvable : FrontendTransformer {
      fun FirMetaContext.resolvable(resolvable: FirResolvable): FirResolvable
    }

    interface ResolvedCallableReference: FrontendTransformer {
      fun FirMetaContext.resolvedCallableReference(resolvedCallableReference: FirResolvedCallableReference): FirReference
    }

    interface ResolvedContractDescription: FrontendTransformer {
      fun FirMetaContext.resolvedContractDescription(resolvedContractDescription: FirResolvedContractDescription): FirContractDescription
    }

    interface ResolvedDeclarationStatus: FrontendTransformer {
      fun FirMetaContext.resolvedDeclarationStatus(resolvedDeclarationStatus: FirResolvedDeclarationStatus): FirDeclarationStatus
    }

    interface ResolvedImport : FrontendTransformer {
      fun FirMetaContext.resolvedImport(resolvedImport: FirResolvedImport): FirImport
    }

    interface ResolvedNamedReference: FrontendTransformer {
      fun FirMetaContext.resolvedNamedReference(resolvedNamedReference: FirResolvedNamedReference): FirReference
    }

    interface ResolvedQualifier : FrontendTransformer {
      fun FirMetaContext.resolvedQualifier(resolvedQualifier: FirResolvedQualifier): FirStatement
    }

    interface ResolvedReifiedParameterReference: FrontendTransformer {
      fun FirMetaContext.resolvedReifiedParameterReference(resolvedReifiedParameterReference: FirResolvedReifiedParameterReference): FirStatement
    }

    interface ResolvedTypeRef : FrontendTransformer {
      fun FirMetaContext.resolvedTypeRef(resolvedTypeRef: FirResolvedTypeRef): FirTypeRef
    }

    interface ReturnExpression : FrontendTransformer {
      fun FirMetaContext.returnExpression(returnExpression: FirReturnExpression): FirStatement
    }

    interface SafeCallExpression : FrontendTransformer {
      fun FirMetaContext.safeCallExpression(safeCallExpression: FirSafeCallExpression): FirStatement
    }

    interface SimpleFunction : FrontendTransformer {
      fun FirMetaContext.simpleFunction(simpleFunction: FirSimpleFunction): FirStatement
    }

    interface SmartCastExpression : FrontendTransformer {
      fun FirMetaContext.smartCastExpression(smartCastExpression: FirSmartCastExpression): FirStatement
    }

    interface SpreadArgumentExpression: FrontendTransformer {
      fun FirMetaContext.spreadArgumentExpression(spreadArgumentExpression: FirSpreadArgumentExpression): FirStatement
    }

    interface StarProjection : FrontendTransformer {
      fun FirMetaContext.starProjection(starProjection: FirStarProjection): FirTypeProjection
    }

    interface Statement : FrontendTransformer {
      fun FirMetaContext.statement(statement: FirStatement): FirStatement
    }

    interface StringConcatenationCall: FrontendTransformer {
      fun FirMetaContext.stringConcatenationCall(stringConcatenationCall: FirStringConcatenationCall): FirStatement
    }

    interface SuperReference : FrontendTransformer {
      fun FirMetaContext.superReference(superReference: FirSuperReference): FirReference
    }

    interface TargetElement : FrontendTransformer {
      fun FirMetaContext.targetElement(targetElement: FirTargetElement): FirTargetElement
    }

    interface ThisReceiverExpression: FrontendTransformer {
      fun FirMetaContext.thisReceiverExpression(thisReceiverExpression: FirThisReceiverExpression): FirStatement
    }

    interface ThisReference : FrontendTransformer {
      fun FirMetaContext.thisReference(thisReference: FirThisReference): FirReference
    }

    interface ThrowExpression : FrontendTransformer {
      fun FirMetaContext.throwExpression(throwExpression: FirThrowExpression): FirStatement
    }

    interface TryExpression : FrontendTransformer {
      fun FirMetaContext.tryExpression(tryExpression: FirTryExpression): FirStatement
    }

    interface TypeAlias : FrontendTransformer {
      fun FirMetaContext.typeAlias(typeAlias: FirTypeAlias): FirStatement
    }

    interface TypeOperatorCall : FrontendTransformer {
      fun FirMetaContext.typeOperatorCall(typeOperatorCall: FirTypeOperatorCall): FirStatement
    }

    interface TypeParameter : FrontendTransformer {
      fun FirMetaContext.typeParameter(typeParameter: FirTypeParameter): FirTypeParameterRef
    }

    interface TypeParameterRef : FrontendTransformer {
      fun FirMetaContext.typeParameterRef(typeParameterRef: FirTypeParameterRef): FirTypeParameterRef
    }

    interface TypeParameterRefsOwner: FrontendTransformer {
      fun FirMetaContext.typeParameterRefsOwner(typeParameterRefsOwner: FirTypeParameterRefsOwner): FirTypeParameterRefsOwner
    }

    interface TypeParametersOwner: FrontendTransformer {
      fun FirMetaContext.typeParametersOwner(typeParametersOwner: FirTypeParametersOwner): FirTypeParametersOwner
    }

    interface TypeProjection : FrontendTransformer {
      fun FirMetaContext.typeProjection(typeProjection: FirTypeProjection): FirTypeProjection
    }

    interface TypeProjectionWithVariance: FrontendTransformer {
      fun FirMetaContext.typeProjectionWithVariance(typeProjectionWithVariance: FirTypeProjectionWithVariance): FirTypeProjection
    }

    interface TypeRef : FrontendTransformer {
      fun FirMetaContext.typeRef(typeRef: FirTypeRef): FirTypeRef
    }

    interface TypeRefWithNullability: FrontendTransformer {
      fun FirMetaContext.typeRefWithNullability(typeRefWithNullability: FirTypeRefWithNullability): FirTypeRef
    }

    interface UserTypeRef : FrontendTransformer {
      fun FirMetaContext.userTypeRef(userTypeRef: FirUserTypeRef): FirTypeRef
    }

    interface ValueParameter : FrontendTransformer {
      fun FirMetaContext.valueParameter(valueParameter: FirValueParameter): FirStatement
    }

    interface VarargArgumentsExpression: FrontendTransformer {
      fun FirMetaContext.varargArgumentsExpression(varargArgumentsExpression: FirVarargArgumentsExpression): FirStatement
    }

    interface Variable : FrontendTransformer {
      fun FirMetaContext.variable(variable: FirVariable): FirStatement
    }

    interface VariableAssignment : FrontendTransformer {
      fun FirMetaContext.variableAssignment(variableAssignment: FirVariableAssignment): FirStatement
    }

    interface WhenBranch : FrontendTransformer {
      fun FirMetaContext.whenBranch(whenBranch: FirWhenBranch): FirWhenBranch
    }

    interface WhenExpression : FrontendTransformer {
      fun FirMetaContext.whenExpression(whenExpression: FirWhenExpression): FirStatement
    }

    interface WhenSubjectExpression: FrontendTransformer {
      fun FirMetaContext.whenSubjectExpression(whenSubjectExpression: FirWhenSubjectExpression): FirStatement
    }

    interface WhileLoop : FrontendTransformer {
      fun FirMetaContext.whileLoop(whileLoop: FirWhileLoop): FirStatement
    }

    interface WrappedArgumentExpression: FrontendTransformer {
      fun FirMetaContext.wrappedArgumentExpression(wrappedArgumentExpression: FirWrappedArgumentExpression): FirStatement
    }

    interface WrappedDelegateExpression: FrontendTransformer {
      fun FirMetaContext.wrappedDelegateExpression(wrappedDelegateExpression: FirWrappedDelegateExpression): FirStatement
    }

    interface WrappedExpression : FrontendTransformer {
      fun FirMetaContext.wrappedExpression(wrappedExpression: FirWrappedExpression): FirStatement
    }
  }

  sealed interface Generate {

    sealed interface TopLevel : Generate {
      interface Class : TopLevel {
        fun FirMetaContext.classes(): Set<ClassId>
        fun FirMetaContext.classes(classId: ClassId): FirClass
      }

      interface Functions : TopLevel {
        fun FirMetaContext.functions(): Set<CallableId>
        fun FirMetaContext.functions(callableId: CallableId): List<FirSimpleFunction>
      }

      interface Properties : TopLevel {
        fun FirMetaContext.properties(): Set<CallableId>
        fun FirMetaContext.properties(callableId: CallableId): List<FirProperty>
      }
    }

    sealed interface Members : Generate {
      interface Constructors : Members {
        fun FirMetaContext.constructors(firClass: FirClass): Boolean
        fun FirMetaContext.constructors(context: MemberGenerationContext): List<FirConstructor>
      }

      interface Functions : Members {
        fun FirMetaContext.functions(firClass: FirClass): Set<Name>
        fun FirMetaContext.functions(callableId: CallableId, context: MemberGenerationContext): List<FirFunction>
      }

      interface Properties : Members {
        fun FirMetaContext.properties(firClass: FirClass): Set<Name>
        fun FirMetaContext.properties(callableId: CallableId, context: MemberGenerationContext): List<FirProperty>
      }

      interface NestedClasses : Members {
        fun FirMetaContext.nestedClasses(firClass: FirClass): Set<Name>
        fun FirMetaContext.nestedClasses(callableId: ClassId): List<FirClass>
      }
    }
  }

  sealed interface Transform {

    interface AnonymousInitializer : Transform {
      fun IrMetaContext.transform(declaration: IrAnonymousInitializer): IrStatement
    }

    interface Block : Transform {
      fun IrMetaContext.transform(expression: IrBlock): IrExpression
    }

    interface BlockBody : Transform {
      fun IrMetaContext.transform(body: IrBlockBody): IrBody
    }

    interface Body : Transform {
      fun IrMetaContext.transform(body: IrBody): IrBody
    }

    interface Branch : Transform {
      fun IrMetaContext.transform(branch: IrBranch): IrBranch
    }

    interface Break : Transform {
      fun IrMetaContext.transform(jump: IrBreak): IrExpression
    }

    interface BreakContinue : Transform {
      fun IrMetaContext.transform(jump: IrBreakContinue): IrExpression
    }

    interface Call : Transform {
      fun IrMetaContext.transform(expression: IrCall): IrElement
    }

    interface CallableReference : Transform {
      fun IrMetaContext.transform(expression: IrCallableReference<*>): IrElement
    }

    interface Catch : Transform {
      fun IrMetaContext.transform(aCatch: IrCatch): IrCatch
    }

    interface Class : Transform {
      fun IrMetaContext.transform(declaration: IrClass): IrStatement
    }

    interface ClassReference : Transform {
      fun IrMetaContext.transform(expression: IrClassReference): IrExpression
    }

    interface Composite : Transform {
      fun IrMetaContext.transform(expression: IrComposite): IrExpression
    }

    interface Const : Transform {
      fun IrMetaContext.transform(expression: IrConst<*>): IrExpression
    }

    interface ConstantArray : Transform {
      fun IrMetaContext.transform(expression: IrConstantArray): IrConstantValue
    }

    interface ConstantObject : Transform {
      fun IrMetaContext.transform(expression: IrConstantObject): IrConstantValue
    }

    interface ConstantPrimitive : Transform {
      fun IrMetaContext.transform(expression: IrConstantPrimitive): IrConstantValue
    }

    interface ConstantValue : Transform {
      fun IrMetaContext.transform(expression: IrConstantValue): IrConstantValue
    }

    interface Constructor : Transform {
      fun IrMetaContext.transform(declaration: IrConstructor): IrStatement
    }

    interface ConstructorCall : Transform {
      fun IrMetaContext.transform(expression: IrConstructorCall): IrElement
    }

    interface ContainerExpression : Transform {
      fun IrMetaContext.transform(expression: IrContainerExpression): IrExpression
    }

    interface Continue : Transform {
      fun IrMetaContext.transform(jump: IrContinue): IrExpression
    }

    interface Declaration : Transform {
      fun IrMetaContext.transform(declaration: IrDeclarationBase): IrStatement
    }

    interface DeclarationReference : Transform {
      fun IrMetaContext.transform(expression: IrDeclarationReference): IrExpression
    }

    interface DelegatingConstructorCall : Transform {
      fun IrMetaContext.transform(expression: IrDelegatingConstructorCall): IrElement
    }

    interface DoWhileLoop : Transform {
      fun IrMetaContext.transform(loop: IrDoWhileLoop): IrExpression
    }

    interface DynamicExpression : Transform {
      fun IrMetaContext.transform(expression: IrDynamicExpression): IrExpression
    }

    interface DynamicMemberExpression : Transform {
      fun IrMetaContext.transform(expression: IrDynamicMemberExpression): IrExpression
    }

    interface DynamicOperatorExpression : Transform {
      fun IrMetaContext.transform(expression: IrDynamicOperatorExpression): IrExpression
    }

    interface Element : Transform {
      fun IrMetaContext.transform(element: IrElement): IrElement
    }

    interface ElseBranch : Transform {
      fun IrMetaContext.transform(branch: IrElseBranch): IrElseBranch
    }

    interface EnumConstructorCall : Transform {
      fun IrMetaContext.transform(expression: IrEnumConstructorCall): IrElement
    }

    interface EnumEntry : Transform {
      fun IrMetaContext.transform(declaration: IrEnumEntry): IrStatement
    }

    interface ErrorCallExpression : Transform {
      fun IrMetaContext.transform(expression: IrErrorCallExpression): IrExpression
    }

    interface ErrorDeclaration : Transform {
      fun IrMetaContext.transform(declaration: IrErrorDeclaration): IrStatement
    }

    interface ErrorExpression : Transform {
      fun IrMetaContext.transform(expression: IrErrorExpression): IrExpression
    }

    interface Expression : Transform {
      fun IrMetaContext.transform(expression: IrExpression): IrExpression
    }

    interface ExpressionBody : Transform {
      fun IrMetaContext.transform(body: IrExpressionBody): IrBody
    }

    interface ExternalPackageFragment : Transform {
      fun IrMetaContext.transform(fragment: IrExternalPackageFragment): IrExternalPackageFragment
    }

    interface Field : Transform {
      fun IrMetaContext.transform(declaration: IrField): IrStatement
    }

    interface FieldAccess : Transform {
      fun IrMetaContext.transform(expression: IrFieldAccessExpression): IrExpression
    }

    interface File : Transform {
      fun IrMetaContext.transform(declaration: IrFile): IrFile
    }

    interface Function : Transform {
      fun IrMetaContext.transform(declaration: IrFunction): IrStatement
    }

    interface FunctionAccess : Transform {
      fun IrMetaContext.transform(expression: IrFunctionAccessExpression): IrElement
    }

    interface FunctionExpression : Transform {
      fun IrMetaContext.transform(expression: IrFunctionExpression): IrElement
    }

    interface FunctionReference : Transform {
      fun IrMetaContext.transform(expression: IrFunctionReference): IrElement
    }

    interface GetClass : Transform {
      fun IrMetaContext.transform(expression: IrGetClass): IrExpression
    }

    interface GetEnumValue : Transform {
      fun IrMetaContext.transform(expression: IrGetEnumValue): IrExpression
    }

    interface GetField : Transform {
      fun IrMetaContext.transform(expression: IrGetField): IrExpression
    }

    interface GetObjectValue : Transform {
      fun IrMetaContext.transform(expression: IrGetObjectValue): IrExpression
    }

    interface GetValue : Transform {
      fun IrMetaContext.transform(expression: IrGetValue): IrExpression
    }

    interface InstanceInitializerCall : Transform {
      fun IrMetaContext.transform(expression: IrInstanceInitializerCall): IrExpression
    }

    interface LocalDelegatedProperty : Transform {
      fun IrMetaContext.transform(declaration: IrLocalDelegatedProperty): IrStatement
    }

    interface LocalDelegatedPropertyReference : Transform {
      fun IrMetaContext.transform(declaration: IrLocalDelegatedPropertyReference): IrElement
    }

    interface Loop : Transform {
      fun IrMetaContext.transform(loop: IrLoop): IrExpression
    }

    interface MemberAccess : Transform {
      fun IrMetaContext.transform(expression: IrMemberAccessExpression<*>): IrElement
    }

    interface ModuleFragment : Transform {
      fun IrMetaContext.transform(declaration: IrModuleFragment): IrModuleFragment
    }

    interface PackageFragment : Transform {
      fun IrMetaContext.transform(declaration: IrPackageFragment): IrElement
    }

    interface Property : Transform {
      fun IrMetaContext.transform(declaration: IrProperty): IrStatement
    }

    interface PropertyReference : Transform {
      fun IrMetaContext.transform(expression: IrPropertyReference): IrElement
    }

    interface RawFunctionReference : Transform {
      fun IrMetaContext.transform(expression: IrRawFunctionReference): IrExpression
    }

    interface Return : Transform {
      fun IrMetaContext.transform(expression: IrReturn): IrExpression
    }

    interface Script : Transform {
      fun IrMetaContext.transform(declaration: IrScript): IrStatement
    }

    interface SetField : Transform {
      fun IrMetaContext.transform(expression: IrSetField): IrExpression
    }

    interface SetValue : Transform {
      fun IrMetaContext.transform(expression: IrSetValue): IrExpression
    }

    interface SimpleFunction : Transform {
      fun IrMetaContext.transform(declaration: IrSimpleFunction): IrStatement
    }

    interface SingletonReference : Transform {
      fun IrMetaContext.transform(expression: IrGetSingletonValue): IrExpression
    }

    interface SpreadElement : Transform {
      fun IrMetaContext.transform(spread: IrSpreadElement): IrSpreadElement
    }

    interface StringConcatenation : Transform {
      fun IrMetaContext.transform(expression: IrStringConcatenation): IrExpression
    }

    interface SuspendableExpression : Transform {
      fun IrMetaContext.transform(expression: IrSuspendableExpression): IrExpression
    }

    interface SuspensionPoint : Transform {
      fun IrMetaContext.transform(expression: IrSuspensionPoint): IrExpression
    }

    interface SyntheticBody : Transform {
      fun IrMetaContext.transform(body: IrSyntheticBody): IrBody
    }

    interface Throw : Transform {
      fun IrMetaContext.transform(expression: IrThrow): IrExpression
    }

    interface Try : Transform {
      fun IrMetaContext.transform(aTry: IrTry): IrExpression
    }

    interface TypeAlias : Transform {
      fun IrMetaContext.transform(declaration: IrTypeAlias): IrStatement
    }

    interface TypeOperator : Transform {
      fun IrMetaContext.transform(expression: IrTypeOperatorCall): IrExpression
    }

    interface TypeParameter : Transform {
      fun IrMetaContext.transform(declaration: IrTypeParameter): IrStatement
    }

    interface ValueAccess : Transform {
      fun IrMetaContext.transform(expression: IrValueAccessExpression): IrExpression
    }

    interface ValueParameter : Transform {
      fun IrMetaContext.transform(declaration: IrValueParameter): IrStatement
    }

    interface Vararg : Transform {
      fun IrMetaContext.transform(expression: IrVararg): IrExpression
    }

    interface Variable : Transform {
      fun IrMetaContext.transform(declaration: IrVariable): IrStatement
    }

    interface When : Transform {
      fun IrMetaContext.transform(expression: IrWhen): IrExpression
    }

    interface WhileLoop : Transform {
      fun IrMetaContext.transform(loop: IrWhileLoop): IrExpression
    }
  }
}

