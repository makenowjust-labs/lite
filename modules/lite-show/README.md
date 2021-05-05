# lite-show

> A small pretty-print library.

[![Maven Central](https://img.shields.io/maven-central/v/codes.quine.labo/lite-show_2.13?logo=scala&style=for-the-badge)](https://search.maven.org/artifact/codes.quine.labo/lite-show_2.13)

## Usage

`Show.show` prints the given value as pretty format.

```scala
import codes.quine.labo.lite.show.Show

println(Show.show("Hello World"))
// "Hello World"

println(Show.show(List.range(0, 10)))
// List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)

println(Show.show(List.range(0, 50).map(n => Map(n -> List.range(1, n / 2 + 1)))))
// List(
//   Map(0 -> List()),
//   Map(1 -> List()),
//   Map(2 -> List(1)),
//   Map(3 -> List(1)),
//   Map(4 -> List(1, 2)),
//   Map(5 -> List(1, 2)),
//   Map(6 -> List(1, 2, 3)),
//   Map(7 -> List(1, 2, 3)),
//   Map(8 -> List(1, 2, 3, 4)),
//   Map(9 -> List(1, 2, 3, 4)),
//   Map(10 -> List(1, 2, 3, 4, 5)),
//   Map(11 -> List(1, 2, 3, 4, 5)),
//   Map(12 -> List(1, 2, 3, 4, 5, 6)),
//   Map(13 -> List(1, 2, 3, 4, 5, 6)),
//   Map(14 -> List(1, 2, 3, 4, 5, 6, 7)),
//   Map(15 -> List(1, 2, 3, 4, 5, 6, 7)),
//   Map(16 -> List(1, 2, 3, 4, 5, 6, 7, 8)),
//   Map(17 -> List(1, 2, 3, 4, 5, 6, 7, 8)),
//   Map(18 -> List(1, 2, 3, 4, 5, 6, 7, 8, 9)),
//   Map(19 -> List(1, 2, 3, 4, 5, 6, 7, 8, 9)),
//   Map(20 -> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
//   Map(21 -> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
//   Map(22 -> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)),
//   Map(23 -> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)),
//   Map(24 -> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)),
//   Map(25 -> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)),
//   Map(26 -> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)),
//   Map(27 -> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)),
//   Map(28 -> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)),
//   Map(29 -> List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)),
//   ...
// )
```

By creating `Show` instance manually it can customize `show` method output.

```scala
val MyShow = Show(indentSize = 4, width = 20)

println(MyShow.show(List.range(0, 10)))
// List(
//     0,
//     1,
//     2,
//     3,
//     4,
//     5,
//     6,
//     7,
//     8,
//     9
// )
```
