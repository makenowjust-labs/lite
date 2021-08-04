package codes.quine.labo.lite.show

import codes.quine.labo.lite.show.Prettify.PrettifyGenOps
import codes.quine.labo.lite.show.Pretty._

class PrettifySuite extends munit.FunSuite {
  test("Prettify.default") {
    assertEquals(Prettify.default().toPrettify(null), Seq(Lit("null")))
    assertEquals(Prettify.default().toPrettify(""), Seq(Lit("\"\"")))
  }

  test("Prettify.null") {
    assertEquals(Prettify.`null`.toPrettify(null), Seq(Lit("null")))
  }

  test("Prettify.string") {
    assertEquals(Prettify.string.toPrettify(""), Seq(Lit("\"\"")))
    assertEquals(Prettify.string.toPrettify("test"), Seq(Lit("\"test\"")))
    assertEquals(Prettify.string.toPrettify("\\\b\f\n\r\t\u007F"), Seq(Lit("\"\\\\\\b\\f\\n\\r\\t\\u007F\"")))
    assertEquals(Prettify.string.toPrettify("\u001B"), Seq(Lit("\"\\u001B\"")))
    assertEquals(Prettify.string.toPrettify("\""), Seq(Lit("\"\\\"\"")))
    assertEquals(Prettify.string.toPrettify("'"), Seq(Lit("\"'\"")))
  }

  test("Prettify.char") {
    assertEquals(Prettify.char.toPrettify('a'), Seq(Lit("'a'")))
    assertEquals(Prettify.char.toPrettify('\n'), Seq(Lit("'\\n'")))
    assertEquals(Prettify.char.toPrettify('\''), Seq(Lit("'\\''")))
  }

  test("Prettify.boolean") {
    assertEquals(Prettify.boolean.toPrettify(true), Seq(Lit("true")))
    assertEquals(Prettify.boolean.toPrettify(false), Seq(Lit("false")))
  }

  test("Prettify.number") {
    assertEquals(Prettify.number.toPrettify(12: Byte), Seq(Lit("12")))
    assertEquals(Prettify.number.toPrettify(12: Short), Seq(Lit("12")))
    assertEquals(Prettify.number.toPrettify(12: Int), Seq(Lit("12")))
    assertEquals(Prettify.number.toPrettify(12L), Seq(Lit("12L")))
    assertEquals(Prettify.number.toPrettify(1.25), Seq(Lit("1.25")))
    assertEquals(Prettify.number.toPrettify(12: BigInt), Seq(Lit("12")))
    assertEquals(Prettify.number.toPrettify(12: BigDecimal), Seq(Lit("12")))
  }

  test("Prettify.iterable") {
    assertEquals(Prettify.iterable().toPrettify(List()), Seq(Lit("List()")))
    assertEquals(
      Prettify.iterable().toPrettify(List(1, 2)),
      Seq(Group(Seq(Lit("List("), Indent(Seq(Break, Lit("1"), Lit(","), Line, Lit("2"))), Break, Lit(")"))))
    )
    assertEquals(
      Prettify.iterable(maxSize = 1).toPrettify(List(1, 2)),
      Seq(Group(Seq(Lit("List("), Indent(Seq(Break, Lit("1"), Lit(","), Line, Lit("..."))), Break, Lit(")"))))
    )
    assertEquals(Prettify.iterable().toPrettify(Map()), Seq(Lit("Map()")))
    assertEquals(
      Prettify.iterable().toPrettify(Map(1 -> 2)),
      Seq(Group(Seq(Lit("Map("), Indent(Seq(Break, Lit("1"), Lit(" -> "), Lit("2"))), Break, Lit(")"))))
    )
  }

  test("Prettify.product") {
    case object X
    final case class Y(value: Int)

    assertEquals(Prettify.product.toPrettify(()), List(Lit("()")))
    assertEquals(Prettify.product.toPrettify(X), List(Lit("X")))
    assertEquals(
      Prettify.product.toPrettify(Y(1)),
      Seq(Group(Seq(Lit("Y("), Indent(Seq(Break, Wide("value = "), Lit("1"))), Break, Lit(")"))))
    )
    assertEquals(
      Prettify.product.toPrettify((1, 2)),
      Seq(
        Group(
          Seq(
            Lit("("),
            Indent(Seq(Break, Wide("_1 = "), Lit("1"), Lit(","), Line, Wide("_2 = "), Lit("2"))),
            Break,
            Lit(")")
          )
        )
      )
    )
  }
}
