package codes.quine.labo.lite.delta

import codes.quine.labo.lite.delta.Key.KeyGenOps

class KeySuite extends munit.FunSuite {
  test("Key.default") {
    val key = Key.default.toKey
    assertEquals(key(12), 12)
  }

  test("Key.primitive") {
    val key = Key.primitive.toKey
    assertEquals(key(12: Byte), 12: Byte)
    assertEquals(key(12: Short), 12: Short)
    assertEquals(key(12: Int), 12: Int)
    assertEquals(key(12L), 12L)
    assertEquals(key(1.23f), 1.23f)
    assertEquals(key(1.23), 1.23)
    assertEquals(key(12: BigInt), 12: BigInt)
    assertEquals(key(1.23: BigDecimal), 1.23: BigDecimal)
  }

  test("Key.iterable") {
    val key = Key.iterable.toKey
    assertEquals(key(Seq(0x10000)), Seq(classOf[Integer]))
    assertEquals(key(Map(0x10000 -> 0x10000)), Map(0x10000 -> classOf[Integer]))
    assertEquals(key(Set(0x10000)), Set(classOf[Integer]))
  }

  test("Key.product") {
    val key = Key.product.toKey
    assertEquals(key(()), ())
    assertEquals(key((0x10000, 0x10000)), (classOf[Integer], classOf[Integer]))
    assertEquals(key((0x10000, 0x10000, 0x10000)), (classOf[Integer], classOf[Integer], classOf[Integer]))
    assertEquals(
      key((0x10000, 0x10000, 0x10000, 0x10000)),
      (classOf[Integer], classOf[Integer], classOf[Integer], classOf[Integer])
    )
    assertEquals(key(Left(1)), "Left")
    assertEquals(key(Right(1)), "Right")
  }
}
