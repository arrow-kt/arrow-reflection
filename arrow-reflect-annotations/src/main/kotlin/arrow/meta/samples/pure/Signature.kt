package arrow.meta.samples.pure

import java.lang.reflect.Constructor
import java.lang.reflect.Method


/**
 * <h2>Type Signatures</h2><br></br>
 * The JNI uses the Java VM’s representation of type signatures. Table 3-2 shows
 * these type signatures.
 *
 *
 * Table 3-2 Java VM Type Signatures
 *
 * <pre>
 * +---------------------------+-----------------------+
 * | Type Signature            | Java Type             |
 * +---------------------------+-----------------------+
 * | Z                         | boolean               |
 * +---------------------------+-----------------------+
 * | B                         | byte                  |
 * +---------------------------+-----------------------+
 * | C                         | char                  |
 * +---------------------------+-----------------------+
 * | S                         | short                 |
 * +---------------------------+-----------------------+
 * | I                         | int                   |
 * +---------------------------+-----------------------+
 * | J                         | long                  |
 * +---------------------------+-----------------------+
 * | F                         | float                 |
 * +---------------------------+-----------------------+
 * | D                         | double                |
 * +---------------------------+-----------------------+
 * | L fully-qualified-class ; | fully-qualified-class |
 * +---------------------------+-----------------------+
 * | [ type                    | type[]                |
 * +---------------------------+-----------------------+
 * | ( arg-types ) ret-type    | method type           |
 * +---------------------------+-----------------------+
</pre> *
 *
 * For example, the Java method:
 *
 * <pre>
 * long f(int n, String s, int[] arr);
</pre> *
 *
 * has the following type signature:
 *
 * <pre>
 * (ILjava/lang/String;[I)J
</pre> *
 *
 * Note, for a constructor, supply &lt;init&gt; as the method name and void (V)
 * as the return type.
 *
 * @see [Type
 * Signatures](http://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/types.html.wp16432)
 */
class Signature {
  private val methods: Array<out Method>

  constructor(cls: Class<*>) {
    methods = cls.declaredMethods
  }

  constructor(vararg methods: Method) {
    this.methods = methods
  }

  /**
   * Returns the index of the first method with the specified name.
   *
   * @throws NoSuchMethodException
   */
  @Throws(NoSuchMethodException::class)
  fun getIndex(methodName: String): Int {
    val methods = methods
    val internedName = methodName.intern()
    var i = 0
    val n = methods.size
    while (i < n) {
      if (methods[i].name === internedName) return i
      i++
    }
    throw NoSuchMethodException(methodName)
  }

  @Throws(NoSuchMethodException::class)
  fun getMethod(methodName: String): Method {
    return methods[getIndex(methodName)]
  }

  @Throws(NoSuchMethodException::class)
  fun getSignature(methodName: String): String {
    return getSignature(getMethod(methodName))
  }

  companion object {
    @Throws(NoSuchMethodException::class, SecurityException::class)
    fun getConstructorSignature(cls: Class<*>, vararg parameterTypes: Class<*>?): String {
      return getSignature(cls.getConstructor(*parameterTypes))
    }

    fun getSignature(m: Method): String {
      return getSignature(m.returnType, *m.parameterTypes)
    }

    /**
     * Returns the type signature corresponding to the given constructor.
     *
     * @param ctor a [Constructor] object.
     * @return the type signature of the given constructor.
     */
    fun getSignature(ctor: Constructor<*>): String {
      return getSignature("V", *ctor.parameterTypes)
    }

    /**
     * Returns the type signature of a java method corresponding to the given
     * parameter and return types.
     *
     * @param returnType the return type for the method.
     * @param parameterTypes the parameter types for the method.
     * @return the type signature corresponding to the given parameter and
     * return types.
     */
    fun getSignature(returnType: Class<*>, vararg parameterTypes: Class<*>): String {
      return getSignature(getSignature(returnType), *parameterTypes)
    }

    private fun getSignature(returnTypeName: String, vararg parameterTypes: Class<*>): String {
      val sb = StringBuilder()
      sb.append('(')
      for (type in parameterTypes) {
        sb.append(getSignature(type))
      }
      sb.append(')')
      return sb.append(returnTypeName).toString()
    }

    /**
     * Converts a Java source-language class name into the internal form. The
     * internal name of a class is its fully qualified name, as returned by
     * Class.getName(), where '.' are replaced by '/'.
     *
     * @param c an object or array class.
     * @return the internal name form of the given class
     */
    fun getSignature(c: Class<*>): String {
      return if (c.isPrimitive && Void.TYPE != c) {
        if (Int::class.javaPrimitiveType == c) { // or Integer.TYPE
          "I"
        } else if (Long::class.javaPrimitiveType == c) { // or Long.TYPE
          "J"
        } else if (Boolean::class.javaPrimitiveType == c) { // or Boolean.TYPE
          "Z"
        } else if (Byte::class.javaPrimitiveType == c) { // or Byte.TYPE
          "B"
        } else if (Short::class.javaPrimitiveType == c) { // or Short.TYPE
          "S"
        } else if (Char::class.javaPrimitiveType == c) { // or Char.TYPE
          "C"
        } else if (Float::class.javaPrimitiveType == c) { // or Float.TYPE
          "F"
        } else if (Double::class.javaPrimitiveType == c) { // or Double.TYPE
          "D"
        } else {
          throw IllegalArgumentException("Should never reach here")
        }
      } else if (Void.TYPE == c || Void::class.java == c) {
        // e.g.
        // public void return_void() { }
        // public Void return_Void() { return null; }
        "V"
      } else {
        val internalName = c.name.replace('.', '/')
        if (c.isArray) {
          /* Already in the correct array style. */
          internalName
        } else {
          "L$internalName;"
        }
      }
    }

    /**
     * Returns the class name of the class corresponding to an internal name.
     *
     * @return the binary name of the class corresponding to this type.
     */
    fun getClassName(internalName: String): String? {
      return when (internalName[0]) {
        'V' -> "void"
        'Z' -> "boolean"
        'C' -> "char"
        'B' -> "byte"
        'S' -> "short"
        'I' -> "int"
        'F' -> "float"
        'J' -> "long"
        'D' -> "double"
        '[' -> internalName.replace('/', '.')
        'L' -> internalName.replace('/', '.').substring(1, internalName.length - 1)
        else -> null
      }
    }

    @Throws(NoSuchMethodException::class, SecurityException::class)
    fun test() {
      println("sun.arch.data.model = " + System.getProperty("sun.arch.data.model"))

      // a class type signature
      val clsLongArray: Class<*> = arrayOfNulls<Long>(0).javaClass
      val typeSig = getSignature(clsLongArray)
      println("Class '$clsLongArray' : $typeSig")
      println(getClassName(typeSig) == clsLongArray.name)

      // a method type signature
      val sig = Signature(Signature::class.java)
      println("Method 'getSignature' : " + sig.getSignature("getSignature"))

      // a constructor type signature
      val ctor: Constructor<*> = Signature::class.java.getConstructor(Class::class.java)
      println("Constructor '" + ctor.name + "' : " + getSignature(ctor))
      println(Integer.TYPE)
    }
  }
}
