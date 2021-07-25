package codes.quine.labo.lite.delta

/** DeltaDiff is a function to computes a delta object between two values. */
trait DeltaDiff[T] {
  def apply(left: T, right: T): Delta

  def narrow[S <: T]: DeltaDiff[S] = this.asInstanceOf[DeltaDiff[S]]
}

object DeltaDiff extends DeltaDiffInstances0 {

  /** Summons the instance. */
  @inline def apply[T](implicit diff: DeltaDiff[T]): DeltaDiff[T] = diff

  /** Computes a delta object between two values. */
  def diff[T](left: T, right: T)(implicit diff: DeltaDiff[T]): Delta = diff(left, right)

  /** Returns a diff function for sequences. */
  def seq[T](name: String)(implicit keyOf: KeyOf[Entry[Int, T]], diff: DeltaDiff[T]): DeltaDiff[Seq[T]] =
    (left: Seq[T], right: Seq[T]) => {
      val leftEntries = left.zipWithIndex.map { case (value, index) => Entry(index, value) }
      val rightEntries = right.zipWithIndex.map { case (value, index) => Entry(index, value) }

      val leftMap = leftEntries.groupBy(keyOf(_)).withDefaultValue(Seq.empty)
      val rightMap = rightEntries.groupBy(keyOf(_)).withDefaultValue(Seq.empty)
      val keys = leftMap.keySet ++ rightMap.keySet

      val builder = Seq.newBuilder[Entry[Delta.Value[Int], Delta]]
      def addEntries(ls: Seq[Entry[Int, T]], rs: Seq[Entry[Int, T]]): Unit =
        (ls.headOption, rs.headOption) match {
          case (Some(l), Some(r)) =>
            val key =
              if (l.key == r.key) Delta.Identical(l.key)
              else Delta.Changed(l.key, r.key)
            val value = diff(l.value, r.value)
            builder.addOne(Entry(key, value))
            addEntries(ls.tail, rs.tail)
          case (Some(l), None) =>
            val key = Delta.Additional(l.key)
            val value = Delta.Additional(l.value)
            builder.addOne(Entry(key, value))
            addEntries(ls.tail, rs)
          case (None, Some(r)) =>
            val key = Delta.Missing(r.key)
            val value = Delta.Missing(r.value)
            builder.addOne(Entry(key, value))
            addEntries(ls, rs.tail)
          case (None, None) => ()
        }
      for (key <- keys) addEntries(leftMap(key), rightMap(key))

      val fields = builder
        .result()
        .sortBy(_.key match {
          case Delta.Identical(v)  => (v, v)
          case Delta.Changed(l, r) => (r, l)
          case Delta.Missing(r)    => (r, Int.MaxValue)
          case Delta.Additional(l) => (Int.MaxValue, l)
        })
        .asInstanceOf[Seq[Entry[Delta, Delta]]]
      Delta.Map(name, fields, ": ")
    }

  /** Returns a diff function for mappings. */
  def map[K, V](name: String)(implicit
      keyOf: KeyOf[Entry[K, V]],
      diffK: DeltaDiff[K],
      diffV: DeltaDiff[V]
  ): DeltaDiff[Map[K, V]] = (left: Map[K, V], right: Map[K, V]) => {
    val leftEntries = left.map { case (key, value) => Entry(key, value) }.toSeq
    val rightEntries = right.map { case (key, value) => Entry(key, value) }.toSeq

    val leftMap = leftEntries.groupBy(keyOf(_)).withDefaultValue(Seq.empty)
    val rightMap = rightEntries.groupBy(keyOf(_)).withDefaultValue(Seq.empty)
    val keys = leftMap.keySet ++ rightMap.keySet

    val builder = Seq.newBuilder[Entry[Delta, Delta]]
    def addEntries(ls: Seq[Entry[K, V]], rs: Seq[Entry[K, V]]): Unit =
      (ls.headOption, rs.headOption) match {
        case (Some(l), Some(r)) =>
          val key = diffK(l.key, r.key)
          val value = diffV(l.value, r.value)
          builder.addOne(Entry(key, value))
          addEntries(ls.tail, rs.tail)
        case (Some(l), None) =>
          val key = Delta.Additional(l.key)
          val value = Delta.Additional(l.value)
          builder.addOne(Entry(key, value))
          addEntries(ls.tail, rs)
        case (None, Some(r)) =>
          val key = Delta.Missing(r.key)
          val value = Delta.Missing(r.value)
          builder.addOne(Entry(key, value))
          addEntries(ls, rs.tail)
        case (None, None) => ()
      }
    for (key <- keys) addEntries(leftMap(key), rightMap(key))

    val fields = builder.result()
    Delta.Map(name, fields, " -> ")
  }

