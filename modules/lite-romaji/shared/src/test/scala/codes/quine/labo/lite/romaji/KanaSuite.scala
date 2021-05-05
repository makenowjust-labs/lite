package codes.quine.labo.lite.romaji

class KanaSuite extends munit.FunSuite {
  test("Kana.toRomaji") {
    assertEquals(Kana.toRomaji("ローマジ カラ カナ ヘノ ヘンカン"), "ro-maji kara kana heno henkan")
    assertEquals(Kana.toRomaji("テンペン チイ"), "tempen chii")
    assertEquals(Kana.toRomaji("アサッテ ノ ホウコウ"), "asatte no houkou")
    assertEquals(Kana.toRomaji("コンニチハ"), "konnnichiha")
    assertEquals(Kana.toRomaji("ウッウ"), "uxtsuu")
  }
}
