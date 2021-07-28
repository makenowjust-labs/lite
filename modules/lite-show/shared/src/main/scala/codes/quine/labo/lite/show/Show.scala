package codes.quine.labo.lite.show

import codes.quine.labo.lite.show.Prettify.GenOps

/** Show is a frontend to show any values. */
class Show private (prettify: Prettify, width: Int, indentSize: Int) {

  /** Shows any values. */
  def show(v: Any): String = Pretty.render(prettify(v), width, indentSize)
}

object Show extends Show(Prettify.default().toPrettify, 80, 2) {

  /** Builds a new Show instance on the given configuration. */
  def apply(prettify: Prettify = Prettify.default().toPrettify, width: Int = 80, indentSize: Int = 2): Show =
    new Show(prettify, width, indentSize)
}
