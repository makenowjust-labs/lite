package codes.quine.labo.lite.delta

class DeltaSuite extends munit.FunSuite {
  test("Delta#isIdentical") {
    assertEquals(Delta.Case("Foo", Seq.empty).isIdentical, true)
    assertEquals(Delta.Case("Foo", Seq(Entry("x", Delta.Identical(1)))).isIdentical, true)
    assertEquals(Delta.Case("Foo", Seq(Entry("x", Delta.Missing(1)))).isIdentical, false)
    assertEquals(Delta.Map("Map", Seq(Entry(Delta.Identical(1), Delta.Identical(2))), " -> ").isIdentical, true)
    assertEquals(Delta.Map("Map", Seq(Entry(Delta.Identical(1), Delta.Missing(2))), " -> ").isIdentical, false)
    assertEquals(Delta.Map("Map", Seq(Entry(Delta.Missing(1), Delta.Identical(2))), " -> ").isIdentical, false)
    assertEquals(Delta.Set("Set", Seq(Delta.Identical(1))).isIdentical, true)
    assertEquals(Delta.Set("Set", Seq(Delta.Missing(1))).isIdentical, false)
    assertEquals(Delta.Identical(1).isIdentical, true)
    assertEquals(Delta.Missing(1).isIdentical, false)
    assertEquals(Delta.Additional(1).isIdentical, false)
    assertEquals(Delta.Changed(1, 2).isIdentical, false)
  }
}
