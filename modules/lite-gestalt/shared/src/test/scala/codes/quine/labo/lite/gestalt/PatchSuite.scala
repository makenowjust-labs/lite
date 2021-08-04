package codes.quine.labo.lite.gestalt

class PatchSuite extends munit.FunSuite {
  test("Patch#toUnified") {
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
    val patch = Patch(left, right, hunks)
    assertEquals(
      patch.toUnified(),
      """|@@ -2,8 +2,8
         | line 1
         | line 2
         | line 3
         |+line 4-0
         | line 4-1
         |-line 4-2
         | line 5
         | line 6
         | line 7
         |""".stripMargin
    )
    assertEquals(
      patch.toUnified(context = 1),
      """|@@ -4,4 +4,4
         | line 3
         |+line 4-0
         | line 4-1
         |-line 4-2
         | line 5
         |""".stripMargin
    )
    assertEquals(
      patch.toUnified(context = 0),
      """|@@ -5,0 +5,1
         |+line 4-0
         |@@ -6,1 +7,0
         |-line 4-2
         |""".stripMargin
    )
  }
}
