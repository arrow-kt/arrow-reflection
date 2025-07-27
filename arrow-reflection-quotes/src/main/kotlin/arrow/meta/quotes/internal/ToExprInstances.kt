package arrow.meta.quotes.internal

import arrow.meta.quotes.ToExpr
import arrow.meta.quotes.QuoteContext
import arrow.meta.quotes.Expr
import arrow.meta.quotes.builders.*
import kotlin.reflect.KClass

/**
 * Registry of ToExpr instances for built-in types.
 */
object ToExprInstances {
    private val instances = mutableMapOf<KClass<*>, ToExpr<*>>()
    
    init {
        // Register built-in instances
        register(IntToExpr)
        register(LongToExpr)
        register(DoubleToExpr)
        register(FloatToExpr)
        register(BooleanToExpr)
        register(StringToExpr)
        register(CharToExpr)
    }
    
    fun <T : Any> register(instance: ToExpr<T>, klass: KClass<T>) {
        instances[klass] = instance
    }
    
    inline fun <reified T : Any> register(instance: ToExpr<T>) {
        register(instance, T::class)
    }
    
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> findInstance(klass: KClass<*>): ToExpr<T>? {
        return instances[klass] as? ToExpr<T>
    }
}

/**
 * ToExpr instance for Int literals.
 * Creates FIR literal expressions using buildLiteralExpression.
 */
object IntToExpr : ToExpr<Int> {
    override fun QuoteContext.toExpr(value: Int): Expr<Int> = 
        intLiteral(value)
}

/**
 * ToExpr instance for Long literals.
 * Creates FIR literal expressions using buildLiteralExpression.
 */
object LongToExpr : ToExpr<Long> {
    override fun QuoteContext.toExpr(value: Long): Expr<Long> = 
        longLiteral(value)
}

/**
 * ToExpr instance for Double literals.
 * Creates FIR literal expressions using buildLiteralExpression.
 */
object DoubleToExpr : ToExpr<Double> {
    override fun QuoteContext.toExpr(value: Double): Expr<Double> = 
        doubleLiteral(value)
}

/**
 * ToExpr instance for Float literals.
 * Creates FIR literal expressions using buildLiteralExpression.
 */
object FloatToExpr : ToExpr<Float> {
    override fun QuoteContext.toExpr(value: Float): Expr<Float> = 
        floatLiteral(value)
}

/**
 * ToExpr instance for Boolean literals.
 * Creates FIR literal expressions using buildLiteralExpression.
 */
object BooleanToExpr : ToExpr<Boolean> {
    override fun QuoteContext.toExpr(value: Boolean): Expr<Boolean> = 
        booleanLiteral(value)
}

/**
 * ToExpr instance for String literals.
 * Creates FIR literal expressions using buildLiteralExpression.
 */
object StringToExpr : ToExpr<String> {
    override fun QuoteContext.toExpr(value: String): Expr<String> = 
        stringLiteral(value)
}

/**
 * ToExpr instance for Char literals.
 * Creates FIR literal expressions using buildLiteralExpression.
 */
object CharToExpr : ToExpr<Char> {
    override fun QuoteContext.toExpr(value: Char): Expr<Char> = 
        charLiteral(value)
}