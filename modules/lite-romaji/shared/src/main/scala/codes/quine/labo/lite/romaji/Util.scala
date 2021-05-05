package codes.quine.labo.lite.romaji

/** Utilities for romaji library. */
private[lite] object Util {

  /** Tests whether the given character is alphabet or not. */
  def isAlphabet(c: Char): Boolean = 'a' <= c && c <= 'z'

  /** Tests whether the given character is vowel or not. */
  def isVowel(c: Char): Boolean = {
    // 'n' is not vowel in Japanese but it is here for convenience.
    c == 'a' || c == 'i' || c == 'u' || c == 'e' || c == 'o' || c == 'n'
  }

  /** Tests whether the given character is one of 'p', 'b' or 'm'. */
  def isPBM(c: Char): Boolean = c == 'p' || c == 'b' || c == 'm'

  /** Returns a upper case character of the given character. */
  def toUpper(c: Char): Char =
    if ('a' <= c && c <= 'z') (c.toInt - 'a'.toInt + 'A'.toInt).toChar
    else c

  /** Returns a upper case string of the given string. */
  def toUpper(s: String): String = s.map(toUpper(_: Char))

  /** Returns a lower case character of the given character. */
  def toLower(c: Char): Char =
    if ('A' <= c && c <= 'Z') (c.toInt - 'A'.toInt + 'a'.toInt).toChar
    else c

  /** Returns a lower case string of the given string. */
  def toLower(s: String): String = s.map(toLower(_: Char))
}
