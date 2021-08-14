package codes.quine.labo.lite.parser

/** Sequencer is a result value abstraction for [[Parser#~]]. */
trait Sequencer[-A, -B] {

  /** A result type of this sequencer. */
  type Result

  /** Creates a result from values. */
  def apply(x: A, y: B): Result
}

object Sequencer extends LowPrioritySequencer {

  /** An aux type of sequencer. */
  type Aux[-A, -B, R] = Sequencer[A, B] {
    type Result = R
  }

  /** Summons a seqencer. */
  def apply[A, B](implicit seq: Sequencer[A, B]): Aux[A, B, seq.Result] = seq

  /** A sequencer for unit types. */
  implicit val unit: Aux[Unit, Unit, Unit] = new Sequencer[Unit, Unit] {
    type Result = Unit
    def apply(x: Unit, y: Unit): Unit = ()
  }

  /** A sequencer for unit type and lefy side any types. */
  implicit def left[A]: Aux[A, Unit, A] = LeftSequencer.asInstanceOf[Aux[A, Unit, A]]

  /** [[Sequencer.left]] implementation. */
  private object LeftSequencer extends Sequencer[Any, Unit] {
    type Result = Any
    def apply(x: Any, y: Unit): Any = x
  }

  /** A sequencer for unit type and right side any types. */
  implicit def right[B]: Aux[Unit, B, B] = RightSequencer.asInstanceOf[Aux[Unit, B, B]]

  /** [[Sequencer.right]] implementation. */
  private object RightSequencer extends Sequencer[Unit, Any] {
    type Result = Any
    def apply(x: Unit, y: Any): Any = y
  }
}

private[parser] trait LowPrioritySequencer extends LowerPrioritySequencer {

  /** A sequencer for three any values. */
  implicit def tuple3[A, B, C]: Sequencer.Aux[(A, B), C, (A, B, C)] =
    Tuple3Sequencer.asInstanceOf[Sequencer.Aux[(A, B), C, (A, B, C)]]

  /** [[Sequencer.tuple3]] implementation. */
  private object Tuple3Sequencer extends Sequencer[(Any, Any), Any] {
    type Result = (Any, Any, Any)
    def apply(x: (Any, Any), y: Any): (Any, Any, Any) = (x._1, x._2, y)
  }

  /** A sequencer for four any values. */
  implicit def tuple4[A, B, C, D]: Sequencer.Aux[(A, B, C), D, (A, B, C, D)] =
    Tuple4Sequencer.asInstanceOf[Sequencer.Aux[(A, B, C), D, (A, B, C, D)]]

  /** [[Sequencer.tuple4]] implementation. */
  private object Tuple4Sequencer extends Sequencer[(Any, Any, Any), Any] {
    type Result = (Any, Any, Any, Any)
    def apply(x: (Any, Any, Any), y: Any): (Any, Any, Any, Any) = (x._1, x._2, x._3, y)
  }

  /** A sequencer for five any values. */
  implicit def tuple5[A, B, C, D, E]: Sequencer.Aux[(A, B, C, D), E, (A, B, C, D, E)] =
    Tuple5Sequencer.asInstanceOf[Sequencer.Aux[(A, B, C, D), E, (A, B, C, D, E)]]

  /** [[Sequencer.tuple5]] implementation. */
  private object Tuple5Sequencer extends Sequencer[(Any, Any, Any, Any), Any] {
    type Result = (Any, Any, Any, Any, Any)
    def apply(x: (Any, Any, Any, Any), y: Any): (Any, Any, Any, Any, Any) = (x._1, x._2, x._3, x._4, y)
  }
}

private[parser] trait LowerPrioritySequencer {

  /** A sequencer for two any values. */
  implicit def tuple2[A, B]: Sequencer.Aux[A, B, (A, B)] = Tuple2Sequencer.asInstanceOf[Sequencer.Aux[A, B, (A, B)]]

  /** [[Sequencer.tuple2]] implementation. */
  private object Tuple2Sequencer extends Sequencer[Any, Any] {
    type Result = (Any, Any)
    def apply(x: Any, y: Any): (Any, Any) = (x, y)
  }
}
