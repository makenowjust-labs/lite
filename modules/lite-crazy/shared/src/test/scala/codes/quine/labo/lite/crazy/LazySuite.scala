package codes.quine.labo.lite.crazy

class LazySuite extends munit.FunSuite {
  test("Lazy.apply") {
    var x = 0
    val lx = Lazy {
      x += 1
      x
    }
    assertEquals(x, 0)
    assertEquals(lx.value, 1)
    assertEquals(x, 1)
    assertEquals(lx.value, 1)
  }

  test("Lazy.fix") {
    val lxs = Lazy.fix[LazyList[Int]](lxs => LazyList.cons(1, lxs.value).scanLeft(0)(_ + _))
    assertEquals(lxs.value.take(10), LazyList(0, 1, 1, 2, 3, 5, 8, 13, 21, 34))
  }

  test("Lazy#map") {
    assertEquals(Lazy(1).map(_ + 2).value, 3)
  }

  test("Lazy#flatMap") {
    assertEquals(Lazy(1).flatMap(x => Lazy(x + 2)).value, 3)
  }
}
