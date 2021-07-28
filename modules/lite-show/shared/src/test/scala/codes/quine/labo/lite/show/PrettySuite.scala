package codes.quine.labo.lite.show

import codes.quine.labo.lite.show.Pretty._

class PrettySuite extends munit.FunSuite {
  test("Pretty#toCompact") {
    assertEquals(Line.toCompact, Seq(Lit(" ")))
    assertEquals(Break.toCompact, Seq.empty)
    assertEquals(Lit("x").toCompact, Seq(Lit("x")))
    assertEquals(Wide("x").toCompact, Seq.empty)
    assertEquals(Indent(Seq(Lit("x"))).toCompact, Seq(Lit("x")))
    assertEquals(Group(Seq(Lit("x"))).toCompact, Seq(Lit("x")))
  }

  test("Pretty.render") {
    assertEquals(Pretty.render(Seq(Lit("x"))), "x")
    assertEquals(Pretty.render(Seq(Lit("x"), Line, Lit("y"))), "x\ny")
    assertEquals(Pretty.render(Seq(Lit("x"), Indent(Seq(Line, Lit("y"))))), "x\n  y")
    assertEquals(Pretty.render(Seq(Lit("x"), Indent(Seq(Line, Lit("y")))), indentSize = 3), "x\n   y")
    assertEquals(Pretty.render(Seq(Group(Seq(Lit("xxx"), Line, Lit("yyy"))))), "xxx yyy")
    assertEquals(Pretty.render(Seq(Group(Seq(Lit("xxx"), Break, Lit("yyy"))))), "xxxyyy")
    assertEquals(Pretty.render(Seq(Group(Seq(Lit("xxx"), Line, Lit("yyy")))), width = 2), "xxx\nyyy")
    assertEquals(Pretty.render(Seq(Group(Seq(Lit("xxx"), Break, Lit("yyy")))), width = 2), "xxx\nyyy")
    assertEquals(Pretty.render(Seq(Group(Seq(Wide("xxx"), Break, Lit("yyy"))))), "yyy")
    assertEquals(Pretty.render(Seq(Group(Seq(Wide("xxx"), Break, Lit("yyy")))), width = 2), "xxx\nyyy")
    assertEquals(Pretty.render(Seq(Group(Seq(Lit("xxx"))), Line)), "xxx\n")
    assertEquals(Pretty.render(Seq(Group(Seq(Lit("xxx"))), Break)), "xxx\n")
    assertEquals(Pretty.render(Seq(Group(Seq(Lit("xxx"))), Wide("yyy"))), "xxxyyy")
    assertEquals(Pretty.render(Seq(Group(Seq(Lit("xxx"))), Indent(Seq(Line)))), "xxx\n  ")
    assertEquals(Pretty.render(Seq(Group(Seq(Lit("xxx"))), Group(Seq(Line)))), "xxx ")
  }
}
