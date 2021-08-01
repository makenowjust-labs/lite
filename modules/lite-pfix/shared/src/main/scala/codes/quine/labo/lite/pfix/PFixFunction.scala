package codes.quine.labo.lite.pfix

/** PFixFunction is a wrapper for [[PFix]] with implementing a function. */
private class PFixFunction[A, B](
    fs: Vector[(A => B) => PartialFunction[A, B]],
    private[this] val fallback: A => B
) extends (A => B) { fix =>

  /** A list of partial functions obtained by fixpoint functions. */
  protected val pfs: Vector[PartialFunction[A, B]] = fs.map(f => f(fix))

  override def apply(x: A): B = {
    for (pf <- pfs) {
      pf.unapply(x) match {
        case Some(y) => return y
        case None    => ()
      }
    }
    fallback(x)
  }
}
