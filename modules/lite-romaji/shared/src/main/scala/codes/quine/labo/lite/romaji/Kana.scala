package codes.quine.labo.lite.romaji

import scala.annotation.tailrec

/** Kana to romaji transliterator. */
object Kana {

  /** Transliterates a kana string to a romaji string. */
  def toRomaji(kana: String): String = {
    val s = kana
    val sb = new StringBuilder

    @tailrec
    def loop(pos: Int): Unit =
      if (pos < s.length) {
        val c = s(pos)
        if (c == 'ン') {
          val next = Constant.KanaToRomaji.get(s.slice(pos + 1, pos + 2)).map(_.head.head)
          if (next.exists(Util.isPBM)) sb.append('m')
          else if (next.exists(Util.isVowel)) sb.append("nn")
          else sb.append('n')
          loop(pos + 1)
        } else if (c == 'ッ') {
          val next = Constant.KanaToRomaji.get(s.slice(pos + 1, pos + 2)).map(_.head.head)
          if (next.forall(Util.isVowel)) sb.append("xtsu")
          else sb.append(next.get)
          loop(pos + 1)
        } else {
          val found = (Constant.MaxKanaLength to 1 by -1).flatMap { n =>
            val k = s.slice(pos, pos + n)
            Constant.KanaToRomaji.get(k).map((k, _))
          }.headOption
          found match {
            case Some((k, rs)) =>
              sb.append(rs.head)
              loop(pos + k.length)
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
