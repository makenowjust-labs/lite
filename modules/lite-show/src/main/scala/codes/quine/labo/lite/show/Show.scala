package codes.quine.labo.lite.show

/** Show is a frontend to show any values. */
class Show private (conv: Conv.Rec, width: Int, indentSize: Int) {

  /** Shows any values. */
  def show(v: Any): String = Frag.render(conv(v), width, indentSize)
}

object Show extends Show(Conv.default().toFunction, 80, 2) {

  /** Builds a new Show instance on the given configuration. */
  def apply(conv: Conv = Conv.default(), width: Int = 80, indentSize: Int = 2): Show =
    new Show(conv.toFunction, width, indentSize)
}
