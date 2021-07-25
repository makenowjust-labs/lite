package codes.quine.labo.lite.delta

import scala.io.AnsiColor

import codes.quine.labo.lite.show.Conv
import codes.quine.labo.lite.show.Frag._

class DeltaConvSuite extends munit.FunSuite {
  test("DeltaConv#create") {
    val conv = new DeltaConv().create(Conv.default().toFunction)
    assertEquals(conv(1), List(Lit("1")))
    assertEquals(conv(Delta.Identical(1)), List(Lit("1")))
    assertEquals(conv(Delta.Additional(1)), List(Lit(AnsiColor.RED, true), Lit("1"), Lit(AnsiColor.RESET, true)))
    assertEquals(conv(Delta.Missing(1)), List(Lit(AnsiColor.GREEN, true), Lit("1"), Lit(AnsiColor.RESET, true)))
    assertEquals(
      conv(Delta.Changed(1, 2)),
      List(
        Lit(AnsiColor.RED, true),
        Lit("1"),
        Lit(AnsiColor.RESET, true),
        Lit(" => "),
        Lit(AnsiColor.GREEN, true),
        Lit("2"),
        Lit(AnsiColor.RESET, true)
      )
    )
    assertEquals(
      conv(Delta.Case("Foo", Seq(Entry("x", Delta.Identical(1)), Entry("y", Delta.Identical(2))))),
      List(
        Group(
          List(
            Lit("Foo("),
            Indent(
              List(
                Break,
                Wide("x = "),
                Lit("1"),
                Lit(","),
                Line,
                Wide("y = "),
                Lit("2")
              )
            ),
            Break,
            Lit(")")
          )
        )
      )
    )
    assertEquals(
      conv(
        Delta.Map(
          "Foo",
          Seq(Entry(Delta.Identical(1), Delta.Identical(1)), Entry(Delta.Identical(2), Delta.Identical(2))),
          " -> "
        )
      ),
      List(
        Group(
          List(
            Lit("Foo("),
            Indent(
              List(
                Break,
                Lit("1"),
                Lit(" -> "),
                Lit("1"),
                Lit(","),
                Line,
                Lit("2"),
                Lit(" -> "),
                Lit("2")
              )
            ),
            Break,
            Lit(")")
          )
        )
      )
    )
    assertEquals(
      conv(Delta.Set("Foo", Seq(Delta.Identical(1), Delta.Identical(2)))),
      List(Group(List(Lit("Foo("), Indent(List(Break, Lit("1"), Lit(","), Line, Lit("2"))), Break, Lit(")"))))
    )
  }

  test("DeltaConv#create: don't show identical") {
    val conv = new DeltaConv(showIdentical = false).create(Conv.default().toFunction)
    assertEquals(
      conv(Delta.Case("Foo", Seq(Entry("x", Delta.Identical(1)), Entry("y", Delta.Identical(2))))),
      List(Group(List(Lit("Foo("), Indent(List(Break, Lit("..."))), Break, Lit(")"))))
    )
    assertEquals(
      conv(
        Delta.Map(
          "Foo",
          Seq(Entry(Delta.Identical(1), Delta.Identical(1)), Entry(Delta.Identical(2), Delta.Identical(2))),
          " -> "
        )
      ),
      List(Group(List(Lit("Foo("), Indent(List(Break, Lit("..."))), Break, Lit(")"))))
    )
    assertEquals(
      conv(Delta.Set("Foo", Seq(Delta.Identical(1), Delta.Identical(2)))),
      List(Group(List(Lit("Foo("), Indent(List(Break, Lit("..."))), Break, Lit(")"))))
    )
  }
}
