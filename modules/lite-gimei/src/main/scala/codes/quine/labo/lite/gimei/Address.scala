package codes.quine.labo.lite.gimei

/** Address is a Japanese address with furigana. */
final case class Address(prefecture: Word, city: Word, town: Word) extends Furigana {
  def toKanji: String = s"${prefecture.toKanji}${city.toKanji}${town.toKanji}"
  def toHiragana: String = s"${prefecture.toHiragana}${city.toHiragana}${town.toHiragana}"
  def toKatakana: String = s"${prefecture.toKatakana}${city.toKatakana}${town.toKatakana}"
  def toRomaji: String = s"${prefecture.toRomaji} ${city.toRomaji} ${town.toRomaji}"
}
