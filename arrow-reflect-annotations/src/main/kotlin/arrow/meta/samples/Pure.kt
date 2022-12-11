package arrow.meta.samples

import arrow.meta.Diagnostics
import arrow.meta.FirMetaCheckerContext
import arrow.meta.Meta
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.resolve.firClassLike
import org.jetbrains.kotlin.fir.resolvedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.load.kotlin.JvmPackagePartSource
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.org.objectweb.asm.*
import java.lang.reflect.Method
import kotlin.annotation.AnnotationTarget.*


object PureErrors : Diagnostics.Error {
  val CallGraphIncludesIO by error1()
}

@Target(
  CLASS, PROPERTY, CONSTRUCTOR, FUNCTION
)
@Retention(AnnotationRetention.SOURCE)
@Meta
annotation class Pure {
  companion object : Meta.Checker.Declaration<FirDeclaration>, Diagnostics(PureErrors.CallGraphIncludesIO) {

    override fun FirMetaCheckerContext.check(
      declaration: FirDeclaration,
    ) {
      if (declaration is FirSimpleFunction) {
        val callGraph =
          createCallGraph(declaration, ProcessingCache(mutableSetOf(), mutableSetOf(), mutableSetOf(), mutableSetOf()))
        println(callGraph.render())
        callGraph.calls.forEach {
          declaration.report(PureErrors.CallGraphIncludesIO, "Detected unsafe call to ${it}")
        }
      }
    }

  }

}

sealed class AnalyzedFunction {
  abstract val renderName: String
  abstract val calls: Set<AnalyzedFunction>
}

data class RecursiveCall(val value: FirSimpleFunction) : AnalyzedFunction() {
  override val renderName: String = "[Recursive] ${value.symbol.callableId.asSingleFqName().asString()}"
  override val calls: Set<AnalyzedFunction> = emptySet()
}

data class LocalFunction(val value: FirSimpleFunction, override val calls: Set<AnalyzedFunction>) : AnalyzedFunction() {
  override val renderName: String = "[Local] ${value.symbol.callableId.asSingleFqName().asString()}"
}

data class RemoteFunction(val method: Method, override val calls: Set<RemoteFunction>) : AnalyzedFunction() {
  override val renderName: String = "[Binaries] ${method.declaringClass.canonicalName + "." + method.name}"
}

fun AnalyzedFunction.render(indentation: String = "", isLastChild: Boolean = true): String {
  val children = if (calls.isEmpty()) "" else calls.joinToString("") {
    val childIndentation = if (isLastChild) "$indentation    " else "$indentation│   "
    it.render(childIndentation, it == this@render.calls.last())
  }
  val connector = if (isLastChild) "└" else "├"
  return "$indentation$connector── $renderName\n$children"
}

class ProcessingCache(
  val processingMethods: MutableSet<Method>,
  val processingFirFunctions: MutableSet<FirSimpleFunction>,
  val processedLocals: MutableSet<LocalFunction>,
  val processedRemote: MutableSet<RemoteFunction>
) {
  fun add(analyzedFunction: AnalyzedFunction) {
    when (analyzedFunction) {
      is LocalFunction -> processedLocals.add(analyzedFunction)
      is RemoteFunction -> processedRemote.add(analyzedFunction)
      is RecursiveCall -> {}
    }
  }

  fun localFunction(fn: FirSimpleFunction): LocalFunction? =
    processedLocals.firstOrNull { it.value == fn }

  fun remoteFunction(fn: Method): RemoteFunction? =
    processedRemote.firstOrNull { it.method == fn }
}

// Define a function to create a call graph
fun FirMetaCheckerContext.createCallGraph(
  function: FirSimpleFunction,
  cache: ProcessingCache
): AnalyzedFunction {
  return if (function.origin == FirDeclarationOrigin.Library) {
    val classId = (function.containerSource as? JvmPackagePartSource)?.knownJvmBinaryClass?.classId
    val callableId = function.symbol.callableId
    val fnClass = classId?.asFqNameString() ?: error("no class found for $callableId")
    val fnClassType = Class.forName(fnClass)
    val argClasses =
      function.valueParameters.mapNotNull { it.returnTypeRef.firClassLike(session)?.symbol?.classId }.mapNotNull {
        if (it == ClassId(FqName("kotlin"), Name.identifier("Any"))) java.lang.Object::class.java
        else Class.forName(it.asFqNameString())
      }.toSet()
    val foundMethod = fnClassType.declaredMethods.find {
      val paramsClasses = it.parameters.map { it.type }
      val allParamsInOriginalFunction = paramsClasses.all { it in argClasses }
      it.name == function.name.asString() && allParamsInOriginalFunction
    }
    if (foundMethod != null) {
      createLibraryCallGraph(foundMethod, cache)
    } else LocalFunction(function, emptySet())
  } else {
    createLocalCallGraph2(function, cache)
  }
}

private fun FirMetaCheckerContext.createLibraryCallGraph(
  function: Method, cache: ProcessingCache
): RemoteFunction {
  val existingReference = cache.remoteFunction(function)
  return if (existingReference == null) {
    val methods = calledMethods(function)
    val childGraphs = methods.map { method ->
      createLibraryCallGraph(method, cache)
    }
    RemoteFunction(function, childGraphs.toSet()).also {
      cache.processedRemote.add(it)
    }
  } else existingReference
}


