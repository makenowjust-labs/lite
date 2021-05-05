package codes.quine.labo.lite.show

import codes.quine.labo.lite.show.Frag._

class ConvJVMSuite extends munit.FunSuite {
  test("Conv.number: on JVM") {
    assertEquals(Conv.number.toFunction(1.25f), List(Lit("1.25F")))
  }
}
