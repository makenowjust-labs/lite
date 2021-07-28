package codes.quine.labo.lite.show

import codes.quine.labo.lite.show.Prettify.GenOps
import codes.quine.labo.lite.show.Pretty._

class PrettifyJVMSuite extends munit.FunSuite {
  test("Prettify.number: on JVM") {
    assertEquals(Prettify.number.toPrettify(1.25f), List(Lit("1.25F")))
  }
}
