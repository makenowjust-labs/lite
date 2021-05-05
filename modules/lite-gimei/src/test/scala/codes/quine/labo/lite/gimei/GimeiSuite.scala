package codes.quine.labo.lite.gimei

import scala.util.Random

class GimeiSuite extends munit.FunSuite {
  test("Gimei#name") {
    assert(Gimei.name() ne null)
    val random = new Random(123)
    assertEquals(Gimei.name(random).toKanji, "川島 悠弥")
    assertEquals(Gimei.name(random).toKanji, "浅野 采樹")
  }

  test("Address#name") {
    assert(Gimei.address() ne null)
    val random = new Random(123)
    assertEquals(Gimei.address(random).toKanji, "宮崎県浜松市北区南起")
  }
}
