package codes.quine.labo.lite.delta

/** Delta is an object for representing a difference between two values. */
sealed abstract class Delta extends Product with Serializable {

  /** Checks whether or not this is identical. */
  def isIdentical: Boolean
}

object Delta {

  /** Apply is a delta object for case classes. */
  final case class Case(name: String, fields: Seq[Entry[String, Delta]]) extends Delta {
    def isIdentical: Boolean = fields.forall(_.value.isIdentical)
  }

  /** Map is a delta object for `Map`. */
  final case class Map(name: String, entries: Seq[Entry[Delta, Delta]], sep: String) extends Delta {
    def isIdentical: Boolean = entries.forall(e => e.key.isIdentical && e.value.isIdentical)
  }

  /** Set is a delta object for `Set`. */
  final case class Set(name: String, deltas: Seq[Delta]) extends Delta {
    def isIdentical: Boolean = deltas.forall(_.isIdentical)
  }

  /** Value is a base type for simple delta objects. */
  sealed abstract class Value[T] extends Delta

  /** Identical is a delta object with an identical value. */
  final case class Identical[T](value: T) extends Value[T] {
    def isIdentical: Boolean = true
  }

  /** Changed is a delta object with changed values. */
  final case class Changed[T](left: T, right: T) extends Value[T] {
    def isIdentical: Boolean = false
  }

  /** Missing is a delta object with a missing value. */
  final case class Missing[T](right: T) extends Value[T] {
    def isIdentical: Boolean = false
  }

  /** Missing is a delta object with an additional value. */
  final case class Additional[T](left: T) extends Value[T] {
    def isIdentical: Boolean = false
  }
}
