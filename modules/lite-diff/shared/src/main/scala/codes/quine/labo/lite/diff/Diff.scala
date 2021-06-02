package codes.quine.labo.lite.diff

import scala.collection.mutable

/** Diff is a frontend object of diff computation. */
object Diff {

  /** Computes diff between two sequence
    * by using [[https://en.wikipedia.org/wiki/Gestalt_Pattern_Matching Gestalt Pattern Matching]].
    */
  def diff[A](left: Seq[A], right: Seq[A]): Patch[A] =
    new Diff(left.toIndexedSeq, right.toIndexedSeq).run()
}

/** Diff is a temporary class for diff computation. */
private final class Diff[A](private[this] val left: IndexedSeq[A], private[this] val right: IndexedSeq[A]) {

  /** A map from a right item to indices. */
  private[this] val rightItemToIndices = right.indices.groupBy(right).withDefaultValue(Seq.empty)

  /** Runs this algorithm. */
  def run(): Patch[A] = {
    val stack = mutable.Stack(Hunk(0, left.size, 0, right.size))
    val hunks = Seq.newBuilder[Hunk]

    while (stack.nonEmpty) {
      val hunk = stack.pop()
      if (hunk.leftStart == hunk.leftEnd && hunk.rightStart == hunk.rightEnd) () // Nothing to do because of empty hunk.
      else if (hunk.leftStart == hunk.leftEnd || hunk.rightStart == hunk.rightEnd) hunks.addOne(hunk)
      else {
        val m = findLongestMatch(hunk)
        if (m.leftStart == m.leftEnd) hunks.addOne(hunk)
        else {
          stack.push(Hunk(m.leftEnd, hunk.leftEnd, m.rightEnd, hunk.rightEnd))
          stack.push(Hunk(hunk.leftStart, m.leftStart, hunk.rightStart, m.rightStart))
        }
      }
    }

    Patch(left, right, hunks.result())
  }

  /** Finds common longest matching in hunk. */
  private def findLongestMatch(hunk: Hunk): Hunk = {
    var jToSize = Map.empty[Int, Int].withDefaultValue(0)

    var bestI = hunk.leftStart
    var bestJ = hunk.rightStart
    var bestSize = 0

    for (i <- hunk.leftStart until hunk.leftEnd) {
      val nextJToSize = Map.newBuilder[Int, Int]
      for (j <- rightItemToIndices(left(i)); if hunk.rightStart <= j && j < hunk.rightEnd) {
        val size = jToSize(j - 1) + 1
        nextJToSize.addOne(j -> size)
        if (size > bestSize) {
          bestI = i - size + 1
          bestJ = j - size + 1
          bestSize = size
        }
      }
      jToSize = nextJToSize.result().withDefaultValue(0)
    }

    Hunk(bestI, bestI + bestSize, bestJ, bestJ + bestSize)
  }
}
