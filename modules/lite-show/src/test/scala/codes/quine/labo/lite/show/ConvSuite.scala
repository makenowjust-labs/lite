package codes.quine.labo.lite.show

import codes.quine.labo.lite.show.Frag._

class ConvSuite extends munit.FunSuite {
  test("Conv#orElse") {
    assertEquals(Conv.`null`.orElse(Conv.boolean).toFunction(null), List(Lit("null")))
    assertEquals(Conv.`null`.orElse(Conv.boolean).toFunction(true), List(Lit("true")))
    assertEquals(Conv.`null`.orElse(Conv.boolean).toFunction(""), List(Lit("")))
  }

  test("Conv.default") {
    assertEquals(Conv.default().toFunction(null), List(Lit("null")))
    assertEquals(Conv.default().toFunction(""), List(Lit("\"\"")))
  }

  test("Conv.null") {
    assertEquals(Conv.`null`.toFunction(null), List(Lit("null")))
  }

  test("Conv.string") {
    assertEquals(Conv.string.toFunction(""), List(Lit("\"\"")))
    assertEquals(Conv.string.toFunction("test"), List(Lit("\"test\"")))
    assertEquals(Conv.string.toFunction("\\\b\f\n\r\t\u007F"), List(Lit("\"\\\\\\b\\f\\n\\r\\t\\u007F\"")))
    assertEquals(Conv.string.toFunction("\u001B"), List(Lit("\"\\u001B\"")))
    assertEquals(Conv.string.toFunction("\""), List(Lit("\"\\\"\"")))
    assertEquals(Conv.string.toFunction("'"), List(Lit("\"'\"")))
  }

  test("Conv.char") {
    assertEquals(Conv.char.toFunction('a'), List(Lit("'a'")))
    assertEquals(Conv.char.toFunction('\n'), List(Lit("'\\n'")))
    assertEquals(Conv.char.toFunction('\''), List(Lit("'\\''")))
  }

  test("Conv.boolean") {
    assertEquals(Conv.boolean.toFunction(true), List(Lit("true")))
    assertEquals(Conv.boolean.toFunction(false), List(Lit("false")))
  }

  test("Conv.number") {
    assertEquals(Conv.number.toFunction(12: Byte), List(Lit("12")))
    assertEquals(Conv.number.toFunction(12: Short), List(Lit("12")))
    assertEquals(Conv.number.toFunction(12: Int), List(Lit("12")))
    assertEquals(Conv.number.toFunction(12L), List(Lit("12L")))
    assertEquals(Conv.number.toFunction(1.23f), List(Lit("1.23F")))
    assertEquals(Conv.number.toFunction(1.23), List(Lit("1.23")))
    assertEquals(Conv.number.toFunction(12: BigInt), List(Lit("12")))
    assertEquals(Conv.number.toFunction(12: BigDecimal), List(Lit("12")))
  }

  test("Conv.iterable") {
    assertEquals(Conv.iterable().toFunction(List()), List(Lit("List()")))
    assertEquals(
      Conv.iterable().toFunction(List(1, 2)),
      List(Group(List(Lit("List("), Indent(List(Break, Lit("1"), Lit(","), Line, Lit("2"))), Break, Lit(")"))))
    )
    assertEquals(
      Conv.iterable(maxSize = 1).toFunction(List(1, 2)),
      List(Group(List(Lit("List("), Indent(List(Break, Lit("1"), Lit(","), Line, Lit("..."))), Break, Lit(")"))))
    )
    assertEquals(Conv.iterable().toFunction(Map()), List(Lit("Map()")))
    assertEquals(
      Conv.iterable().toFunction(Map(1 -> 2)),
      List(Group(List(Lit("Map("), Indent(List(Break, Lit("1"), Lit(" -> "), Lit("2"))), Break, Lit(")"))))
    )
  }

  test("Conv.product") {
    case object X
    final case class Y(value: Int)

    assertEquals(Conv.product.toFunction(()), List(Lit("()")))
    assertEquals(Conv.product.toFunction(X), List(Lit("X")))
    assertEquals(
      Conv.product.toFunction(Y(1)),
      List(Group(List(Lit("Y("), Indent(List(Break, Wide("value = "), Lit("1"))), Break, Lit(")"))))
    )
    assertEquals(
      Conv.product.toFunction((1, 2)),
      List(Group(List(Lit("("), Indent(List(Break, Lit("1"), Lit(","), Line, Lit("2"))), Break, Lit(")"))))
    )
  }
}
