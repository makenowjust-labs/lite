package codes.quine.labo.lite.crazy

/** Lazy is a lazy-evaluated value cell. */
final class Lazy[A] private (private[this] var thunk: () => A) {

  private[this] var cache: Option[A] = None

  /** Returns an underlying value. */
  def value: A = cache match {
    case Some(x) => x
    case None =>
      synchronized {
        val x = thunk()
        thunk = null
        cache = Some(x)
        x
      }
  }

  /** Applies an underlying value to the given mapping, and returns a new lazy cell holds this result. */
  def map[B](f: A => B): Lazy[B] = Lazy(f(value))

  /** Applies an underlying value to the given mapping, and returns a new lazy cell holds flattened result. */
  def flatMap[B](f: A => Lazy[B]): Lazy[B] = Lazy(f(value).value)
}

object Lazy {

  /** Returns a lazy-evaluated value cell with the given value. */
  def apply[A](x: => A): Lazy[A] = new Lazy(() => x)

  /** Returns an lazy-evaluated value cell getting from fixpoint function. */
  def fix[A](f: Lazy[A] => A): Lazy[A] = {
    var lx: Lazy[A] = null
    lx = Lazy(f(lx))
    lx
  }
}
