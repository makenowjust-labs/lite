package codes.quine.labo.lite.gimei

class AddressSuite extends munit.FunSuite {
  val address: Address = {
    val prefecture = Word("東京都", "とうきょうと", "トウキョウト")
    val city = Word("北区", "きたく", "キタク")
    val town = Word("赤羽", "あかばね", "アカバネ")
    Address(prefecture, city, town)
  }

  test("Address#toKanji") {
    assertEquals(address.toKanji, "東京都北区赤羽")
  }

  test("Address#toHiragana") {
    assertEquals(address.toHiragana, "とうきょうときたくあかばね")
  }

  test("Address#toKatakana") {
    assertEquals(address.toKatakana, "トウキョウトキタクアカバネ")
  }

  test("Address#toRomaji") {
    assertEquals(address.toRomaji, "Toukyouto Kitaku Akabane")
  }
}
