package codes.quine.labo.lite.delta

import codes.quine.labo.lite.delta.Key.KeyGenOps
import codes.quine.labo.lite.gestalt.Gestalt
import codes.quine.labo.lite.pfix.PFix
import codes.quine.labo.lite.show.{Prettify, Pretty}
import codes.quine.labo.lite.show.Prettify.PrettifyGenOps

/** Diff is a function for computing a difference between two vales. */
trait Diff extends ((Any, Any) => Delta) {

  /** Computes a difference between two values and returns prettified string of this diff. */
  def diff(left: Any, right: Any)(implicit opts: Diff.Opts = Diff.Opts()): String = {
    val delta = apply(left, right)
    val frags = delta.prettify(opts.showIdentical, opts.prettify)
    Pretty.render(frags, opts.width, opts.indentSize)
  }
}

object Diff {

  /** A generator function of a diff function. */
  type Gen = PFix[(Any, Any), Delta]

  object Gen {

    /** Returns a new generator. */
    def apply(f: ((Any, Any) => Delta) => PartialFunction[(Any, Any), Delta]): Gen =
      PFix(rec => f((l, r) => rec((l, r))))

    /** Converts a partial function into a generator. */
    def from(pf: PartialFunction[(Any, Any), Delta]): Gen = PFix.from(pf)
  }

  /** GenOps provides `toDiff` method into `Gen` instance. */
  implicit class DiffGenOps(private val g: Gen) extends AnyVal {

    /** Converts a generator into a diff function. */
    def toDiff: Diff = {
      val f = g.toFunction { case (l, r) => if (l == r) Delta.Identical(l) else Delta.Changed(l, r) }
      (l, r) => f((l, r))
    }
  }

  /** Opts is a set of options for [[Diff.diff]] method. */
  final case class Opts(
      width: Int = 80,
      indentSize: Int = 2,
      showIdentical: Boolean = true,
      prettify: Prettify = Prettify.default().toPrettify
  )

  /** A default diff function. */
  def default(key: Key = Key.default.toKey): Gen =
    `null`.orElse(map).orElse(set).orElse(seq(key)).orElse(product)

  /** A diff function for null values. */
  def `null`: Gen = Gen.from {
    case (null, null) => Delta.Identical(null)
    case (null, r)    => Delta.Changed(null, r)
    case (l, null)    => Delta.Changed(l, null)
  }

  /** A diff function for mapping values. */
  def map: Gen = Gen { rec =>
    { case (left: Map[_, _], right: Map[_, _]) =>
      val name = Prettify.stringPrefix(left)
      val keys =
        (left.asInstanceOf[Map[Any, Any]].keySet | right.asInstanceOf[Map[Any, Any]].keySet).toSeq.sortBy(_.toString)
      val entries = Seq.newBuilder[(Any, Delta)]
      for (k <- keys) {
        (left.asInstanceOf[Map[Any, Any]].get(k), right.asInstanceOf[Map[Any, Any]].get(k)) match {
          case (Some(l), Some(r)) => entries.addOne(k -> rec(l, r))
          case (None, Some(r))    => entries.addOne(k -> Delta.Missing(r))
          case (Some(l), None)    => entries.addOne(k -> Delta.Additional(l))
          case (None, None)       =>
            // $COVERAGE-OFF$
            sys.error("unreachable")
          // $COVERAGE-ON$
        }
      }
      Delta.Mapping(name, entries.result())
    }
  }

  /** A diff function for sequence values. */
  def seq(key: Key): Gen = Gen { rec =>
    { case (left: Seq[_], right: Seq[_]) =>
      val name = Prettify.stringPrefix(left)
      val leftISeq = left.toIndexedSeq
      val rightISeq = right.toIndexedSeq
      val leftKeys = leftISeq.map(key)
      val rightKeys = rightISeq.map(key)
      val hunks = Gestalt.diff(leftKeys, rightKeys).hunks

      val deltas = Seq.newBuilder[Delta]
      var leftIndex = 0
      var rightIndex = 0
      for (hunk <- hunks) {
        for ((i, j) <- (leftIndex until hunk.leftStart).zip(rightIndex until hunk.rightStart))
          deltas.addOne(rec(leftISeq(i), rightISeq(j)))
        for (i <- hunk.leftStart until hunk.leftEnd) deltas.addOne(Delta.Additional(leftISeq(i)))
        for (j <- hunk.rightStart until hunk.rightEnd) deltas.addOne(Delta.Missing(rightISeq(j)))
        leftIndex = hunk.leftEnd
        rightIndex = hunk.rightEnd
      }
      for ((i, j) <- (leftIndex until leftISeq.size).zip(rightIndex until rightISeq.size))
        deltas.addOne(rec(leftISeq(i), rightISeq(j)))

      Delta.Sequence(name, deltas.result())
    }
  }

  /** A diff function for set values. */
  def set: Gen = Gen.from { case (left: Set[_], right: Set[_]) =>
    val name = Prettify.stringPrefix(left)
    val is = left.asInstanceOf[Set[Any]] & right.asInstanceOf[Set[Any]]

    val deltas = Seq.newBuilder[Delta]
    for (x <- is) deltas.addOne(Delta.Identical(x))
    for (l <- left.asInstanceOf[Set[Any]]; if !is.contains(l)) deltas.addOne(Delta.Additional(l))
    for (r <- right.asInstanceOf[Set[Any]]; if !is.contains(r)) deltas.addOne(Delta.Missing(r))

    Delta.Sequence(name, deltas.result())
  }

  /** A diff function for product values. */
  def product: Gen = Gen { rec =>
    {
      case ((), ()) => Delta.Identical(())
      case (left: Product, right: Product) if left.getClass == right.getClass =>
        val isTuple = left.productPrefix.startsWith("Tuple")
        val name = if (isTuple) "" else left.productPrefix
        val fields = Seq.newBuilder[(String, Delta)]
        for (((l, r), i) <- left.productIterator.zip(right.productIterator).zipWithIndex) {
          val fieldName = if (isTuple) s"_${i + 1}" else left.productElementName(i)
          fields.addOne(fieldName -> rec(l, r))
        }
        Delta.Case(name, fields.result())
    }
  }
}
