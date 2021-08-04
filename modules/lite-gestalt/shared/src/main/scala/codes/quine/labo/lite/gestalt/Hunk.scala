package codes.quine.labo.lite.gestalt

/** Hunk is a hunk of diff.
  *
  * We can get the right sequence from the left sequence
  * by replacing `left.slice(leftStart, leftEnd)` with `right.slice(rightStart, rightEnd)`.
  */
final case class Hunk(leftStart: Int, leftEnd: Int, rightStart: Int, rightEnd: Int)
