package codes.quine.labo.lite.pfix

/** PFix is a partially defined fixpoint combinator. The above "fixpoint combinator" means, `PFix(h).toPartialFunction =
  * h(PFix(h).toPartialFunction)` holds for any `h`.
  *
  * This implementation is efficient for combining multiple instances via [[orElse]] method.
  */
final class PFix[A, B] private (private val fs: Vector[(A => B) => PartialFunction[A, B]]) {

  /** Converts this into a total function.
    *
    * The result function handles a value as using this instance as possible. If this instance cannot handle the value,
    * then it passes to the fallback function.
    */
  def toFunction(fallback: A => B): A => B = new PFixFunction[A, B](fs, fallback)

  /** Converts this into a partial function.
    *
    * This method is almost same as `toFunction(_ => throw new MatchError)`, but the result implements `PartialFunction`
    * correctly.
    */
  def toPartialFunction: PartialFunction[A, B] = new PFixPartialFunction(fs)

  /** Returns a new instance combined with this and that instances.
    *
    * The result instance handles a value as using `this` instance as possible. If this instance cannot handle the
    * value, then it tries to use `that` instance instead.
    */
  def orElse(that: PFix[A, B]): PFix[A, B] = new PFix(fs ++ that.fs)
}

object PFix {

  /** Creates a new instance from a higher-order function.
    *
    * The specified function takes a total function to call a future result function recursively, and returns a partial
    * function handles a value.
    */
  def apply[A, B](f: (A => B) => PartialFunction[A, B]): PFix[A, B] = new PFix(Vector(f))

  /** Creates a new instance from a partial function. */
  def from[A, B](pf: PartialFunction[A, B]): PFix[A, B] = new PFix(Vector(_ => pf))

  /** Returns an empty partial function. */
  def empty[A, B]: PFix[A, B] = new PFix(Vector.empty)
}
