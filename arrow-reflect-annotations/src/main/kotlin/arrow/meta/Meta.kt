package arrow.meta

import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirLabel
import org.jetbrains.kotlin.fir.contracts.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.references.*
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import kotlin.reflect.KClass
import org.jetbrains.kotlin.fir.FirSession

annotation class Meta {

  interface CallInterceptor : FrontendTransformer.FunctionCall {

    abstract val annotation: KClass<*>

    fun <In, Out> intercept(args: List<In>, func: (List<In>) -> Out): Out

    override fun FirMetaCheckerContext.functionCall(functionCall: FirFunctionCall): FirStatement {
      val newCall = if (isDecorated(functionCall, session)) {
        //language=kotlin
        val call: FirCall = decoratedCall(functionCall)
        call
      } else functionCall
      return newCall
    }

    @OptIn(SymbolInternals::class)
    private fun isDecorated(newElement: FirFunctionCall, session: FirSession): Boolean =
      newElement.toResolvedCallableSymbol()?.fir?.annotations?.hasAnnotation(
        classId = ClassId.topLevel(
          FqName(
            annotation.java.canonicalName
          )
        ),
        session = session
      ) == true

    private fun FirMetaContext.decoratedCall(
      newElement: FirFunctionCall
    ): FirCall {
      val args = newElement.arguments
      val argsApplied = args.mapIndexed { n, expr -> "args[$n] as ${+expr.typeRef}" }
      val name = newElement.toResolvedCallableSymbol()?.callableId?.asSingleFqName()?.asString()

      return compile(
        """
             import ${annotation.java.canonicalName}
             val x = ${annotation.java.simpleName}.intercept(listOf(${+args})) { args: List<Any?> -> ${name}(${+argsApplied}) }
             """
      )
    }

  }

  sealed interface Checker {

    interface Declaration<E : FirDeclaration> {
      fun FirMetaCheckerContext.check(declaration: E)
    }

    interface Expression<E : FirStatement> {
      fun FirMetaCheckerContext.check(expression: E)
    }

    interface Type<E : FirTypeRef> {
      fun FirMetaCheckerContext.check(typeRef: E)
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
        @OptIn(SymbolInternals::class)
        fun FirMetaMemberGenerationContext.constructors(firClass: FirClassSymbol<*>): Set<Name> =
          newConstructors(firClass.fir).keys.map { Name.identifier(it) }.toSet()

        @OptIn(SymbolInternals::class)
        fun FirMetaMemberGenerationContext.constructors(callableId: CallableId, context: MemberGenerationContext): List<FirConstructor> {
          val firClass = context.owner.fir
          return newConstructors(firClass).values.map { it().constructor }
        }

        fun FirMetaMemberGenerationContext.newConstructors(firClass: FirClass): Map<String, () -> String>
      }

      interface Functions : Members {

        @OptIn(SymbolInternals::class)
        fun FirMetaMemberGenerationContext.functions(firClass: FirClassSymbol<*>): Set<Name> =
          newFunctions(firClass.fir).keys.map { Name.identifier(it) }.toSet()

        @OptIn(SymbolInternals::class)
        fun FirMetaMemberGenerationContext.functions(callableId: CallableId, context: MemberGenerationContext): List<FirFunction> {
          val firClass = context.owner.fir
          return newFunctions(firClass).values.map { it().function }
        }

        fun FirMetaMemberGenerationContext.newFunctions(firClass: FirClass): Map<String, () -> String>
      }

      interface Properties : Members {
        fun FirMetaMemberGenerationContext.properties(firClass: FirClassSymbol<*>): Set<Name>
        fun FirMetaMemberGenerationContext.properties(callableId: CallableId, context: MemberGenerationContext): List<FirProperty>
      }

      interface NestedClasses : Members {
        fun FirMetaMemberGenerationContext.nestedClasses(firClass: FirClassSymbol<*>): Set<Name>

        fun FirMetaMemberGenerationContext.nestedClasses(classId: ClassId): List<FirClass>
      }
    }
  }

  sealed interface FrontendTransformer {

    interface Declaration : FrontendTransformer {
      fun FirMetaCheckerContext.annotation(annotation: FirDeclaration): FirStatement
    }

    interface Annotation : FrontendTransformer {
      fun FirMetaCheckerContext.annotation(annotation: FirAnnotation): FirStatement
    }

    interface AnnotationCall : FrontendTransformer {
      fun FirMetaCheckerContext.annotationCall(annotationCall: FirAnnotationCall): FirStatement
    }

    interface AnnotationContainer : FrontendTransformer {
      fun FirMetaCheckerContext.annotationContainer(annotationContainer: FirAnnotationContainer): FirAnnotationContainer
    }

    interface AnonymousFunction : FrontendTransformer {
      fun FirMetaCheckerContext.anonymousFunction(anonymousFunction: FirAnonymousFunction): FirStatement
    }

    interface AnonymousFunctionExpression : FrontendTransformer {
      fun FirMetaCheckerContext.anonymousFunctionExpression(anonymousFunctionExpression: FirAnonymousFunctionExpression): FirStatement
    }

    interface AnonymousInitializer : FrontendTransformer {
      fun FirMetaCheckerContext.anonymousInitializer(anonymousInitializer: FirAnonymousInitializer): FirAnonymousInitializer
    }

    interface AnonymousObject : FrontendTransformer {
      fun FirMetaCheckerContext.anonymousObject(anonymousObject: FirAnonymousObject): FirStatement
    }

    interface AnonymousObjectExpression : FrontendTransformer {
      fun FirMetaCheckerContext.anonymousObjectExpression(anonymousObjectExpression: FirAnonymousObjectExpression): FirStatement
    }

    interface ArrayOfCall : FrontendTransformer {
      fun FirMetaCheckerContext.arrayOfCall(arrayOfCall: FirArrayOfCall): FirStatement
    }

    interface AssignmentOperatorStatement : FrontendTransformer {
      fun FirMetaCheckerContext.assignmentOperatorStatement(assignmentOperatorStatement: FirAssignmentOperatorStatement): FirStatement
    }

    interface AugmentedArraySetCall : FrontendTransformer {
      fun FirMetaCheckerContext.augmentedArraySetCall(augmentedArraySetCall: FirAugmentedArraySetCall): FirStatement
    }

    interface BackingField : FrontendTransformer {
      fun FirMetaCheckerContext.backingField(backingField: FirBackingField): FirStatement
    }

    interface BinaryLogicExpression : FrontendTransformer {
      fun FirMetaCheckerContext.binaryLogicExpression(binaryLogicExpression: FirBinaryLogicExpression): FirStatement
    }

    interface Block : FrontendTransformer {
      fun FirMetaCheckerContext.block(block: FirBlock): FirStatement
    }

    interface BreakExpression : FrontendTransformer {
      fun FirMetaCheckerContext.breakExpression(breakExpression: FirBreakExpression): FirStatement
    }

    interface Call : FrontendTransformer {
      fun FirMetaCheckerContext.call(call: FirCall): FirStatement
    }

    interface CallableDeclaration : FrontendTransformer {
      fun FirMetaCheckerContext.callableDeclaration(callableDeclaration: FirCallableDeclaration): FirCallableDeclaration
    }

    interface CallableReferenceAccess : FrontendTransformer {
      fun FirMetaCheckerContext.callableReferenceAccess(callableReferenceAccess: FirCallableReferenceAccess): FirStatement
    }

    interface CheckNotNullCall : FrontendTransformer {
      fun FirMetaCheckerContext.checkNotNullCall(checkNotNullCall: FirCheckNotNullCall): FirStatement
    }

    interface CheckedSafeCallSubject : FrontendTransformer {
      fun FirMetaCheckerContext.checkedSafeCallSubject(checkedSafeCallSubject: FirCheckedSafeCallSubject): FirStatement
    }

    interface Class : FrontendTransformer {
      fun FirMetaCheckerContext.regularClass(firClass: FirRegularClass): FirStatement
    }

    interface ClassLikeDeclaration : FrontendTransformer {
      fun FirMetaCheckerContext.classLikeDeclaration(classLikeDeclaration: FirClassLikeDeclaration): FirStatement
    }

    interface ClassReferenceExpression : FrontendTransformer {
      fun FirMetaCheckerContext.classReferenceExpression(classReferenceExpression: FirClassReferenceExpression): FirStatement
    }

    interface ComparisonExpression : FrontendTransformer {
      fun FirMetaCheckerContext.comparisonExpression(comparisonExpression: FirComparisonExpression): FirStatement
    }

    interface ComponentCall : FrontendTransformer {
      fun FirMetaCheckerContext.componentCall(componentCall: FirComponentCall): FirStatement
    }

    interface ConstExpression : FrontendTransformer {
      fun FirMetaCheckerContext.constExpression(constExpression: FirConstExpression<*>): FirStatement
    }

    interface Constructor : FrontendTransformer {
      fun FirMetaCheckerContext.constructor(constructor: FirConstructor): FirStatement
    }

    interface DelegatedConstructorCall : FrontendTransformer {
      fun FirMetaCheckerContext.delegatedConstructorCall(delegatedConstructorCall: FirDelegatedConstructorCall): FirStatement
    }

    interface DoWhileLoop : FrontendTransformer {
      fun FirMetaCheckerContext.doWhileLoop(doWhileLoop: FirDoWhileLoop): FirStatement
    }

    interface DynamicTypeRef : FrontendTransformer {
      fun FirMetaCheckerContext.dynamicTypeRef(dynamicTypeRef: FirDynamicTypeRef): FirTypeRef
    }

    interface ElvisExpression : FrontendTransformer {
      fun FirMetaCheckerContext.elvisExpression(elvisExpression: FirElvisExpression): FirStatement
    }

    interface EnumEntry : FrontendTransformer {
      fun FirMetaCheckerContext.enumEntry(enumEntry: FirEnumEntry): FirStatement
    }

    interface EqualityOperatorCall : FrontendTransformer {
      fun FirMetaCheckerContext.equalityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall): FirStatement
    }

    interface ErrorAnnotationCall : FrontendTransformer {
      fun FirMetaCheckerContext.errorAnnotationCall(errorAnnotationCall: FirErrorAnnotationCall): FirStatement
    }

    interface ErrorExpression : FrontendTransformer {
      fun FirMetaCheckerContext.errorExpression(errorExpression: FirErrorExpression): FirStatement
    }

    interface ErrorFunction : FrontendTransformer {
      fun FirMetaCheckerContext.errorFunction(errorFunction: FirErrorFunction): FirStatement
    }

    interface ErrorLoop : FrontendTransformer {
      fun FirMetaCheckerContext.errorLoop(errorLoop: FirErrorLoop): FirStatement
    }

    interface ErrorProperty : FrontendTransformer {
      fun FirMetaCheckerContext.errorProperty(errorProperty: FirErrorProperty): FirStatement
    }

    interface ErrorResolvedQualifier : FrontendTransformer {
      fun FirMetaCheckerContext.errorResolvedQualifier(errorResolvedQualifier: FirErrorResolvedQualifier): FirStatement
    }

    interface ErrorTypeRef : FrontendTransformer {
      fun FirMetaCheckerContext.errorTypeRef(errorTypeRef: FirErrorTypeRef): FirTypeRef
    }

    interface Expression : FrontendTransformer {
      fun FirMetaCheckerContext.expression(expression: FirExpression): FirStatement
    }

    interface Field : FrontendTransformer {
      fun FirMetaCheckerContext.field(field: FirField): FirStatement
    }

    interface File : FrontendTransformer {
      fun FirMetaCheckerContext.file(file: FirFile): FirFile
    }

    interface Function : FrontendTransformer {
      fun FirMetaCheckerContext.function(function: FirFunction): FirStatement
    }

    interface FunctionCall : FrontendTransformer {
      fun FirMetaCheckerContext.functionCall(functionCall: FirFunctionCall): FirStatement
    }

    interface FunctionTypeRef : FrontendTransformer {
      fun FirMetaCheckerContext.functionTypeRef(functionTypeRef: FirFunctionTypeRef): FirTypeRef
    }

    interface GetClassCall : FrontendTransformer {
      fun FirMetaCheckerContext.getClassCall(getClassCall: FirGetClassCall): FirStatement
    }

    interface ImplicitInvokeCall : FrontendTransformer {
      fun FirMetaCheckerContext.implicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall): FirStatement
    }

    interface ImplicitTypeRef : FrontendTransformer {
      fun FirMetaCheckerContext.implicitTypeRef(implicitTypeRef: FirImplicitTypeRef): FirTypeRef
    }

    interface IntegerLiteralOperatorCall : FrontendTransformer {
      fun FirMetaCheckerContext.integerLiteralOperatorCall(integerLiteralOperatorCall: FirIntegerLiteralOperatorCall): FirStatement
    }

    interface IntersectionTypeRef : FrontendTransformer {
      fun FirMetaCheckerContext.intersectionTypeRef(intersectionTypeRef: FirIntersectionTypeRef): FirTypeRef
    }

    interface Jump : FrontendTransformer {
      fun FirMetaCheckerContext.jump(jump: FirJump<*>): FirStatement
    }

    interface Label : FrontendTransformer {
      fun FirMetaCheckerContext.label(label: FirLabel): FirLabel
    }

    interface LambdaArgumentExpression : FrontendTransformer {
      fun FirMetaCheckerContext.lambdaArgumentExpression(lambdaArgumentExpression: FirLambdaArgumentExpression): FirStatement
    }

    interface Loop : FrontendTransformer {
      fun FirMetaCheckerContext.loop(loop: FirLoop): FirStatement
    }

    interface LoopJump : FrontendTransformer {
      fun FirMetaCheckerContext.loopJump(loopJump: FirLoopJump): FirStatement
    }

    interface MemberDeclaration : FrontendTransformer {
      fun FirMetaCheckerContext.memberDeclaration(memberDeclaration: FirMemberDeclaration): FirMemberDeclaration
    }

    interface NamedArgumentExpression : FrontendTransformer {
      fun FirMetaCheckerContext.namedArgumentExpression(namedArgumentExpression: FirNamedArgumentExpression): FirStatement
    }

    interface Property : FrontendTransformer {
      fun FirMetaCheckerContext.property(property: FirProperty): FirStatement
    }

    interface PropertyAccessExpression : FrontendTransformer {
      fun FirMetaCheckerContext.propertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression): FirStatement
    }

    interface PropertyAccessor : FrontendTransformer {
      fun FirMetaCheckerContext.propertyAccessor(propertyAccessor: FirPropertyAccessor): FirStatement
    }

    interface QualifiedAccess : FrontendTransformer {
      fun FirMetaCheckerContext.qualifiedAccess(qualifiedAccess: FirQualifiedAccess): FirStatement
    }

    interface QualifiedAccessExpression : FrontendTransformer {
      fun FirMetaCheckerContext.qualifiedAccessExpression(qualifiedAccessExpression: FirQualifiedAccessExpression): FirStatement
    }

    interface RegularClass : FrontendTransformer {
      fun FirMetaCheckerContext.regularClass(regularClass: FirRegularClass): FirStatement
    }

    interface ResolvedQualifier : FrontendTransformer {
      fun FirMetaCheckerContext.resolvedQualifier(resolvedQualifier: FirResolvedQualifier): FirStatement
    }

    interface ResolvedReifiedParameterReference : FrontendTransformer {
      fun FirMetaCheckerContext.resolvedReifiedParameterReference(resolvedReifiedParameterReference: FirResolvedReifiedParameterReference): FirStatement
    }

    interface ResolvedTypeRef : FrontendTransformer {
      fun FirMetaCheckerContext.resolvedTypeRef(resolvedTypeRef: FirResolvedTypeRef): FirTypeRef
    }

    interface ReturnExpression : FrontendTransformer {
      fun FirMetaCheckerContext.returnExpression(returnExpression: FirReturnExpression): FirStatement
    }

    interface SafeCallExpression : FrontendTransformer {
      fun FirMetaCheckerContext.safeCallExpression(safeCallExpression: FirSafeCallExpression): FirStatement
    }

    interface SimpleFunction : FrontendTransformer {
      fun FirMetaCheckerContext.simpleFunction(simpleFunction: FirSimpleFunction): FirStatement
    }

    interface SmartCastExpression : FrontendTransformer {
      fun FirMetaCheckerContext.smartCastExpression(smartCastExpression: FirSmartCastExpression): FirStatement
    }

    interface SpreadArgumentExpression : FrontendTransformer {
      fun FirMetaCheckerContext.spreadArgumentExpression(spreadArgumentExpression: FirSpreadArgumentExpression): FirStatement
    }

    interface Statement : FrontendTransformer {
      fun FirMetaCheckerContext.statement(statement: FirStatement): FirStatement
    }

    interface StringConcatenationCall : FrontendTransformer {
      fun FirMetaCheckerContext.stringConcatenationCall(stringConcatenationCall: FirStringConcatenationCall): FirStatement
    }

    interface ThisReceiverExpression : FrontendTransformer {
      fun FirMetaCheckerContext.thisReceiverExpression(thisReceiverExpression: FirThisReceiverExpression): FirStatement
    }

    interface ThrowExpression : FrontendTransformer {
      fun FirMetaCheckerContext.throwExpression(throwExpression: FirThrowExpression): FirStatement
    }

    interface TryExpression : FrontendTransformer {
      fun FirMetaCheckerContext.tryExpression(tryExpression: FirTryExpression): FirStatement
    }

    interface TypeAlias : FrontendTransformer {
      fun FirMetaCheckerContext.typeAlias(typeAlias: FirTypeAlias): FirStatement
    }

    interface TypeOperatorCall : FrontendTransformer {
      fun FirMetaCheckerContext.typeOperatorCall(typeOperatorCall: FirTypeOperatorCall): FirStatement
    }

    interface TypeParameter : FrontendTransformer {
      fun FirMetaCheckerContext.typeParameter(typeParameter: FirTypeParameter): FirTypeParameterRef
    }

    interface TypeRef : FrontendTransformer {
      fun FirMetaCheckerContext.typeRef(typeRef: FirTypeRef): FirTypeRef
    }

    interface TypeRefWithNullability : FrontendTransformer {
      fun FirMetaCheckerContext.typeRefWithNullability(typeRefWithNullability: FirTypeRefWithNullability): FirTypeRef
    }

    interface UserTypeRef : FrontendTransformer {
      fun FirMetaCheckerContext.userTypeRef(userTypeRef: FirUserTypeRef): FirTypeRef
    }

    interface ValueParameter : FrontendTransformer {
      fun FirMetaCheckerContext.valueParameter(valueParameter: FirValueParameter): FirStatement
    }

    interface VarargArgumentsExpression : FrontendTransformer {
      fun FirMetaCheckerContext.varargArgumentsExpression(varargArgumentsExpression: FirVarargArgumentsExpression): FirStatement
    }

    interface Variable : FrontendTransformer {
      fun FirMetaCheckerContext.variable(variable: FirVariable): FirStatement
    }

    interface VariableAssignment : FrontendTransformer {
      fun FirMetaCheckerContext.variableAssignment(variableAssignment: FirVariableAssignment): FirStatement
    }

    interface WhenExpression : FrontendTransformer {
      fun FirMetaCheckerContext.whenExpression(whenExpression: FirWhenExpression): FirStatement
    }

    interface WhenSubjectExpression : FrontendTransformer {
      fun FirMetaCheckerContext.whenSubjectExpression(whenSubjectExpression: FirWhenSubjectExpression): FirStatement
    }

    interface WhileLoop : FrontendTransformer {
      fun FirMetaCheckerContext.whileLoop(whileLoop: FirWhileLoop): FirStatement
    }

    interface WrappedArgumentExpression : FrontendTransformer {
      fun FirMetaCheckerContext.wrappedArgumentExpression(wrappedArgumentExpression: FirWrappedArgumentExpression): FirStatement
    }

    interface WrappedDelegateExpression : FrontendTransformer {
      fun FirMetaCheckerContext.wrappedDelegateExpression(wrappedDelegateExpression: FirWrappedDelegateExpression): FirStatement
    }

    interface WrappedExpression : FrontendTransformer {
      fun FirMetaCheckerContext.wrappedExpression(wrappedExpression: FirWrappedExpression): FirStatement
    }
  }

}