// This class is used to traverse the bytecode instructions of a method and
// collect the names of the methods that are called by the method
class MethodCallGraphVisitor : MethodVisitor(Opcodes.ASM9) {
  // Set to store the names of the methods that are called by the current method
  val calledMethods = mutableSetOf<Method>()

  override fun visitMethodInsn(
    opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean
  ) {
    val classId = ClassId.fromString(owner)
    val nextClass = Class.forName(classId.asFqNameString())
    val calledFunction = nextClass.methods.firstOrNull {
      val methodSignature = computeJvmSignature(it)
      it.name == name && methodSignature == descriptor
    }
    // When a method instruction is encountered, add the name of the called method
    // to the set of called methods
    calledFunction?.let { calledMethods.add(it) }
  }
}

@OptIn(SymbolInternals::class)
fun calledMethods(function: FirSimpleFunction): Set<FirSimpleFunction> {
  val calledMethods = mutableSetOf<FirSimpleFunction>()
  val visitor = object : FirVisitorVoid() {
    override fun visitElement(element: FirElement) {
      element.acceptChildren(this)
    }

    override fun visitFunctionCall(functionCall: FirFunctionCall) {
      // If the current element is a function call, add it to the list of vertices

      // Look up the callee reference element for the function call
      val callee = functionCall.calleeReference.resolvedSymbol?.fir
      if (callee is FirSimpleFunction) {
        calledMethods.add(callee)
      }
    }
  }
  function.accept(visitor)

  return calledMethods
}


fun calledMethods(method: Method): Set<Method> {
  // Get the bytecode of the method
  val className = method.declaringClass.simpleName
  val methodName = method.name
  val methodDescriptor = Type.getMethodDescriptor(method)

  val classBytes = method.declaringClass.getResourceAsStream("$className.class")?.readBytes()
  val methodVisitor = MethodCallGraphVisitor()
  if (classBytes != null) {

    // Use ASM to read the bytecode and visit the instructions of the method
    val classReader = ClassReader(classBytes)

    classReader.accept(object : ClassVisitor(Opcodes.ASM7) {
      override fun visitMethod(
        access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?
      ): MethodVisitor? {
        // When the target method is found, return the method visitor that
        // collects the names of the called methods
        val evalMethod = method
        if (name == methodName && descriptor == methodDescriptor) {
          return methodVisitor
        }
        return null
      }
    }, ClassReader.SKIP_FRAMES)
  }
  return methodVisitor.calledMethods
}

private fun FirMetaCheckerContext.createLocalCallGraph2(
  function: FirSimpleFunction, cache: ProcessingCache
): LocalFunction {
  val existingReference = cache.localFunction(function)
  return if (existingReference == null) {
    val functions = calledMethods(function)
    val childGraphs = functions.mapNotNull { firFunction ->
      if (firFunction !in cache.processingFirFunctions) {
        cache.processingFirFunctions.add(firFunction)
        createCallGraph(firFunction, cache)
      } else cache.localFunction(firFunction) ?: RecursiveCall(firFunction) //recursive ends here
    }
    LocalFunction(function, childGraphs.toSet()).also {
      cache.processedLocals.add(it)
    }
  } else existingReference
}

@OptIn(SymbolInternals::class)
private fun FirMetaCheckerContext.createLocalCallGraph(
  function: FirSimpleFunction,
  cache: ProcessingCache
): AnalyzedFunction {
  val existingReference = cache.localFunction(function)
  return if (existingReference == null) {
    // Define a list to hold the vertices (function calls) in the call graph
    val vertices = mutableSetOf<AnalyzedFunction>()

    // Create a visitor to traverse the children elements of the input function
    val visitor = object : FirVisitorVoid() {
      override fun visitElement(element: FirElement) {
        element.acceptChildren(this)
      }

      override fun visitFunctionCall(functionCall: FirFunctionCall) {
        // If the current element is a function call, add it to the list of vertices

        // Look up the callee reference element for the function call
        val callee = functionCall.calleeReference.resolvedSymbol?.fir

        // If the callee is a simple function, recursively create a call graph for it
        if (callee is FirSimpleFunction && cache.localFunction(callee) == null) {
          val calleeCallGraph = createCallGraph(callee, cache).also {
            cache.add(it)
          }
          vertices.add(calleeCallGraph)
        }
      }
    }

    // Traverse the children elements of the input function with the visitor
    function.accept(visitor)

    // Return the call graph with the input function as the root and the collected vertices
    LocalFunction(function, vertices).also {
      cache.add(it)
    }
  } else existingReference
}

fun computeJvmSignature(method: Method): String {
  // Get the return type of the method
  val returnType = Type.getType(method.returnType)

  // Get the parameter types of the method
  val parameterTypes = method.parameterTypes.map { Type.getType(it) }

  // Compute the JVM signature
  return Type.getMethodDescriptor(returnType, *parameterTypes.toTypedArray())
}
