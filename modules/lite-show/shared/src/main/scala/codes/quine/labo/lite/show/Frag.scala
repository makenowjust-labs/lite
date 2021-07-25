package codes.quine.labo.lite.show

import scala.annotation.tailrec

/** Frag is a fragment of pretty-printed string. */
sealed abstract class Frag extends Product with Serializable {

  /** Converts this into a compact form. */
  protected[show] def toCompact: List[Frag.Lit]
}

object Frag {

  /** Line is a line fragment. Its compact form is a single whitespace. */
  case object Line extends Frag {
    protected[show] def toCompact: List[Lit] = List(Lit(" "))
  }

  /** Break is a line-break fragment. Its compact form is empty. */
  case object Break extends Frag {
    protected[show] def toCompact: List[Lit] = List.empty
  }

  /** Lit is a literal string fragment. */
  final case class Lit(content: String, isControl: Boolean = false) extends Frag {
    protected[show] def toCompact: List[Lit] = List(this)
  }

  /** Wide is a literal string fragment only for wide rendering. */
  final case class Wide(content: String) extends Frag {
    protected[show] def toCompact: List[Lit] = List.empty
  }

  /** Indent is a fragment to increase indentation in its fragments. */
  final case class Indent(frags: List[Frag]) extends Frag {
    protected[show] def toCompact: List[Lit] = frags.flatMap(_.toCompact)
  }

  /** Group is a group of fragments to control line breaks. */
  final case class Group(frags: List[Frag]) extends Frag {
    protected[show] def toCompact: List[Lit] = frags.flatMap(_.toCompact)
  }

  /** Renders fragments by fitting the specified width. */
  def render(frags: List[Frag], width: Int = 80, indentSize: Int = 2): String = {

    // Tests the given stack can fit the width starting from the column.
    @tailrec
    def fits(column: Int, stack: List[(Int, List[Frag])]): Boolean =
      if (column > width) false
      else
        stack match {
          case Nil => true
          case (indent, top) :: remain =>
            top match {
              case Nil                 => fits(column, remain)
              case Line :: _           => true
              case Break :: _          => true
              case Lit(s, false) :: fs => fits(column + s.length, (indent, fs) :: remain)
              case Lit(_, true) :: fs  => fits(column, (indent, fs) :: remain)
              case Wide(s) :: fs       => fits(column + s.length, (indent, fs) :: remain)
              case Indent(fs1) :: fs2  => fits(column, (indent + indentSize, fs1) :: (indent, fs2) :: remain)
              case Group(fs1) :: fs2   => fits(column, (indent, fs1) :: (indent, fs2) :: remain)
            }
        }

    // A result string builder.
    val sb = new StringBuilder

    // A main loop for rendering.
    // The first value of stack item is the current indentation size, and the second is fragments.
    @tailrec
    def loop(column: Int, stack: List[(Int, List[Frag])]): Unit =
      stack match {
        case Nil => ()
        case (indent, top) :: remain =>
          top match {
            case Nil =>
              loop(column, remain)
            case (Line | Break) :: fs =>
              sb.append("\n").append(" " * indent)
              loop(indent, (indent, fs) :: remain)
            case Lit(s, false) :: fs =>
              sb.append(s)
              loop(column + s.length, (indent, fs) :: remain)
            case Lit(s, true) :: fs =>
              sb.append(s)
              loop(column, (indent, fs) :: remain)
            case Wide(s) :: fs =>
              sb.append(s)
              loop(column + s.length, (indent, fs) :: remain)
            case Indent(fs1) :: fs2 =>
              loop(column, (indent + indentSize, fs1) :: (indent, fs2) :: remain)
            case (g @ Group(fs1)) :: fs2 =>
              val c = g.toCompact
              // If a compact form can fit the width, then it renders the compact form.
              if (fits(column, (indent, c) :: (indent, fs2) :: remain)) {
                c.foreach { case Lit(s, _) => sb.append(s) }
                loop(column + c.map(_.content.length).sum, (indent, fs2) :: remain)
              } else loop(column, (indent, fs1) :: (indent, fs2) :: remain)
          }
      }

    loop(0, List((0, frags)))
    sb.result()
  }
}
