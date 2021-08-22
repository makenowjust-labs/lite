package codes.quine.labo.lite.gestalt

/** Patch is a result of diff operation.
  *
  * Note that this data class is not for patching purpose against its naming, but it is for generating a patch string
  * like unified format. This contains original sequences for such a purpose.
  */
final case class Patch[A](left: IndexedSeq[A], right: IndexedSeq[A], hunks: Seq[Hunk]) {

  /** Returns this patch string as unified diff format. */
  def toUnified(context: Int = 3): String = {
    val str = new StringBuilder

    var hunks = this.hunks
    while (hunks.nonEmpty) {
      // Finds continuous hunks.
      val continues = Seq.newBuilder[Hunk]
      val head = hunks.head
      var last = head
      hunks = hunks.tail
      continues.addOne(head)
      while (hunks.nonEmpty && hunks.head.leftStart - last.leftEnd <= context * 2) {
        last = hunks.head
        hunks = hunks.tail
        continues.addOne(last)
      }

      // Shows hunk header.
      val leftStart = Math.max(head.leftStart - context, 0)
      val rightStart = Math.max(head.rightStart - context, 0)
      val leftEnd = Math.min(last.leftEnd + context, left.size)
      val rightEnd = Math.min(last.rightEnd + context, right.size)
      str.append(s"@@ -${leftStart + 1},${leftEnd - leftStart} +${rightStart + 1},${rightEnd - rightStart}\n")

      // Shows lines.
      var leftLast = leftStart
      for (hunk <- continues.result()) {
        for (i <- leftLast until hunk.leftStart) str.append(s" ${left(i)}\n")
        for (i <- hunk.leftStart until hunk.leftEnd) str.append(s"-${left(i)}\n")
        for (j <- hunk.rightStart until hunk.rightEnd) str.append(s"+${right(j)}\n")
        leftLast = hunk.leftEnd
      }
      for (i <- leftLast until leftEnd) str.append(s" ${left(i)}\n")
    }

    str.result()
  }
}
