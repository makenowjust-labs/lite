package codes.quine.labo.lite.delta

import codes.quine.labo.lite.pfix.PFix

/** Key is a key locator function. */
trait Key extends (Any => Any)

object Key {

  /** A generator function of a key function. */
  type Gen = PFix[Any, Any]

  object Gen {

    /** Returns a new generator. */
    def apply(f: (Any => Any) => PartialFunction[Any, Any]): Gen = PFix(f)

    /** Converts a partial function into a generator. */
    def from(pf: PartialFunction[Any, Any]): Gen = PFix.from(pf)
  }

  /** GenOps provides `toKey` method into `Gen` instance. */
  implicit class KeyGenOps(private val g: Gen) extends AnyVal {

    /** Converts a generator into a key function. */
    def toKey: Key = {
      val f = g.toFunction(_.getClass)
      f(_)
    }
  }

  /** A default key locator. */
  def default: Gen = primitive.orElse(iterable).orElse(product)

  /** A key locator for primitive values. */
  def primitive: Gen = Gen.from {
    case x: Boolean    => x
    case x: String     => x
    case x: Char       => x
    case x: Byte       => x
    case x: Short      => x
    case x: Int        => x
    case x: Long       => x
    case x: Double     => x
    case x: Float      => x
    case x: BigInt     => x
    case x: BigDecimal => x
  }

  /** A key locator for iterable values. */
  def iterable: Gen = Gen { rec =>
    {
      case s: Set[_]    => s.map(rec)
      case m: Map[_, _] => m.map { case (k, v) => (k, rec(v)) }
      case s: Seq[_]    => s.map(rec)
    }
  }

  /** A key locator for product values. */
  def product: Gen = Gen { rec =>
    {
      case ()               => ()
      case (x1, x2)         => (rec(x1), rec(x2))
      case (x1, x2, x3)     => (rec(x1), rec(x2), rec(x3))
      case (x1, x2, x3, x4) => (rec(x1), rec(x2), rec(x3), rec(x4))
      case p: Product       => p.productPrefix
    }
  }
}
