package codes.quine.labo.lite.pfix

/** PFixPartialFunction is a wrapper for [[PFix]] with implementing a partial function. */
private class PFixPartialFunction[A, B](fs: Vector[(A => B) => PartialFunction[A, B]])
    extends PFixFunction[A, B](fs, (x: A) => throw new MatchError(x))
    with PartialFunction[A, B] { fix =>
  override def isDefinedAt(x: A): Boolean = pfs.exists(pf => pf.isDefinedAt(x))
}
