package codes.quine.labo.lite.crazy

class RStateSuite extends munit.FunSuite {
  // https://lukepalmer.wordpress.com/2008/08/10/mindfuck-the-reverse-state-monad/
  test("RState: fibs") {
    import codes.quine.labo.lite.crazy.implicits._

    def cumulativeSums(xs: LazyList[Int]): LazyList[Int] = xs.scanLeft(0)(_ + _)

    def computeFibs(): RState[LazyList[Int], LazyList[Int]] = for {
      fibs <- RState.get[LazyList[Int]]
      _ <- RState.modify[LazyList[Int]](cumulativeSums(_))
      _ <- RState.put[LazyList[Int]](LazyList.cons(1, fibs))
    } yield fibs

    assertEquals(
      computeFibs().run(LazyList.empty[Int])._2.take(15),
      LazyList(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377)
    )
  }
}
