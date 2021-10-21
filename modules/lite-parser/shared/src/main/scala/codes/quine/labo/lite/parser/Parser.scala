package codes.quine.labo.lite.parser

import scala.language.implicitConversions

import codes.quine.labo.lite.parser.Parser.State

/** Parser is a parser combinator implementation. */
abstract class Parser[+A] extends Serializable { self =>

  /** Runs this parser against the input string. */
  def parse(input: String, offset: Int = 0): Either[Parser.Error, (Int, A)] = {
    val state = new State(input)
    state.offset = offset
    unsafeParse(state)
    if (state.isOK) Right((state.offset, state.value.asInstanceOf[A]))
    else Left(state.error)
  }

  /** Runs this parser against the mutable state. */
  def unsafeParse(state: State): Unit

  /** Returns a parser combined with two parsers sequentially. */
  def ~[B, C](other: Parser[B])(implicit seq: Sequencer.Aux[A, B, C]): Parser[C] = new Parser.Sequence(self, other, seq)

  /** Like [[self ~ other]], but parsing is cut after the first parser. It is equivalent to `self./ ~ other`. */
  def ~/[B, C](other: Parser[B])(implicit seq: Sequencer.Aux[A, B, C]): Parser[C] = self./ ~ other

  /** Returns a parser combined with two parsers selectively. */
  def |[B >: A](other: Parser[B]): Parser[B] = (self, other) match {
    case (p1: Parser.OneOf[A], p2: Parser.OneOf[B]) => new Parser.OneOf(p1.parsers ++ p2.parsers)
    case (p1: Parser.OneOf[A], p2)                  => new Parser.OneOf(p1.parsers :+ p2)
    case (p1, p2: Parser.OneOf[B])                  => new Parser.OneOf(p1 +: p2.parsers)
    case (p1, p2)                                   => new Parser.OneOf(Vector(p1, p2))
  }

  /** Returns a parser to repeat this parser. */
  def rep[B](implicit rep: Repeater.Aux[A, B]): Parser[B] =
    new Parser.Rep(self, 0, Int.MaxValue, None, rep)

  /** Returns a parser to repeat this parser. */
  def rep[B](
      min: Int = 0,
      max: Int = Int.MaxValue,
      sep: Parser[Unit] = null,
      exactly: Int = -1
  )(implicit rep: Repeater.Aux[A, B]): Parser[B] =
    if (exactly >= 0) new Parser.Rep(self, exactly, exactly, Option(sep), rep)
    else new Parser.Rep(self, min, max, Option(sep), rep)

  /** Returns a parser for parsing this optionally. */
  def ?[B](implicit opt: Optioner.Aux[A, B]): Parser[B] = new Parser.Optional(self, opt)

  /** Returns a parser to capture a string in this parsing. */
  def ! : Parser[String] = new Parser.Capture(self)

  /** Returns a parser after that parsing is cut. */
  def / : Parser[A] = new Parser.Cut(self)

  /** Returns a parser whose result is converted by the given mapping. */
  def map[B](f: A => B): Parser[B] = new Parser.Map(self, f)

  /** Returns a parser whose result is replaced as the given value. */
  def as[B](value: B): Parser[B] = map(_ => value)

  /** Sets a parser's name as the given. */
  def named(name: String): Parser[A] = new Parser.Named(self, name)
}

object Parser {

  /** State is a mutable state in parsing. */
  final class State(val input: String) {

    /** A current parsing offset. */
    var offset: Int = 0

    /** A flag representing whether parsing is or not succeeded. */
    var isOK: Boolean = false

    /** A parsing result value. It is available on `isOK == true`. */
    var value: Any = null

    /** A parsing error. It is available on `isOK == false`. */
    var error: Error = null

    /** A flag representing whether parsing is cut. */
    var isCut: Boolean = false

    /** Sets this state as default, and updates offset as the given. */
    def reset(offset: Int = this.offset): Unit = {
      this.offset = offset
      this.isOK = false
      this.value = null
      this.error = null
      this.isCut = false
    }

    /** Sets this state as succeeded with the offset and the result value. */
    def done(offset: Int, value: Any): Unit = {
      this.offset = offset
      this.isOK = true
      this.value = value
      this.error = null
    }

    /** Sets this state as failed. */
    def failed(error: Error): Unit = {
      this.offset = -1
      this.isOK = false
      this.value = null
      this.error = error
    }

    override def toString: String =
      s"""|State($input) {
          |  var offset = $offset
          |  var isOK   = $isOK
          |  var value  = $value
          |  var error  = $error
          |  var isCut  = $isCut
          |}""".stripMargin
  }

  /** Error is a parsing error. */
  sealed abstract class Error extends Product with Serializable {
    def offset: Int
  }

  object Error {

