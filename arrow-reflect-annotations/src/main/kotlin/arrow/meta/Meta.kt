package arrow.meta

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirLabel
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.contracts.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.references.*
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import kotlin.reflect.KClass

annotation class Meta {

  interface CallInterceptor : FrontendTransformer.FunctionCall {

    abstract val annotation: KClass<*>

    fun <In, Out> intercept(args: List<In>, func: (List<In>) -> Out): Out

    override fun FirMetaContext.functionCall(functionCall: FirFunctionCall, context: CheckerContext): FirStatement {
      val newCall = if (isDecorated(functionCall)) {
        //language=kotlin
        val call: FirCall = decoratedCall(functionCall, context)
        call
      } else functionCall
      return newCall
    }

    @OptIn(SymbolInternals::class)
    private fun isDecorated(newElement: FirFunctionCall): Boolean =
      newElement.toResolvedCallableSymbol()?.fir?.annotations?.hasAnnotation(
        ClassId.topLevel(
          FqName(
            annotation.java.canonicalName
          )
        )
      ) == true

    @OptIn(SymbolInternals::class)
    private fun FirMetaContext.decoratedCall(
      newElement: FirFunctionCall,
      context: CheckerContext
    ): FirCall {
      val args = newElement.arguments
      val argsApplied = args.mapIndexed { n, expr -> "args[$n] as ${+expr.typeRef}" }
      val name = newElement.toResolvedCallableSymbol()?.callableId?.asSingleFqName()?.asString()
      //language=kotlin
      return """| import ${annotation.java.canonicalName}
                | val x = ${annotation.java.simpleName}.intercept(listOf(${+args})) { args: List<Any?> -> ${name}(${+argsApplied}) }
                |""".trimMargin().frontend<FirCall>(context.containingDeclarations)
    }

  }

  sealed interface Checker {

    interface Declaration<E : FirDeclaration> {
      fun FirMetaContext.check(declaration: E, context: CheckerContext, reporter: DiagnosticReporter)
    }

    interface Expression<E : FirStatement> {
      fun FirMetaContext.check(expression: E, context: CheckerContext, reporter: DiagnosticReporter)
    }

    interface Type<E : FirTypeRef> {
      fun FirMetaContext.check(typeRef: E, context: CheckerContext, reporter: DiagnosticReporter)
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

        @OptIn(SymbolInternals::class)
        fun FirMetaContext.functions(firClass: FirClassSymbol<*>): Set<Name> =
          newFunctions(firClass.fir).keys.map { Name.identifier(it) }.toSet()

        @OptIn(SymbolInternals::class)
        fun FirMetaContext.functions(callableId: CallableId, context: MemberGenerationContext): List<FirFunction> {
          val firClass = context.owner.fir
          return newFunctions(firClass).values.map { it().functionIn(firClass) }
        }

        fun FirMetaContext.newFunctions(firClass: FirClass): Map<String, () -> String>
      }

      interface Properties : Members {
        fun FirMetaContext.properties(firClass: FirClassSymbol<*>): Set<Name>
        fun FirMetaContext.properties(callableId: CallableId, context: MemberGenerationContext): List<FirProperty>
      }

      interface NestedClasses : Members {
        fun FirMetaContext.nestedClasses(firClass: FirClassSymbol<*>): Set<Name>
        fun FirMetaContext.nestedClasses(callableId: ClassId): List<FirClass>
      }
    }
  }

  sealed interface FrontendTransformer {

    interface Declaration : FrontendTransformer {
      fun FirMetaContext.annotation(annotation: FirDeclaration, context: CheckerContext): FirStatement
    }

    interface Annotation : FrontendTransformer {
      fun FirMetaContext.annotation(annotation: FirAnnotation, context: CheckerContext): FirStatement
    }

    interface AnnotationCall : FrontendTransformer {
      fun FirMetaContext.annotationCall(annotationCall: FirAnnotationCall, context: CheckerContext): FirStatement
    }

    interface AnnotationContainer: FrontendTransformer {
      fun FirMetaContext.annotationContainer(annotationContainer: FirAnnotationContainer, context: CheckerContext): FirAnnotationContainer
    }

    interface AnonymousFunction : FrontendTransformer {
      fun FirMetaContext.anonymousFunction(anonymousFunction: FirAnonymousFunction, context: CheckerContext): FirStatement
    }

    interface AnonymousFunctionExpression: FrontendTransformer {
      fun FirMetaContext.anonymousFunctionExpression(anonymousFunctionExpression: FirAnonymousFunctionExpression, context: CheckerContext): FirStatement
    }

    interface AnonymousInitializer: FrontendTransformer {
      fun FirMetaContext.anonymousInitializer(anonymousInitializer: FirAnonymousInitializer, context: CheckerContext): FirAnonymousInitializer
    }

    interface AnonymousObject : FrontendTransformer {
      fun FirMetaContext.anonymousObject(anonymousObject: FirAnonymousObject, context: CheckerContext): FirStatement
    }

    interface AnonymousObjectExpression: FrontendTransformer {
      fun FirMetaContext.anonymousObjectExpression(anonymousObjectExpression: FirAnonymousObjectExpression, context: CheckerContext): FirStatement
    }

    interface ArrayOfCall : FrontendTransformer {
      fun FirMetaContext.arrayOfCall(arrayOfCall: FirArrayOfCall, context: CheckerContext): FirStatement
    }

    interface AssignmentOperatorStatement: FrontendTransformer {
      fun FirMetaContext.assignmentOperatorStatement(assignmentOperatorStatement: FirAssignmentOperatorStatement, context: CheckerContext): FirStatement
    }

    interface AugmentedArraySetCall: FrontendTransformer {
      fun FirMetaContext.augmentedArraySetCall(augmentedArraySetCall: FirAugmentedArraySetCall, context: CheckerContext): FirStatement
    }

    interface BackingField : FrontendTransformer {
      fun FirMetaContext.backingField(backingField: FirBackingField, context: CheckerContext): FirStatement
    }

    interface BinaryLogicExpression: FrontendTransformer {
      fun FirMetaContext.binaryLogicExpression(binaryLogicExpression: FirBinaryLogicExpression, context: CheckerContext): FirStatement
    }

    interface Block : FrontendTransformer {
      fun FirMetaContext.block(block: FirBlock, context: CheckerContext): FirStatement
    }

    interface BreakExpression : FrontendTransformer {
      fun FirMetaContext.breakExpression(breakExpression: FirBreakExpression, context: CheckerContext): FirStatement
    }

    interface Call : FrontendTransformer {
      fun FirMetaContext.call(call: FirCall, context: CheckerContext): FirStatement
    }

    interface CallableDeclaration: FrontendTransformer {
      fun FirMetaContext.callableDeclaration(callableDeclaration: FirCallableDeclaration, context: CheckerContext): FirCallableDeclaration
    }

    interface CallableReferenceAccess: FrontendTransformer {
      fun FirMetaContext.callableReferenceAccess(callableReferenceAccess: FirCallableReferenceAccess, context: CheckerContext): FirStatement
    }

    interface CheckNotNullCall : FrontendTransformer {
      fun FirMetaContext.checkNotNullCall(checkNotNullCall: FirCheckNotNullCall, context: CheckerContext): FirStatement
    }

    interface CheckedSafeCallSubject: FrontendTransformer {
      fun FirMetaContext.checkedSafeCallSubject(checkedSafeCallSubject: FirCheckedSafeCallSubject, context: CheckerContext): FirStatement
    }

    interface Class : FrontendTransformer {
      fun FirMetaContext.regularClass(firClass: FirRegularClass, context: CheckerContext): FirStatement
    }

    interface ClassLikeDeclaration: FrontendTransformer {
      fun FirMetaContext.classLikeDeclaration(classLikeDeclaration: FirClassLikeDeclaration, context: CheckerContext): FirStatement
    }

    interface ClassReferenceExpression: FrontendTransformer {
      fun FirMetaContext.classReferenceExpression(classReferenceExpression: FirClassReferenceExpression, context: CheckerContext): FirStatement
    }

    interface ComparisonExpression: FrontendTransformer {
      fun FirMetaContext.comparisonExpression(comparisonExpression: FirComparisonExpression, context: CheckerContext): FirStatement
    }

    interface ComponentCall : FrontendTransformer {
      fun FirMetaContext.componentCall(componentCall: FirComponentCall, context: CheckerContext): FirStatement
    }

    interface ConstExpression : FrontendTransformer {
      fun FirMetaContext.constExpression(constExpression: FirConstExpression<*>, context: CheckerContext): FirStatement
    }

    interface Constructor : FrontendTransformer {
      fun FirMetaContext.constructor(constructor: FirConstructor, context: CheckerContext): FirStatement
    }

    interface DelegatedConstructorCall: FrontendTransformer {
      fun FirMetaContext.delegatedConstructorCall(delegatedConstructorCall: FirDelegatedConstructorCall, context: CheckerContext): FirStatement
    }

    interface DoWhileLoop : FrontendTransformer {
      fun FirMetaContext.doWhileLoop(doWhileLoop: FirDoWhileLoop, context: CheckerContext): FirStatement
    }

    interface DynamicTypeRef : FrontendTransformer {
      fun FirMetaContext.dynamicTypeRef(dynamicTypeRef: FirDynamicTypeRef, context: CheckerContext): FirTypeRef
    }

    interface ElvisExpression : FrontendTransformer {
      fun FirMetaContext.elvisExpression(elvisExpression: FirElvisExpression, context: CheckerContext): FirStatement
    }

    interface EnumEntry : FrontendTransformer {
      fun FirMetaContext.enumEntry(enumEntry: FirEnumEntry, context: CheckerContext): FirStatement
    }

    interface EqualityOperatorCall: FrontendTransformer {
      fun FirMetaContext.equalityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall, context: CheckerContext): FirStatement
    }

    interface ErrorAnnotationCall : FrontendTransformer {
      fun FirMetaContext.errorAnnotationCall(errorAnnotationCall: FirErrorAnnotationCall, context: CheckerContext): FirStatement
    }

    interface ErrorExpression : FrontendTransformer {
      fun FirMetaContext.errorExpression(errorExpression: FirErrorExpression, context: CheckerContext): FirStatement
    }

    interface ErrorFunction : FrontendTransformer {
      fun FirMetaContext.errorFunction(errorFunction: FirErrorFunction, context: CheckerContext): FirStatement
    }

    interface ErrorLoop : FrontendTransformer {
      fun FirMetaContext.errorLoop(errorLoop: FirErrorLoop, context: CheckerContext): FirStatement
    }

    interface ErrorProperty : FrontendTransformer {
      fun FirMetaContext.errorProperty(errorProperty: FirErrorProperty, context: CheckerContext): FirStatement
    }

    interface ErrorResolvedQualifier: FrontendTransformer {
      fun FirMetaContext.errorResolvedQualifier(errorResolvedQualifier: FirErrorResolvedQualifier, context: CheckerContext): FirStatement
    }

    interface ErrorTypeRef : FrontendTransformer {
      fun FirMetaContext.errorTypeRef(errorTypeRef: FirErrorTypeRef, context: CheckerContext): FirTypeRef
    }

    interface Expression : FrontendTransformer {
      fun FirMetaContext.expression(expression: FirExpression, context: CheckerContext): FirStatement
    }

    interface Field : FrontendTransformer {
      fun FirMetaContext.field(field: FirField, context: CheckerContext): FirStatement
    }

    interface File : FrontendTransformer {
      fun FirMetaContext.file(file: FirFile, context: CheckerContext): FirFile
    }

    interface Function : FrontendTransformer {
      fun FirMetaContext.function(function: FirFunction, context: CheckerContext): FirStatement
    }

    interface FunctionCall : FrontendTransformer {
      fun FirMetaContext.functionCall(functionCall: FirFunctionCall, context: CheckerContext): FirStatement
    }

    interface FunctionTypeRef : FrontendTransformer {
      fun FirMetaContext.functionTypeRef(functionTypeRef: FirFunctionTypeRef, context: CheckerContext): FirTypeRef
    }

    interface GetClassCall : FrontendTransformer {
      fun FirMetaContext.getClassCall(getClassCall: FirGetClassCall, context: CheckerContext): FirStatement
    }

    interface ImplicitInvokeCall : FrontendTransformer {
      fun FirMetaContext.implicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall, context: CheckerContext): FirStatement
    }

    interface ImplicitTypeRef : FrontendTransformer {
      fun FirMetaContext.implicitTypeRef(implicitTypeRef: FirImplicitTypeRef, context: CheckerContext): FirTypeRef
    }

    interface IntegerLiteralOperatorCall: FrontendTransformer {
      fun FirMetaContext.integerLiteralOperatorCall(integerLiteralOperatorCall: FirIntegerLiteralOperatorCall, context: CheckerContext): FirStatement
    }

    interface IntersectionTypeRef : FrontendTransformer {
      fun FirMetaContext.intersectionTypeRef(intersectionTypeRef: FirIntersectionTypeRef, context: CheckerContext): FirTypeRef
    }

    interface Jump : FrontendTransformer {
      fun FirMetaContext.jump(jump: FirJump<*>, context: CheckerContext): FirStatement
    }

    interface Label : FrontendTransformer {
      fun FirMetaContext.label(label: FirLabel, context: CheckerContext): FirLabel
    }

    interface LambdaArgumentExpression: FrontendTransformer {
      fun FirMetaContext.lambdaArgumentExpression(lambdaArgumentExpression: FirLambdaArgumentExpression, context: CheckerContext): FirStatement
    }

    interface Loop : FrontendTransformer {
      fun FirMetaContext.loop(loop: FirLoop, context: CheckerContext): FirStatement
    }

    interface LoopJump : FrontendTransformer {
      fun FirMetaContext.loopJump(loopJump: FirLoopJump, context: CheckerContext): FirStatement
    }

    interface MemberDeclaration : FrontendTransformer {
      fun FirMetaContext.memberDeclaration(memberDeclaration: FirMemberDeclaration, context: CheckerContext): FirMemberDeclaration
    }

    interface NamedArgumentExpression: FrontendTransformer {
      fun FirMetaContext.namedArgumentExpression(namedArgumentExpression: FirNamedArgumentExpression, context: CheckerContext): FirStatement
    }

    interface Property : FrontendTransformer {
      fun FirMetaContext.property(property: FirProperty, context: CheckerContext): FirStatement
    }

    interface PropertyAccessExpression: FrontendTransformer {
      fun FirMetaContext.propertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression, context: CheckerContext): FirStatement
    }

    interface PropertyAccessor : FrontendTransformer {
      fun FirMetaContext.propertyAccessor(propertyAccessor: FirPropertyAccessor, context: CheckerContext): FirStatement
    }

    interface QualifiedAccess : FrontendTransformer {
      fun FirMetaContext.qualifiedAccess(qualifiedAccess: FirQualifiedAccess, context: CheckerContext): FirStatement
    }

    interface QualifiedAccessExpression: FrontendTransformer {
      fun FirMetaContext.qualifiedAccessExpression(qualifiedAccessExpression: FirQualifiedAccessExpression, context: CheckerContext): FirStatement
    }

    interface RegularClass : FrontendTransformer {
      fun FirMetaContext.regularClass(regularClass: FirRegularClass, context: CheckerContext): FirStatement
    }

    interface ResolvedQualifier : FrontendTransformer {
      fun FirMetaContext.resolvedQualifier(resolvedQualifier: FirResolvedQualifier, context: CheckerContext): FirStatement
    }

    interface ResolvedReifiedParameterReference: FrontendTransformer {
      fun FirMetaContext.resolvedReifiedParameterReference(resolvedReifiedParameterReference: FirResolvedReifiedParameterReference, context: CheckerContext): FirStatement
    }

    interface ResolvedTypeRef : FrontendTransformer {
      fun FirMetaContext.resolvedTypeRef(resolvedTypeRef: FirResolvedTypeRef, context: CheckerContext): FirTypeRef
    }

    interface ReturnExpression : FrontendTransformer {
      fun FirMetaContext.returnExpression(returnExpression: FirReturnExpression, context: CheckerContext): FirStatement
    }

    interface SafeCallExpression : FrontendTransformer {
      fun FirMetaContext.safeCallExpression(safeCallExpression: FirSafeCallExpression, context: CheckerContext): FirStatement
    }

    interface SimpleFunction : FrontendTransformer {
      fun FirMetaContext.simpleFunction(simpleFunction: FirSimpleFunction, context: CheckerContext): FirStatement
    }

    interface SmartCastExpression : FrontendTransformer {
      fun FirMetaContext.smartCastExpression(smartCastExpression: FirSmartCastExpression, context: CheckerContext): FirStatement
    }

    interface SpreadArgumentExpression: FrontendTransformer {
      fun FirMetaContext.spreadArgumentExpression(spreadArgumentExpression: FirSpreadArgumentExpression, context: CheckerContext): FirStatement
    }

    interface Statement : FrontendTransformer {
      fun FirMetaContext.statement(statement: FirStatement, context: CheckerContext): FirStatement
    }

    interface StringConcatenationCall: FrontendTransformer {
      fun FirMetaContext.stringConcatenationCall(stringConcatenationCall: FirStringConcatenationCall, context: CheckerContext): FirStatement
    }

    interface ThisReceiverExpression: FrontendTransformer {
      fun FirMetaContext.thisReceiverExpression(thisReceiverExpression: FirThisReceiverExpression, context: CheckerContext): FirStatement
    }

    interface ThrowExpression : FrontendTransformer {
      fun FirMetaContext.throwExpression(throwExpression: FirThrowExpression, context: CheckerContext): FirStatement
    }

    interface TryExpression : FrontendTransformer {
      fun FirMetaContext.tryExpression(tryExpression: FirTryExpression, context: CheckerContext): FirStatement
    }

    interface TypeAlias : FrontendTransformer {
      fun FirMetaContext.typeAlias(typeAlias: FirTypeAlias, context: CheckerContext): FirStatement
    }

    interface TypeOperatorCall : FrontendTransformer {
      fun FirMetaContext.typeOperatorCall(typeOperatorCall: FirTypeOperatorCall, context: CheckerContext): FirStatement
    }

    interface TypeParameter : FrontendTransformer {
      fun FirMetaContext.typeParameter(typeParameter: FirTypeParameter, context: CheckerContext): FirTypeParameterRef
    }

    interface TypeRef : FrontendTransformer {
      fun FirMetaContext.typeRef(typeRef: FirTypeRef, context: CheckerContext): FirTypeRef
    }

    interface TypeRefWithNullability: FrontendTransformer {
      fun FirMetaContext.typeRefWithNullability(typeRefWithNullability: FirTypeRefWithNullability, context: CheckerContext): FirTypeRef
    }

    interface UserTypeRef : FrontendTransformer {
      fun FirMetaContext.userTypeRef(userTypeRef: FirUserTypeRef, context: CheckerContext): FirTypeRef
    }

    interface ValueParameter : FrontendTransformer {
      fun FirMetaContext.valueParameter(valueParameter: FirValueParameter, context: CheckerContext): FirStatement
    }

    interface VarargArgumentsExpression: FrontendTransformer {
      fun FirMetaContext.varargArgumentsExpression(varargArgumentsExpression: FirVarargArgumentsExpression, context: CheckerContext): FirStatement
    }

    interface Variable : FrontendTransformer {
      fun FirMetaContext.variable(variable: FirVariable, context: CheckerContext): FirStatement
    }

    interface VariableAssignment : FrontendTransformer {
      fun FirMetaContext.variableAssignment(variableAssignment: FirVariableAssignment, context: CheckerContext): FirStatement
    }

    interface WhenExpression : FrontendTransformer {
      fun FirMetaContext.whenExpression(whenExpression: FirWhenExpression, context: CheckerContext): FirStatement
    }

    interface WhenSubjectExpression: FrontendTransformer {
      fun FirMetaContext.whenSubjectExpression(whenSubjectExpression: FirWhenSubjectExpression, context: CheckerContext): FirStatement
    }

    interface WhileLoop : FrontendTransformer {
      fun FirMetaContext.whileLoop(whileLoop: FirWhileLoop, context: CheckerContext): FirStatement
    }

    interface WrappedArgumentExpression: FrontendTransformer {
      fun FirMetaContext.wrappedArgumentExpression(wrappedArgumentExpression: FirWrappedArgumentExpression, context: CheckerContext): FirStatement
    }

    interface WrappedDelegateExpression: FrontendTransformer {
      fun FirMetaContext.wrappedDelegateExpression(wrappedDelegateExpression: FirWrappedDelegateExpression, context: CheckerContext): FirStatement
    }

    interface WrappedExpression : FrontendTransformer {
      fun FirMetaContext.wrappedExpression(wrappedExpression: FirWrappedExpression, context: CheckerContext): FirStatement
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

