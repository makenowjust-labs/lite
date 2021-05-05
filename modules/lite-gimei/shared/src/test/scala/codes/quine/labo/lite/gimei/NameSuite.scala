package codes.quine.labo.lite.gimei

class NameSuite extends munit.FunSuite {
  def name: Name = {
    val firstName = Word("太郎", "たろう", "タロウ")
    val lastName = Word("田中", "たなか", "タナカ")
    Name(Name.Gender.Male, firstName, lastName)
  }

  test("Name#toKanji") {
    assertEquals(name.toKanji, "田中 太郎")
  }

  test("Name#toHiragana") {
    assertEquals(name.toHiragana, "たなか たろう")
  }

  test("Name#toKatakana") {
    assertEquals(name.toKatakana, "タナカ タロウ")
  }

  test("Name#toRomaji") {
    assertEquals(name.toRomaji, "Tarou Tanaka")
  }
}