    /** Merges two errors on the base offset. */
    def merge(offset0: Int, error1: Error, error2: Error): Error =
      if (error1.offset > error2.offset) error1
      else if (error1.offset < error2.offset) error2
      else
        (error1, error2) match {
          case (_: Failure, _: Failure)                                         => error2
          case (_: Failure, _)                                                  => error1
          case (_, _: Failure)                                                  => error2
          case (Expected(offset1, ts1), Expected(_, ts2)) if offset0 == offset1 => Expected(offset1, ts1 | ts2)
          case (_: Expected, _: Expected)                                       => error2
          case (_: Expected, _)                                                 => error1
          case (_, _: Expected)                                                 => error2
          case (_, _)                                                           => Unexpected(error1.offset)
        }

    /** Failure is a parsing error with explicit message. */
    final case class Failure(offset: Int, message: String) extends Error

    /** Expected is a parsing error with expected tokens. */
    final case class Expected(offset: Int, tokens: Set[String]) extends Error

    /** Unexpected is a parsing error without explicit message. */
    final case class Unexpected(offset: Int) extends Error
  }

  /** Returns a parser to match a character satisfies the given proposition. */
  def satisfy(p: Char => Boolean): Parser[Unit] = new Satisfy(p)

  /** Returns a parser to match characters while current character satisfies the given proposition. */
  def satisfyWhile(p: Char => Boolean, min: Int = 1): Parser[Unit] = new SatisfyWhile(p, min)

  /** Returns a parser to match the given character. */
  implicit def charLiteral(c: Char): Parser[Unit] = satisfy(_ == c)

  /** Returns a parser to match characters while current character is the given character. */
  def charWhile(c: Char, min: Int = 1): Parser[Unit] = satisfyWhile(_ == c, min = min)

  /** Returns a parser to match a character included in the given characters. */
  def charIn(s: String): Parser[Unit] = satisfy(s.contains(_))

  /** Returns a parser to match character while current character is included in the given characters. */
  def charInWhile(s: String, min: Int = 1): Parser[Unit] = satisfyWhile(s.contains(_), min = min)

  /** Returns a parser to match the given string. */
  implicit def stringLiteral(s: String): Parser[Unit] = new StringLiteral(s)

  /** Returns a parser to match the start position. */
  def start: Parser[Unit] = Start

  /** Returns a parser to match the end position. */
  def end: Parser[Unit] = End

  /** Returns a parser to succeed with the value without any consuming. */
  def pass[A](value: A): Parser[A] = new Pass(value)

  /** Returns a parser to be failed with the given message. */
  def fail(message: String): Parser[Any] = new Fail(message)

  /** Returns a positive look-ahead parser. */
  def &?[A](parser: Parser[A]): Parser[Unit] = new PosLookAhead(parser)

  /** Returns a negative look-ahead parser. */
  def &![A](parser: Parser[A]): Parser[Unit] = new NegLookAhead(parser)

  /** Returns a delayed parser. */
  def delay[A](parser: => Parser[A]): Parser[A] = new Delay(() => parser)

  /** [[Parser#~]] implementation. */
  private final class Sequence[A, B, C](
      val parser1: Parser[A],
      val parser2: Parser[B],
      val seq: Sequencer.Aux[A, B, C]
  ) extends Parser[C] {
    def unsafeParse(state: State): Unit = {
      parser1.unsafeParse(state)
      if (!state.isOK) return

      val value1 = state.value.asInstanceOf[A]
      val isCut1 = state.isCut

      state.reset()
      parser2.unsafeParse(state)
      val isCut2 = state.isCut

      state.isCut = isCut1 || isCut2
      if (!state.isOK) return

      val value2 = state.value.asInstanceOf[B]
      state.done(state.offset, seq(value1, value2))
    }
  }

  /** [[Parser#|]] implementation. */
  private final class OneOf[A](val parsers: Seq[Parser[A]]) extends Parser[A] {
    def unsafeParse(state: State): Unit = {
      val offset0 = state.offset
      var error0: Error = Error.Unexpected(offset0)

      for (parser <- parsers) {
        state.reset(offset0)
        parser.unsafeParse(state)

        if (state.isOK) return

        error0 = Error.merge(offset0, error0, state.error)
        if (state.isCut) {
          state.failed(error0)
          return
        }
      }

      state.failed(error0)
    }
  }

  /** [[Parser#rep]] implementation. */
  private final class Rep[A, B](
      val parser: Parser[A],
      val min: Int,
      val max: Int,
      val sep: Option[Parser[Unit]],
      val rep: Repeater.Aux[A, B]
  ) extends Parser[B] {
    def unsafeParse(state: State): Unit = {
      var n = 0
      var isCut = false
      val b = rep.newBuilder

      while (n < max) {
        val offset = state.offset
        var isOK = true

        if (n > 0) {
          sep.foreach { sepParser =>
            state.reset()
            sepParser.unsafeParse(state)

            isCut = isCut || state.isCut
            isOK = state.isOK
          }
        }

        if (isOK) {
          state.reset()
          parser.unsafeParse(state)
        }

        isCut = isCut || state.isCut
        isOK = isOK && state.isOK
        if (!isOK) {
          if (n >= min && !state.isCut) state.done(offset, rep.result(b))
          state.isCut = isCut
          return
        }

        rep.addOne(b, state.value.asInstanceOf[A])
        n += 1
      }

      state.isCut = isCut
      state.done(state.offset, rep.result(b))
    }
  }

