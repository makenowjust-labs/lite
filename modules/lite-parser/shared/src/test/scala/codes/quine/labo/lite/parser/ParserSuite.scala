package codes.quine.labo.lite.parser

import codes.quine.labo.lite.parser.Parser.Error

class ParserSuite extends munit.FunSuite {
  test("Parseer.State") {
    assertEquals(
      new Parser.State("foo").toString,
      """|State(foo) {
         |  var offset = 0
         |  var isOK   = false
         |  var value  = null
         |  var error  = null
         |  var isCut  = false
         |}""".stripMargin
    )
  }

  test("Parser.Error#merge") {
    import Error._
    assertEquals(merge(0, Unexpected(1), Unexpected(2)), Unexpected(2))
    assertEquals(merge(0, Unexpected(2), Unexpected(1)), Unexpected(2))
    assertEquals(merge(0, Failure(0, "foo"), Failure(0, "bar")), Failure(0, "bar"))
    assertEquals(merge(0, Failure(0, "foo"), Unexpected(0)), Failure(0, "foo"))
    assertEquals(merge(0, Unexpected(0), Failure(0, "foo")), Failure(0, "foo"))
    assertEquals(merge(0, Expected(0, Set("foo")), Expected(0, Set("bar"))), Expected(0, Set("foo", "bar")))
    assertEquals(merge(0, Expected(1, Set("foo")), Expected(1, Set("bar"))), Expected(1, Set("bar")))
    assertEquals(merge(0, Expected(0, Set("foo")), Unexpected(0)), Expected(0, Set("foo")))
    assertEquals(merge(0, Unexpected(0), Expected(0, Set("foo"))), Expected(0, Set("foo")))
    assertEquals(merge(0, Unexpected(1), Unexpected(1)), Unexpected(1))
  }

  test("Parser.satisfy") {
    val parser = Parser.satisfy(_ == 'x')
    assertEquals(parser.parse("xyz"), Right((1, ())))
    assertEquals(parser.parse("abc"), Left(Error.Unexpected(0)))
  }

  test("Parser.satisfyWhile") {
    val parser = Parser.satisfyWhile(_ == 'x')
    assertEquals(parser.parse("xyz"), Right((1, ())))
    assertEquals(parser.parse("xxx"), Right((3, ())))
    assertEquals(parser.parse("abc"), Left(Error.Unexpected(0)))
  }

  test("Parser.charLiteral") {
    val parser = Parser.charLiteral('x')
    assertEquals(parser.parse("xyz"), Right((1, ())))
    assertEquals(parser.parse("abc"), Left(Error.Unexpected(0)))
  }

  test("Parser.charWhile") {
    val parser = Parser.charWhile('x')
    assertEquals(parser.parse("xyz"), Right((1, ())))
    assertEquals(parser.parse("xxx"), Right((3, ())))
    assertEquals(parser.parse("abc"), Left(Error.Unexpected(0)))
  }

  test("Parser.charIn") {
    val parser = Parser.charIn("xyz")
    assertEquals(parser.parse("xyz"), Right((1, ())))
    assertEquals(parser.parse("yzx"), Right((1, ())))
    assertEquals(parser.parse("zxy"), Right((1, ())))
    assertEquals(parser.parse("abc"), Left(Error.Unexpected(0)))
  }

  test("Parser.charInWhile") {
    val parser = Parser.charInWhile("xyz")
    assertEquals(parser.parse("xyz"), Right((3, ())))
    assertEquals(parser.parse("xyzabc"), Right((3, ())))
    assertEquals(parser.parse("abc"), Left(Error.Unexpected(0)))
  }

  test("Parser.stringLiteral") {
    val parser = Parser.stringLiteral("xyz")
    assertEquals(parser.parse("xyz"), Right((3, ())))
    assertEquals(parser.parse("xyzabc"), Right((3, ())))
    assertEquals(parser.parse("abc"), Left(Error.Unexpected(0)))
  }

  test("Parser.start") {
    val parser = Parser.start
    assertEquals(parser.parse("xyz"), Right((0, ())))
    assertEquals(parser.parse("xyz", 1), Left(Error.Unexpected(1)))
  }

  test("Parser.end") {
    val parser = Parser.end
    assertEquals(parser.parse("xyz", 3), Right((3, ())))
    assertEquals(parser.parse("xyz", 1), Left(Error.Unexpected(1)))
  }

  test("Parser.pass") {
    val parser = Parser.pass(1)
    assertEquals(parser.parse("xyz"), Right((0, 1)))
  }

  test("Parser.fail") {
    val parser = Parser.fail("foo")
    assertEquals(parser.parse("xyz"), Left(Error.Failure(0, "foo")))
  }

  test("Parser.delay") {
    val parser = Parser.delay(Parser.pass(1))
    assertEquals(parser.parse("xyz"), Right((0, 1)))
  }

  test("Parser.&?") {
    val parser = Parser.&?(Parser.charLiteral('x'))
    assertEquals(parser.parse("xyz"), Right((0, ())))
    assertEquals(parser.parse("abc"), Left(Error.Unexpected(0)))
  }

