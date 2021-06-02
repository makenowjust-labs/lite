package codes.quine.labo.lite.diff

class DiffSuite extends munit.FunSuite {
  test("Diff.diff") {
    val left = IndexedSeq(
      "line 0",
      "line 1",
      "line 2",
      "line 3",
      "line 4-1",
      "line 4-2",
      "line 5",
      "line 6",
      "line 7",
      "line 8"
    )
    val right = IndexedSeq(
      "line 0",
      "line 1",
      "line 2",
      "line 3",
      "line 4-0",
      "line 4-1",
      "line 5",
      "line 6",
      "line 7",
      "line 8"
    )
    val hunks = Seq(Hunk(4, 4, 4, 5), Hunk(5, 6, 6, 6))
    assertEquals(Diff.diff(left, right), Patch(left, right, hunks))
    assertEquals(
      Diff.diff(IndexedSeq("x"), IndexedSeq("y")),
      Patch(IndexedSeq("x"), IndexedSeq("y"), Seq(Hunk(0, 1, 0, 1)))
    )
  }
}
