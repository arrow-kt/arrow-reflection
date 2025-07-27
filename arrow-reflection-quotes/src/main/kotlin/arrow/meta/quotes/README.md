# Arrow Reflection Quasiquotes

This package implements a Scala 3-style quasiquote system for Arrow Reflection, providing a type-safe alternative to string-based code generation.

## Current Status: Phase 1 Complete (API-Only)

Phase 1 (Core Foundation) has been implemented as an API-only layer with the following features:

### Core Types

- **`Expr<T>`**: Type-safe wrapper for FIR expressions
- **`QuoteContext`**: Context for building quoted expressions  
- **`ToExpr<T>`**: Typeclass for converting values to expressions

### Basic Operations

```kotlin
// Create quoted literals
val expr = quote(metaContext) { 42.q() }

// Splice expressions
val fir: FirExpression = +expr  // or splice(expr)

// Convert back to Expr
val expr2 = quote(metaContext) { fir.toExpr<Int>() }
```

### Important Note

**Phase 1 is API-only**. The actual FIR expression creation throws `NotImplementedError`. This allows us to:
- Establish the API surface
- Get early feedback on the design
- Ensure compatibility before implementing internals

Phase 2 will provide the actual implementation using FIR builders.

### Supported Literal Types (API)

- Primitives: Int, Long, Float, Double, Boolean, Char
- String literals
- Custom types via ToExpr implementation

## Usage Example

```kotlin
val metaContext = FirMetaContext(session)

// Create expressions
val x = quote(metaContext) { 10.q() }
val y = quote(metaContext) { "hello".q() }

// Splice and manipulate
val firExpr = +x
val combined = quote(metaContext) {
    buildBinaryExpression(firExpr, "+", 20.q().fir).toExpr<Int>()
}
```

## Next Phases

- **Phase 2**: Expression builders for type-safe AST construction
- **Phase 3**: Declaration builders (functions, classes, properties)
- **Phase 4**: Advanced features (hygiene, level checking)
- **Phase 5**: Tooling and documentation
- **Phase 6**: Optional compiler plugin for custom syntax

## Package Structure

```
arrow.meta.quotes/
├── Expr.kt              # Core expression type
├── QuoteContext.kt      # Quote building context
├── ToExpr.kt           # Value-to-expression conversion
├── internal/           # Internal implementations
│   └── ToExprInstances.kt
├── examples/           # Usage examples
└── builders/           # (Future) Type-safe builders
```