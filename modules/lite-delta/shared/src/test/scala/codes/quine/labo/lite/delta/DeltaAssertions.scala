package codes.quine.labo.lite.delta

import codes.quine.labo.lite.show.Conv
import codes.quine.labo.lite.show.Show
import munit.FailException

trait DeltaAssertions { self: munit.FunSuite =>
  def assertEquals[T](obtained: T, expected: T)(implicit loc: munit.Location, diff: DeltaDiff[T]): Unit = {
    if (obtained == expected) return
    val message = "Equality checking is failed.\n\ndiff:\n" ++
      Show(new DeltaConv(showIdentical = false).create(Conv.default().toFunction)).show(diff(obtained, expected))
    throw new FailException(message, location = loc)
  }

  implicit def diffInstanceForDelta: DeltaDiff[Delta] = {
    case (Delta.Identical(l), Delta.Identical(r)) =>
      Delta.Case("Identical", Seq(Entry("value", DeltaDiff.default(l, r))))
    case (Delta.Additional(l), Delta.Additional(r)) =>
      Delta.Case("Additional", Seq(Entry("value", DeltaDiff.default(l, r))))
    case (Delta.Missing(l), Delta.Missing(r)) =>
      Delta.Case("Missing", Seq(Entry("value", DeltaDiff.default(l, r))))
    case (Delta.Changed(l1, l2), Delta.Changed(r1, r2)) =>
      Delta.Case("Changed", Seq(Entry("left", DeltaDiff.default(l1, r1)), Entry("right", DeltaDiff.default(l2, r2))))
    case (Delta.Case(n1, fs1), Delta.Case(n2, fs2)) =>
      Delta.Case(
        "Case",
        Seq(
          Entry("name", DeltaDiff[String].apply(n1, n2)),
          Entry("fields", DeltaDiff[Seq[Entry[String, Delta]]].apply(fs1, fs2))
        )
      )
    case (Delta.Map(n1, es1, s1), Delta.Map(n2, es2, s2)) =>
      Delta.Case(
        "Map",
        Seq(
          Entry("name", DeltaDiff[String].apply(n1, n2)),
          Entry("entries", DeltaDiff[Seq[Entry[Delta, Delta]]].apply(es1, es2)),
          Entry("sep", DeltaDiff[String].apply(s1, s2))
        )
      )
    case (Delta.Set(n1, ds1), Delta.Set(n2, ds2)) =>
      Delta.Case(
        "Map",
        Seq(
          Entry("name", DeltaDiff[String].apply(n1, n2)),
          Entry("deltas", DeltaDiff[Seq[Delta]].apply(ds1, ds2))
        )
      )
    case (l, r) => Delta.Changed(l, r)
  }

  implicit def diffInstanceForEntry[K, V](implicit diffK: DeltaDiff[K], diffV: DeltaDiff[V]): DeltaDiff[Entry[K, V]] =
    (left: Entry[K, V], right: Entry[K, V]) => {
      Delta.Case("Entry", Seq(Entry("key", diffK(left.key, right.key)), Entry("value", diffV(left.value, right.value))))
    }
}
