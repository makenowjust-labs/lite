package codes.quine.labo.lite.delta

import scala.io.AnsiColor

import codes.quine.labo.lite.show.Conv
import codes.quine.labo.lite.show.Conv._
import codes.quine.labo.lite.show.Frag
import codes.quine.labo.lite.show.Frag._

/** DeltaConv is a converter for showing a delta object. */
class DeltaConv(showIdentical: Boolean = true) {
  def create(rec: Rec): Rec = {
    case Delta.Case(name, fs) =>
      val fields = shows(fs)(
        { e => e.value.isIdentical },
        { case Entry(n, v) =>
          List(Wide(s"$n = ")) ++ create(rec)(v)
        }
      )
      Conv.buildApply(name, fields)
    case Delta.Map(name, es, sep) =>
      val entries = shows(es)(
        { e => e.key.isIdentical && e.value.isIdentical },
        { case Entry(k, v) =>
          create(rec)(k) ++ List(Lit(sep)) ++ create(rec)(v)
        }
      )
      Conv.buildApply(name, entries)
    case Delta.Set(name, ds) =>
      val deltas = shows(ds)(_.isIdentical, create(rec)(_))
      Conv.buildApply(name, deltas)
    case Delta.Identical(v)  => rec(v)
    case Delta.Changed(l, r) => showLeft(rec(l)) ++ List(Lit(" => ")) ++ showRight(rec(r))
    case Delta.Additional(l) => showLeft(rec(l))
    case Delta.Missing(r)    => showRight(rec(r))
    case x                   => rec(x)
  }

  /** Shows the given fragments as left item. */
  protected def showLeft(l: List[Frag]): List[Frag] =
    List(Lit(AnsiColor.RED, true)) ++ l ++ List(Lit(AnsiColor.RESET, true))

  /** Shows the given fragments as right item. */
  protected def showRight(r: List[Frag]): List[Frag] =
    List(Lit(AnsiColor.GREEN, true)) ++ r ++ List(Lit(AnsiColor.RESET, true))

  /** Shows items with ignoring by the given function. */
  protected def shows[T](ts: Seq[T])(isIgnore: T => Boolean, show: T => List[Frag]): LazyList[List[Frag]] =
    if (ts.isEmpty) LazyList.empty
    else {
      val (ls, rs) = if (showIdentical) (Seq.empty, ts) else ts.span(isIgnore)
      val prefix = if (ls.isEmpty) LazyList.empty else LazyList(List(Lit("...")))
      rs.headOption match {
        case Some(r) =>
          prefix ++ LazyList(show(r)) ++ shows(rs.tail)(isIgnore, show)
        case None => prefix
      }
    }
}
