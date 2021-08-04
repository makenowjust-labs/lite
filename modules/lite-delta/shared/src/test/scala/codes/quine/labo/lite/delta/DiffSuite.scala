package codes.quine.labo.lite.delta

import codes.quine.labo.lite.delta.Diff.DiffGenOps
import codes.quine.labo.lite.delta.Key.KeyGenOps

class DiffSuite extends munit.FunSuite {
  test("Diff.default") {
    val diff = Diff.default().toDiff
    assertEquals(diff(1, 2), Delta.Changed(1, 2))
  }

  test("Diff.null") {
    val diff = Diff.`null`.toDiff
    assertEquals(diff(null, null), Delta.Identical(null))
    assertEquals(diff(null, 1), Delta.Changed(null, 1))
    assertEquals(diff(1, null), Delta.Changed(1, null))
  }

  test("Diff.map") {
    val diff = Diff.map.toDiff
    assertEquals(diff(Map.empty, Map(1 -> 1)), Delta.Mapping("Map", Seq(1 -> Delta.Missing(1))))
    assertEquals(diff(Map(1 -> 1), Map.empty), Delta.Mapping("Map", Seq(1 -> Delta.Additional(1))))
    assertEquals(
      diff(Map(1 -> 1, 2 -> 2), Map(1 -> 1, 2 -> 2)),
      Delta.Mapping("Map", Seq(1 -> Delta.Identical(1), 2 -> Delta.Identical(2)))
    )
  }

  test("Diff.seq") {
    val diff = Diff.seq(Key.default.toKey).toDiff
    assertEquals(diff(Seq.empty, Seq(1)), Delta.Sequence("List", Seq(Delta.Missing(1))))
    assertEquals(diff(Seq(1), Seq.empty), Delta.Sequence("List", Seq(Delta.Additional(1))))
    assertEquals(diff(Seq(1), Seq(1)), Delta.Sequence("List", Seq(Delta.Identical(1))))
  }

  test("Diff.set") {
    val diff = Diff.set.toDiff
    assertEquals(diff(Set.empty, Set(1)), Delta.Sequence("Set", Seq(Delta.Missing(1))))
    assertEquals(diff(Set(1), Set.empty), Delta.Sequence("Set", Seq(Delta.Additional(1))))
    assertEquals(diff(Set(1), Set(1)), Delta.Sequence("Set", Seq(Delta.Identical(1))))
  }

  test("Diff.product") {
    val diff = Diff.product.toDiff
    assertEquals(diff((), ()), Delta.Identical(()))
    assertEquals(diff(Left(1), Left(1)), Delta.Case("Left", Seq(("value", Delta.Identical(1)))))
    assertEquals(diff(Left(1), Right(1)), Delta.Changed(Left(1), Right(1)))
    assertEquals(diff((1, 2), (1, 2)), Delta.Case("", Seq(("_1", Delta.Identical(1)), ("_2", Delta.Identical(2)))))
  }
}
