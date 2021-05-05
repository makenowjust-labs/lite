package codes.quine.labo.lite.gimei

class WordSuite extends munit.FunSuite {
  test("Word.toRomaji") {
    assertEquals(Word("田中", "たなか", "タナカ").toRomaji, "Tanaka")
  }
}
