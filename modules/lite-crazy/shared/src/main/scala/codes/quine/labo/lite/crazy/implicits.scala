package codes.quine.labo.lite.crazy

/** implicits provides convenience implicit conversions. */
object implicits {
  import scala.language.implicitConversions

  /** Wraps any value to lazy cell. */
  implicit def any2lazy[A](x: => A): Lazy[A] = Lazy(x)

  /** Unwraps any value from a lazy cell. */
  implicit def lazy2any[A](lx: Lazy[A]): A = lx.value
}
