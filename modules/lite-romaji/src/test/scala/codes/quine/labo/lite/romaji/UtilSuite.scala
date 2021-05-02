package codes.quine.labo.lite.romaji

class UtilSuite extends munit.FunSuite {
  test("Util.isAlphabet") {
    assertEquals(Util.isAlphabet('a'), true)
    assertEquals(Util.isAlphabet('m'), true)
    assertEquals(Util.isAlphabet('z'), true)
    assertEquals(Util.isAlphabet('0'), false)
    assertEquals(Util.isAlphabet('#'), false)
    assertEquals(Util.isAlphabet(' '), false)
  }

  test("Util.isVowel") {
    assertEquals(Util.isVowel('a'), true)
    assertEquals(Util.isVowel('i'), true)
    assertEquals(Util.isVowel('u'), true)
    assertEquals(Util.isVowel('e'), true)
    assertEquals(Util.isVowel('o'), true)
    assertEquals(Util.isVowel('n'), true)
    assertEquals(Util.isVowel('p'), false)
    assertEquals(Util.isVowel('b'), false)
    assertEquals(Util.isVowel('m'), false)
  }

  test("Util.isPBM") {
    assertEquals(Util.isPBM('p'), true)
    assertEquals(Util.isPBM('b'), true)
    assertEquals(Util.isPBM('m'), true)
    assertEquals(Util.isPBM('a'), false)
    assertEquals(Util.isPBM('z'), false)
  }
}
