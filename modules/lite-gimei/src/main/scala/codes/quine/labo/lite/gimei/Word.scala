package codes.quine.labo.lite.gimei

import java.util.Locale

import codes.quine.labo.lite.romaji.Kana

/** Word is a kanji word with furigana (hiragana and katakana). */
final case class Word(toKanji: String, toHiragana: String, toKatakana: String) extends Furigana {
  def toRomaji: String = {
    val s = Kana.toRomaji(toKatakana)
    s.slice(0, 1).toUpperCase(Locale.ROOT) + s.slice(1, s.length)
  }
}

object Word {

  /** Parses the given string as word data set.
    *
    * It is needed to avoid "Method too large" error.
    */
  private[gimei] def load(s: String): IndexedSeq[Word] =
    s.linesIterator.map { s =>
      val Array(kan, hira, kata) = s.split(' ')
      Word(kan, hira, kata)
    }.toIndexedSeq
}
