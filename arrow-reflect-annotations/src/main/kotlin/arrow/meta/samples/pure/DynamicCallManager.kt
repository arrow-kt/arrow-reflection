package arrow.meta.samples.pure

import org.apache.bcel.classfile.*
import java.util.regex.Pattern


/**
 * [DynamicCallManager] provides facilities to retrieve information about
 * dynamic calls statically.
 *
 *
 * Most of the time, call relationships are explicit, which allows to properly
 * build the call graph statically. But in the case of dynamic linking, i.e.
 * `invokedynamic` instructions, this relationship might be unknown
 * until the code is actually executed. Indeed, bootstrap methods are used to
 * dynamically link the code at first call. One can read details about the
 * [`invokedynamic`
 * instruction](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/multiple-language-support.html#invokedynamic) to know more about this mechanism.
 *
 *
 * Nested lambdas are particularly subject to such absence of concrete caller,
 * which lead us to produce method names like `lambda$null$0`, which
 * breaks the call graph. This information can however be retrieved statically
 * through the code of the bootstrap method called.
 *
 *
 * In [.retrieveCalls], we retrieve the (called,
 * caller) relationships by analyzing the code of the caller [Method].
 * This information is then used in [.linkCalls] to rename the
 * called [Method] properly.
 *
 * @author Matthieu Vergne <matthieu.vergne></matthieu.vergne>@gmail.com>
 */
class DynamicCallManager {
  private val dynamicCallers: MutableMap<String, String> = HashMap()

  /**
   * Retrieve dynamic call relationships based on the code of the provided
   * [Method].
   *
   * @param method [Method] to analyze the code
   * @param jc     [JavaClass] info, which contains the bootstrap methods
   * @see .linkCalls
   */
  fun retrieveCalls(method: Method, jc: JavaClass) {
    if (method.isAbstract || method.isNative) {
      // No code to consider
      return
    }
    val cp = method.constantPool
    val boots = getBootstrapMethods(jc)
    val code = method.code.toString()
    val matcher = BOOTSTRAP_CALL_PATTERN.matcher(code)
    while (matcher.find()) {
      val bootIndex = matcher.group(1).toInt()
      val bootMethod = boots[bootIndex]
      val calledIndex = bootMethod.bootstrapArguments[CALL_HANDLE_INDEX_ARGUMENT]
      val calledName = getMethodNameFromHandleIndex(cp, calledIndex)
      val callerName = method.name
      dynamicCallers[calledName] = callerName
    }
  }

  private fun getMethodNameFromHandleIndex(cp: ConstantPool, callIndex: Int): String {
    val handle = cp.getConstant(callIndex) as ConstantMethodHandle
    val ref = cp.getConstant(handle.referenceIndex) as ConstantCP
    val nameAndType = cp.getConstant(ref.nameAndTypeIndex) as ConstantNameAndType
    return nameAndType.getName(cp)
  }

  /**
   * Link the [Method]'s name to its concrete caller if required.
   *
   * @param method [Method] to analyze
   * @see .retrieveCalls
   */
  fun linkCalls(method: Method) {
    val nameIndex = method.nameIndex
    val cp = method.constantPool
    val methodName = (cp.getConstant(nameIndex) as ConstantUtf8).bytes
    var linkedName = methodName
    var callerName = methodName
    while (linkedName.matches("(lambda\\$)+null(\\$\\d+)+".toRegex())) {
      callerName = dynamicCallers[callerName]
      linkedName = linkedName.replace("null", callerName)
    }
    cp.setConstant(nameIndex, ConstantUtf8(linkedName))
  }

  private fun getBootstrapMethods(jc: JavaClass): Array<BootstrapMethod> {
    for (attribute in jc.attributes) {
      if (attribute is BootstrapMethods) {
        return attribute.bootstrapMethods
      }
    }
    return arrayOf()
  }

  companion object {
    private val BOOTSTRAP_CALL_PATTERN = Pattern
      .compile("invokedynamic\t(\\d+):\\S+ \\S+ \\(\\d+\\)")
    private const val CALL_HANDLE_INDEX_ARGUMENT = 1
  }
}
