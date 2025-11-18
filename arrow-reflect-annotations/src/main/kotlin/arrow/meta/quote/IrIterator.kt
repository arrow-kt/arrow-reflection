package arrow.meta.module.impl.arrow.meta.quote

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid

@JvmInline
value class IrIterable(private val file: IrFile) : Iterable<IrElement> {

  override fun iterator(): Iterator<IrElement> {
    return IrIterator(file)
  }
}

class IrIterator(private val file: IrFile) : Iterator<IrElement> {
  private val deque = ArrayDeque<IrElement>().apply { add(file) }
  private val visited = mutableSetOf<IrElement>()

  override fun hasNext(): Boolean = deque.isNotEmpty()

  override fun next(): IrElement {
    val current = deque.removeFirst()
    current.acceptChildren(object : IrVisitorVoid() {
      override fun visitElement(element: IrElement, data: Nothing?) {
        if (element !in visited) {
          deque.addLast(element)
          visited.add(element)
        }
      }
    }, null)
    return current
  }
}
