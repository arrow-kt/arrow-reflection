# Arrow Meta

Arrow Meta is a Kotlin library for metaprogramming with the Kotlin compiler. 

It provides an annotation-based API that you can use to define compiler plugins and macros that interface with the different phases of the Kotlin compiler.

## Getting started
To get started with Arrow Meta, add it as a dependency to your project by adding the following line to your build.gradle file:

```kotlin
plugins {
    id("io.arrow-kt.meta") version "$latestVersion"
}
```

## Creating Compiler Plugins and Macros

To define a macro, you can create an annotation and a companion object that extends one of the interfaces provided by Arrow Meta. 
In the following example we create the annotation macro `@Increment`.

```kotlin
package arrow.meta.samples

import arrow.meta.Meta

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Increment
```
By annotating `@Increment` with `@arrow.meta.Meta` the Arrow Meta compiler plugin registers the annotation as compiler Macro.

This annotation has two main responsibilities.

1. Annotate elements that should be checked, transformed or where new declarations should be generated

```kotlin
package foo.bar

import arrow.meta.samples.Increment

fun main() {
  val x = @Increment 0
  println(x) //1
}
```

2. Provide a companion object implementing the `arrow.Meta` interfaces that include the functions that will be called at compile time

```kotlin
package arrow.meta.samples

import arrow.meta.Meta

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Increment {
    companion object : Meta.Checker.Expression<FirExpression> {
            ... 
    }
}
```

In order for Arrow Meta to be able to invoke your macros and compiler plugins at compiler-time the macro definitions and annotations must be compiled in a different module.
Both the module where the macro is defined and the module where it's used must be compiled with the arrow meta compiler plugin.

In the following sections, we will explore three of the most important parts of Arrow Meta: **checkers**, **declaration generators**, and **frontend transformers**.
These are the kind of interfaces the annotation companion object extends to interface with the Kotlin compiler.

### Checkers
Checkers offer the ability to perform various checks on declarations, expressions, and types during the compilation process. To utilize this feature, extend one of the Checker interfaces provided by Arrow Meta, such as `arrow.meta.Check.Declaration`, `arrow.meta.Check.Expression`, or `arrow.meta.Check.Type`. These interfaces provide a `check` function that allows you to validate the intercepted elements. For instance, you could use a checker to ensure that a particular annotation is only applied to constant expressions of type `Int`, or to confirm that a class has a specified property. Checkers are a useful way to enforce rules and ensure that code adheres to certain conventions or requirements.
The following code example demonstrates how to use the `@Increment` annotation to validate a constant is of type `Int` before transforming it.

```kotlin
package arrow.meta.samples

import arrow.meta.Diagnostics
import arrow.meta.FirMetaCheckerContext
import arrow.meta.Meta
import arrow.meta.samples.IncrementErrors.IncrementNotInConstantExpression
import arrow.meta.samples.IncrementErrors.IncrementNotInConstantInt
import org.jetbrains.kotlin.fir.expressions.FirConstExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.types.ConstantValueKind

object IncrementErrors : Diagnostics.Error {
    val IncrementNotInConstantExpression by error1()
    val IncrementNotInConstantInt by error1()
}

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Increment {
    companion object :
        Meta.Checker.Expression<FirExpression>,
        Diagnostics(IncrementNotInConstantExpression, IncrementNotInConstantInt) {

        override fun FirMetaCheckerContext.check(expression: FirExpression) {
            if (expression !is FirConstExpression<*>)
                expression.report(
                    IncrementNotInConstantExpression,
                    "@Increment only works on constant expressions of type `Int`"
                )

            if (expression is FirConstExpression<*> && expression.kind != ConstantValueKind.Int)
                expression.report(
                    IncrementNotInConstantInt,
                    "found `${+expression}` but @Increment expects a constant of type `Int`"
                )
        }
        
    }
}
```
The `expression.report` method emits a diagnostics whose target is the `expression` where the report happens.
We extend the `arrow.meta.Diagnostics` and `arrow.meta.Diagnostics.Error` to register all unique message keys that the compiler uses to identify them as unique diagnostics.

Using this annotation over a constant that is not of type `Int` will cause the compiler to emit the following error:

```kotlin
package foo.bar

import arrow.meta.samples.Increment

fun main() {
  val x = @Increment <!IncrementNotInConstantInt!>0.0<!>
  println(x)
}
```

### Frontend transformers
Frontend transformers provide the ability to transform elements of the Kotlin AST (abstract syntax tree) during the compilation process. 
To utilize this feature, extend one of the `arrow.Meta.FrontendTransformer` sub interfaces provided by Arrow Meta, such as `Meta.FrontendTransformer.Expression` or `Meta.FrontendTransformer.Declaration`. These interfaces provide a transform function named after the element being transformed that allows you to replace the element by the return of the function. For example, you could use a frontend transformer to transform an expression annotated with the `@Increment` annotation by adding `1` to its value continuing with what we did in the checker example.

