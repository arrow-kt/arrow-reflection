package arrow.meta.samples.pure


import org.apache.bcel.Const
import org.apache.bcel.classfile.JavaClass
import org.apache.bcel.generic.*


/**
 * The simplest of method visitors, prints any invoked method
 * signature for all method invocations.
 *
 * Class copied with modifications from CJKM: http://www.spinellis.gr/sw/ckjm/
 */
class MethodVisitor(private val mg: MethodGen, visitedClass: JavaClass) : EmptyVisitor() {
  private val cp: ConstantPoolGen = mg.constantPool
  private val format: String
  private val unsafePaths: MutableList<UnsafePath> = ArrayList()
  private fun argumentList(arguments: Array<Type>): String {
    val sb = StringBuilder()
    for (i in arguments.indices) {
      if (i != 0) {
        sb.append(",")
      }
      sb.append(arguments[i].toString())
    }
    return sb.toString()
  }

  fun start(): List<UnsafePath> {
    if (mg.isAbstract || mg.isNative) return emptyList()
    var ih = mg.instructionList.start
    while (ih != null) {
      val i = ih.instruction
      if (!visitInstruction(i)) i.accept(this)
      ih = ih.next
    }
    return unsafePaths
  }

  private fun visitInstruction(i: Instruction): Boolean {
    println(i)
    val opcode = i.opcode
    if (i.opcode == Const.ATHROW) {
      unsafePaths.add(
        UnsafePath(
          context = mg.className,
          name = mg.name,
          throwable = i.toString(true),
          preCondition = "<unknown>",
        )
      )
    }
    return (InstructionConst.getInstruction(opcode.toInt()) != null
      && i !is ConstantPushInstruction
      && i !is ReturnInstruction)
  }

  override fun visitIfInstruction(i: IfInstruction) {
    println("\tIF ${i.name}")
  }

  override fun visitLDC(i: LDC) {
    println("\tLDC ${i.name} = ${i.getValue(cp)}")
  }

  override fun visitLDC2_W(i: LDC2_W) {
    println("\t${i.name} = ${i.getValue(cp)}")
  }

  override fun visitLoadInstruction(i: LoadInstruction) {
    println("\tLOAD ${i.name} = ${i.getType(cp)}")
  }

  override fun visitLCMP(i: LCMP) {
    println("\tLCMP ${i.name} = ${i.getType(cp)}")
  }

  override fun visitINVOKEVIRTUAL(i: INVOKEVIRTUAL) {
    println(
      String.format(
        format,
        "INVOKEVIRTUAL",
        i.getReferenceType(cp),
        i.getMethodName(cp),
        argumentList(i.getArgumentTypes(cp))
      )
    )
  }

  override fun visitINVOKEINTERFACE(i: INVOKEINTERFACE) {
    println(
      String.format(
        format,
        "INVOKEINTERFACE",
        i.getReferenceType(cp),
        i.getMethodName(cp),
        argumentList(i.getArgumentTypes(cp))
      )
    )
  }

  override fun visitINVOKESPECIAL(i: INVOKESPECIAL) {
    println(
      String.format(
        format,
        "INVOKESPECIAL",
        i.getReferenceType(cp),
        i.getMethodName(cp),
        argumentList(i.getArgumentTypes(cp))
      )
    )
  }

  override fun visitINVOKESTATIC(i: INVOKESTATIC) {
    println(
      String.format(
        format,
        "INVOKESTATIC",
        i.getReferenceType(cp),
        i.getMethodName(cp),
        argumentList(i.getArgumentTypes(cp))
      )
    )
  }

  override fun visitINVOKEDYNAMIC(i: INVOKEDYNAMIC) {
    println(
      String.format(
        format, "INVOKEDYNAMIC", i.getType(cp), i.getMethodName(cp),
        argumentList(i.getArgumentTypes(cp))
      )
    )
  }

  init {
    format = ("\t\tcall:" + visitedClass.className + ":" + mg.name + "(" + argumentList(mg.argumentTypes) + ")"
      + " " + "(%s)%s:%s(%s)")
  }
}
