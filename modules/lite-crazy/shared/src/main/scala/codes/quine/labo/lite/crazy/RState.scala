package codes.quine.labo.lite.crazy

/** RState is reversed state monad implementation. */
final case class RState[S, A](run: Lazy[S] => (Lazy[S], Lazy[A])) {

  /** Applies a result value to the given mapping, and returns a new reversed state monad holds this result. */
  def map[B](f: Lazy[A] => Lazy[B]): RState[S, B] = flatMap(lx => RState.pure(f(lx)))

  /** Applies a result value to the given mapping, and returns a new reversed state monad holds flattened result. Hence
    * the mapping requires a lazy cell as its argument, we can write an action depends on future state.
    */
  def flatMap[B](f: Lazy[A] => RState[S, B]): RState[S, B] =
    RState[S, B] { ls0 =>
      type Fix = (Lazy[(Lazy[S], Lazy[A])], Lazy[(Lazy[S], Lazy[B])], Lazy[S], Lazy[S], Lazy[A], Lazy[B])
      val lfix = Lazy.fix[Fix] { lfix =>
        val ls2x = Lazy(run(lfix.value._3))
        val ls1y = Lazy(f(lfix.value._5).run(ls0))
        val ls1 = Lazy(ls1y.value._1.value)
        val ls2 = Lazy(ls2x.value._1.value)
        val lx = Lazy(ls2x.value._2.value)
        val ly = Lazy(ls1y.value._2.value)
        (ls2x, ls1y, ls1, ls2, lx, ly)
      }
      val (_, _, _, ls2, _, ly) = lfix.value
      (ls2, ly)
    }
}

object RState {

  /** Returns a reversed state monad holds the specified value as its result value. */
  def pure[S, A](lx: Lazy[A]): RState[S, A] = RState(ls => (ls, lx))

  /** Returns a reversed state monad holds a future state as its result value. */
  def get[S]: RState[S, S] = RState(ls => (ls, ls))

  /** Returns a reversed state monad for putting the specified value to its state. */
  def put[S](ls: Lazy[S]): RState[S, Unit] = RState(_ => (ls, Lazy(())))

  /** Returns a reversed state monad for modifying its state by the given mapping. */
  def modify[S](f: Lazy[S] => Lazy[S]): RState[S, Unit] = RState(ls => (f(ls), Lazy(())))
}
