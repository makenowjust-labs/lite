package codes.quine.labo.lite.delta

/** KeyOf is a function for extracting a key from a value. */
trait KeyOf[T] { self =>

  /** A key type. */
  type Key

  def apply(value: T): Key

  /** Returns a new key-of function with pre-mapping `f`. */
  def contramap[S](f: S => T): KeyOf[S] = new KeyOf[S] {
    type Key = self.Key
    def apply(value: S): Key = self(f(value))
  }
}

object KeyOf extends KeyOfInstances0 {

  /** Summons the instance. */
  @inline def apply[T](implicit keyOf: KeyOf[T]): KeyOf[T] = keyOf
}

trait KeyOfInstances0 extends KeyOfInstances1 {
  implicit def keyOfInstanceForEntry[K, V](implicit keyOf: KeyOf[K]): KeyOf[Entry[K, V]] =
    keyOf.contramap(_.key)
}

trait KeyOfInstances1 {
  implicit def keyOfInstanceForAny[T]: KeyOf[T] = new KeyOf[T] {
    type Key = T
    def apply(value: T): Key = value
  }
}
