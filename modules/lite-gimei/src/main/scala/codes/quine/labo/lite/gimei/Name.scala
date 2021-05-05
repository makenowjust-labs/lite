package codes.quine.labo.lite.gimei

/** Name is a Japanese name with furigana. */
final case class Name(gender: Name.Gender, firstName: Word, lastName: Word) extends Furigana {
  def toKanji: String = s"${lastName.toKanji} ${firstName.toKanji}"
  def toHiragana: String = s"${lastName.toHiragana} ${firstName.toHiragana}"
  def toKatakana: String = s"${lastName.toKatakana} ${firstName.toKatakana}"
  def toRomaji: String = s"${firstName.toRomaji} ${lastName.toRomaji}"
}

object Name {

  /** Gender is a gender type to choose a name. */
  sealed abstract class Gender extends Product with Serializable

  object Gender {
    case object Male extends Gender
    case object Female extends Gender
  }
}
