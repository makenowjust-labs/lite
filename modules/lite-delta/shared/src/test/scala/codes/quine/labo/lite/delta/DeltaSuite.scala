package codes.quine.labo.lite.delta

import scala.io.AnsiColor.{RED, GREEN, RESET}
import codes.quine.labo.lite.show.Prettify
import codes.quine.labo.lite.show.Prettify.PrettifyGenOps
import codes.quine.labo.lite.show.Pretty._

class DeltaSuite extends munit.FunSuite {
  test("Delta.diff: example") {
    sealed abstract class FooBar
    case class Foo(x: Int, y: Int) extends FooBar
    case class Bar(z: String) extends FooBar

    val obtained = Delta.diff(
      Seq(Foo(1, 2), Bar("foo"), Foo(3, 4), Foo(5, 6)),
      Seq(Foo(1, 2), Bar("bar"), Foo(5, 6), Bar("foobar"))
    )
    val expected =
      s"""|List(
          |  Foo(1, 2),
          |  Bar($RED"foo"$RESET => $GREEN"bar"$RESET),
          |  Foo(${RED}3$RESET => ${GREEN}5$RESET, ${RED}4$RESET => ${GREEN}6$RESET),
          |  ${RED}Foo(5, 6)$RESET,
          |  ${GREEN}Bar("foobar")$RESET
          |)""".stripMargin
    assertEquals(obtained, expected)
  }

  test("Delta#isIdentical") {
    assertEquals(Delta.Case("Foo", Seq.empty).isIdentical, true)
    assertEquals(Delta.Case("Foo", Seq(("x", Delta.Identical(1)))).isIdentical, true)
    assertEquals(Delta.Case("Foo", Seq(("x", Delta.Missing(1)))).isIdentical, false)
    assertEquals(Delta.Mapping("Map", Seq((1, Delta.Identical(2)))).isIdentical, true)
    assertEquals(Delta.Mapping("Map", Seq((1, Delta.Missing(2)))).isIdentical, false)
    assertEquals(Delta.Sequence("Seq", Seq(Delta.Identical(1))).isIdentical, true)
    assertEquals(Delta.Sequence("Seq", Seq(Delta.Missing(1))).isIdentical, false)
    assertEquals(Delta.Identical(1).isIdentical, true)
    assertEquals(Delta.Missing(1).isIdentical, false)
    assertEquals(Delta.Additional(1).isIdentical, false)
    assertEquals(Delta.Changed(1, 2).isIdentical, false)
  }

  test("Delta#prettify") {
    val prettify = Prettify.default().toPrettify
    assertEquals(
      Delta.Case("Foo", Seq(("x", Delta.Identical(1)), ("y", Delta.Identical(2)))).prettify(true, prettify),
      Seq(
        Group(
          Seq(
            Lit("Foo("),
            Indent(
              Seq(
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
      Delta.Case("Foo", Seq(("x", Delta.Identical(1)), ("y", Delta.Identical(2)))).prettify(false, prettify),
      Seq(Group(Seq(Lit("Foo("), Indent(Seq(Break, Lit("..."))), Break, Lit(")"))))
    )
    assertEquals(
      Delta.Mapping("Foo", Seq((1, Delta.Identical(1)), (2, Delta.Identical(2)))).prettify(true, prettify),
      Seq(
        Group(
          Seq(
            Lit("Foo("),
            Indent(
              Seq(
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
      Delta.Mapping("Foo", Seq((1, Delta.Identical(1)), (2, Delta.Identical(2)))).prettify(false, prettify),
      Seq(Group(Seq(Lit("Foo("), Indent(Seq(Break, Lit("..."))), Break, Lit(")"))))
    )
    assertEquals(
      Delta.Sequence("Foo", Seq(Delta.Identical(1), Delta.Identical(2))).prettify(true, prettify),
      Seq(Group(Seq(Lit("Foo("), Indent(Seq(Break, Lit("1"), Lit(","), Line, Lit("2"))), Break, Lit(")"))))
    )
    assertEquals(
      Delta.Sequence("Foo", Seq(Delta.Identical(1), Delta.Identical(2))).prettify(false, prettify),
      Seq(Group(Seq(Lit("Foo("), Indent(Seq(Break, Lit("..."))), Break, Lit(")"))))
    )
    assertEquals(Delta.Identical(1).prettify(true, prettify), Seq(Lit("1")))
    assertEquals(
      Delta.Changed(1, 2).prettify(true, prettify),
      Seq(
        Lit(RED, true),
        Lit("1"),
        Lit(RESET, true),
        Lit(" => "),
        Lit(GREEN, true),
        Lit("2"),
        Lit(RESET, true)
      )
    )
    assertEquals(
      Delta.Missing(1).prettify(true, prettify),
      Seq(Lit(GREEN, true), Lit("1"), Lit(RESET, true))
    )
    assertEquals(
      Delta.Additional(1).prettify(true, prettify),
      Seq(Lit(RED, true), Lit("1"), Lit(RESET, true))
    )
  }
}
