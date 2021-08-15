package codes.quine.labo.lite.gitignore

import java.nio.file.Path

import codes.quine.labo.lite.gitignore.GlobPath._

class GlobPathSuite extends munit.FunSuite {
  val resourcePath: Path = Path.of("modules/lite-gitignore/shared/src/test/resources").toAbsolutePath

  test("GlobPath.parse") {
    def glob(s: String): Glob = Glob(s.toSeq.map(Literal(_)))
    val base = resourcePath.resolve("glob-path")
    assertEquals(parse("# comment", base), None)
    assertEquals(parse("", base), None)
    assertEquals(parse("!", base), None)
    assertEquals(parse("foo", base), Some((false, FileNameGlobPath(glob("foo"), false))))
    assertEquals(parse("foo ", base), Some((false, FileNameGlobPath(glob("foo"), false))))
    assertEquals(parse("foo/", base), Some((false, FileNameGlobPath(glob("foo"), true))))
    assertEquals(parse("/foo", base), Some((false, RelativeGlobPath(Seq(glob("foo")), false, base))))
    assertEquals(parse("/foo/", base), Some((false, RelativeGlobPath(Seq(glob("foo")), true, base))))
    assertEquals(parse("foo/bar", base), Some((false, RelativeGlobPath(Seq(glob("foo"), glob("bar")), false, base))))
    assertEquals(parse("foo/bar/", base), Some((false, RelativeGlobPath(Seq(glob("foo"), glob("bar")), true, base))))
    assertEquals(parse("/foo/bar", base), Some((false, RelativeGlobPath(Seq(glob("foo"), glob("bar")), false, base))))
    assertEquals(parse("/foo/bar/", base), Some((false, RelativeGlobPath(Seq(glob("foo"), glob("bar")), true, base))))
    assertEquals(parse("**", base), Some((false, RelativeGlobPath(Seq(StarStar), false, base))))
    assertEquals(parse("x/**", base), Some((false, RelativeGlobPath(Seq(glob("x"), StarStar), false, base))))
    assertEquals(parse("**/y", base), Some((false, RelativeGlobPath(Seq(StarStar, glob("y")), false, base))))
    assertEquals(parse("[a]", base), Some((false, FileNameGlobPath(Glob(Seq(Range(false, Seq(('a', 'a'))))), false))))
    assertEquals(parse("[!a]", base), Some((false, FileNameGlobPath(Glob(Seq(Range(true, Seq(('a', 'a'))))), false))))
    assertEquals(parse("[a-c]", base), Some((false, FileNameGlobPath(Glob(Seq(Range(false, Seq(('a', 'c'))))), false))))
    assertEquals(parse("*", base), Some((false, FileNameGlobPath(Glob(Seq(Star)), false))))
    assertEquals(parse("?", base), Some((false, FileNameGlobPath(Glob(Seq(Quest)), false))))
    assertEquals(parse("\\\\", base), Some((false, FileNameGlobPath(glob("\\"), false))))
    assertEquals(parse("!foo", base), Some((true, FileNameGlobPath(glob("foo"), false))))
  }

  test("GlobPath#matches") {
    val base = resourcePath.resolve("glob-path")
    def matches(line: String, path: String): Boolean = parse(line, base).get._2.matches(base.resolve(path))
    assertEquals(matches("foo", "foo"), true)
    assertEquals(matches("foo", "bar"), false)
    assertEquals(matches("foo", "x/foo"), true)
    assertEquals(matches("foo", "x/bar"), false)
    assertEquals(matches("f*", "foo"), true)
    assertEquals(matches("f*", "bar"), false)
    assertEquals(matches("*o", "foo"), true)
    assertEquals(matches("*o", "bar"), false)
    assertEquals(matches("f*o", "foo"), true)
    assertEquals(matches("f*o", "bar"), false)
    assertEquals(matches("[a-z]oo", "foo"), true)
    assertEquals(matches("[a-z]oo", "bar"), false)
    assertEquals(matches("????", "fizz"), true)
    assertEquals(matches("????", "foo"), false)
    assertEquals(matches("????", "bar"), false)
    assertEquals(matches("x/foo", "x/foo"), true)
    assertEquals(matches("x/foo", "x/bar"), false)
    assertEquals(matches("x/foo", ".."), false)
    assertEquals(matches("**/foo", "foo"), true)
    assertEquals(matches("**/foo", "bar"), false)
    assertEquals(matches("**/foo", "x/foo"), true)
    assertEquals(matches("**/foo", "x/bar"), false)
    assertEquals(matches("**/foo", "x/y/z/foo"), true)
    assertEquals(matches("**/foo", "x/y/z/bar"), false)
    assertEquals(matches("*/", "x"), true)
    assertEquals(matches("*/", "foo"), false)
    assertEquals(matches("/*/", "x"), true)
    assertEquals(matches("/*/", "foo"), false)
    assertEquals(matches("/*/", "x/y"), false)
  }
}
