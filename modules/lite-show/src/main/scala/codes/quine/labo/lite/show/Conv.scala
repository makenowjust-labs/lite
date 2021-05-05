package codes.quine.labo.lite.show

import scala.annotation.switch
import scala.annotation.tailrec

import codes.quine.labo.lite.show.Frag._

/** Conv is a creator of a function converts any values to fragments. */
trait Conv { f =>

  /** Creates a partial converter on taking the final converter. */
  def create(rec: Conv.Rec): Conv.Partial

  /** Converts this into a usual function. */
  final def toFunction: Conv.Rec = new Conv.Rec { rec =>
    val pf: Conv.Partial = create(rec)
    def apply(v: Any): List[Frag] = pf.applyOrElse(v, (_: Any) => List(Lit(v.toString)))
  }

  /** Combines two converters into a new converter. */
  def orElse(g: Conv): Conv = new Conv.Chain(Vector(f, g))
}

object Conv {

  /** A function type from any values to fragments. */
  type Rec = Any => List[Frag]

  /** A partial function type from any values to fragments. */
  type Partial = PartialFunction[Any, List[Frag]]

  /** Chain is a converter implementation for efficient combination. */
  private class Chain(convs: Vector[Conv]) extends Conv {
    def create(rec: Rec): Partial = {
      val pfs = convs.map(_.create(rec))

      @tailrec
      def loop(pfs: Vector[Partial], v: Any): Option[List[Frag]] =
        if (pfs.isEmpty) None
        else
          pfs.head.unapply(v) match {
            case None        => loop(pfs.tail, v)
            case Some(frags) => Some(frags)
          }

      (loop(pfs, _)).unlift
    }

    override def orElse(g: Conv): Conv = new Chain(convs :+ g)
  }

  /** Builds fragments look like `apply` syntax. */
  private def buildApply(name: String, values: LazyList[List[Frag]], maxSize: Int = Int.MaxValue): List[Frag] =
    if (values.isEmpty) List(Lit(s"$name()"))
    else {
      val v = values.head
      val vs = values.take(maxSize).tail.flatMap(v => List(Lit(","), Line) ++ v)
      val r = if (values.sizeIs > maxSize) List(Lit(","), Line, Lit("...")) else List.empty
      List(Group(List(Lit(s"$name("), Indent(List(Break) ++ v ++ vs ++ r), Break, Lit(")"))))
    }

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
        // On Scala 3.0.0-RC3 `f"\\"` returns `"\\\\"`.
        // The following concatenation is to avoid this.
        if (c < ' ') "\\" + f"u${c.toInt}%04X"
        else c.toString
    }

  def default(maxSize: Int = 30): Conv =
    `null`.orElse(string).orElse(char).orElse(boolean).orElse(number).orElse(iterable(maxSize)).orElse(product)

  /** A converter for `null`. */
  def `null`: Conv = { _ =>
    { case null => List(Lit("null")) }
  }

  /** A converter for string values. */
  def string: Conv = { _ =>
    { case s: String =>
      val sb = new StringBuilder
      sb.append('"')
      for (c <- s) sb.append(if (c == '"') "\\\"" else escape(c))
      sb.append('"')
      List(Lit(sb.result()))
    }
  }

  /** A converter for char values. */
  def char: Conv = { _ =>
    { case c: Char =>
      List(Lit("'" + (if (c == '\'') "\\'" else escape(c)) + "'"))
    }
  }

  /** A converter for boolean values. */
  def boolean: Conv = { _ =>
    { case b: Boolean => List(Lit(b.toString)) }
  }

  /** A converter for number values. */
  def number: Conv = { _ =>
    {
      case x: Byte       => List(Lit(x.toString))
      case x: Short      => List(Lit(x.toString))
      case x: Int        => List(Lit(x.toString))
      case x: Long       => List(Lit(x.toString + "L"))
      case x: Float      => List(Lit(x.toString + "F"))
      case x: Double     => List(Lit(x.toString))
      case x: BigInt     => List(Lit(x.toString))
      case x: BigDecimal => List(Lit(x.toString))
    }
  }

  /** A converter for iterable values. */
  def iterable(maxSize: Int = 30): Conv = { rec =>
    {
      case m: Map[_, _] =>
        buildApply(
          Compat.stringPrefix(m),
          LazyList.from(m).map { case (k, v) => rec(k) ++ List(Lit(" -> ")) ++ rec(v) },
          maxSize
        )
      case i: Iterable[_] =>
        buildApply(Compat.stringPrefix(i), LazyList.from(i).map(rec), maxSize)
    }
  }

  /** A converter for product and tuple values. */
  def product: Conv = { rec =>
    {
      case ()                                => List(Lit("()"))
      case p: Product if p.productArity == 0 => List(Lit(p.toString))
      case p: Product =>
        val isTuple = p.productPrefix.startsWith("Tuple")
        val prefix = if (isTuple) "" else p.productPrefix
        buildApply(
          prefix,
          LazyList.from(p.productIterator).zipWithIndex.map { case (v, i) =>
            (if (isTuple) List.empty else List(Wide(s"${p.productElementName(i)} = "))) ++ rec(v)
          }
        )
    }
  }
}
