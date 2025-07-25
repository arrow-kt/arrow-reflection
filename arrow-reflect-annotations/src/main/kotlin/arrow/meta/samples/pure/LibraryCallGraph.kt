package arrow.meta.samples.pure

import arrow.meta.module.impl.arrow.meta.macro.compilation.DiagnosticsContext
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.org.objectweb.asm.*
import java.lang.reflect.Method

internal fun DiagnosticsContext.createLibraryCallGraph(
  call : FirFunctionCall?,
  function: Method, cache: ProcessingCache
): RemoteFunction {
  val existingReference = cache.remoteFunction(function)
  return if (existingReference == null) {

    val functions = function.calledMethods()
    val childGraphs = functions.map { firFunction ->
      if (firFunction !in cache.processingMethods) {
        cache.processingMethods.add(firFunction)
        createLibraryCallGraph(call, firFunction, cache)
      } else cache.remoteFunction(firFunction) ?: RecursiveMethodCall(firFunction) //recursive ends here
    }
    RemoteFunction(call, function, childGraphs.toSet()).also {
      cache.processedRemote.add(it)
    }
  } else existingReference
}


// This class is used to traverse the bytecode instructions of a method and
// collect the names of the methods that are called by the method
private class MethodCallGraphVisitor : MethodVisitor(Opcodes.ASM9) {
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

private fun Method.calledMethods(): Set<Method> {
  // Get the bytecode of the method
  val className = declaringClass.simpleName
  val methodName = name
  val methodDescriptor = Type.getMethodDescriptor(this)

  val classBytes = declaringClass.getResourceAsStream("$className.class")?.readBytes()
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
        val evalMethod = this@calledMethods
        if (name == methodName && descriptor == methodDescriptor) {
          return methodVisitor
        }
        return null
      }
    }, ClassReader.SKIP_FRAMES)
  }
  return methodVisitor.calledMethods
}

fun computeJvmSignature(method: Method): String {
  // Get the return type of the method
  val returnType = Type.getType(method.returnType)

  // Get the parameter types of the method
  val parameterTypes = method.parameterTypes.map { Type.getType(it) }

  // Compute the JVM signature
  return Type.getMethodDescriptor(returnType, *parameterTypes.toTypedArray())
}
