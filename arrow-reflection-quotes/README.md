# Arrow Reflection Quotes

A type-safe quasiquote system for Kotlin metaprogramming, providing an alternative to string-based template compilation.

## Overview

The arrow-reflection-quotes module implements a Scala 3-inspired quasiquote system that enables type-safe expression construction and manipulation at compile time. This system provides a cleaner, more composable alternative to the traditional string-based template compiler.

## Features

### ✅ Implemented

1. **Core Quote/Splice System**
   - `Expr<T>` - Type-safe wrapper for FIR expressions
   - `quote { ... }` - Build expressions in a type-safe context
   - `+expr` - Splice expressions back into FIR

2. **Expression Builders**
   - Literal builders: `intLiteral`, `stringLiteral`, `booleanLiteral`, etc.
   - Binary operations: `binaryOp(left, "plus", right)`
   - Variable references: `variableRef("name")`
   - Function calls: `functionCall("name", receiver, args)`
   - Conditional expressions: `ifExpr(condition, thenBranch, elseBranch)`
   - Block expressions: `block(statements)`

3. **ToExpr Typeclass**
   - Automatic conversion of values to expressions
   - Implementations for all primitive types
   - Extensible for custom types

4. **Operator Overloading**
   - Natural syntax for arithmetic: `expr1 + expr2`
   - Comparisons: `expr1 eq expr2`
   - String concatenation: `str1 concat str2`

## Usage Examples

### Basic Expression Building

```kotlin
@Meta
annotation class IncrementWithQuotes {
    companion object : Meta.FrontendTransformer.Expression {
        override fun FirMetaCheckerContext.expression(
            expression: FirExpression
        ): FirStatement {
            return when (expression) {
                is FirLiteralExpression -> {
                    val value = expression.value as? Int ?: return expression
                    // Build new expression using quasiquotes
                    +quote { 
                        intLiteral(value + 1)
                    }
                }
                else -> {
                    // For non-literals: expression + 1
                    +quote {
                        binaryOp(
                            Expr<Any?>(expression),
                            "plus",
                            intLiteral(1)
                        )
                    }
                }
            }
        }
    }
}
```

### Conditional Expressions

```kotlin
@Meta
annotation class QuasiquoteConditional {
    companion object : Meta.FrontendTransformer.Expression {
        override fun FirMetaCheckerContext.expression(
            expression: FirExpression
        ): FirStatement {
            val result = quote {
                val expr = expression.toExpr<Any?>()
                
                // Create: if (expr == null) "default" else expr
                ifExpr(
                    condition = binaryOp(expr, "equals", nullLiteral()) as Expr<Boolean>,
                    thenBranch = stringLiteral("default"),
                    elseBranch = expr
                )
            }
            
            return +result
        }
    }
}
```

### Using ToExpr

```kotlin
val result = quote {
    val x = 10.toExpr()  // Converts Int to Expr<Int>
    val y = "hello".toExpr()  // Converts String to Expr<String>
    
    // Use the expressions
    binaryOp(x, "plus", intLiteral(5))
}
```

## Architecture

The quasiquote system is built on top of Kotlin's FIR (Frontend Intermediate Representation):

1. **Expr<T>** - Wraps FIR expressions with type information
2. **QuoteContext** - Provides the DSL scope for building expressions
3. **ToExpr<T>** - Typeclass for converting values to expressions
4. **Expression Builders** - Type-safe builders for various FIR node types

## Comparison with Template Compiler

### Before (Template Compiler)
```kotlin
"${+expression} + 1".call
```

### After (Quasiquotes)
```kotlin
+quote {
    binaryOp(Expr(expression), "plus", intLiteral(1))
}
```

## Benefits

1. **Type Safety** - Compile-time checking of expression types
2. **Composability** - Build complex expressions from simple parts
3. **IDE Support** - Full autocomplete and refactoring support
4. **No String Parsing** - Avoid runtime parsing errors
5. **Clear Intent** - Express transformations in Kotlin code

## Limitations

- Currently supports expression-level metaprogramming only
- Declaration building (classes, functions) requires deeper compiler integration
- Some complex FIR constructs may not have builders yet

## Future Work

- Add more expression builders (when expressions, lambdas, etc.)
- Improve type inference in quote contexts
- Add pattern matching capabilities
- Explore hygiene and fresh name generation

## Testing

All functionality is tested through the arrow-reflect-compiler-plugin test suite:
- `quasiquote_increment_test.kt` - Tests basic expression transformation
- `quasiquote_double_expression_test.kt` - Tests binary operations
- `quasiquote_conditional_test.kt` - Tests conditional expressions

## Contributing

When adding new features:
1. Add expression builders to the appropriate package
2. Implement ToExpr instances for new types
3. Create Meta annotations demonstrating usage
4. Add tests following the compiler plugin test pattern