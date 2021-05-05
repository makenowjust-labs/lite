package codes.quine.labo.lite.gimei

import scala.util.Random

/** Gimei is a frontend of the generator. */
object Gimei {

  /** Generates a Japanese name randomly. */
  def name(random: Random = Random): Name = {
    val gender = if (random.nextBoolean()) Name.Gender.Male else Name.Gender.Female
    val firstNameData = gender match {
      case Name.Gender.Male   => Data.MaleFirstName
      case Name.Gender.Female => Data.FemaleFirstName
    }
    val firstName = firstNameData(random.nextInt(firstNameData.size))
    val lastName = Data.LastName(random.nextInt(Data.LastName.size))
    Name(gender, firstName, lastName)
  }

  /** Generates a Japanese address randomly. */
  def address(random: Random = Random): Address = {
    val prefecture = Data.Prefecture(random.nextInt(Data.Prefecture.size))
    val city = Data.City(random.nextInt(Data.City.size))
    val town = Data.Town(random.nextInt(Data.Town.size))
    Address(prefecture, city, town)
  }
}
