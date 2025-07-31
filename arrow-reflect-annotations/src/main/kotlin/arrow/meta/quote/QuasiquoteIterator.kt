package arrow.meta.module.impl.arrow.meta.quote

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid

@JvmInline
value class QuasiquoteIterable(private val file: FirFile) : Iterable<FirElement> {

  override fun iterator(): Iterator<FirElement> {
    return QuasiquoteIterator(file)
  }
}

class QuasiquoteIterator(private val file: FirFile) : Iterator<FirElement> {
  private val deque = ArrayDeque<FirElement>().apply { add(file) }
  private val visited = mutableSetOf<FirElement>()

  override fun hasNext(): Boolean = deque.isNotEmpty()

  override fun next(): FirElement {
    val current = deque.removeLast()
    current.acceptChildren(object : FirVisitorVoid() {
      override fun visitElement(element: FirElement) {
        if (element !in visited) {
          deque.addLast(element)
          visited.add(element)
        }
      }
    })
    return current
  }
}
