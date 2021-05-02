package codes.quine.labo.lite.romaji

class RomajiSuite extends munit.FunSuite {
  test("Romaji.toKana") {
    assertEquals(Romaji.toKana("ro-maji to kana wo henkan suru"), "ローマジ ト カナ ヲ ヘンカン スル")
    assertEquals(Romaji.toKana("O-Moji Ga Fukumarete Ite Mo O-Kei"), "オーモジ ガ フクマレテ イテ モ オーケイ")
    assertEquals(Romaji.toKana("tempen chii"), "テンペン チイ")
    assertEquals(Romaji.toKana("asatte no houkou"), "アサッテ ノ ホウコウ")
  }
}
