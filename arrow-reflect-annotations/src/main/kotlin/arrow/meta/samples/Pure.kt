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
        val callGraph = createCallGraph(declaration)
        println(callGraph.render())
        callGraph.calls.forEach {
          declaration.report(PureErrors.CallGraphIncludesIO, "Detected unsafe call to ${it}")
        }
      }
    }

  }

}

sealed class AnalyzedFunction {
  abstract val name: String
  abstract val calls: Set<AnalyzedFunction>
}

data class LocalFunction(val value: FirSimpleFunction, override val calls: Set<AnalyzedFunction>) : AnalyzedFunction() {
  override val name: String = value.name.asString()
}

data class RemoteFunction(val method: Method, override val calls: Set<RemoteFunction>) : AnalyzedFunction() {
  override val name: String = method.name
}

fun AnalyzedFunction.render(indentation: String = "", isLastChild: Boolean = true): String {
  val children = if (calls.isEmpty()) "" else calls.joinToString("") {
    val childIndentation = if (isLastChild) "$indentation    " else "$indentation│   "
    it.render(childIndentation, it == this@render.calls.last())
  }
  val connector = if (isLastChild) "└" else "├"
  return "$indentation$connector── $name\n$children"
}

// Define a class representing a call graph
//data class CallGraph(val parent: CallGraph?, val function: AnalyzedFunction, val calls: Set<CallGraph>)

// Define a function to create a call graph
@OptIn(SymbolInternals::class)
fun FirMetaCheckerContext.createCallGraph(function: FirSimpleFunction): AnalyzedFunction {
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
      createLibraryCallGraph(foundMethod)
    } else LocalFunction(function, emptySet())
  } else {
    createLocalCallGraph(function)
  }
}

private fun FirMetaCheckerContext.createLibraryCallGraph(
  function: Method, calls: MutableSet<RemoteFunction> = mutableSetOf()
): RemoteFunction {
  val methods = calledMethods(function)
  val childGraphs = methods.map { method ->
    createLibraryCallGraph(method, calls)
  }
  return RemoteFunction(function, childGraphs.toSet())
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

@OptIn(SymbolInternals::class)
private fun FirMetaCheckerContext.createLocalCallGraph(function: FirSimpleFunction): AnalyzedFunction {
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
      if (callee is FirSimpleFunction) {
        //vertices.add(LocalFunction(callee))
        val calleeCallGraph = createCallGraph(callee)
        vertices.add(calleeCallGraph)
      }
    }
  }

  // Traverse the children elements of the input function with the visitor
  function.accept(visitor)

  // Return the call graph with the input function as the root and the collected vertices
  return LocalFunction(function, vertices)
}

fun computeJvmSignature(method: Method): String {
  // Get the return type of the method
  val returnType = Type.getType(method.returnType)

  // Get the parameter types of the method
  val parameterTypes = method.parameterTypes.map { Type.getType(it) }

  // Compute the JVM signature
  return Type.getMethodDescriptor(returnType, *parameterTypes.toTypedArray())
}
