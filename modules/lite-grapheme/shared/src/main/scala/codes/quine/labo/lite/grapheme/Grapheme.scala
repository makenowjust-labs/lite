package codes.quine.labo.lite.grapheme

import scala.annotation.tailrec
import scala.collection.mutable

/** Grapheme represents a grapheme cluster as defined in [[https://unicode.org/reports/tr29/ UAX29]]. */
final case class Grapheme(value: String)

object Grapheme {

  /** State is a state of the transition table to detect a grapheme boundary. */
  private sealed abstract class State extends Product with Serializable

  private object State {
    case object Init extends State
    case object NoExtend extends State
    case object CR extends State
    case object Prepend extends State
    case object L extends State
    case object LV extends State
    case object LVT extends State
    case object RI extends State
    case object XPicto extends State
    case object ZWJ extends State
    case object Extend extends State
  }

  /** The deterministic transition table to detect a grapheme boundary.
    * It is assembled by hand from the regexes of Table 1b. and 1c. in [[https://unicode.org/reports/tr29/ UAX29]].
    */
  private final val Transition: Map[(State, Property), State] = Map(
    // crlf:
    // - CR
    (State.Init, Property.CR) -> State.CR,
    // - LF
    (State.Init, Property.LF) -> State.NoExtend,
    (State.CR, Property.LF) -> State.NoExtend,
    // control:
    // - Control
    (State.Init, Property.Control) -> State.NoExtend,
    // precore:
    // - Prepend
    (State.Init, Property.Prepend) -> State.Prepend,
    // According to UAX29, a number of `Prepend` can be placed before a `core`,
    // and the ICU, Ruby and Swift implementations behave such the way.
    // However, `BreakIterator` in Java and `uniseg` in Go allows only one `Prepend` before a `core`.
    // Here, it follows the UAX29 way because it is standard, but it may be surprising someone.
    (State.Prepend, Property.Prepend) -> State.Prepend,
    // core:
    // - Other
    (State.Init, Property.Other) -> State.Extend,
    (State.Prepend, Property.Other) -> State.Extend,
    // hangul-syllable:
    // - L
    (State.Init, Property.L) -> State.L,
    (State.Prepend, Property.L) -> State.L,
    (State.L, Property.L) -> State.L,
    // - V
    (State.Init, Property.V) -> State.LV,
    (State.Prepend, Property.V) -> State.LV,
    (State.Init, Property.V) -> State.LV,
    (State.L, Property.V) -> State.LV,
    (State.LV, Property.V) -> State.LV,
    // - T
    (State.Init, Property.T) -> State.LVT,
    (State.Prepend, Property.T) -> State.LVT,
    (State.LV, Property.T) -> State.LVT,
    (State.LVT, Property.T) -> State.LVT,
    // - LV
    (State.Init, Property.LV) -> State.LV,
    (State.Prepend, Property.LV) -> State.LV,
    (State.L, Property.LV) -> State.LV,
    // - LVT
    (State.Init, Property.LVT) -> State.LVT,
    (State.Prepend, Property.LVT) -> State.LVT,
    (State.L, Property.LVT) -> State.LVT,
    // ri-sequence:
    // - Regional_Indicator
    (State.Init, Property.Regional_Indicator) -> State.RI,
    (State.Prepend, Property.Regional_Indicator) -> State.RI,
    (State.RI, Property.Regional_Indicator) -> State.Extend,
    // xpicto-sequence:
    // - Extended_Pictographic
    // Note that transitions by `ZWJ` and `Extend` are covered in the below 'postcore' section.
    (State.Init, Property.Extended_Pictographic) -> State.XPicto,
    (State.Prepend, Property.Extended_Pictographic) -> State.XPicto,
    (State.ZWJ, Property.Extended_Pictographic) -> State.XPicto,
    // postcore:
    // - Extend
    (State.Init, Property.Extend) -> State.Extend,
    (State.Prepend, Property.Extend) -> State.Extend,
    (State.L, Property.Extend) -> State.Extend,
    (State.LV, Property.Extend) -> State.Extend,
    (State.LVT, Property.Extend) -> State.Extend,
    (State.RI, Property.Extend) -> State.Extend,
    (State.XPicto, Property.Extend) -> State.XPicto,
    (State.ZWJ, Property.Extend) -> State.Extend,
    (State.Extend, Property.Extend) -> State.Extend,
    // - ZWJ
    (State.Init, Property.ZWJ) -> State.Extend,
    (State.Prepend, Property.ZWJ) -> State.Extend,
    (State.L, Property.ZWJ) -> State.Extend,
    (State.LV, Property.ZWJ) -> State.Extend,
    (State.LVT, Property.ZWJ) -> State.Extend,
    (State.RI, Property.ZWJ) -> State.Extend,
    (State.XPicto, Property.ZWJ) -> State.ZWJ,
    (State.ZWJ, Property.ZWJ) -> State.Extend,
    (State.Extend, Property.ZWJ) -> State.Extend,
    // - SpacingMask
    (State.Init, Property.SpacingMark) -> State.Extend,
    (State.Prepend, Property.SpacingMark) -> State.Extend,
    (State.L, Property.SpacingMark) -> State.Extend,
    (State.LV, Property.SpacingMark) -> State.Extend,
    (State.LVT, Property.SpacingMark) -> State.Extend,
    (State.RI, Property.SpacingMark) -> State.Extend,
    (State.XPicto, Property.SpacingMark) -> State.Extend,
    (State.ZWJ, Property.SpacingMark) -> State.Extend,
    (State.Extend, Property.SpacingMark) -> State.Extend
  )

