package codes.quine.labo.lite.delta

import scala.io.AnsiColor

import codes.quine.labo.lite.delta.Diff.DiffGenOps
import codes.quine.labo.lite.show.Prettify
import codes.quine.labo.lite.show.Pretty
import codes.quine.labo.lite.show.Pretty._

/** Delta is an object for representing a difference between two values. */
sealed abstract class Delta extends Product with Serializable {

  /** Checks whether or not this is identical. */
  def isIdentical: Boolean

  /** Renders this into pretty fragments for showing. */
  def prettify(showIdentical: Boolean, prettify: Prettify): Seq[Pretty]
}

object Delta {

  /** Computes a difference between two values and returns prettified string of this diff. */
  def diff(left: Any, right: Any)(implicit diff: Diff = Diff.default().toDiff, opts: Diff.Opts = Diff.Opts()): String =
    diff.diff(left, right)

  /** Renders the given fragments as left item. */
  private def buildLeft(l: Seq[Pretty]): Seq[Pretty] =
    Seq(Lit(AnsiColor.RED, true)) ++ l ++ Seq(Lit(AnsiColor.RESET, true))

  /** Renders the given fragments as right item. */
  private def buildRight(r: Seq[Pretty]): Seq[Pretty] =
    Seq(Lit(AnsiColor.GREEN, true)) ++ r ++ Seq(Lit(AnsiColor.RESET, true))

  /** Renders items with ignoring by the given function. */
  private def buildSeq[T](ts: Seq[T])(isIgnore: T => Boolean, show: T => Seq[Pretty]): LazyList[Seq[Pretty]] =
    if (ts.isEmpty) LazyList.empty
    else {
      val (ls, rs) = ts.span(isIgnore)
      val prefix = if (ls.isEmpty) LazyList.empty else LazyList(Seq(Lit("...")))
      rs.headOption match {
        case Some(r) =>
          prefix ++ LazyList(show(r)) ++ buildSeq(rs.tail)(isIgnore, show)
        case None => prefix
      }
    }

  /** Apply is a delta object for case classes. */
  final case class Case(name: String, fields: Seq[(String, Delta)]) extends Delta {
    def isIdentical: Boolean = fields.forall(_._2.isIdentical)
    def prettify(showIdentical: Boolean, prettify: Prettify): Seq[Pretty] = {
      val fs = buildSeq(fields)(
        { f => !showIdentical && f._2.isIdentical },
        { f => List(Wide(s"${f._1} = ")) ++ f._2.prettify(showIdentical, prettify) }
      )
      Prettify.buildApply(name, fs)
    }
  }

  /** Map is a delta object for mappings. */
  final case class Mapping(name: String, entries: Seq[(Any, Delta)]) extends Delta {
    def isIdentical: Boolean = entries.forall(e => e._2.isIdentical)
    def prettify(showIdentical: Boolean, prettify: Prettify): Seq[Pretty] = {
      val es = buildSeq(entries)(
        { e => !showIdentical && e._2.isIdentical },
        { e => prettify(e._1) ++ Seq(Lit(" -> ")) ++ e._2.prettify(showIdentical, prettify) }
      )
      Prettify.buildApply(name, es)
    }
  }

  /** Set is a delta object for sequences. */
  final case class Sequence(name: String, deltas: Seq[Delta]) extends Delta {
    def isIdentical: Boolean = deltas.forall(_.isIdentical)
    def prettify(showIdentical: Boolean, prettify: Prettify): Seq[Pretty] = {
      val ds = buildSeq(deltas)(d => !showIdentical && d.isIdentical, _.prettify(showIdentical, prettify))
      Prettify.buildApply(name, ds)
    }
  }

  /** Identical is a delta object with an identical value. */
  final case class Identical(value: Any) extends Delta {
    def isIdentical: Boolean = true
    def prettify(showIdentical: Boolean, prettify: Prettify): Seq[Pretty] =
      prettify(value)
  }

  /** Changed is a delta object with changed values. */
  final case class Changed(left: Any, right: Any) extends Delta {
    def isIdentical: Boolean = false
    def prettify(showIdentical: Boolean, prettify: Prettify): Seq[Pretty] =
      buildLeft(prettify(left)) ++ Seq(Lit(" => ")) ++ buildRight(prettify(right))
  }

  /** Missing is a delta object with a missing value. */
  final case class Missing(right: Any) extends Delta {
    def isIdentical: Boolean = false
    def prettify(showIdentical: Boolean, prettify: Prettify): Seq[Pretty] =
      buildRight(prettify(right))
  }

  /** Missing is a delta object with an additional value. */
  final case class Additional(left: Any) extends Delta {
    def isIdentical: Boolean = false
    def prettify(showIdentical: Boolean, prettify: Prettify): Seq[Pretty] =
      buildLeft(prettify(left))
  }
}
