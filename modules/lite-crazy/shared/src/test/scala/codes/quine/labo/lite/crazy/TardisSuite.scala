package codes.quine.labo.lite.crazy

class TardisSuite extends munit.FunSuite {
  // https://blog.csongor.co.uk/time-travel-in-haskell-for-dummies/
  test("Tardis: single-pass assembler") {
    import codes.quine.labo.lite.crazy.implicits._

    type Addr = Int
    type SymTable = Map[String, Addr]

    sealed abstract class Instr extends Product with Serializable
    case object Add extends Instr
    case object Mov extends Instr
    final case class ToLabel(label: String) extends Instr
    final case class ToAddr(addr: Addr) extends Instr
    final case class Label(label: String) extends Instr
    case object Err extends Instr

    type Assembler[A] = Tardis[SymTable, SymTable, A]

    def assemble(addr: Addr, is0: List[Instr]): Assembler[List[(Addr, Instr)]] =
      is0 match {
        case Nil => Tardis.pure(List.empty)
        case Label(label) :: is1 =>
          for {
            _ <- Tardis.modifyBackward[SymTable, SymTable](_.updated(label, addr))
            _ <- Tardis.modifyForward[SymTable, SymTable](_.updated(label, addr))
            is2 <- assemble(addr, is1)
          } yield is2
        case ToLabel(label) :: is1 =>
          for {
            bw <- Tardis.getBackward[SymTable, SymTable]
            fw <- Tardis.getForward[SymTable, SymTable]
            is2 <- assemble(addr + 1, is1)
          } yield Lazy { // Here's `Lazy` is necessary.
            val union = bw ++ fw
            val i = union.get(label) match {
              case Some(a) => (addr, ToAddr(a))
              case None    => (addr, Err)
            }
            i :: is2
          }
        case i :: is1 =>
          for {
            rest <- assemble(addr + 1, is1)
          } yield (addr, i) :: rest
      }

    val input = List(
      Add,
      Add,
      ToLabel("my_label"),
      Mov,
      Mov,
      Label("my_label"),
      Label("second_label"),
      Mov,
      ToLabel("second_label"),
      Mov
    )

    val (lb, lf, lis) = assemble(0, input).run(Map.empty[String, Int], Map.empty[String, Int])
    assertEquals(lb.value, Map("my_label" -> 5, "second_label" -> 5))
    assertEquals(lf.value, Map("my_label" -> 5, "second_label" -> 5))
    assertEquals(
      lis.value,
      List(
        (0, Add),
        (1, Add),
        (2, ToAddr(5)),
        (3, Mov),
        (4, Mov),
        (5, Mov),
        (6, ToAddr(5)),
        (7, Mov)
      )
    )
  }

  test("Tardis.putBackward") {
    val program: Tardis[Int, Int, (Int, Int, Int)] = for {
      s1 <- Tardis.getBackward[Int, Int]
      _ <- Tardis.putBackward[Int, Int](Lazy(1))
      s2 <- Tardis.getBackward[Int, Int]
      _ <- Tardis.putBackward[Int, Int](Lazy(2))
      s3 <- Tardis.getBackward[Int, Int]
    } yield Lazy((s1.value, s2.value, s3.value))
    val (_, _, ls123) = program.run(Lazy(3), Lazy(0))
    assertEquals(ls123.value, (1, 2, 3))
  }

  test("Tardis.putForward") {
    val program: Tardis[Int, Int, (Int, Int, Int)] = for {
      s1 <- Tardis.getForward[Int, Int]
      _ <- Tardis.putForward[Int, Int](Lazy(2))
      s2 <- Tardis.getForward[Int, Int]
      _ <- Tardis.putForward[Int, Int](Lazy(3))
      s3 <- Tardis.getForward[Int, Int]
    } yield Lazy((s1.value, s2.value, s3.value))
    val (_, _, ls123) = program.run(Lazy(0), Lazy(1))
    assertEquals(ls123.value, (1, 2, 3))
  }
}
