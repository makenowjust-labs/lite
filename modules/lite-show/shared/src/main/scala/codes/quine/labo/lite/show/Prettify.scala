package codes.quine.labo.lite.show

import codes.quine.labo.lite.pfix.PFix

import scala.annotation.switch
import codes.quine.labo.lite.show.Pretty._

/** Prettify is a function for converting any objects into fragments. */
trait Prettify extends (Any => Seq[Pretty])

object Prettify {

  /** A generator function of a prettify function. */
  type Gen = PFix[Any, Seq[Pretty]]

  object Gen {

    /** Returns a new generator. */
    def apply(f: (Any => Seq[Pretty]) => PartialFunction[Any, Seq[Pretty]]): Gen = PFix(f)

    /** Converts a partial function into a generator. */
    def from(pf: PartialFunction[Any, Seq[Pretty]]): Gen = PFix.from(pf)
  }

  /** GenOps provides `toPrettify` method into `Gen` instance. */
  implicit class GenOps(val g: Gen) extends AnyVal {

    /** Converts a generator into a prettify function. */
    def toPrettify: Prettify = {
      val f = g.toFunction(x => Seq(Pretty.Lit(x.toString)))
      f(_)
    }
  }

  /** Builds fragments look like `apply` syntax. */
  def buildApply(name: String, values: LazyList[Seq[Pretty]], maxSize: Int = Int.MaxValue): Seq[Pretty] =
    if (values.isEmpty) Seq(Lit(s"$name()"))
    else {
      val v = values.head
      val vs = values.take(maxSize).tail.flatMap(v => Seq(Lit(","), Line) ++ v)
      val r = if (values.sizeIs > maxSize) Seq(Lit(","), Line, Lit("...")) else Seq.empty
      Seq(Group(Seq(Lit(s"$name("), Indent(Seq(Break) ++ v ++ vs ++ r), Break, Lit(")"))))
    }

  /** Returns a string prefix of a iterable value. */
  def stringPrefix(i: Iterable[_]): String = Compat.stringPrefix(i)

  /** Escapes the character as literal if needed. */
  private def escape(c: Char): String =
    (c: @switch) match {
      case '\\'     => "\\\\"
      case '\b'     => "\\b"
      case '\f'     => "\\f"
      case '\n'     => "\\n"
      case '\r'     => "\\r"
      case '\t'     => "\\t"
      case '\u007F' => "\\u007F"
      case c        =>
        // On Scala 3.0.0 `f"\\"` returns `"\\\\"`.
        // See https://github.com/lampepfl/dotty/issues/11750.
        // The following concatenation is to avoid this.
        if (c < ' ') "\\" + f"u${c.toInt}%04X"
        else c.toString
    }

  def default(maxSize: Int = 30): Gen =
    `null`.orElse(string).orElse(char).orElse(boolean).orElse(number).orElse(iterable(maxSize)).orElse(product)

  /** A converter for `null`. */
  def `null`: Gen = Gen.from { case null => List(Lit("null")) }

  /** A converter for string values. */
  def string: Gen = Gen.from { case s: String =>
    val sb = new StringBuilder
    sb.append('"')
    for (c <- s) sb.append(if (c == '"') "\\\"" else escape(c))
    sb.append('"')
    Seq(Lit(sb.result()))
  }

  /** A converter for char values. */
  def char: Gen = Gen.from { case c: Char =>
    Seq(Lit("'" + (if (c == '\'') "\\'" else escape(c)) + "'"))
  }

  /** A converter for boolean values. */
  def boolean: Gen = Gen.from { case b: Boolean => Seq(Lit(b.toString)) }

  /** A converter for number values. */
  def number: Gen = Gen.from {
    case x: Byte       => Seq(Lit(x.toString))
    case x: Short      => Seq(Lit(x.toString))
    case x: Int        => Seq(Lit(x.toString))
    case x: Long       => Seq(Lit(x.toString + "L"))
    case x: Double     => Seq(Lit(x.toString))
    case x: Float      => Seq(Lit(x.toString + "F"))
    case x: BigInt     => Seq(Lit(x.toString))
    case x: BigDecimal => Seq(Lit(x.toString))
  }

  /** A converter for iterable values. */
  def iterable(maxSize: Int = 30): Gen = Gen { rec =>
    {
      case m: Map[_, _] =>
        buildApply(
          stringPrefix(m),
          LazyList.from(m).map { case (k, v) => rec(k) ++ Seq(Lit(" -> ")) ++ rec(v) },
          maxSize
        )
      case i: Iterable[_] =>
        buildApply(stringPrefix(i), LazyList.from(i).map(rec), maxSize)
    }
  }

  /** A converter for product and tuple values. */
  def product: Gen = Gen { rec =>
    {
      case ()                                => Seq(Lit("()"))
      case p: Product if p.productArity == 0 => Seq(Lit(p.toString))
      case p: Product =>
        val isTuple = p.productPrefix.startsWith("Tuple")
        val prefix = if (isTuple) "" else p.productPrefix
        buildApply(
          prefix,
          LazyList.from(p.productIterator).zipWithIndex.map { case (v, i) =>
            (if (isTuple) Seq.empty else Seq(Wide(s"${p.productElementName(i)} = "))) ++ rec(v)
          }
        )
    }
  }
}
