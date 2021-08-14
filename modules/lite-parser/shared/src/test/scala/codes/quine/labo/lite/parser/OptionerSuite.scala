package codes.quine.labo.lite.parser

class OptionerSuite extends munit.FunSuite {
  test("Optioner.apply") {
    Optioner[Unit]
    Optioner[Int]
  }

  test("Optioner.unit") {
    val opt = Optioner.unit
    assertEquals(opt.none, ())
    assertEquals(opt.some(()), ())
  }

  test("Optioner.option") {
    val opt = Optioner.option[Int]
    assertEquals(opt.none, None)
    assertEquals(opt.some(1), Some(1))
  }
}
