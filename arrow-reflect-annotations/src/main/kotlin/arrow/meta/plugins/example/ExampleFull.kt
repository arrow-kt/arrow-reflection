package arrow.meta.plugins.example

import arrow.meta.Meta
import arrow.meta.plugins.*


/**
 * A global compiler plugin that mirrors the extensions
 * in the Kotlin compiler
 */
@Meta
class MyCompilerPluginWithDSL : FrontendPlugin.Builder() {
  init {
    +Supertypes {
      shouldTransformSupertypes = { false }
      additionalSupertypes = { _, _ -> emptyList() }
    }
    +ScriptConfigurator {
      configureScript = { fileBuilder -> }
    }
    +SAMConversionTransformer {
      getCustomFunctionalTypeForSamConversion = { function -> null }
    }
    +ExpressionResolution {
      newImplicitReceivers = { emptyList() }
    }
    +Checkers {
      expressionCheckers.add { expression, context, reporter -> }
      declarationCheckers.add { declaration, context, reporter -> }
      typeCheckers.add { type, context, reporter -> }
    }
    +Generation {
      callableNamesForClass = { emptySet() }
      nestedClassifiersNames = { emptySet() }
      topLevelCallableIds = { emptySet() }
      topLevelClassIds = { emptySet() }
      containsPackage = { false }
      classLikeDeclaration = { null }
      constructors = { emptyList() }
      functions = { _, _ -> emptyList() }
      properties = { _, _ -> emptyList() }
    }
    +Transformer<String> {
      file = { file, _ -> file }
      declaration = { declaration, _ -> declaration }
      expression = { expression, _ -> expression }
      statement = { statement, _ -> statement }
      typeAlias = { typeAlias, _ -> typeAlias }
      typeParameter = { typeParameter, _ -> typeParameter }
      valueParameter = { valueParameter, _ -> valueParameter }
      property = { property, _ -> property }
      function = { function, _ -> function }
      classLikeDeclaration = { classLikeDeclaration, _ -> classLikeDeclaration }
      typeRef = { typeRef, _ -> typeRef }
      annotation = { annotation, _ -> annotation }
      call = { call, _ -> call }
      import = { import, _ -> import }
      loop = { loop, _ -> loop }
      jump = { jump, _ -> jump }
      whenExpression = { whenExpression, _ -> whenExpression }
      tryExpression = { tryExpression, _ -> tryExpression }
      catch = { catch, _ -> catch }
      block = { block, _ -> block }
      qualifiedAccess = { qualifiedAccess, _ -> qualifiedAccess }
      variableAssignment = { variableAssignment, _ -> variableAssignment }
      binaryLogicExpression = { binaryLogicExpression, _ -> binaryLogicExpression }
      backingField = { backingField, _ -> backingField }
      backingFieldReference = { backingFieldReference, _ -> backingFieldReference }
      delegatedConstructorCall = { delegatedConstructorCall, _ -> delegatedConstructorCall }
      enumEntry = { enumEntry, _ -> enumEntry }
      errorExpression = { errorExpression, _ -> errorExpression }
      resolvedQualifier = { resolvedQualifier, _ -> resolvedQualifier }
      contextReceiver = { contextReceiver, _ -> contextReceiver }
      elvisExpression = { elvisExpression, _ -> elvisExpression }
    }
  }
}
