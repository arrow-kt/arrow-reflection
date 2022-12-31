package arrow.reflect.compiler.plugin.fir

import arrow.meta.TemplateCompiler
import arrow.meta.plugins.*
import arrow.reflect.compiler.plugin.fir.checkers.FirMetaAdditionalCheckersExtension
import arrow.reflect.compiler.plugin.fir.codegen.CompilerPluginGenerationExtension
import arrow.reflect.compiler.plugin.fir.codegen.FirMetaCodegenExtension
import arrow.reflect.compiler.plugin.targets.MetaTarget
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirBasicDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFileChecker
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirBasicExpressionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.type.FirTypeRefChecker
import org.jetbrains.kotlin.fir.analysis.checkers.type.TypeCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.builder.FirScriptConfiguratorExtension
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.FirFileBuilder
import org.jetbrains.kotlin.fir.declarations.builder.FirScriptBuilder
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.expressions.FirVariableAssignment
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.resolve.FirSamConversionTransformerExtension
import org.jetbrains.kotlin.fir.types.*
import kotlin.reflect.full.isSubclassOf

class FirArrowReflectExtensionRegistrar(val templateCompiler: TemplateCompiler, val metaTargets: List<MetaTarget>) :
  FirExtensionRegistrar() {

  private fun compilerPlugins(): Sequence<(FirSession) -> FrontendPlugin> =
    metaTargets.filter { it.companion.isSubclassOf(FrontendPlugin.Builder::class) }
      .map { it.companion.objectInstance }
      .distinct()
      .filterIsInstance<FrontendPlugin.Builder>()
      .asSequence()
      .flatMap {
        it.plugins
      }

  override fun ExtensionRegistrarContext.configurePlugin() {
    val compilerPlugins = compilerPlugins()
    compilerPlugins.forEach { pluginFn ->
      val registration = { session: FirSession ->
        val plugin = pluginFn(session)
        val result = when (plugin) {
          is Supertypes -> registerSuperTypes(plugin)
          is StatusTransformer -> registerStatusTransformer(plugin)
          is AssignmentTransformer -> registerAssignmentTransformer(plugin)
          is Checkers -> registerCheckers(plugin)
          is ExpressionResolution -> registerExpressionResolution(plugin)
          is Generation -> registerGeneration(plugin)
          is SAMConversionTransformer -> registerSAMConversionTransformer(plugin)
          is ScriptConfigurator -> registerScriptConfigurator(plugin)
          is Transformer -> registerTransformer(plugin)
          is TypeAttributes -> registerTypeAttributes(plugin)
        }
        result
      }

      +{ session: FirSession ->
        templateCompiler.session = session
        FirMetaCodegenExtension(session, templateCompiler, metaTargets)
      }
    }

    //todo remove old stuff below when compiler plugin is ready
    +{ session: FirSession ->
      templateCompiler.session = session
      FirMetaCodegenExtension(session, templateCompiler, metaTargets)
    }
    +{ session: FirSession ->
      templateCompiler.session = session
      FirMetaAdditionalCheckersExtension(session, templateCompiler, metaTargets)
    }
  }

  private fun ExtensionRegistrarContext.registerTransformer(plugin: Transformer) =
    object : FirAdditionalCheckersExtension(plugin.session) {
      override val declarationCheckers: DeclarationCheckers
        get() = object : DeclarationCheckers() {
          override val fileCheckers: Set<FirFileChecker>
            get() = setOf(object : FirFileChecker() {
              override fun check(declaration: FirFile, context: CheckerContext, reporter: DiagnosticReporter) {
                declaration.transform<FirElement, FirElement>(MetaFirTransformer(plugin), declaration)
              }
            })
        }
    }


  private fun registerTypeAttributes(plugin: TypeAttributes): FirTypeAttributeExtension =
    object : FirTypeAttributeExtension(plugin.session) {
      override fun convertAttributeToAnnotation(attribute: ConeAttribute<*>): FirAnnotation? {
        return plugin.convertAttributeToAnnotation(attribute)
      }

      override fun extractAttributeFromAnnotation(annotation: FirAnnotation): ConeAttribute<*>? {
        return plugin.extractAttributeFromAnnotation(annotation)
      }
    }


  private fun registerScriptConfigurator(plugin: ScriptConfigurator) =
    object : FirScriptConfiguratorExtension(plugin.session) {
      override fun FirScriptBuilder.configure(fileBuilder: FirFileBuilder) {
        plugin.run { configure(fileBuilder) }
      }
    }


  private fun registerSAMConversionTransformer(plugin: SAMConversionTransformer) =
    object : FirSamConversionTransformerExtension(plugin.session) {
      override fun getCustomFunctionalTypeForSamConversion(function: FirSimpleFunction): ConeLookupTagBasedType? {
        return plugin.getCustomFunctionalTypeForSamConversion(function)
      }
    }


  private fun registerGeneration(plugin: Generation) =
    CompilerPluginGenerationExtension(plugin.session, plugin)

  private fun registerExpressionResolution(plugin: ExpressionResolution) =
    object : FirExpressionResolutionExtension(plugin.session) {
      override fun addNewImplicitReceivers(functionCall: FirFunctionCall): List<ConeKotlinType> {
        return plugin.addNewImplicitReceivers(functionCall)
      }
    }


  private fun registerCheckers(plugin: Checkers) =
    object : FirAdditionalCheckersExtension(plugin.session) {
      override val declarationCheckers: DeclarationCheckers
        get() = object : DeclarationCheckers() {
          override val basicDeclarationCheckers: Set<FirBasicDeclarationChecker>
            get() = plugin.declarationCheckers.map {
              object : FirBasicDeclarationChecker() {
                override fun check(
                  declaration: FirDeclaration,
                  context: CheckerContext,
                  reporter: DiagnosticReporter
                ) {
                  it.check(declaration, context, reporter)
                }
              }
            }.toSet()
        }
      override val expressionCheckers: ExpressionCheckers
        get() = object : ExpressionCheckers() {
          override val basicExpressionCheckers: Set<FirBasicExpressionChecker>
            get() = plugin.expressionCheckers.map {
              object : FirBasicExpressionChecker() {
                override fun check(expression: FirStatement, context: CheckerContext, reporter: DiagnosticReporter) {
                  it.check(expression, context, reporter)
                }
              }
            }.toSet()
        }
      override val typeCheckers: TypeCheckers
        get() = object : TypeCheckers() {
          override val typeRefCheckers: Set<FirTypeRefChecker>
            get() = plugin.typeCheckers.map {
              object : FirTypeRefChecker() {
                override fun check(typeRef: FirTypeRef, context: CheckerContext, reporter: DiagnosticReporter) {
                  it.check(typeRef, context, reporter)
                }
              }
            }.toSet()
        }
    }


  private fun registerAssignmentTransformer(plugin: AssignmentTransformer) =
    object : FirAssignExpressionAltererExtension(plugin.session) {
      override fun transformVariableAssignment(variableAssignment: FirVariableAssignment): FirStatement? {
        return plugin.transformVariableAssignment(variableAssignment)
      }
    }

  private fun registerSuperTypes(plugin: Supertypes): FirSupertypeGenerationExtension =
    object : FirSupertypeGenerationExtension(plugin.session) {
      context(TypeResolveServiceContainer) override fun computeAdditionalSupertypes(
        classLikeDeclaration: FirClassLikeDeclaration,
        resolvedSupertypes: List<FirResolvedTypeRef>
      ): List<FirResolvedTypeRef> =
        plugin.computeAdditionalSupertypes(classLikeDeclaration, resolvedSupertypes)

      override fun needTransformSupertypes(declaration: FirClassLikeDeclaration): Boolean =
        plugin.needTransformSupertypes(declaration)
    }

  private fun registerStatusTransformer(plugin: StatusTransformer) =
    object : FirStatusTransformerExtension(plugin.session) {
      override fun needTransformStatus(declaration: FirDeclaration): Boolean =
        plugin.needTransformStatus(declaration)

      override fun transformStatus(status: FirDeclarationStatus, declaration: FirDeclaration): FirDeclarationStatus =
        plugin.transformStatus(status, declaration)
    }


}

