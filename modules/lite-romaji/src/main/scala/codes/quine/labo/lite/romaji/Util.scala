package codes.quine.labo.lite.romaji

/** Utilities for romaji library. */
private object Util {

  /** Tests whether the given character is alphabet or not. */
  def isAlphabet(c: Char): Boolean = 'a' <= c && c <= 'z'

  /** Tests whether the given character is vowel or not. */
  def isVowel(c: Char): Boolean = {
    // 'n' is not vowel in Japanese but it is here for convenience.
    c == 'a' || c == 'i' || c == 'u' || c == 'e' || c == 'o' || c == 'n'
  }

  /** Tests whether the given character is one of 'p', 'b' or 'm'. */
  def isPBM(c: Char): Boolean = c == 'p' || c == 'b' || c == 'm'
}
