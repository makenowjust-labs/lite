package codes.quine.labo.lite.gimei

/** Furigana is a base trait of a kanji word with furigana (hiragana and katakana). */
trait Furigana {

  /** Returns a kanji notation of the word. */
  def toKanji: String

  /** Returns a hiragana notation of the word. */
  def toHiragana: String

  /** Returns a katakana notation of the word. */
  def toKatakana: String

  /** Returns a romaji notation of the word. This result is title case (The first letter is upper case.) */
  def toRomaji: String
}
