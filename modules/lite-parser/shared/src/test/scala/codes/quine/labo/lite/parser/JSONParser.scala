package codes.quine.labo.lite.parser

import codes.quine.labo.lite.parser.Parser._

/** A JSON parser implementation. */
object JSONParser {

  /** Value is a JSON value. */
  sealed abstract class Value extends Product with Serializable

  object Value {
    case object Null extends Value
    final case class String(value: java.lang.String) extends Value
    final case class Number(value: Double) extends Value
    final case class Bool(value: Boolean) extends Value
    final case class Array(values: Value*) extends Value
    final case class Object(entries: (java.lang.String, Value)*) extends Value
  }

  val root: Parser[Value] = delay(space ~ value ~ end)
  val value: Parser[Value] = delay((number | string | `null` | `true` | `false` | array | `object`) ~ space)

  val space: Parser[Unit] = charInWhile(" \r\n", min = 0)
  val digits: Parser[Unit] = charInWhile("0123456789")
  val exponent: Parser[Unit] = charIn("eE") ~ charIn("+-").? ~ digits
  val fractional: Parser[Unit] = "." ~ digits
  val integral: Parser[Unit] = "0" | charIn("123456789") ~ digits.?

  val number: Parser[Value] =
    (charIn("+-").? ~ integral ~ fractional.? ~ exponent.?).!.map(s => Value.Number(s.toDouble)).named("<number>")

  val hexDigit: Parser[Unit] = charIn("0123456789abcdefABCDEF")
  val unicodeEscape: Parser[Char] = "u" ~ hexDigit.rep(exactly = 4).!.map(s => Integer.parseInt(s, 16).toChar)
  val simpleEscape: Parser[Char] =
    charIn("\"\\/bfnrt").!.map { s =>
      s.charAt(0) match {
        case 'b' => '\b'
        case 'f' => '\f'
        case 'n' => '\n'
        case 'r' => '\r'
        case 't' => '\t'
        case c   => c
      }
    }
  val escape: Parser[Char] = "\\" ~/ (simpleEscape | unicodeEscape)
  val stringContent: Parser[String] = satisfyWhile(c => c != '"' && c != '\\').! | escape.map(String.valueOf)
  val key: Parser[String] = ('"' ~/ stringContent.rep.map(_.mkString) ~ '"').named("<string>")
  val string: Parser[Value] = key.map(Value.String)

  val `null`: Parser[Value] = "null".as(Value.Null).named("'null'")
  val `true`: Parser[Value] = "true".as(Value.Bool(true)).named("'true'")
  val `false`: Parser[Value] = "false".as(Value.Bool(false)).named("'false'")

  val comma: Parser[Unit] = (',' ~ space).named("','")
  val colon: Parser[Unit] = (':' ~ space).named("':'")
  val lbrack: Parser[Unit] = ('[' ~ space).named("'['")
  val rbrack: Parser[Unit] = (']' ~ space).named("']'")
  val lcurly: Parser[Unit] = ('{' ~ space).named("'{'")
  val rcurly: Parser[Unit] = ('}' ~ space).named("'}'")

  val array: Parser[Value] = (lbrack ~ value.rep(sep = comma) ~ rbrack).map(Value.Array(_: _*))

  val entry: Parser[(String, Value)] = key ~/ space ~ colon ~ value
  val `object`: Parser[Value] = (lcurly ~ entry.rep(sep = comma) ~ rcurly).map(Value.Object(_: _*))
}