```kotlin
package arrow.meta.samples

import arrow.meta.Diagnostics
import arrow.meta.FirMetaCheckerContext
import arrow.meta.Meta
import arrow.meta.samples.IncrementErrors.IncrementNotInConstantExpression
import arrow.meta.samples.IncrementErrors.IncrementNotInConstantInt
import org.jetbrains.kotlin.fir.expressions.FirConstExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.types.ConstantValueKind

object IncrementErrors : Diagnostics.Error {
    val IncrementNotInConstantExpression by error1()
    val IncrementNotInConstantInt by error1()
}

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Increment {
    companion object :
        Meta.Checker.Expression<FirExpression>,
        Meta.FrontendTransformer.Expression,
        Diagnostics(IncrementNotInConstantExpression, IncrementNotInConstantInt) {

        override fun FirMetaCheckerContext.check(expression: FirExpression) {
            if (expression !is FirConstExpression<*>)
                expression.report(
                    IncrementNotInConstantExpression,
                    "@Increment only works on constant expressions of type `Int`"
                )

            if (expression is FirConstExpression<*> && expression.kind != ConstantValueKind.Int)
                expression.report(
                    IncrementNotInConstantInt,
                    "found `${+expression}` but @Increment expects a constant of type `Int`"
                )
        }

        override fun FirMetaCheckerContext.expression(
            expression: FirExpression
        ): FirStatement {
            check(expression)
            //language=kotlin
            return "${+expression} + 1".call
        }

    }
}
```

We notice here that in the `expression` function we are calling `check(expression)` explicitly.
This is because the transformer phase runs before the `checkers` phase.
If we did not call `check` by the time our checks will be invoked it would be too late as the `FirConstExpression<Int>` has been already transformed to a `IrFunctionCall`.

An alternative is to transform and check in place by simply extending the transformer:

```kotlin
package arrow.meta.samples

import arrow.meta.Diagnostics
import arrow.meta.FirMetaCheckerContext
import arrow.meta.Meta
import arrow.meta.samples.IncrementErrors.IncrementNotInConstantExpression
import arrow.meta.samples.IncrementErrors.IncrementNotInConstantInt
import org.jetbrains.kotlin.fir.expressions.FirConstExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.types.ConstantValueKind

object IncrementErrors : Diagnostics.Error {
    val IncrementNotInConstantExpression by error1()
    val IncrementNotInConstantInt by error1()
}

@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Increment {
    companion object :
        Meta.FrontendTransformer.Expression,
        Diagnostics(IncrementNotInConstantExpression, IncrementNotInConstantInt) {

        override fun FirMetaCheckerContext.expression(
            expression: FirExpression
        ): FirStatement {
            if (expression !is FirConstExpression<*>)
                expression.report(
                    IncrementNotInConstantExpression,
                    "@Increment only works on constant expressions of type `Int`"
                )

            if (expression is FirConstExpression<*> && expression.kind != ConstantValueKind.Int)
                expression.report(
                    IncrementNotInConstantInt,
                    "found `${+expression}` but @Increment expects a constant of type `Int`"
                )
            //language=kotlin
            return "${+expression} + 1".call
        }

    }
}
```

### Declaration generators
Declaration generators enable the generation of top-level declarations and members during the compilation process. To use this feature, extend one of the `arrow.meta.Meta.Generate` sub interfaces, such as `arrow.meta.Meta..Generate.TopLevel` or `arrow.meta.Meta.Generate.Members`. These interfaces provide functions that allow you to generate various elements, including classes, functions, and properties.
As an example, a declaration generator could generate a `product` function for a class annotated with the `@Product` annotation, which returns a list of pairs containing the names and values of the class's properties.

```kotlin
package arrow.meta.samples

import arrow.meta.FirMetaContext
import arrow.meta.Meta
import org.jetbrains.kotlin.fir.declarations.*

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Meta
annotation class Product {
  companion object : Meta.Generate.Members.Functions {

    override fun FirMetaContext.newFunctions(firClass: FirClass): Map<String, () -> String> =
      mapOf(
        "product" to {
          //language=kotlin
          """|
             |fun product(): List<Pair<String, *>> = 
             |  listOf(${propertiesOf(firClass) { """"${+it.name}" to this.${+it.name}""" }})
          """.trimMargin()
        }
      )

  }
}
```

### Call Interceptors

Call interceptors are a type of frontend transformer in Arrow Meta that allow you to intercept and modify function calls at compile time. They are defined by implementing the CallInterceptor interface and providing an intercept function that takes a list of arguments and a function to call with those arguments.

To use a call interceptor, you will need to annotate the function that you want to intercept with the corresponding annotation. The intercept function will then be called every time the function is called, allowing you to modify the arguments and return value of the function.

Here is an example of how you might use a call interceptor to increment all integer arguments passed to a function:

```kotlin
package arrow.meta.samples

import arrow.meta.Meta
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Meta
annotation class IncrementArgs

companion object : Meta.CallInterceptor {

  override val annotation: KClass<*> = IncrementArgs::class

  override fun <In, Out> intercept(args: List<In>, func: (List<In>) -> Out): Out {
    val newArgs = args.map {
      when (it) {
        is Int -> (it + 1) as In //increments by one if it's an int
        else -> it
      }
    }
    return func(newArgs)
  }

}
```

To use the IncrementArgs call interceptor, you would simply annotate the function you want to intercept with `@IncrementArgs`:

```kotlin
package foo.bar

import arrow.meta.samples.IncrementArgs

@IncrementArgs
fun foo(value: Int): Int = value + 41

fun main() {
  val x = foo(0)
  println(x) //42
}
```

Every time the `foo` function is called, the `intercept` function in the `@IncrementArgs` companion will be called, allowing you to modify the arguments passed to the function before it is executed. In this case, the call interceptor increments the integer argument by `1` before calling the `foo` function.
