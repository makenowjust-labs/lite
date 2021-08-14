package codes.quine.labo.lite.parser

import scala.collection.mutable

/** Repeater is a result value abstraction for [[Parser#rep]]. */
trait Repeater[-A] {

  /** A builder type of this repeater. */
  type Builder

  /** A result type of this repeater. */
  type Result

  /** Returns a new builder. */
  def newBuilder: Builder

  /** Appends the value to the builder. */
  def addOne(b: Builder, x: A): Unit

  /** Returns a result value from the builder. */
  def result(b: Builder): Result
}

object Repeater extends LowPriorityRepeater {

  /** An aux type of repeater. */
  type Aux[-A, R] = Repeater[A] {
    type Result = R
  }

  /** Summons a repeater. */
  def apply[A](implicit rep: Repeater[A]): Aux[A, rep.Result] = rep

  /** A repeater for unit type. */
  implicit val unit: Aux[Unit, Unit] = new Repeater[Unit] {
    type Builder = Unit
    type Result = Unit
    def newBuilder: Unit = ()
    def addOne(b: Unit, x: Unit): Unit = ()
    def result(b: Unit): Unit = ()
  }
}

private[parser] trait LowPriorityRepeater {

  /** A repeater for any types. */
  implicit def seq[A]: Repeater.Aux[A, Seq[A]] = SeqRepeater.asInstanceOf[Repeater.Aux[A, Seq[A]]]

  /** [[LowPriorityRepeater#seq]] implementation. */
  object SeqRepeater extends Repeater[Any] {
    type Builder = mutable.Builder[Any, Seq[Any]]
    type Result = Seq[Any]
    def newBuilder: mutable.Builder[Any, Seq[Any]] = Seq.newBuilder[Any]
    def addOne(b: mutable.Builder[Any, Seq[Any]], x: Any): Unit = b.addOne(x)
    def result(b: mutable.Builder[Any, Seq[Any]]): Seq[Any] = b.result()
  }
}
