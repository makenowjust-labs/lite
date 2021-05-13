package codes.quine.labo.lite.grapheme

class GraphemeSuite extends munit.FunSuite {
  test("Grapheme.iterate") {
    assertEquals(
      Grapheme.iterate("👨‍👨‍👧‍👦👩‍👩‍👧‍👦👨‍👨‍👧‍👦").toList,
      List(Grapheme("👨‍👨‍👧‍👦"), Grapheme("👩‍👩‍👧‍👦"), Grapheme("👨‍👨‍👧‍👦"))
    )
  }

  test("Grapheme.findNextBoundary") {
    interceptMessage[StringIndexOutOfBoundsException]("index: -1, length: 3") {
      Grapheme.findNextBoundary("foo", -1)
    }
    interceptMessage[StringIndexOutOfBoundsException]("index: 3, length: 3") {
      Grapheme.findNextBoundary("foo", 3)
    }
    // Non error cases are covered in auto generated tests.
  }

  test("Grapheme.unsafeFindNextBoundary") {
    interceptMessage[StringIndexOutOfBoundsException]("index: -1, length: 3") {
      Grapheme.unsafeFindNextBoundary("foo", -1)
    }
    interceptMessage[StringIndexOutOfBoundsException]("index: 3, length: 3") {
      Grapheme.unsafeFindNextBoundary("foo", 3)
    }
    val ri = String.valueOf(Character.toChars(0x1f1ee))
    assertEquals(Grapheme.unsafeFindNextBoundary(ri * 3, 0), 4)
    // Note that the correct boundary index is `4` in this case, it is one of the unsafe examples.
    assertEquals(Grapheme.unsafeFindNextBoundary(ri * 3, 2), 6)
  }

  test("Grapheme.findPreviousBoundary") {
    interceptMessage[StringIndexOutOfBoundsException]("index: 0, length: 3") {
      Grapheme.findPreviousBoundary("foo", 0)
    }
    interceptMessage[StringIndexOutOfBoundsException]("index: 4, length: 3") {
      Grapheme.findPreviousBoundary("foo", 4)
    }
    // Non error cases are covered in auto generated tests.
  }

  test("Grapheme.unsafeFindPreviousBoundary") {
    interceptMessage[StringIndexOutOfBoundsException]("index: 0, length: 3") {
      Grapheme.unsafeFindPreviousBoundary("foo", 0)
    }
    interceptMessage[StringIndexOutOfBoundsException]("index: 4, length: 3") {
      Grapheme.unsafeFindPreviousBoundary("foo", 4)
    }
    val ri = String.valueOf(Character.toChars(0x1f1ee))
    assertEquals(Grapheme.unsafeFindPreviousBoundary(ri * 3, 4), 0)
    // Note that the correct boundary index is `4` in this case, it is one of the unsafe examples.
    assertEquals(Grapheme.unsafeFindPreviousBoundary(ri * 3, 6), 2)
  }
}
