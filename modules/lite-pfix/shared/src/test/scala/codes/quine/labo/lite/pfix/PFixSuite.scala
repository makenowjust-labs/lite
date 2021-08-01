package codes.quine.labo.lite.pfix

class PFixSuite extends munit.FunSuite {
  test("PFix: fizzbuzz example") {
    val fizzbuzz = PFix[Int, List[String]](rec => { case n if n > 0 && n % 15 == 0 => rec(n - 1) ++ List("FizzBuzz") })
    val fizz = PFix[Int, List[String]](rec => { case n if n > 0 && n % 3 == 0 => rec(n - 1) ++ List("Fizz") })
    val buzz = PFix[Int, List[String]](rec => { case n if n > 0 && n % 5 == 0 => rec(n - 1) ++ List("Buzz") })
    val other = PFix[Int, List[String]](rec => { case n if n > 0 => rec(n - 1) ++ List(n.toString) })

    val f = fizzbuzz.orElse(fizz).orElse(buzz).orElse(other).toFunction(_ => List.empty)
    assertEquals(
      f(15),
      List("1", "2", "Fizz", "4", "Buzz", "Fizz", "7", "8", "Fizz", "Buzz", "11", "Fizz", "13", "14", "FizzBuzz")
    )
  }

  test("PFix.apply") {
    val pfix = PFix[Int, List[Int]](rec => { case n if n > 0 => rec(n - 1) ++ List(n) })
    val f = pfix.toFunction(_ => List.empty)
    assertEquals(f(0), List.empty)
    assertEquals(f(3), List(1, 2, 3))
  }

  test("PFix.from") {
    val pfix = PFix.from[Int, Int] { case n if n > 0 => n * 2 }
    val f = pfix.toFunction(n => n)
    assertEquals(f(0), 0)
    assertEquals(f(-1), -1)
    assertEquals(f(2), 4)
  }

  test("PFix.empty") {
    val f = PFix.empty[Int, Int].toFunction(n => n)
    assertEquals(f(0), 0)
    assertEquals(f(-1), -1)
    assertEquals(f(1), 1)
  }

  test("PFix#toPartialFunction") {
    val pfix = PFix.from[Int, Int] { case n if n > 0 => n * 2 }
    val pf = pfix.toPartialFunction
    assertEquals(pf(1), 2)
    val err = intercept[MatchError](pf(-1))
    assert(err.getMessage().contains("-1"))
    assertEquals(pf.isDefinedAt(1), true)
    assertEquals(pf.isDefinedAt(-1), false)
  }

  test("PFix#orElse") {
    // https://en.wikipedia.org/wiki/Collatz_conjecture
    val pfix0 = PFix[Int, Int](rec => { case n if n > 1 && n % 2 == 0 => rec(n / 2) })
    val pfix1 = PFix[Int, Int](rec => { case n if n > 1 && n % 2 == 1 => rec(3 * n + 1) })
    val f = pfix0.orElse(pfix1).toFunction(n => n)
    assertEquals(f(12), 1)
  }
}