  /** The initial state of the reversed deterministic transition table.
    * It is a set of all states except for `Init` due to non final state.
    */
  private final val ReversedInitState: Set[State] = Set(
    State.NoExtend,
    State.CR,
    State.Prepend,
    State.L,
    State.LV,
    State.LVT,
    State.RI,
    State.XPicto,
    State.ZWJ,
    State.Extend
  )

  /** The RI state of the reversed deterministic transition table. */
  private final lazy val ReversedRIState: Set[State] =
    ReversedTransition((ReversedInitState, Property.Regional_Indicator))

  /** The reversed deterministic transition table. */
  private final lazy val ReversedTransition: Map[(Set[State], Property), Set[State]] = {
    // Reverses `Transition`. It becomes non deterministic transition table.
    val nonDeterministic =
      Transition
        .groupMap { case ((_, p), t) => (t, p) } { case ((s, _), _) => s }
        .view
        .mapValues(_.toSet)
        .toMap
        .withDefaultValue(Set.empty)

    // Constructs a deterministic transition table from non deterministic one.
    val visited = mutable.Set(ReversedInitState)
    val queue = mutable.Queue.from(visited)
    val transition = Map.newBuilder[(Set[State], Property), Set[State]]

    while (queue.nonEmpty) {
      val ss = queue.dequeue()
      for (p <- Property.All) {
        val ts = ss.flatMap(s => nonDeterministic((s, p)))
        if (ts.nonEmpty) {
          if (!visited.contains(ts)) {
            visited.add(ts)
            queue.enqueue(ts)
          }
          transition.addOne((ss, p) -> ts)
        }
      }
    }

    transition.result()
  }

  /** Iterates the given string on each grapheme cluster. */
  def iterate(s: String): Iterator[Grapheme] = {
    Iterator.unfold(0) { i =>
      if (i < s.length) {
        val j = execTransition(s, State.Init, i)
        Some((Grapheme(s.slice(i, j)), j))
      } else None
    }
  }

  /** Finds the next grapheme boundary index of the given string from the specified index.
    * When the index is invalid as a start position of a grapheme cluster, it throws a StringIndexOutOfBounds exception.
    */
  def findNextBoundary(s: String, index: Int): Int = {
    if (index < 0 || s.length <= index) {
      throw new StringIndexOutOfBoundsException(s"index: $index, length: ${s.length}")
    }

    // Corrects the index when the index points between surrogate pair.
    val i =
      if (index - 1 >= 0 && s.charAt(index).isLowSurrogate && s.charAt(index - 1).isHighSurrogate) index - 1
      else index

    // Some properties of the current position's character need preprocessing before executing the transition.
    val cp = s.codePointAt(i)
    val prop = Property.of(cp)
    val initState = prop match {
      // Case 1. inside of `Regional_Indicator` sequence.
      // If length of the leading RI sequence is odd, the current RI is the second RI in fact,
      // so it should start from the `RI` state. If it is even, it can start `Init` usually.
      case Property.Regional_Indicator =>
        val countRI = countPreviousRISequence(s, i)
        if (countRI % 2 == 1) State.RI else State.Init
      // Case 2. maybe inside of emoji combination sequence.
      // It can not decide it is in emoji combination sequence by starting from `Extend` and `ZWJ`.
      // First, it finds the `Extended_Pictographic` character in backward direction.
      // If it is found, it executes the transition from `XPicto`, or it falls back to usual.
      case Property.Extend | Property.ZWJ =>
        if (findPreviousXPicto(s, i)) State.XPicto else State.Init
      // Case 3. others.
      case _ => State.Init
    }

    // Executes the transition. It skips the first character for efficient.
    // Note that transitions of pairs of `Init` and any properties, `RI` and `Regional_Indicator`,
    // and `XPicto` and `Extend` or `ZWJ` are defined.
    execTransition(s, Transition((initState, prop)), i + Character.charCount(cp))
  }

