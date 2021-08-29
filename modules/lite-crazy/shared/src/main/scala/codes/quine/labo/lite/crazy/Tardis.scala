package codes.quine.labo.lite.crazy

/** Tardis a state monad implementation holds both of backward and forward states. */
final case class Tardis[BW, FW, A](run: (Lazy[BW], Lazy[FW]) => (Lazy[BW], Lazy[FW], Lazy[A])) {

  /** Applies a result value to the given mapping, and returns a new tardis monad holds this result. */
  def map[B](f: Lazy[A] => Lazy[B]): Tardis[BW, FW, B] = flatMap(lx => Tardis.pure(f(lx)))

  /** Applies a result value to the given mapping, and returns a new tardis monad holds flattened result. Hence the
    * mapping requires lazy cells as its arguments, we can write an action depends on both of future and past state.
    */
  def flatMap[B](f: Lazy[A] => Tardis[BW, FW, B]): Tardis[BW, FW, B] =
    Tardis { case (lb0, lf0) =>
      type Fix = (
          Lazy[(Lazy[BW], Lazy[FW], Lazy[A])],
          Lazy[(Lazy[BW], Lazy[FW], Lazy[B])],
          Lazy[BW],
          Lazy[BW],
          Lazy[FW],
          Lazy[FW],
          Lazy[A],
          Lazy[B]
      )
      val lfix = Lazy.fix[Fix] { lfix =>
        val lb2f1x = Lazy(run(lfix.value._3, lf0))
        val lb1f2y = Lazy(f(lfix.value._7).run(lb0, lfix.value._5))
        val lb1 = Lazy(lb1f2y.value._1.value)
        val lb2 = Lazy(lb2f1x.value._1.value)
        val lf1 = Lazy(lb2f1x.value._2.value)
        val lf2 = Lazy(lb1f2y.value._2.value)
        val lx = Lazy(lb2f1x.value._3.value)
        val ly = Lazy(lb1f2y.value._3.value)
        (lb2f1x, lb1f2y, lb1, lb2, lf1, lf2, lx, ly)
      }
      val (_, _, _, lb2, _, lf2, _, ly) = lfix.value
      (lb2, lf2, ly)
    }
}

object Tardis {

  /** Returns a new tardis monad holds the specified value as its result. */
  def pure[BW, FW, A](lx: Lazy[A]): Tardis[BW, FW, A] = Tardis((lb, lf) => (lb, lf, lx))

  /** Returns a new tardis monad holds a backward state as its result. */
  def getBackward[BW, FW]: Tardis[BW, FW, BW] = Tardis((lb, lf) => (lb, lf, lb))

  /** Returns a new tardis monad for putting the specified value to a backward state. */
  def putBackward[BW, FW](lb: Lazy[BW]): Tardis[BW, FW, Unit] = Tardis((_, lf) => (lb, lf, Lazy(())))

  /** Returns a new tardis monad for modifying a backward state by the given mapping. */
  def modifyBackward[BW, FW](f: Lazy[BW] => Lazy[BW]): Tardis[BW, FW, Unit] =
    Tardis((lb, lf) => (f(lb), lf, Lazy(())))

  /** Returns a new tardis monad holds a forward state as its result. */
  def getForward[BW, FW]: Tardis[BW, FW, FW] = Tardis((lb, lf) => (lb, lf, lf))

  /** Returns a new tardis monad for putting the specified value to a forward state. */
  def putForward[BW, FW](lf: Lazy[FW]): Tardis[BW, FW, Unit] = Tardis((lb, _) => (lb, lf, Lazy(())))

  /** Returns a new tardis monad for modifying a forward state by the given mapping. */
  def modifyForward[BW, FW](f: Lazy[FW] => Lazy[FW]): Tardis[BW, FW, Unit] =
    Tardis((lb, lf) => (lb, f(lf), Lazy(())))
}
