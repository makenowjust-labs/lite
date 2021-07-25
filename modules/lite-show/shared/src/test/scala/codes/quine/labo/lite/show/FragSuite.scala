package codes.quine.labo.lite.show

import codes.quine.labo.lite.show.Frag._

class FragSuite extends munit.FunSuite {
  test("Frag#toCompact") {
    assertEquals(Line.toCompact, List(Lit(" ")))
    assertEquals(Break.toCompact, List.empty)
    assertEquals(Lit("x").toCompact, List(Lit("x")))
    assertEquals(Wide("x").toCompact, List.empty)
    assertEquals(Indent(List(Lit("x"))).toCompact, List(Lit("x")))
    assertEquals(Group(List(Lit("x"))).toCompact, List(Lit("x")))
  }

  test("Frag.render") {
    assertEquals(Frag.render(List(Lit("x"))), "x")
    assertEquals(Frag.render(List(Lit("x", true))), "x")
    assertEquals(Frag.render(List(Lit("x"), Line, Lit("y"))), "x\ny")
    assertEquals(Frag.render(List(Lit("x"), Indent(List(Line, Lit("y"))))), "x\n  y")
    assertEquals(Frag.render(List(Lit("x"), Indent(List(Line, Lit("y")))), indentSize = 3), "x\n   y")
    assertEquals(Frag.render(List(Group(List(Lit("xxx"), Line, Lit("yyy"))))), "xxx yyy")
    assertEquals(Frag.render(List(Group(List(Lit("xxx"), Break, Lit("yyy"))))), "xxxyyy")
    assertEquals(Frag.render(List(Group(List(Lit("xxx"), Line, Lit("yyy")))), width = 2), "xxx\nyyy")
    assertEquals(Frag.render(List(Group(List(Lit("xxx"), Break, Lit("yyy")))), width = 2), "xxx\nyyy")
    assertEquals(Frag.render(List(Group(List(Lit("xx", true), Break, Lit("y")))), width = 2), "xxy")
    assertEquals(Frag.render(List(Group(List(Wide("xxx"), Break, Lit("yyy"))))), "yyy")
    assertEquals(Frag.render(List(Group(List(Wide("xxx"), Break, Lit("yyy")))), width = 2), "xxx\nyyy")
    assertEquals(Frag.render(List(Group(List(Lit("xxx"))), Line)), "xxx\n")
    assertEquals(Frag.render(List(Group(List(Lit("xxx"))), Break)), "xxx\n")
    assertEquals(Frag.render(List(Group(List(Lit("xxx"))), Wide("yyy"))), "xxxyyy")
    assertEquals(Frag.render(List(Group(List(Lit("xxx"))), Indent(List(Line)))), "xxx\n  ")
    assertEquals(Frag.render(List(Group(List(Lit("xxx"))), Group(List(Line)))), "xxx ")
  }
}
