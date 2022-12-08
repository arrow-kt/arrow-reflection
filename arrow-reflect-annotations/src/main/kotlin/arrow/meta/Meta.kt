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

    override fun FirMetaContext.functionCall(functionCall: FirFunctionCall,context: CheckerContext, reporter: DiagnosticReporter): FirStatement {
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
      fun FirMetaContext.check(declaration: E,context: CheckerContext, reporter: DiagnosticReporter)
    }

    interface Expression<E : FirStatement> {
      fun FirMetaContext.check(expression: E,context: CheckerContext, reporter: DiagnosticReporter)
    }

    interface Type<E : FirTypeRef> {
      fun FirMetaContext.check(typeRef: E,context: CheckerContext, reporter: DiagnosticReporter)
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
      fun FirMetaContext.annotation(annotation: FirDeclaration,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface Annotation : FrontendTransformer {
      fun FirMetaContext.annotation(annotation: FirAnnotation,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface AnnotationCall : FrontendTransformer {
      fun FirMetaContext.annotationCall(annotationCall: FirAnnotationCall,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface AnnotationContainer: FrontendTransformer {
      fun FirMetaContext.annotationContainer(annotationContainer: FirAnnotationContainer,context: CheckerContext, reporter: DiagnosticReporter): FirAnnotationContainer
    }

    interface AnonymousFunction : FrontendTransformer {
      fun FirMetaContext.anonymousFunction(anonymousFunction: FirAnonymousFunction,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface AnonymousFunctionExpression: FrontendTransformer {
      fun FirMetaContext.anonymousFunctionExpression(anonymousFunctionExpression: FirAnonymousFunctionExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface AnonymousInitializer: FrontendTransformer {
      fun FirMetaContext.anonymousInitializer(anonymousInitializer: FirAnonymousInitializer,context: CheckerContext, reporter: DiagnosticReporter): FirAnonymousInitializer
    }

    interface AnonymousObject : FrontendTransformer {
      fun FirMetaContext.anonymousObject(anonymousObject: FirAnonymousObject,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface AnonymousObjectExpression: FrontendTransformer {
      fun FirMetaContext.anonymousObjectExpression(anonymousObjectExpression: FirAnonymousObjectExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ArrayOfCall : FrontendTransformer {
      fun FirMetaContext.arrayOfCall(arrayOfCall: FirArrayOfCall,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface AssignmentOperatorStatement: FrontendTransformer {
      fun FirMetaContext.assignmentOperatorStatement(assignmentOperatorStatement: FirAssignmentOperatorStatement,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface AugmentedArraySetCall: FrontendTransformer {
      fun FirMetaContext.augmentedArraySetCall(augmentedArraySetCall: FirAugmentedArraySetCall,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface BackingField : FrontendTransformer {
      fun FirMetaContext.backingField(backingField: FirBackingField,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface BinaryLogicExpression: FrontendTransformer {
      fun FirMetaContext.binaryLogicExpression(binaryLogicExpression: FirBinaryLogicExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface Block : FrontendTransformer {
      fun FirMetaContext.block(block: FirBlock,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface BreakExpression : FrontendTransformer {
      fun FirMetaContext.breakExpression(breakExpression: FirBreakExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface Call : FrontendTransformer {
      fun FirMetaContext.call(call: FirCall,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface CallableDeclaration: FrontendTransformer {
      fun FirMetaContext.callableDeclaration(callableDeclaration: FirCallableDeclaration,context: CheckerContext, reporter: DiagnosticReporter): FirCallableDeclaration
    }

    interface CallableReferenceAccess: FrontendTransformer {
      fun FirMetaContext.callableReferenceAccess(callableReferenceAccess: FirCallableReferenceAccess,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface CheckNotNullCall : FrontendTransformer {
      fun FirMetaContext.checkNotNullCall(checkNotNullCall: FirCheckNotNullCall,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface CheckedSafeCallSubject: FrontendTransformer {
      fun FirMetaContext.checkedSafeCallSubject(checkedSafeCallSubject: FirCheckedSafeCallSubject,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface Class : FrontendTransformer {
      fun FirMetaContext.regularClass(firClass: FirRegularClass,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ClassLikeDeclaration: FrontendTransformer {
      fun FirMetaContext.classLikeDeclaration(classLikeDeclaration: FirClassLikeDeclaration,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ClassReferenceExpression: FrontendTransformer {
      fun FirMetaContext.classReferenceExpression(classReferenceExpression: FirClassReferenceExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ComparisonExpression: FrontendTransformer {
      fun FirMetaContext.comparisonExpression(comparisonExpression: FirComparisonExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ComponentCall : FrontendTransformer {
      fun FirMetaContext.componentCall(componentCall: FirComponentCall,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ConstExpression : FrontendTransformer {
      fun FirMetaContext.constExpression(constExpression: FirConstExpression<*>,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface Constructor : FrontendTransformer {
      fun FirMetaContext.constructor(constructor: FirConstructor,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface DelegatedConstructorCall: FrontendTransformer {
      fun FirMetaContext.delegatedConstructorCall(delegatedConstructorCall: FirDelegatedConstructorCall,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface DoWhileLoop : FrontendTransformer {
      fun FirMetaContext.doWhileLoop(doWhileLoop: FirDoWhileLoop,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface DynamicTypeRef : FrontendTransformer {
      fun FirMetaContext.dynamicTypeRef(dynamicTypeRef: FirDynamicTypeRef,context: CheckerContext, reporter: DiagnosticReporter): FirTypeRef
    }

    interface ElvisExpression : FrontendTransformer {
      fun FirMetaContext.elvisExpression(elvisExpression: FirElvisExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface EnumEntry : FrontendTransformer {
      fun FirMetaContext.enumEntry(enumEntry: FirEnumEntry,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface EqualityOperatorCall: FrontendTransformer {
      fun FirMetaContext.equalityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ErrorAnnotationCall : FrontendTransformer {
      fun FirMetaContext.errorAnnotationCall(errorAnnotationCall: FirErrorAnnotationCall,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ErrorExpression : FrontendTransformer {
      fun FirMetaContext.errorExpression(errorExpression: FirErrorExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ErrorFunction : FrontendTransformer {
      fun FirMetaContext.errorFunction(errorFunction: FirErrorFunction,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ErrorLoop : FrontendTransformer {
      fun FirMetaContext.errorLoop(errorLoop: FirErrorLoop,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ErrorProperty : FrontendTransformer {
      fun FirMetaContext.errorProperty(errorProperty: FirErrorProperty,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ErrorResolvedQualifier: FrontendTransformer {
      fun FirMetaContext.errorResolvedQualifier(errorResolvedQualifier: FirErrorResolvedQualifier,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ErrorTypeRef : FrontendTransformer {
      fun FirMetaContext.errorTypeRef(errorTypeRef: FirErrorTypeRef,context: CheckerContext, reporter: DiagnosticReporter): FirTypeRef
    }

    interface Expression : FrontendTransformer {
      fun FirMetaContext.expression(expression: FirExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface Field : FrontendTransformer {
      fun FirMetaContext.field(field: FirField,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface File : FrontendTransformer {
      fun FirMetaContext.file(file: FirFile,context: CheckerContext, reporter: DiagnosticReporter): FirFile
    }

    interface Function : FrontendTransformer {
      fun FirMetaContext.function(function: FirFunction,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface FunctionCall : FrontendTransformer {
      fun FirMetaContext.functionCall(functionCall: FirFunctionCall,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface FunctionTypeRef : FrontendTransformer {
      fun FirMetaContext.functionTypeRef(functionTypeRef: FirFunctionTypeRef,context: CheckerContext, reporter: DiagnosticReporter): FirTypeRef
    }

    interface GetClassCall : FrontendTransformer {
      fun FirMetaContext.getClassCall(getClassCall: FirGetClassCall,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ImplicitInvokeCall : FrontendTransformer {
      fun FirMetaContext.implicitInvokeCall(implicitInvokeCall: FirImplicitInvokeCall,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ImplicitTypeRef : FrontendTransformer {
      fun FirMetaContext.implicitTypeRef(implicitTypeRef: FirImplicitTypeRef,context: CheckerContext, reporter: DiagnosticReporter): FirTypeRef
    }

    interface IntegerLiteralOperatorCall: FrontendTransformer {
      fun FirMetaContext.integerLiteralOperatorCall(integerLiteralOperatorCall: FirIntegerLiteralOperatorCall,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface IntersectionTypeRef : FrontendTransformer {
      fun FirMetaContext.intersectionTypeRef(intersectionTypeRef: FirIntersectionTypeRef,context: CheckerContext, reporter: DiagnosticReporter): FirTypeRef
    }

    interface Jump : FrontendTransformer {
      fun FirMetaContext.jump(jump: FirJump<*>,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface Label : FrontendTransformer {
      fun FirMetaContext.label(label: FirLabel,context: CheckerContext, reporter: DiagnosticReporter): FirLabel
    }

    interface LambdaArgumentExpression: FrontendTransformer {
      fun FirMetaContext.lambdaArgumentExpression(lambdaArgumentExpression: FirLambdaArgumentExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface Loop : FrontendTransformer {
      fun FirMetaContext.loop(loop: FirLoop,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface LoopJump : FrontendTransformer {
      fun FirMetaContext.loopJump(loopJump: FirLoopJump,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface MemberDeclaration : FrontendTransformer {
      fun FirMetaContext.memberDeclaration(memberDeclaration: FirMemberDeclaration,context: CheckerContext, reporter: DiagnosticReporter): FirMemberDeclaration
    }

    interface NamedArgumentExpression: FrontendTransformer {
      fun FirMetaContext.namedArgumentExpression(namedArgumentExpression: FirNamedArgumentExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface Property : FrontendTransformer {
      fun FirMetaContext.property(property: FirProperty,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface PropertyAccessExpression: FrontendTransformer {
      fun FirMetaContext.propertyAccessExpression(propertyAccessExpression: FirPropertyAccessExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface PropertyAccessor : FrontendTransformer {
      fun FirMetaContext.propertyAccessor(propertyAccessor: FirPropertyAccessor,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface QualifiedAccess : FrontendTransformer {
      fun FirMetaContext.qualifiedAccess(qualifiedAccess: FirQualifiedAccess,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface QualifiedAccessExpression: FrontendTransformer {
      fun FirMetaContext.qualifiedAccessExpression(qualifiedAccessExpression: FirQualifiedAccessExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface RegularClass : FrontendTransformer {
      fun FirMetaContext.regularClass(regularClass: FirRegularClass,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ResolvedQualifier : FrontendTransformer {
      fun FirMetaContext.resolvedQualifier(resolvedQualifier: FirResolvedQualifier,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ResolvedReifiedParameterReference: FrontendTransformer {
      fun FirMetaContext.resolvedReifiedParameterReference(resolvedReifiedParameterReference: FirResolvedReifiedParameterReference,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ResolvedTypeRef : FrontendTransformer {
      fun FirMetaContext.resolvedTypeRef(resolvedTypeRef: FirResolvedTypeRef,context: CheckerContext, reporter: DiagnosticReporter): FirTypeRef
    }

    interface ReturnExpression : FrontendTransformer {
      fun FirMetaContext.returnExpression(returnExpression: FirReturnExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface SafeCallExpression : FrontendTransformer {
      fun FirMetaContext.safeCallExpression(safeCallExpression: FirSafeCallExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface SimpleFunction : FrontendTransformer {
      fun FirMetaContext.simpleFunction(simpleFunction: FirSimpleFunction,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface SmartCastExpression : FrontendTransformer {
      fun FirMetaContext.smartCastExpression(smartCastExpression: FirSmartCastExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface SpreadArgumentExpression: FrontendTransformer {
      fun FirMetaContext.spreadArgumentExpression(spreadArgumentExpression: FirSpreadArgumentExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface Statement : FrontendTransformer {
      fun FirMetaContext.statement(statement: FirStatement,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface StringConcatenationCall: FrontendTransformer {
      fun FirMetaContext.stringConcatenationCall(stringConcatenationCall: FirStringConcatenationCall,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ThisReceiverExpression: FrontendTransformer {
      fun FirMetaContext.thisReceiverExpression(thisReceiverExpression: FirThisReceiverExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface ThrowExpression : FrontendTransformer {
      fun FirMetaContext.throwExpression(throwExpression: FirThrowExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface TryExpression : FrontendTransformer {
      fun FirMetaContext.tryExpression(tryExpression: FirTryExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface TypeAlias : FrontendTransformer {
      fun FirMetaContext.typeAlias(typeAlias: FirTypeAlias,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface TypeOperatorCall : FrontendTransformer {
      fun FirMetaContext.typeOperatorCall(typeOperatorCall: FirTypeOperatorCall,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface TypeParameter : FrontendTransformer {
      fun FirMetaContext.typeParameter(typeParameter: FirTypeParameter,context: CheckerContext, reporter: DiagnosticReporter): FirTypeParameterRef
    }

    interface TypeRef : FrontendTransformer {
      fun FirMetaContext.typeRef(typeRef: FirTypeRef,context: CheckerContext, reporter: DiagnosticReporter): FirTypeRef
    }

    interface TypeRefWithNullability: FrontendTransformer {
      fun FirMetaContext.typeRefWithNullability(typeRefWithNullability: FirTypeRefWithNullability,context: CheckerContext, reporter: DiagnosticReporter): FirTypeRef
    }

    interface UserTypeRef : FrontendTransformer {
      fun FirMetaContext.userTypeRef(userTypeRef: FirUserTypeRef,context: CheckerContext, reporter: DiagnosticReporter): FirTypeRef
    }

    interface ValueParameter : FrontendTransformer {
      fun FirMetaContext.valueParameter(valueParameter: FirValueParameter,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface VarargArgumentsExpression: FrontendTransformer {
      fun FirMetaContext.varargArgumentsExpression(varargArgumentsExpression: FirVarargArgumentsExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface Variable : FrontendTransformer {
      fun FirMetaContext.variable(variable: FirVariable,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface VariableAssignment : FrontendTransformer {
      fun FirMetaContext.variableAssignment(variableAssignment: FirVariableAssignment,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface WhenExpression : FrontendTransformer {
      fun FirMetaContext.whenExpression(whenExpression: FirWhenExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface WhenSubjectExpression: FrontendTransformer {
      fun FirMetaContext.whenSubjectExpression(whenSubjectExpression: FirWhenSubjectExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface WhileLoop : FrontendTransformer {
      fun FirMetaContext.whileLoop(whileLoop: FirWhileLoop,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface WrappedArgumentExpression: FrontendTransformer {
      fun FirMetaContext.wrappedArgumentExpression(wrappedArgumentExpression: FirWrappedArgumentExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface WrappedDelegateExpression: FrontendTransformer {
      fun FirMetaContext.wrappedDelegateExpression(wrappedDelegateExpression: FirWrappedDelegateExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }

    interface WrappedExpression : FrontendTransformer {
      fun FirMetaContext.wrappedExpression(wrappedExpression: FirWrappedExpression,context: CheckerContext, reporter: DiagnosticReporter): FirStatement
    }
  }

}

