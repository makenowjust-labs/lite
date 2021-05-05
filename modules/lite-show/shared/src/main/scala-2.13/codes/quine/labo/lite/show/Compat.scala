package codes.quine.labo.lite.show

import scala.language.reflectiveCalls

private object Compat {

  /** Extracts a `toString` prefix of the given collection. */
  def stringPrefix(i: Iterable[_]): String =
    i.asInstanceOf[{ def collectionClassName: String }].collectionClassName
}