  test("Parser.&!") {
    val parser = Parser.&!(Parser.charLiteral('x'))
    assertEquals(parser.parse("abc"), Right((0, ())))
    assertEquals(parser.parse("xyz"), Left(Error.Unexpected(0)))
  }

  test("Parser#~") {
    val parser = Parser.charLiteral('x') ~ Parser.charLiteral('y')
    assertEquals(parser.parse("xyz"), Right((2, ())))
    assertEquals(parser.parse("abc"), Left(Error.Unexpected(0)))
    assertEquals(parser.parse("xbc"), Left(Error.Unexpected(1)))
  }

  test("Parser#~/") {
    val parser = (Parser.charLiteral('x') ~/ Parser.charLiteral('y')) | Parser.stringLiteral("xz")
    assertEquals(parser.parse("xyz"), Right((2, ())))
    assertEquals(parser.parse("abc"), Left(Error.Unexpected(0)))
    assertEquals(parser.parse("xbc"), Left(Error.Unexpected(1)))
    assertEquals(parser.parse("xz"), Left(Error.Unexpected(1)))
  }

  test("Parser#|") {
    val parser1 = (Parser.charLiteral('x') | Parser.charLiteral('y')) | Parser.charLiteral('z')
    val parser2 = Parser.charLiteral('a') | (Parser.charLiteral('b') | Parser.charLiteral('c'))
    val parser = parser1 | parser2
    assertEquals(parser.parse("x"), Right((1, ())))
    assertEquals(parser.parse("y"), Right((1, ())))
    assertEquals(parser.parse("z"), Right((1, ())))
    assertEquals(parser.parse("a"), Right((1, ())))
    assertEquals(parser.parse("b"), Right((1, ())))
    assertEquals(parser.parse("c"), Right((1, ())))
    assertEquals(parser.parse("w"), Left(Error.Unexpected(0)))
    assertEquals(parser.parse("d"), Left(Error.Unexpected(0)))
  }

  test("Parser#rep") {
    val parser = Parser.charIn("xyz").rep
    assertEquals(parser.parse("xyz"), Right((3, ())))
    assertEquals(parser.parse("abc"), Right((0, ())))
  }

  test("Parser#rep: min") {
    val parser = Parser.charIn("xyz").rep(min = 1)
    assertEquals(parser.parse("xyz"), Right((3, ())))
    assertEquals(parser.parse("abc"), Left(Error.Unexpected(0)))
  }

  test("Parser#rep: max") {
    val parser = Parser.charIn("xyz").rep(max = 2)
    assertEquals(parser.parse("xyz"), Right((2, ())))
    assertEquals(parser.parse("abc"), Right((0, ())))
  }

  test("Parser#rep: exactly") {
    val parser = Parser.charIn("xyz").rep(exactly = 3)
    assertEquals(parser.parse("xyz"), Right((3, ())))
    assertEquals(parser.parse("abc"), Left(Error.Unexpected(0)))
  }

  test("Parser#rep: sep") {
    val parser = Parser.charIn("xyz").rep(sep = Parser.charLiteral('.'))
    assertEquals(parser.parse("xyz"), Right((1, ())))
    assertEquals(parser.parse("x.y.z"), Right((5, ())))
    assertEquals(parser.parse("abc"), Right((0, ())))
  }

  test("Parser#?") {
    val parser = Parser.charLiteral('x').?
    assertEquals(parser.parse("x"), Right((1, ())))
    assertEquals(parser.parse(""), Right((0, ())))
    assertEquals(parser.parse("a"), Right((0, ())))
  }

  test("Parser#!") {
    val parser = Parser.charLiteral('x').!
    assertEquals(parser.parse("x"), Right((1, "x")))
    assertEquals(parser.parse(""), Left(Error.Unexpected(0)))
  }

  test("Parser#/") {
    val parser = (Parser.charLiteral('x')./ ~ Parser.charLiteral('y')) | Parser.stringLiteral("xz")
    assertEquals(parser.parse("xyz"), Right((2, ())))
    assertEquals(parser.parse("abc"), Left(Error.Unexpected(0)))
    assertEquals(parser.parse("xbc"), Left(Error.Unexpected(1)))
    assertEquals(parser.parse("xz"), Left(Error.Unexpected(1)))
  }

  test("Parser#map") {
    val parser = Parser.charLiteral('x').map(_ => 1)
    assertEquals(parser.parse("x"), Right((1, 1)))
    assertEquals(parser.parse(""), Left(Error.Unexpected(0)))
  }

  test("Parser#as") {
    val parser = Parser.charLiteral('x').as(1)
    assertEquals(parser.parse("x"), Right((1, 1)))
    assertEquals(parser.parse(""), Left(Error.Unexpected(0)))
  }

  test("Parser#named") {
    val parser = Parser.charLiteral('x').named("foo")
    assertEquals(parser.parse("x"), Right((1, ())))
    assertEquals(parser.parse(""), Left(Error.Expected(0, Set("foo"))))
  }
}
