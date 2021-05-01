package codes.quine.labo.lite.show

class ShowSuite extends munit.FunSuite {
  test("Show.apply") {
    assertEquals(Show().show(List(1, 2, 3)), "List(1, 2, 3)")
    assertEquals(Show(width = 5).show(List(1, 2, 3)), "List(\n  1,\n  2,\n  3\n)")
  }

  test("Show.show") {
    assertEquals(Show.show(List(1, 2, 3)), "List(1, 2, 3)")
    assertEquals(Show.show(Vector(1, 2, 3)), "Vector(1, 2, 3)")
  }
}
