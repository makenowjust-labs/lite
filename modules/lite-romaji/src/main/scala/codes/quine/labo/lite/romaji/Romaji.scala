package codes.quine.labo.lite.romaji

import java.util.Locale

import scala.annotation.tailrec

/** Romaji to kana transliterator. */
object Romaji {

  /** Transliterates a romaji string to a kana string. */
  def toKana(romaji: String): String = {
    val s = romaji.toLowerCase(Locale.ROOT)
    val sb = new StringBuilder

    @tailrec
    def loop(pos: Int): Unit =
      if (pos < s.length) {
        val c = s(pos)
        val next = if (pos + 1 < s.length) Some(s(pos + 1)) else None
        if (c == 'm' && next.exists(Util.isPBM)) {
          sb.append('ン')
          loop(pos + 1)
        } else if (next.contains(c) && Util.isAlphabet(c) && !Util.isVowel(c)) {
          sb.append('ッ')
          loop(pos + 1)
        } else {
          val found = (Constant.MaxRomajiLength to 1 by -1).flatMap { n =>
            val r = s.slice(pos, pos + n)
            Constant.RomajiToKana.get(r).map((r, _))
          }.headOption
          found match {
            case Some((r, k)) =>
              sb.append(k)
              loop(pos + r.length)
            case None =>
              sb.append(c)
              loop(pos + 1)
          }
        }
      }

    loop(0)
    sb.result()
  }
}