  /** Returns a diff function for sets. */
  def set[T](name: String)(implicit keyOf: KeyOf[T], diff: DeltaDiff[T]): DeltaDiff[Set[T]] =
    (left: Set[T], right: Set[T]) => {
      val leftMap = left.toSeq.groupBy(keyOf(_)).withDefaultValue(Seq.empty)
      val rightMap = right.toSeq.groupBy(keyOf(_)).withDefaultValue(Seq.empty)
      val keys = leftMap.keySet ++ rightMap.keySet

      val builder = Seq.newBuilder[Delta]
      def addDelta(ls: Seq[T], rs: Seq[T]): Unit =
        (ls.headOption, rs.headOption) match {
          case (Some(l), Some(r)) =>
            builder.addOne(diff(l, r))
            addDelta(ls.tail, rs.tail)
          case (Some(l), None) =>
            builder.addOne(Delta.Additional(l))
            addDelta(ls.tail, rs)
          case (None, Some(r)) =>
            builder.addOne(Delta.Missing(r))
            addDelta(ls, rs.tail)
          case (None, None) => ()
        }
      for (key <- keys) addDelta(leftMap(key), rightMap(key))

      val deltas = builder.result()
      Delta.Set(name, deltas)
    }

  /** Returns a diff function based on Scala default equality. */
  def default[T]: DeltaDiff[T] = (left: T, right: T) =>
    if (left == right) Delta.Identical(left)
    else Delta.Changed(left, right)
}

trait DeltaDiffInstances0 {
  implicit def diffInstanceForOption[T](implicit diff: DeltaDiff[T]): DeltaDiff[Option[T]] = {
    case (Some(l), Some(r)) => Delta.Case("Option", Seq(Entry("value", diff(l, r))))
    case (None, None)       => Delta.Identical(None)
    case (l, r)             => Delta.Changed(l, r)
  }

  implicit def diffInstanceForEither[A, B](implicit
      diffA: DeltaDiff[A],
      diffB: DeltaDiff[B]
  ): DeltaDiff[Either[A, B]] = {
    case (Left(l), Left(r))   => Delta.Case("Left", Seq(Entry("value", diffA(l, r))))
    case (Right(l), Right(r)) => Delta.Case("Right", Seq(Entry("value", diffB(l, r))))
    case (l, r)               => Delta.Changed(l, r)
  }

  implicit def diffInstanceForSeq[T](implicit keyOf: KeyOf[Entry[Int, T]], diff: DeltaDiff[T]): DeltaDiff[Seq[T]] =
    DeltaDiff.seq("Seq")

  implicit def diffInstanceForList[T](implicit keyOf: KeyOf[Entry[Int, T]], diff: DeltaDiff[T]): DeltaDiff[List[T]] =
    DeltaDiff.seq("List").narrow[List[T]]

  implicit def diffInstanceForVector[T](implicit
      keyOf: KeyOf[Entry[Int, T]],
      diff: DeltaDiff[T]
  ): DeltaDiff[Vector[T]] = DeltaDiff.seq("Vector").narrow[Vector[T]]

  implicit def diffInstanceForMap[K, V](implicit
      keyOf: KeyOf[Entry[K, V]],
      diffK: DeltaDiff[K],
      diffV: DeltaDiff[V]
  ): DeltaDiff[Map[K, V]] = DeltaDiff.map("Map")

  implicit def diffInstanceForSet[T](implicit keyOf: KeyOf[T], diff: DeltaDiff[T]): DeltaDiff[Set[T]] =
    DeltaDiff.set("Set")

  implicit def diffInstanceForUnit: DeltaDiff[Unit] = DeltaDiff.default[Unit]

  implicit def diffInstanceForTuple2[A, B](implicit diffA: DeltaDiff[A], diffB: DeltaDiff[B]): DeltaDiff[(A, B)] = {
    case ((l1, l2), (r1, r2)) => Delta.Case("", Seq(Entry("_1", diffA(l1, r1)), Entry("_2", diffB(l2, r2))))
  }

  implicit def diffInstanceForTuple3[A, B, C](implicit
      diffA: DeltaDiff[A],
      diffB: DeltaDiff[B],
      diffC: DeltaDiff[C]
  ): DeltaDiff[(A, B, C)] = { case ((l1, l2, l3), (r1, r2, r3)) =>
    Delta.Case("", Seq(Entry("_1", diffA(l1, r1)), Entry("_2", diffB(l2, r2)), Entry("_3", diffC(l3, r3))))
  }

  implicit def diffInstanceForTuple4[A, B, C, D](implicit
      diffA: DeltaDiff[A],
      diffB: DeltaDiff[B],
      diffC: DeltaDiff[C],
      diffD: DeltaDiff[D]
  ): DeltaDiff[(A, B, C, D)] = { case ((l1, l2, l3, l4), (r1, r2, r3, r4)) =>
    Delta.Case(
      "",
      Seq(
        Entry("_1", diffA(l1, r1)),
        Entry("_2", diffB(l2, r2)),
        Entry("_3", diffC(l3, r3)),
        Entry("_4", diffD(l4, r4))
      )
    )
  }

  implicit def diffInstanceForString: DeltaDiff[String] = DeltaDiff.default[String]

  implicit def diffInstanceForBoolean: DeltaDiff[Boolean] = DeltaDiff.default[Boolean]

  implicit def diffInstanceForByte: DeltaDiff[Byte] = DeltaDiff.default[Byte]
  implicit def diffInstanceForShort: DeltaDiff[Short] = DeltaDiff.default[Short]
  implicit def diffInstanceForInt: DeltaDiff[Int] = DeltaDiff.default[Int]
  implicit def diffInstanceForLong: DeltaDiff[Long] = DeltaDiff.default[Long]
}
