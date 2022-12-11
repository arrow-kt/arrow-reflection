package arrow.meta.samples.pure

data class UnsafePath(
  val context : String,
  val name : String,
  val throwable: String,
  val preCondition: String
) {
  override fun toString(): String =
    "$name throws $throwable"
}
