package codes.quine.labo.lite.parser

class SequencerSuite extends munit.FunSuite {
  test("Sequencer.apply") {
    Sequencer[Unit, Unit]
    Sequencer[Int, Unit]
    Sequencer[Unit, Int]
    Sequencer[(Int, Int), Int]
    Sequencer[(Int, Int, Int), Int]
    Sequencer[(Int, Int, Int, Int), Int]
    Sequencer[Int, Int]
  }

  test("Sequencer.unit") {
    val seq = Sequencer.unit
    assertEquals(seq((), ()), ())
  }

  test("Sequencer.left") {
    val seq = Sequencer.left[Int]
    assertEquals(seq(1, ()), 1)
  }

  test("Sequencer.right") {
    val seq = Sequencer.right[Int]
    assertEquals(seq((), 1), 1)
  }

  test("Sequencer.tuple3") {
    val seq = Sequencer.tuple3[Int, Int, Int]
    assertEquals(seq((1, 2), 3), (1, 2, 3))
  }

  test("Sequencer.tuple4") {
    val seq = Sequencer.tuple4[Int, Int, Int, Int]
    assertEquals(seq((1, 2, 3), 4), (1, 2, 3, 4))
  }

  test("Sequencer.tuple5") {
    val seq = Sequencer.tuple5[Int, Int, Int, Int, Int]
    assertEquals(seq((1, 2, 3, 4), 5), (1, 2, 3, 4, 5))
  }

  test("Sequencer.tuple2") {
    val seq = Sequencer.tuple2[Int, Int]
    assertEquals(seq(1, 2), (1, 2))
  }
}
