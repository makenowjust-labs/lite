package codes.quine.labo.lite.delta

class KeyOfSuite extends munit.FunSuite {
  test("KeyOf.keyOfInstanceForAny") {
    assertEquals(KeyOf.keyOfInstanceForAny[Int].apply(1).asInstanceOf[Any], 1)
  }
  test("KeyOf.keyOfInstanceForEntry") {
    assertEquals(KeyOf.keyOfInstanceForEntry[Int, Int].apply(Entry(1, 2)).asInstanceOf[Any], 1)
  }
}
