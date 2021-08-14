package codes.quine.labo.lite.parser

class RepeaterSuite extends munit.FunSuite {
  test("Repeater.apply") {
    Repeater[Unit]
    Repeater[Int]
  }

  test("Repeater.unit") {
    val rep = Repeater.unit
    val b = rep.newBuilder
    assertEquals(rep.result(b), ())
    rep.addOne(b, ())
    assertEquals(rep.result(b), ())
  }

  test("Repeater.seq") {
    val rep = Repeater.seq[Int]
    val b = rep.newBuilder
    assertEquals(rep.result(b), Seq.empty)
    rep.addOne(b, 1)
    assertEquals(rep.result(b), Seq(1))
  }
}
