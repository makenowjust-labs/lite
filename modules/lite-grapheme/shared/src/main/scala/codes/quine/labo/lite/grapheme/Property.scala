package codes.quine.labo.lite.grapheme

/** Property is a Unicode property value. Only the ones needed in the context in this package are included. */
sealed abstract class Property extends Product with Serializable

object Property {
  case object Prepend extends Property
  case object CR extends Property
  case object LF extends Property
  case object Control extends Property
  case object Other extends Property
  case object L extends Property
  case object V extends Property
  case object T extends Property
  case object LV extends Property
  case object LVT extends Property
  case object Regional_Indicator extends Property
  case object Extended_Pictographic extends Property
  case object Extend extends Property
  case object SpacingMark extends Property
  case object ZWJ extends Property

  /** A set of all possible properties. */
  final val All: Set[Property] =
    Set(
      Prepend,
      CR,
      LF,
      Control,
      Other,
      L,
      V,
      T,
      LV,
      LVT,
      Regional_Indicator,
      Extended_Pictographic,
      Extend,
      SpacingMark,
      ZWJ
    )

  /** Returns a property of the given code point. */
  def of(cp: Int): Property = {
    var from = 0
    var to = Data.CodePoints.size
    while (to > from) {
      val middle = (from + to) / 2
      val (b, e, p) = Data.CodePoints(middle)
      if (cp < b) to = middle
      else if (e < cp) from = middle + 1
      else return p
    }
    Other
  }
}
