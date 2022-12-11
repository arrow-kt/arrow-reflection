package arrow.meta.samples.pure


import org.apache.bcel.classfile.EmptyVisitor
import org.apache.bcel.classfile.JavaClass
import org.apache.bcel.classfile.Method
import org.apache.bcel.generic.ConstantPoolGen
import org.apache.bcel.generic.MethodGen


/**
 * The simplest of class visitors, invokes the method visitor class for each
 * method found.
 */
class ClassVisitor(private val clazz: JavaClass) : EmptyVisitor() {
  private val constants: ConstantPoolGen
  private val classReferenceFormat: String
  private val DCManager: DynamicCallManager = DynamicCallManager()
  private val unsafePaths: MutableList<UnsafePath> = ArrayList()
  override fun visitJavaClass(jc: JavaClass) {
    jc.constantPool.accept(this)
    val methods = jc.methods
    for (i in methods.indices) {
      val method = methods[i]
      DCManager.retrieveCalls(method, jc)
      DCManager.linkCalls(method)
      method.accept(this)
    }
  }


  override fun visitMethod(method: Method) {
    val mg = MethodGen(method, clazz.className, constants)
    if (mg.instructionList.instructions.any { it.name == "athrow" }) {
      val visitor = MethodVisitor(mg, clazz)
      unsafePaths.addAll(visitor.start())
    }
  }

  fun start(): ClassVisitor {
    visitJavaClass(clazz)
    return this
  }

  fun methodCalls(): List<UnsafePath> {
    return unsafePaths
  }

  init {
    constants = ConstantPoolGen(clazz.constantPool)
    classReferenceFormat = "C:" + clazz.className + " %s"
  }
}
