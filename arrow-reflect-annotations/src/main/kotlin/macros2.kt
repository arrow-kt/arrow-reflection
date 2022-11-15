package arrow.meta

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*

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

  sealed interface Generate {

    sealed interface TopLevel : Generate {
      interface Class : TopLevel {
        fun generate(): FirClass
      }

      interface Functions : TopLevel {
        fun generate(): List<FirFunction>
      }

      interface Properties : TopLevel {
        fun generate(): List<FirProperty>
      }

      interface TypeAlias : TopLevel {
        fun generate(): FirTypeAlias
      }
    }

    sealed interface Members : Generate {
      interface Constructors : Members {
        fun generate(ir: FirClass): List<FirConstructor>
      }

      interface Functions : Members {
        fun generate(ir: FirClass): List<FirFunction>
      }

      interface Properties : Members {
        fun generate(ir: FirClass): List<FirProperty>
      }

      interface NestedClasses : Members {
        fun generate(ir: FirClass): List<FirClass>
      }
    }
  }

  sealed interface Transform {

    interface AnonymousInitializer : Transform {
      fun transform(declaration: IrAnonymousInitializer): IrStatement
    }

    interface Block : Transform {
      fun transform(expression: IrBlock): IrExpression
    }

    interface BlockBody : Transform {
      fun transform(body: IrBlockBody): IrBody
    }

    interface Body : Transform {
      fun transform(body: IrBody): IrBody
    }

    interface Branch : Transform {
      fun transform(branch: IrBranch): IrBranch
    }

    interface Break : Transform {
      fun transform(jump: IrBreak): IrExpression
    }

    interface BreakContinue : Transform {
      fun transform(jump: IrBreakContinue): IrExpression
    }

    interface Call : Transform {
      fun transform(expression: IrCall): IrElement
    }

    interface CallableReference : Transform {
      fun transform(expression: IrCallableReference<*>): IrElement
    }

    interface Catch : Transform {
      fun transform(aCatch: IrCatch): IrCatch
    }

    interface Class : Transform {
      fun transform(declaration: IrClass): IrStatement
    }

    interface ClassReference : Transform {
      fun transform(expression: IrClassReference): IrExpression
    }

    interface Composite : Transform {
      fun transform(expression: IrComposite): IrExpression
    }

    interface Const : Transform {
      fun transform(expression: IrConst<*>): IrExpression
    }

    interface ConstantArray : Transform {
      fun transform(expression: IrConstantArray): IrConstantValue
    }

    interface ConstantObject : Transform {
      fun transform(expression: IrConstantObject): IrConstantValue
    }

    interface ConstantPrimitive : Transform {
      fun transform(expression: IrConstantPrimitive): IrConstantValue
    }

    interface ConstantValue : Transform {
      fun transform(expression: IrConstantValue): IrConstantValue
    }

    interface Constructor : Transform {
      fun transform(declaration: IrConstructor): IrStatement
    }

    interface ConstructorCall : Transform {
      fun transform(expression: IrConstructorCall): IrElement
    }

    interface ContainerExpression : Transform {
      fun transform(expression: IrContainerExpression): IrExpression
    }

    interface Continue : Transform {
      fun transform(jump: IrContinue): IrExpression
    }

    interface Declaration : Transform {
      fun transform(declaration: IrDeclarationBase): IrStatement
    }

    interface DeclarationReference : Transform {
      fun transform(expression: IrDeclarationReference): IrExpression
    }

    interface DelegatingConstructorCall : Transform {
      fun transform(expression: IrDelegatingConstructorCall): IrElement
    }

    interface DoWhileLoop : Transform {
      fun transform(loop: IrDoWhileLoop): IrExpression
    }

    interface DynamicExpression : Transform {
      fun transform(expression: IrDynamicExpression): IrExpression
    }

    interface DynamicMemberExpression : Transform {
      fun transform(expression: IrDynamicMemberExpression): IrExpression
    }

    interface DynamicOperatorExpression : Transform {
      fun transform(expression: IrDynamicOperatorExpression): IrExpression
    }

    interface Element : Transform {
      fun transform(element: IrElement): IrElement
    }

    interface ElseBranch : Transform {
      fun transform(branch: IrElseBranch): IrElseBranch
    }

    interface EnumConstructorCall : Transform {
      fun transform(expression: IrEnumConstructorCall): IrElement
    }

    interface EnumEntry : Transform {
      fun transform(declaration: IrEnumEntry): IrStatement
    }

    interface ErrorCallExpression : Transform {
      fun transform(expression: IrErrorCallExpression): IrExpression
    }

    interface ErrorDeclaration : Transform {
      fun transform(declaration: IrErrorDeclaration): IrStatement
    }

    interface ErrorExpression : Transform {
      fun transform(expression: IrErrorExpression): IrExpression
    }

    interface Expression : Transform {
      fun transform(expression: IrExpression): IrExpression
    }

    interface ExpressionBody : Transform {
      fun transform(body: IrExpressionBody): IrBody
    }

    interface ExternalPackageFragment: Transform {
      fun transform(fragment: IrExternalPackageFragment): IrExternalPackageFragment
    }

    interface Field : Transform {
      fun transform(declaration: IrField): IrStatement
    }

    interface FieldAccess : Transform {
      fun transform(expression: IrFieldAccessExpression): IrExpression
    }

    interface File : Transform {
      fun transform(declaration: IrFile): IrFile
    }

    interface Function : Transform {
      fun transform(declaration: IrFunction): IrStatement
    }

    interface FunctionAccess : Transform {
      fun transform(expression: IrFunctionAccessExpression): IrElement
    }

    interface FunctionExpression : Transform {
      fun transform(expression: IrFunctionExpression): IrElement
    }

    interface FunctionReference : Transform {
      fun transform(expression: IrFunctionReference): IrElement
    }

    interface GetClass : Transform {
      fun transform(expression: IrGetClass): IrExpression
    }

    interface GetEnumValue : Transform {
      fun transform(expression: IrGetEnumValue): IrExpression
    }

    interface GetField : Transform {
      fun transform(expression: IrGetField): IrExpression
    }

    interface GetObjectValue : Transform {
      fun transform(expression: IrGetObjectValue): IrExpression
    }

    interface GetValue : Transform {
      fun transform(expression: IrGetValue): IrExpression
    }

    interface InstanceInitializerCall : Transform {
      fun transform(expression: IrInstanceInitializerCall): IrExpression
    }

    interface LocalDelegatedProperty : Transform {
      fun transform(declaration: IrLocalDelegatedProperty): IrStatement
    }

    interface LocalDelegatedPropertyReference : Transform {
      fun transform(declaration: IrLocalDelegatedPropertyReference): IrElement
    }

    interface Loop : Transform {
      fun transform(loop: IrLoop): IrExpression
    }

    interface MemberAccess : Transform {
      fun transform(expression: IrMemberAccessExpression<*>): IrElement
    }

    interface ModuleFragment : Transform {
      fun transform(declaration: IrModuleFragment): IrModuleFragment
    }

    interface PackageFragment : Transform {
      fun transform(declaration: IrPackageFragment): IrElement
    }

    interface Property : Transform {
      fun transform(declaration: IrProperty): IrStatement
    }

    interface PropertyReference : Transform {
      fun transform(expression: IrPropertyReference): IrElement
    }

    interface RawFunctionReference : Transform {
      fun transform(expression: IrRawFunctionReference): IrExpression
    }

    interface Return : Transform {
      fun transform(expression: IrReturn): IrExpression
    }

    interface Script : Transform {
      fun transform(declaration: IrScript): IrStatement
    }

    interface SetField : Transform {
      fun transform(expression: IrSetField): IrExpression
    }

    interface SetValue : Transform {
      fun transform(expression: IrSetValue): IrExpression
    }

    interface SimpleFunction : Transform {
      fun transform(declaration: IrSimpleFunction): IrStatement
    }

    interface SingletonReference : Transform {
      fun transform(expression: IrGetSingletonValue): IrExpression
    }

    interface SpreadElement : Transform {
      fun transform(spread: IrSpreadElement): IrSpreadElement
    }

    interface StringConcatenation : Transform {
      fun transform(expression: IrStringConcatenation): IrExpression
    }

    interface SuspendableExpression : Transform {
      fun transform(expression: IrSuspendableExpression): IrExpression
    }

    interface SuspensionPoint : Transform {
      fun transform(expression: IrSuspensionPoint): IrExpression
    }

    interface SyntheticBody : Transform {
      fun transform(body: IrSyntheticBody): IrBody
    }

    interface Throw : Transform {
      fun transform(expression: IrThrow): IrExpression
    }

    interface Try : Transform {
      fun transform(aTry: IrTry): IrExpression
    }

    interface TypeAlias : Transform {
      fun transform(declaration: IrTypeAlias): IrStatement
    }

    interface TypeOperator : Transform {
      fun transform(expression: IrTypeOperatorCall): IrExpression
    }

    interface TypeParameter : Transform {
      fun transform(declaration: IrTypeParameter): IrStatement
    }

    interface ValueAccess : Transform {
      fun transform(expression: IrValueAccessExpression): IrExpression
    }

    interface ValueParameter : Transform {
      fun transform(declaration: IrValueParameter): IrStatement
    }

    interface Vararg : Transform {
      fun transform(expression: IrVararg): IrExpression
    }

    interface Variable : Transform {
      fun transform(declaration: IrVariable): IrStatement
    }

    interface When : Transform {
      fun transform(expression: IrWhen): IrExpression
    }

    interface WhileLoop : Transform {
      fun transform(loop: IrWhileLoop): IrExpression
    }
  }
}

