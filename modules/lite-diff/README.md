# lite-diff

> Computes a diff between two sequences.

## Install

Insert the following to your `build.sbt`.

```sbt
libraryDependencies += "codes.quine.labo" %% "lite-diff" % "0.3.0"
```

## Usage

`Diff.diff(seq1, seq2)` computes a diff between `seq1` and `seq2` by using [Gestalt Pattern Matching](https://en.wikipedia.org/wiki/Gestalt_Pattern_Matching) algorithm.
It returns a `Patch` object, and we can generate a unified diff format text from this patch.

Note that `Diff.diff` accepts any values whose equality and hash function is defined correctly.

```scala
import codes.quine.labo.lite.diff.Diff

val seq1 = Seq(1, 2, 3, 4, 5)
val seq2 = Seq(1, 2, 2, 5, 6)

val patch = Diff.diff(seq1, seq2)
patch.toUnified()
// res0: String =
// "@@ -1,5 +1,5
//  1
//  2
// -3
// -4
// +2
//  5
// +6
// "
```
