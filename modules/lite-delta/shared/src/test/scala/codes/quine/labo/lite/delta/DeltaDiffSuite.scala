package codes.quine.labo.lite.delta

class DeltaDiffSuite extends munit.FunSuite with DeltaAssertions {
  test("DeltaDiff.apply") {
    DeltaDiff[Option[Int]]
    DeltaDiff[Either[Int, Int]]
    DeltaDiff[Seq[Int]]
    DeltaDiff[List[Int]]
    DeltaDiff[Vector[Int]]
    DeltaDiff[Map[Int, Int]]
    DeltaDiff[Set[Int]]
    DeltaDiff[Unit]
    DeltaDiff[(Int, Int)]
    DeltaDiff[(Int, Int, Int)]
    DeltaDiff[(Int, Int, Int, Int)]
    DeltaDiff[String]
    DeltaDiff[Boolean]
    DeltaDiff[Byte]
    DeltaDiff[Short]
    DeltaDiff[Int]
    DeltaDiff[Long]
  }

  test("DeltaDiff.diff") {
    assertEquals(DeltaDiff.diff(1, 2), Delta.Changed(1, 2))
  }

  test("DeltaDiff.seq") {
    val diff = DeltaDiff.seq[Int]("Foo")(KeyOf[Int].contramap(_.value), DeltaDiff.default[Int])
    assertEquals(
      diff(Seq(1, 2, 3, 4), Seq(2, 1, 3, 5)),
      Delta.Map(
        "Foo",
        Seq(
          Entry(Delta.Changed(1, 0), Delta.Identical(2)),
          Entry(Delta.Changed(0, 1), Delta.Identical(1)),
          Entry(Delta.Identical(2), Delta.Identical(3)),
          Entry(Delta.Missing(3), Delta.Missing(5)),
          Entry(Delta.Additional(3), Delta.Additional(4))
        ),
        ": "
      )
    )
  }

  test("DeltaDiff.map") {
    val diff =
      DeltaDiff.map[Int, Int]("Foo")
    assertEquals(
      diff(Map(1 -> 1, 2 -> 3, 5 -> 6), Map(1 -> 1, 2 -> 4, 7 -> 8)),
      Delta.Map(
        "Foo",
        Seq(
          Entry(Delta.Additional(5), Delta.Additional(6)),
          Entry(Delta.Identical(1), Delta.Identical(1)),
          Entry(Delta.Identical(2), Delta.Changed(3, 4)),
          Entry(Delta.Missing(7), Delta.Missing(8))
        ),
        " -> "
      )
    )
  }

  test("DeltaDiff.set") {
    val diff =
      DeltaDiff.set[(Int, Int)]("Foo")(KeyOf[Int].contramap(_._1), DeltaDiff.default[(Int, Int)])
    assertEquals(
      diff(Set((1, 1), (2, 1), (3, 3)), Set((1, 1), (2, 2), (4, 4))),
      Delta.Set(
        "Foo",
        Seq(
          Delta.Identical((1, 1)),
          Delta.Changed((2, 1), (2, 2)),
          Delta.Additional((3, 3)),
          Delta.Missing((4, 4))
        )
      )
    )
  }

  test("DeltaDiff.diffInstanceForOption") {
    val diff = DeltaDiff.diffInstanceForOption[Int]
    assertEquals(
      diff(Some(1), Some(2)),
      Delta.Case("Option", Seq(Entry("value", Delta.Changed(1, 2))))
    )
    assertEquals(diff(None, None), Delta.Identical(None))
    assertEquals(diff(Some(1), None), Delta.Changed(Some(1), None))
    assertEquals(diff(None, Some(2)), Delta.Changed(None, Some(2)))
  }

  test("DeltaDiff.diffInstanceForEither") {
    val diff = DeltaDiff.diffInstanceForEither[Int, Int]
    assertEquals(
      diff(Left(1), Left(2)),
      Delta.Case("Left", Seq(Entry("value", Delta.Changed(1, 2))))
    )
    assertEquals(
      diff(Right(1), Right(2)),
      Delta.Case("Right", Seq(Entry("value", Delta.Changed(1, 2))))
    )
    assertEquals(diff(Left(1), Right(1)), Delta.Changed(Left(1), Right(1)))
  }

  test("DeltaDiff.diffInstanceForTuple2") {
    val diff = DeltaDiff.diffInstanceForTuple2[Int, Int]
    assertEquals(
      diff((1, 1), (1, 1)),
      Delta.Case("", Seq(Entry("_1", Delta.Identical(1)), Entry("_2", Delta.Identical(1))))
    )
  }

  test("DeltaDiff.diffInstanceForTuple3") {
    val diff = DeltaDiff.diffInstanceForTuple3[Int, Int, Int]
    assertEquals(
      diff((1, 1, 1), (1, 1, 1)),
      Delta.Case(
        "",
        Seq(Entry("_1", Delta.Identical(1)), Entry("_2", Delta.Identical(1)), Entry("_3", Delta.Identical(1)))
      )
    )
  }

  test("DeltaDiff.diffInstanceForTuple4") {
    val diff = DeltaDiff.diffInstanceForTuple4[Int, Int, Int, Int]
    assertEquals(
      diff((1, 1, 1, 1), (1, 1, 1, 1)),
      Delta.Case(
        "",
        Seq(
          Entry("_1", Delta.Identical(1)),
          Entry("_2", Delta.Identical(1)),
          Entry("_3", Delta.Identical(1)),
          Entry("_4", Delta.Identical(1))
        )
      )
    )
  }
}
