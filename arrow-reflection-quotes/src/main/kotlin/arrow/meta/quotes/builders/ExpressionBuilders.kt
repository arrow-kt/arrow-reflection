package arrow.meta.quotes.builders

import arrow.meta.quotes.Expr
import arrow.meta.quotes.QuoteContext
import arrow.meta.quotes.QuoteContextImpl
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.*
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.references.builder.buildSimpleNamedReference
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.ConstantValueKind

/**
 * Extension functions for building common FIR expressions within a QuoteContext.
 * These builders provide a type-safe way to construct FIR nodes programmatically.
 */

/**
 * Creates a literal integer expression.
 */
fun QuoteContext.intLiteral(value: Int): Expr<Int> {
    val literal = buildLiteralExpression(
        source = null,
        kind = ConstantValueKind.Int,
        value = value,
        setType = true
    )
    return Expr(literal)
}

/**
 * Creates a literal string expression.
 */
fun QuoteContext.stringLiteral(value: String): Expr<String> {
    val literal = buildLiteralExpression(
        source = null,
        kind = ConstantValueKind.String,
        value = value,
        setType = true
    )
    return Expr(literal)
}

/**
 * Creates a literal boolean expression.
 */
fun QuoteContext.booleanLiteral(value: Boolean): Expr<Boolean> {
    val literal = buildLiteralExpression(
        source = null,
        kind = ConstantValueKind.Boolean,
        value = value,
        setType = true
    )
    return Expr(literal)
}

/**
 * Creates a literal null expression.
 */
fun QuoteContext.nullLiteral(): Expr<Nothing?> {
    val literal = buildLiteralExpression(
        source = null,
        kind = ConstantValueKind.Null,
        value = null,
        setType = true
    )
    return Expr(literal)
}

/**
 * Creates a literal long expression.
 */
fun QuoteContext.longLiteral(value: Long): Expr<Long> {
    val literal = buildLiteralExpression(
        source = null,
        kind = ConstantValueKind.Long,
        value = value,
        setType = true
    )
    return Expr(literal)
}

/**
 * Creates a literal double expression.
 */
fun QuoteContext.doubleLiteral(value: Double): Expr<Double> {
    val literal = buildLiteralExpression(
        source = null,
        kind = ConstantValueKind.Double,
        value = value,
        setType = true
    )
    return Expr(literal)
}

/**
 * Creates a literal float expression.
 */
fun QuoteContext.floatLiteral(value: Float): Expr<Float> {
    val literal = buildLiteralExpression(
        source = null,
        kind = ConstantValueKind.Float,
        value = value,
        setType = true
    )
    return Expr(literal)
}

/**
 * Creates a literal char expression.
 */
fun QuoteContext.charLiteral(value: Char): Expr<Char> {
    val literal = buildLiteralExpression(
        source = null,
        kind = ConstantValueKind.Char,
        value = value,
        setType = true
    )
    return Expr(literal)
}

/**
 * Creates a variable reference expression.
 */
fun QuoteContext.variableRef(name: String): Expr<Any?> {
    val propertyAccess = buildPropertyAccessExpression {
        calleeReference = buildSimpleNamedReference {
            source = null
            this.name = Name.identifier(name)
        }
    }
    return Expr(propertyAccess)
}

/**
 * Creates a function call expression.
 * 
 * @param functionName The name of the function to call
 * @param receiver Optional receiver expression
 * @param arguments List of argument expressions
 */
fun QuoteContext.functionCall(
    functionName: String,
    receiver: Expr<*>? = null,
    arguments: List<Expr<*>> = emptyList()
): Expr<Any?> {
    val call = buildFunctionCall {
        calleeReference = buildSimpleNamedReference {
            source = null
            name = Name.identifier(functionName)
        }
        
        receiver?.let {
            explicitReceiver = it.fir
        }
        
        argumentList = buildArgumentList {
            arguments.forEach { arg ->
                this.arguments += arg.fir
            }
        }
    }
    return Expr(call)
}

/**
 * Creates a binary operation expression.
 * 
 * @param left Left operand
 * @param operator Operator name (e.g., "plus", "minus", "times")
 * @param right Right operand
 */
fun QuoteContext.binaryOp(
    left: Expr<*>,
    operator: String,
    right: Expr<*>
): Expr<Any?> {
    return functionCall(operator, left, listOf(right))
}

/**
 * Creates an if expression.
 * 
 * @param condition The condition expression
 * @param thenBranch The expression for the true branch
 * @param elseBranch The expression for the false branch (optional)
 */
fun QuoteContext.ifExpr(
    condition: Expr<Boolean>,
    thenBranch: Expr<*>,
    elseBranch: Expr<*>? = null
): Expr<Any?> {
    val whenExpr = buildWhenExpression {
        // Add the condition branch
        branches += buildWhenBranch(
            hasGuard = false,
            init = {
                this.condition = condition.fir
                this.result = buildBlock {
                    statements += thenBranch.fir
                }
            }
        )
        
        // Add the else branch if present
        if (elseBranch != null) {
            branches += buildWhenBranch(
                hasGuard = false,
                init = {
                    this.condition = buildElseIfTrueCondition()
                    this.result = buildBlock {
                        statements += elseBranch.fir
                    }
                }
            )
        }
        
        usedAsExpression = true
    }
    
    return Expr(whenExpr)
}

/**
 * Creates a block expression containing multiple statements.
 * 
 * @param statements List of expressions to include in the block
 * @return The last expression in the block
 */
fun QuoteContext.block(statements: List<Expr<*>>): Expr<Any?> {
    val block = buildBlock {
        statements.forEach { stmt ->
            this.statements += stmt.fir
        }
    }
    return Expr(block)
}

/**
 * Extension operators for more natural syntax.
 */

/**
 * Plus operator for numeric expressions.
 */
operator fun Expr<Int>.plus(other: Expr<Int>): Expr<Int> =
    QuoteContextImpl.current.binaryOp(this, "plus", other) as Expr<Int>

/**
 * Minus operator for numeric expressions.
 */
operator fun Expr<Int>.minus(other: Expr<Int>): Expr<Int> =
    QuoteContextImpl.current.binaryOp(this, "minus", other) as Expr<Int>

/**
 * Times operator for numeric expressions.
 */
operator fun Expr<Int>.times(other: Expr<Int>): Expr<Int> =
    QuoteContextImpl.current.binaryOp(this, "times", other) as Expr<Int>

/**
 * Div operator for numeric expressions.
 */
operator fun Expr<Int>.div(other: Expr<Int>): Expr<Int> =
    QuoteContextImpl.current.binaryOp(this, "div", other) as Expr<Int>

/**
 * Comparison operators
 */
infix fun Expr<*>.eq(other: Expr<*>): Expr<Boolean> =
    QuoteContextImpl.current.binaryOp(this, "equals", other) as Expr<Boolean>

infix fun Expr<Int>.lt(other: Expr<Int>): Expr<Boolean> =
    QuoteContextImpl.current.binaryOp(this, "compareTo", other) as Expr<Boolean>

infix fun Expr<Int>.gt(other: Expr<Int>): Expr<Boolean> =
    QuoteContextImpl.current.binaryOp(this, "compareTo", other) as Expr<Boolean>

/**
 * String concatenation
 */
infix fun Expr<String>.concat(other: Expr<String>): Expr<String> =
    QuoteContextImpl.current.binaryOp(this, "plus", other) as Expr<String>