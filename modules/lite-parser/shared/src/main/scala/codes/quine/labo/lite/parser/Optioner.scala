package codes.quine.labo.lite.parser

/** Optioner is a result value abstraction for [[Parser#?]]. */
trait Optioner[-A] {

  /** A result type of this optioner. */
  type Result

  /** Returns a new `none` value. */
  def none: Result

  /** Returns a new `some` value with the given value. */
  def some(x: A): Result
}

object Optioner extends LowPriorityOptioner {

  /** An aux type of optioner. */
  type Aux[-A, R] = Optioner[A] {
    type Result = R
  }

  /** Summons an optioner. */
  def apply[A](implicit opt: Optioner[A]): Aux[A, opt.Result] = opt

  /** An optioner for unit type. */
  implicit val unit: Aux[Unit, Unit] = new Optioner[Unit] {
    type Result = Unit
    def none: Unit = ()
    def some(x: Unit): Unit = ()
  }
}

private[parser] trait LowPriorityOptioner {

  /** An optioner for any types. */
  implicit def option[A]: Optioner.Aux[A, Option[A]] = OptionOptioner.asInstanceOf[Optioner.Aux[A, Option[A]]]

  /** [[LowPriorityOptioner#option]] implementation. */
  private object OptionOptioner extends Optioner[Any] {
    type Result = Option[Any]
    def none: Option[Any] = None
    def some(x: Any): Option[Any] = Some(x)
  }
}
