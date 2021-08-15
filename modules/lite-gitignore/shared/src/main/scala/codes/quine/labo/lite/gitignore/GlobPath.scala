package codes.quine.labo.lite.gitignore

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._

import codes.quine.labo.lite.parser.Parser

/** GlobPath is a path matcher including a glob pattern. */
sealed abstract class GlobPath extends Product with Serializable {

  /** Checks whether or not the given path matches this. */
  def matches(path: Path): Boolean
}

object GlobPath {

  import Parser._

  /** Parses the given line as a path matcher.
    * When the line is not a valid path matcher (e.g. comment),  it returns `None` instead.
    */
  def parse(line: String, base: Path): Option[(Boolean, GlobPath)] =
    if (line.startsWith("#")) None
    else {
      val isNegated = line.startsWith("!")
      parser(base).parse(line, if (isNegated) 1 else 0) match {
        case Right((_, path)) => path.map((isNegated, _))
        case Left(_) => {
          // $COVERAGE-OFF$
          None
          // $COVERAGE-ON$
        }
      }
    }

  private[gitignore] def parser(base: Path): Parser[Option[GlobPath]] = {
    val space = charInWhile(" \t", min = 0)

    (Component.parser ~ ('/' ~ Component.parser).rep ~ space ~ end).map {
      case (Glob.Empty, Seq())            => None
      case (glob: Glob, Seq(Glob.Empty))  => Some(FileNameGlobPath(glob, isDir = true))
      case (glob: Glob, Seq())            => Some(FileNameGlobPath(glob, isDir = false))
      case (Glob.Empty, cs :+ Glob.Empty) => Some(RelativeGlobPath(cs, isDir = true, base))
      case (Glob.Empty, cs)               => Some(RelativeGlobPath(cs, isDir = false, base))
      case (c, cs :+ Glob.Empty)          => Some(RelativeGlobPath(c +: cs, isDir = true, base))
      case (c, cs)                        => Some(RelativeGlobPath(c +: cs, isDir = false, base))
    }
  }

  /** RelativeGlobPath is a path matcher to match a path from a base path. */
  final case class RelativeGlobPath(
      components: Seq[Component],
      isDir: Boolean,
      base: Path
  ) extends GlobPath {
    def matches(path: Path): Boolean = {
      val rel = base.relativize(path)
      val rels = rel.iterator().asScala.map(_.toString).toVector
      if (rels.isEmpty || rels.head == "..") false
      else {
        @tailrec
        def loop(pos: Int, state: Seq[Component], nextPos: Int, nextState: Seq[Component]): Boolean =
          if (pos >= rels.length && state.isEmpty) true
          else
            state.headOption match {
              case Some(StarStar) => loop(pos, state.tail, pos + 1, state)
              case Some(g: Glob) if pos < rels.size && g.matches(rels(pos)) =>
                loop(pos + 1, state.tail, nextPos, nextState)
              case _ if 0 < nextPos && nextPos <= rels.size =>
                loop(nextPos, nextState, nextPos, nextState)
              case _ => false
            }

        val matched = loop(0, components, 0, components)
        if (matched && isDir) Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
        else matched
      }
    }
  }

  /** FileNameGlobPath is a path matcher to match a filename of a path. */
  final case class FileNameGlobPath(glob: Glob, isDir: Boolean) extends GlobPath {
    def matches(path: Path): Boolean = {
      // When `path` is root, `path.getFileName` returns `null`, so it is wrapped by `Option`.
      val matched = Option(path.getFileName).exists(fileName => glob.matches(fileName.toString))
      if (matched && isDir) Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
      else matched
    }
  }

  /** Component is a component of a path matcher. */
  sealed abstract class Component extends Product with Serializable

  object Component {
    private[gitignore] lazy val parser: Parser[Component] = StarStar.parser | Glob.parser
  }

  /** StarStar is a double star glob. */
  case object StarStar extends Component {
    lazy val parser: Parser[Component] = "**".as(StarStar)
  }

  /** Glob is a glob to match a component of a path. */
  final case class Glob(chars: Seq[GlobChar]) extends Component {

    /** Checks whether or not the given file name matches this. */
    def matches(fileName: String): Boolean = {
      @tailrec
      def loop(pos: Int, state: Seq[GlobChar], nextPos: Int, nextState: Seq[GlobChar]): Boolean =
        if (pos >= fileName.length && state.isEmpty) true
        else
          state.headOption match {
            case Some(Star) => loop(pos, state.tail, pos + 1, state)
            case Some(c) if pos < fileName.length && c.accepts(fileName(pos)) =>
              loop(pos + 1, state.tail, nextPos, nextState)
            case _ if 0 < nextPos && nextPos <= fileName.length =>
              loop(nextPos, nextState, nextPos, nextState)
            case _ => false
          }
      loop(0, chars, 0, chars)
    }
  }

  object Glob {

    /** An empty glob. */
    val Empty: Glob = Glob(Seq.empty)

    private[gitignore] val parser: Parser[Glob] = GlobChar.parser.rep.map(Glob(_))
  }

  /** GlobChar is a character in a glob. */
  sealed abstract class GlobChar extends Product with Serializable {

    /** Checks whether or not this accepts the given character. */
    private[gitignore] def accepts(c: Char): Boolean
  }

  object GlobChar {
    private[gitignore] lazy val parser: Parser[GlobChar] =
      Star.parser | Quest.parser | Range.parser | Literal.parser
  }

  /** Star is `*` in a glob. */
  case object Star extends GlobChar {
    private[gitignore] def accepts(c: Char): Boolean = {
      // $COVERAGE-OFF$
      sys.error("GlobPath.Star#accepts: invalid call")
      // $COVERAGE-ON$
    }

    private[gitignore] lazy val parser: Parser[GlobChar] = '*'.as(Star)
  }

  /** Quest is `?` in a glob. */
  case object Quest extends GlobChar {
    private[gitignore] def accepts(c: Char): Boolean = true

    private[gitignore] lazy val parser: Parser[GlobChar] = '?'.as(Quest)
  }

  /** Range is a range of characters in a glob. */
  final case class Range(isNegated: Boolean, ranges: Seq[(Char, Char)]) extends GlobChar {
    private[gitignore] def accepts(c: Char): Boolean =
      !isNegated == ranges.exists { case (b, e) => b <= c && c <= e }
  }

  object Range {
    private[gitignore] lazy val parser: Parser[Range] = {
      val range: Parser[(Char, Char)] =
        ((&!(']') ~ Literal.parser) ~ ('-' ~ (&!(']') ~ Literal.parser)).?).map {
          case (b, Some(e)) => (b.char, e.char)
          case (c, None)    => (c.char, c.char)
        }

      ('[' ~ ('!'.as(true) | pass(false)) ~ range.rep ~ ']').map { case (ne, rs) => Range(ne, rs) }
    }
  }

  /** Literal is a literal character in a glob. */
  final case class Literal(char: Char) extends GlobChar {
    private[gitignore] def accepts(c: Char): Boolean = c == char
  }

  object Literal {
    private[gitignore] lazy val parser: Parser[Literal] = {
      val escape = '\\' ~ satisfy(_ => true).!.map(_.charAt(0))
      val space = charInWhile(" \t")
      val char = satisfy(_ != '/').!.map(_.charAt(0))

      (escape | (&!(space ~ end) ~ char)).map(Literal(_))
    }
  }
}
