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
import org.jetbrains.org.objectweb.asm.*
import java.lang.reflect.Method
import kotlin.annotation.AnnotationTarget.*


object PureErrors : Diagnostics.Error {
  val CallGraphIncludesIO by error1()
}

@Target(
  CLASS,
  PROPERTY,
  CONSTRUCTOR,
  FUNCTION
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
        callGraph.calls.forEach {
          declaration.report(PureErrors.CallGraphIncludesIO, "Detected unsafe call to ${it}")
        }
      }
    }

  }

}

sealed class AnalyzedFunction {
  abstract val calls: Set<AnalyzedFunction>
}
data class LocalFunction(val value: FirSimpleFunction, override val calls: Set<AnalyzedFunction>) : AnalyzedFunction()
data class RemoteFunction(val method: Method, override val calls: Set<RemoteFunction>) : AnalyzedFunction()


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
    val argClasses = function.valueParameters.mapNotNull { it.returnTypeRef.firClassLike(session)?.symbol?.classId }
      .map { Class.forName(it.asFqNameString()) }
      .toSet()
    val foundMethod = fnClassType.declaredMethods.find {
      val paramsClasses = it.parameters.map { it.type }
      val allParamsInOriginalFunction = paramsClasses.all { it in argClasses }
      it.name == function.name.asString() && allParamsInOriginalFunction
    }
    if (foundMethod != null) {
      createLibraryCallGraph(null, foundMethod)
    } else LocalFunction(function, emptySet())
  } else {
    createLocalCallGraph(function)
  }
}

private fun FirMetaCheckerContext.createLibraryCallGraph(
  parent: AnalyzedFunction?,
  function: Method,
  calls: MutableSet<RemoteFunction> = mutableSetOf()
): RemoteFunction {

  val stream = function.declaringClass.getResourceAsStream(function.declaringClass.simpleName + ".class")

  stream?.use {
    extractCalls(parent, it.readBytes(), function, calls)
  }

  val current = RemoteFunction(function, calls)
  return if (calls.isEmpty()) current
  else current.copy(calls = calls.map { createLibraryCallGraph(it, it.method) }.toSet())
}

@OptIn(SymbolInternals::class)
private fun FirMetaCheckerContext.extractCalls(
  parent: AnalyzedFunction?,
  bytes: ByteArray?,
  function: Method,
  calls: MutableSet<RemoteFunction>
) {

  if (bytes != null) {
    // Load the class that contains the function using the ASM library
    val classReader = ClassReader(bytes)

    // Create a class visitor to iterate over the methods in the class
    val classVisitor = object : ClassVisitor(Opcodes.ASM9) {

      override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
      ):
        MethodVisitor? {
        // Check if the method we are visiting is the one we are interested in
        if (name == function.name) {
          // Create a method visitor to iterate over the instructions in the method
          return object : MethodVisitor(Opcodes.ASM9) {


            // Override the visitMethodInsn method to handle method calls
            override fun visitMethodInsn(
              opcode: Int,
              owner: String,
              name: String,
              descriptor: String,
              isInterface: Boolean
            ) {
              super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
              //Class.forName(owner.replace("/",".")).getResource("PrintStream.class")
              // Convert the asm method object to a FirSimpleFunction using the conversion function
              val classId = ClassId.fromString(owner)
              val nextClass = Class.forName(classId.asFqNameString())
              val calledFunction = nextClass.methods.firstOrNull {
                val methodSignature = computeJvmSignature(it)
                it.name == name && methodSignature == descriptor
              }

              if (calledFunction != null) {

                //val rmFn = RemoteFunction(calledFunction)

                // Add the called function to the list of vertices in the call graph
                // vertices.add(RemoteFunction(calledFunction))

                val call = RemoteFunction(calledFunction, emptySet())

                // Add the vertices in the called function's call graph to the list of vertices in the current call graph
                calls.add(call)

              }
            }
          }
        }

        return null
      }
    }



    // Visit the class to iterate over its methods
    classReader.accept(classVisitor, ClassReader.SKIP_FRAMES)
  }
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