  /** Like `findNextBoundary`, but it is '''UNSAFE''' in fact.
    * '''UNSAFE''' means it does not safe when the index points non boundary of grapheme cluster.
    * It is useful to imitate PCRE and Ruby's `/\X/` behavior for example.
    */
  def unsafeFindNextBoundary(s: String, index: Int): Int = {
    if (index < 0 || s.length <= index) {
      throw new StringIndexOutOfBoundsException(s"index: $index, length: ${s.length}")
    }

    execTransition(s, State.Init, index)
  }

  /** Executes the transition function on the given string from the index and the state. */
  @tailrec
  private def execTransition(s: String, state: State, index: Int): Int =
    if (s.length <= index) s.length
    else {
      val cp = s.codePointAt(index)
      val prop = Property.of(cp)
      Transition.get((state, prop)) match {
        case Some(to) => execTransition(s, to, index + Character.charCount(cp))
        case None     => index
      }
    }

  /** Finds a `XPicto` character on skipping `Extend` characters in backward direction.
    * It returns `true` if it is found, or it returns `false`.
    */
  @tailrec
  private def findPreviousXPicto(s: String, index: Int): Boolean =
    if (index <= 0) false
    else {
      val cp = s.codePointBefore(index)
      val prop = Property.of(cp)
      prop match {
        case Property.Extend                => findPreviousXPicto(s, index - Character.charCount(cp))
        case Property.Extended_Pictographic => true
        case _                              => false
      }
    }

  /** Finds the previous grapheme boundary index of the given string from the specified index.
    * When the index is invalid as an end position of a grapheme cluster, it throws a StringIndexOutOfBounds exception.
    */
  def findPreviousBoundary(s: String, index: Int): Int = {
    if (index <= 0 || s.length < index) {
      throw new StringIndexOutOfBoundsException(s"index: $index, length: ${s.length}")
    }

    // Corrects the index when the index points between surrogate pair.
    val i =
      if (index < s.length && s.charAt(index - 1).isHighSurrogate && s.charAt(index).isLowSurrogate) index + 1
      else index

    // If length of the leading `Regional_Indicator` sequence is odd, the current RI is the first RI in fact,
    // so it should start from the `RI` state. If it is even, it can start `Init` usually.
    val countRI = countPreviousRISequence(s, i)
    val initState = if (countRI % 2 == 0) ReversedInitState else ReversedRIState
    execReversedTransition(s, initState, i, i)
  }

  /** Like `findPreviousBoundary`, but it is '''UNSAFE''' in fact.
    * '''UNSAFE''' means it does not safe when the index points non boundary of grapheme cluster.
    * It is useful to imitate PCRE and Ruby's `/\X/` behavior for example.
    */
  def unsafeFindPreviousBoundary(s: String, index: Int): Int = {
    if (index <= 0 || s.length < index) {
      throw new StringIndexOutOfBoundsException(s"index: $index, length: ${s.length}")
    }

    execReversedTransition(s, ReversedInitState, index, index)
  }

  /** Executes the reversed transition function on the given string from the index and the state. */
  @tailrec
  def execReversedTransition(s: String, state: Set[State], index: Int, last: Int): Int =
    if (index <= 0) last
    else {
      val cp = s.codePointBefore(index)
      val prop = Property.of(cp)
      ReversedTransition.get((state, prop)) match {
        case Some(to) =>
          val j = index - Character.charCount(cp)
          execReversedTransition(s, to, j, if (to.contains(State.Init)) j else last)
        case None => last
      }
    }

  /** Counts the number of `Regional_Indicator` characters from the index in backward direction. */
  private def countPreviousRISequence(s: String, index: Int): Int = {
    @tailrec
    def loop(index: Int, n: Int): Int =
      if (index <= 0) n
      else {
        val cp = s.codePointBefore(index)
        val prop = Property.of(cp)
        if (prop == Property.Regional_Indicator) loop(index - Character.charCount(cp), n + 1)
        else n
      }

    loop(index, 0)
  }
}
