package arrow.meta

import arrow.meta.module.impl.arrow.meta.FirMetaContext
import arrow.meta.module.impl.arrow.meta.IrMetaContext
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.types.FirTypeRef
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