  /** [[Parser#?]] implementation. */
  private final class Optional[A, B](val parser: Parser[A], opt: Optioner.Aux[A, B]) extends Parser[B] {
    def unsafeParse(state: State): Unit = {
      val offset = state.offset
      parser.unsafeParse(state)
      if (state.isOK) state.done(state.offset, opt.some(state.value.asInstanceOf[A]))
      else state.done(offset, opt.none)
    }
  }

  /** [[Parser#!]] implementation. */
  private final class Capture(val parser: Parser[Any]) extends Parser[String] {
    def unsafeParse(state: State): Unit = {
      val offset = state.offset
      parser.unsafeParse(state)
      if (!state.isOK) return
      val value = state.input.substring(offset, state.offset)
      state.done(state.offset, value)
    }
  }

  /** [[Parser#/]] implementation. */
  private final class Cut[A](val parser: Parser[A]) extends Parser[A] {
    def unsafeParse(state: State): Unit = {
      parser.unsafeParse(state)
      if (!state.isOK) return
      state.isCut = true
    }
  }

  /** [[Parser#map]] implementation. */
  private final class Map[A, B](val parser: Parser[A], val f: A => B) extends Parser[B] {
    def unsafeParse(state: State): Unit = {
      parser.unsafeParse(state)
      if (!state.isOK) return
      state.value = f(state.value.asInstanceOf[A])
    }
  }

  /** [[Parser#named]] implementation. */
  private class Named[A](parser: Parser[A], name: String) extends Parser[A] {
    def unsafeParse(state: State): Unit = {
      val offset = state.offset
      parser.unsafeParse(state)
      if (state.isOK) return
      state.failed(Error.Expected(offset, Set(name)))
    }
  }

  /** [[Parser.satisfy]] implementation. */
  private final class Satisfy(p: Char => Boolean) extends Parser[Unit] {
    def unsafeParse(state: State): Unit =
      if (state.offset < state.input.length && p(state.input.charAt(state.offset))) state.done(state.offset + 1, ())
      else state.failed(Error.Unexpected(state.offset))
  }

  /** [[Parser.satisfyWhile]] implementation. */
  private final class SatisfyWhile(p: Char => Boolean, min: Int) extends Parser[Unit] {
    def unsafeParse(state: State): Unit = {
      var n = 0
      while (state.offset < state.input.length && p(state.input.charAt(state.offset))) {
        state.offset += 1
        n += 1
      }
      if (n >= min) state.done(state.offset, ())
      else state.failed(Error.Unexpected(state.offset))
    }
  }

  /** [[Parser.stringLiteral]] implementation. */
  private final class StringLiteral(s: String) extends Parser[Unit] {
    def unsafeParse(state: State): Unit =
      if (state.input.startsWith(s, state.offset)) state.done(state.offset + s.length, ())
      else state.failed(Error.Unexpected(state.offset))
  }

  /** [[Parser.start]] implementation. */
  private object Start extends Parser[Unit] {
    def unsafeParse(state: State): Unit =
      if (state.offset == 0) state.done(state.offset, ())
      else state.failed(Error.Unexpected(state.offset))
  }

  /** [[Parser.end]] implementation. */
  private object End extends Parser[Unit] {
    def unsafeParse(state: State): Unit =
      if (state.offset == state.input.length) state.done(state.offset, ())
      else state.failed(Error.Unexpected(state.offset))
  }

  /** [[Parser.pass]] implementation. */
  private final class Pass[A](value: A) extends Parser[A] {
    def unsafeParse(state: State): Unit = {
      state.done(state.offset, value)
    }
  }

  /** [[Parser.fail]] implementation. */
  private final class Fail(message: String) extends Parser[Any] {
    def unsafeParse(state: State): Unit = {
      state.failed(Error.Failure(state.offset, message))
    }
  }

  /** [[Parser.delay]] implementation. */
  private final class Delay[A](var parser0: () => Parser[A]) extends Parser[A] {
    lazy val parser: Parser[A] = {
      val p = parser0()
      parser0 = null
      p
    }

    def unsafeParse(state: State): Unit = parser.unsafeParse(state)
  }

  private final class PosLookAhead[A](val parser: Parser[A]) extends Parser[Unit] {
    def unsafeParse(state: State): Unit = {
      val offset = state.offset
      parser.unsafeParse(state)
      if (state.isOK) state.done(offset, ())
    }
  }

  private final class NegLookAhead[A](val parser: Parser[A]) extends Parser[Unit] {
    def unsafeParse(state: State): Unit = {
      val offset = state.offset
      parser.unsafeParse(state)
      if (!state.isOK) state.done(offset, ())
      else state.failed(Error.Unexpected(offset))
    }
  }
}
