package arrow.reflect.compiler.plugin.ir

import arrow.meta.FromTemplate
import arrow.meta.TemplateCompiler
import arrow.meta.module.impl.arrow.meta.IrMetaContext
import arrow.reflect.compiler.plugin.targets.MetaTarget
import arrow.reflect.compiler.plugin.targets.MetagenerationTarget
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.getValueArgument
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.isAnnotationWithEqualFqName
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class IrMetaExtensionRegistrar(
  private val templateCompiler: TemplateCompiler,
  private val metaTargets: List<MetaTarget>
) :
  IrGenerationExtension {

  val fromTemplateFqName = FqName(FromTemplate::class.java.canonicalName)

  fun IrElement.isMetaAnnotated(templateCompiler: TemplateCompiler): Boolean =
    templateCompiler.isInSourceCache(this)

  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {

    moduleFragment.transform(object : IrElementTransformer<Unit> {

      override fun visitCall(expression: IrCall, data: Unit): IrElement {
        val declaration = expression.symbol.owner
        val fromTemplateAnnotation = fromTemplateAnnotation(declaration)
        if (fromTemplateAnnotation != null) {
          val parentClassIdConst = fromTemplateAnnotation.getValueArgument(Name.identifier("parent")) as? IrConst<*>
          val parent = parentClassIdConst?.value as? String
          if (parent != null) {
            val foundParent = pluginContext.referenceClass(ClassId.fromString(parent))
            if (foundParent != null) {
              declaration.parent = foundParent.owner
            }
          }
        }
        return super.visitCall(expression, data)
      }

    }, Unit)

    moduleFragment.transform(object : IrElementTransformer<Unit> {

      private inline fun <reified In : IrElement, reified Out : IrElement> invokeMeta(arg: In): Out? =
        if (arg.isMetaAnnotated(templateCompiler)) {
          MetaTarget.find("transform", null, MetagenerationTarget.Ir, listOf(In::class), Out::class, metaTargets)?.let { target ->
            val metaContext = IrMetaContext(templateCompiler, pluginContext)
            val result = target.method.invoke(target.companion.objectInstance, metaContext, arg)
            result as? Out
          }
        } else null

      override fun visitAnonymousInitializer(declaration: IrAnonymousInitializer, data: Unit): IrStatement =
        invokeMeta(declaration) ?: super.visitAnonymousInitializer(declaration, data)

      override fun visitBlock(expression: IrBlock, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitBlock(expression, data)

      override fun visitBlockBody(body: IrBlockBody, data: Unit): IrBody =
        invokeMeta(body) ?: super.visitBlockBody(body, data)

      override fun visitBody(body: IrBody, data: Unit): IrBody =
        invokeMeta(body) ?: super.visitBody(body, data)

      override fun visitBranch(branch: IrBranch, data: Unit): IrBranch =
        invokeMeta(branch) ?: super.visitBranch(branch, data)

      override fun visitBreak(jump: IrBreak, data: Unit): IrExpression =
        invokeMeta(jump) ?: super.visitBreak(jump, data)

      override fun visitBreakContinue(jump: IrBreakContinue, data: Unit): IrExpression =
        invokeMeta(jump) ?: super.visitBreakContinue(jump, data)

      override fun visitCall(expression: IrCall, data: Unit): IrElement =
        invokeMeta(expression) ?: super.visitCall(expression, data)

      override fun visitCallableReference(expression: IrCallableReference<*>, data: Unit): IrElement =
        invokeMeta(expression) ?: super.visitCallableReference(expression, data)

      override fun visitCatch(aCatch: IrCatch, data: Unit): IrCatch =
        invokeMeta(aCatch) ?: super.visitCatch(aCatch, data)

      override fun visitClass(declaration: IrClass, data: Unit): IrStatement =
        invokeMeta(declaration) ?: super.visitClass(declaration, data)

      override fun visitClassReference(expression: IrClassReference, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitClassReference(expression, data)

      override fun visitComposite(expression: IrComposite, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitComposite(expression, data)

      override fun visitConst(expression: IrConst<*>, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitConst(expression, data)

      override fun visitConstantArray(expression: IrConstantArray, data: Unit): IrConstantValue =
        invokeMeta(expression) ?: super.visitConstantArray(expression, data)

      override fun visitConstantObject(expression: IrConstantObject, data: Unit): IrConstantValue =
        invokeMeta(expression) ?: super.visitConstantObject(expression, data)

      override fun visitConstantPrimitive(expression: IrConstantPrimitive, data: Unit): IrConstantValue =
        invokeMeta(expression) ?: super.visitConstantPrimitive(expression, data)

      override fun visitConstantValue(expression: IrConstantValue, data: Unit): IrConstantValue =
        invokeMeta(expression) ?: super.visitConstantValue(expression, data)

      override fun visitConstructor(declaration: IrConstructor, data: Unit): IrStatement =
        invokeMeta(declaration) ?: super.visitConstructor(declaration, data)

      override fun visitConstructorCall(expression: IrConstructorCall, data: Unit): IrElement =
        invokeMeta(expression) ?: super.visitConstructorCall(expression, data)

      override fun visitContainerExpression(expression: IrContainerExpression, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitContainerExpression(expression, data)

      override fun visitContinue(jump: IrContinue, data: Unit): IrExpression =
        invokeMeta(jump) ?: super.visitContinue(jump, data)

      override fun visitDeclaration(declaration: IrDeclarationBase, data: Unit): IrStatement =
        invokeMeta(declaration) ?: super.visitDeclaration(declaration, data)

      override fun visitDeclarationReference(expression: IrDeclarationReference, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitDeclarationReference(expression, data)

      override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall, data: Unit): IrElement =
        invokeMeta(expression) ?: super.visitDelegatingConstructorCall(expression, data)

      override fun visitDoWhileLoop(loop: IrDoWhileLoop, data: Unit): IrExpression =
        invokeMeta(loop) ?: super.visitDoWhileLoop(loop, data)

      override fun visitDynamicExpression(expression: IrDynamicExpression, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitDynamicExpression(expression, data)

      override fun visitDynamicMemberExpression(expression: IrDynamicMemberExpression, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitDynamicMemberExpression(expression, data)

      override fun visitDynamicOperatorExpression(expression: IrDynamicOperatorExpression, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitDynamicOperatorExpression(expression, data)

      override fun visitElement(element: IrElement, data: Unit): IrElement =
        invokeMeta(element) ?: super.visitElement(element, data)

      override fun visitElseBranch(branch: IrElseBranch, data: Unit): IrElseBranch =
        invokeMeta(branch) ?: super.visitElseBranch(branch, data)

      override fun visitEnumConstructorCall(expression: IrEnumConstructorCall, data: Unit): IrElement =
        invokeMeta(expression) ?: super.visitEnumConstructorCall(expression, data)

      override fun visitEnumEntry(declaration: IrEnumEntry, data: Unit): IrStatement =
        invokeMeta(declaration) ?: super.visitEnumEntry(declaration, data)

      override fun visitErrorCallExpression(expression: IrErrorCallExpression, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitErrorCallExpression(expression, data)

      override fun visitErrorDeclaration(declaration: IrErrorDeclaration, data: Unit): IrStatement =
        invokeMeta(declaration) ?: super.visitErrorDeclaration(declaration, data)

      override fun visitErrorExpression(expression: IrErrorExpression, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitErrorExpression(expression, data)

      override fun visitExpression(expression: IrExpression, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitExpression(expression, data)

      override fun visitExpressionBody(body: IrExpressionBody, data: Unit): IrBody =
        invokeMeta(body) ?: super.visitExpressionBody(body, data)

      override fun visitExternalPackageFragment(
        declaration: IrExternalPackageFragment,
        data: Unit
      ): IrExternalPackageFragment {
        return super.visitExternalPackageFragment(declaration, data)
      }

      override fun visitField(declaration: IrField, data: Unit): IrStatement =
        invokeMeta(declaration) ?: super.visitField(declaration, data)

      override fun visitFieldAccess(expression: IrFieldAccessExpression, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitFieldAccess(expression, data)

      override fun visitFile(declaration: IrFile, data: Unit): IrFile =
        invokeMeta(declaration) ?: super.visitFile(declaration, data)

      override fun visitFunction(declaration: IrFunction, data: Unit): IrStatement =
        invokeMeta(declaration) ?: super.visitFunction(declaration, data)

      override fun visitFunctionAccess(expression: IrFunctionAccessExpression, data: Unit): IrElement =
        invokeMeta(expression) ?: super.visitFunctionAccess(expression, data)

      override fun visitFunctionExpression(expression: IrFunctionExpression, data: Unit): IrElement =
        invokeMeta(expression) ?: super.visitFunctionExpression(expression, data)

      override fun visitFunctionReference(expression: IrFunctionReference, data: Unit): IrElement =
        invokeMeta(expression) ?: super.visitFunctionReference(expression, data)

      override fun visitGetClass(expression: IrGetClass, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitGetClass(expression, data)

      override fun visitGetEnumValue(expression: IrGetEnumValue, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitGetEnumValue(expression, data)

      override fun visitGetField(expression: IrGetField, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitGetField(expression, data)

      override fun visitGetObjectValue(expression: IrGetObjectValue, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitGetObjectValue(expression, data)

      override fun visitGetValue(expression: IrGetValue, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitGetValue(expression, data)

      override fun visitInstanceInitializerCall(expression: IrInstanceInitializerCall, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitInstanceInitializerCall(expression, data)

      override fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty, data: Unit): IrStatement =
        invokeMeta(declaration) ?: super.visitLocalDelegatedProperty(declaration, data)

      override fun visitLocalDelegatedPropertyReference(
        expression: IrLocalDelegatedPropertyReference,
        data: Unit
      ): IrElement =
        invokeMeta(expression) ?: super.visitLocalDelegatedPropertyReference(expression, data)

      override fun visitLoop(loop: IrLoop, data: Unit): IrExpression =
        invokeMeta(loop) ?: super.visitLoop(loop, data)

      override fun visitMemberAccess(expression: IrMemberAccessExpression<*>, data: Unit): IrElement =
        invokeMeta(expression) ?: super.visitMemberAccess(expression, data)

      override fun visitModuleFragment(declaration: IrModuleFragment, data: Unit): IrModuleFragment =
        invokeMeta(declaration) ?: super.visitModuleFragment(declaration, data)

      override fun visitPackageFragment(declaration: IrPackageFragment, data: Unit): IrElement =
        invokeMeta(declaration) ?: super.visitPackageFragment(declaration, data)

      override fun visitProperty(declaration: IrProperty, data: Unit): IrStatement =
        invokeMeta(declaration) ?: super.visitProperty(declaration, data)

      override fun visitPropertyReference(expression: IrPropertyReference, data: Unit): IrElement =
        invokeMeta(expression) ?: super.visitPropertyReference(expression, data)

      override fun visitRawFunctionReference(expression: IrRawFunctionReference, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitRawFunctionReference(expression, data)

      override fun visitReturn(expression: IrReturn, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitReturn(expression, data)

      override fun visitScript(declaration: IrScript, data: Unit): IrStatement =
        invokeMeta(declaration) ?: super.visitScript(declaration, data)

      override fun visitSetField(expression: IrSetField, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitSetField(expression, data)

      override fun visitSetValue(expression: IrSetValue, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitSetValue(expression, data)

      override fun visitSimpleFunction(declaration: IrSimpleFunction, data: Unit): IrStatement =
        invokeMeta(declaration) ?: super.visitSimpleFunction(declaration, data)

      override fun visitSingletonReference(expression: IrGetSingletonValue, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitSingletonReference(expression, data)

      override fun visitSpreadElement(spread: IrSpreadElement, data: Unit): IrSpreadElement =
        invokeMeta(spread) ?: super.visitSpreadElement(spread, data)

      override fun visitStringConcatenation(expression: IrStringConcatenation, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitStringConcatenation(expression, data)

      override fun visitSuspendableExpression(expression: IrSuspendableExpression, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitSuspendableExpression(expression, data)

      override fun visitSuspensionPoint(expression: IrSuspensionPoint, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitSuspensionPoint(expression, data)

      override fun visitSyntheticBody(body: IrSyntheticBody, data: Unit): IrBody =
        invokeMeta(body) ?: super.visitSyntheticBody(body, data)

      override fun visitThrow(expression: IrThrow, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitThrow(expression, data)

      override fun visitTry(aTry: IrTry, data: Unit): IrExpression =
        invokeMeta(aTry) ?: super.visitTry(aTry, data)

      override fun visitTypeAlias(declaration: IrTypeAlias, data: Unit): IrStatement =
        invokeMeta(declaration) ?: super.visitTypeAlias(declaration, data)

      override fun visitTypeOperator(expression: IrTypeOperatorCall, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitTypeOperator(expression, data)

      override fun visitTypeParameter(declaration: IrTypeParameter, data: Unit): IrStatement =
        invokeMeta(declaration) ?: super.visitTypeParameter(declaration, data)

      override fun visitValueAccess(expression: IrValueAccessExpression, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitValueAccess(expression, data)

      override fun visitValueParameter(declaration: IrValueParameter, data: Unit): IrStatement =
        invokeMeta(declaration) ?: super.visitValueParameter(declaration, data)

      override fun visitVararg(expression: IrVararg, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitVararg(expression, data)

      override fun visitVariable(declaration: IrVariable, data: Unit): IrStatement =
        invokeMeta(declaration) ?: super.visitVariable(declaration, data)

      override fun visitWhen(expression: IrWhen, data: Unit): IrExpression =
        invokeMeta(expression) ?: super.visitWhen(expression, data)

      override fun visitWhileLoop(loop: IrWhileLoop, data: Unit): IrExpression =
        invokeMeta(loop) ?: super.visitWhileLoop(loop, data)
    }, Unit)
    println(moduleFragment.dumpKotlinLike())
  }

  private fun fromTemplateAnnotation(declaration: IrSimpleFunction): IrConstructorCall? =
    declaration.annotations.firstOrNull { it.isAnnotationWithEqualFqName(fromTemplateFqName) }

}

