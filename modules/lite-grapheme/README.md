# lite-grapheme

> Iterates the given string on each grapheme cluster.

[![Maven Central](https://img.shields.io/maven-central/v/codes.quine.labo/lite-grapheme_2.13?logo=scala&style=for-the-badge)](https://search.maven.org/artifact/codes.quine.labo/lite-grapheme_2.13)


## Install

Insert the following to your `build.sbt`.

```sbt
libraryDependencies += "codes.quine.labo" %% "lite-grapheme" % "<latest version>"
```

## Usage

`Grapheme.iterate` returns an iterator to iterate grapheme clusters of the string.

```scala
import codes.quine.labo.lite.grapheme.Grapheme

Grapheme.iterate("👨‍👨‍👧‍👦👩‍👩‍👧‍👦👨‍👨‍👧‍👦").toList
// res0: List[Grapheme] = List(Grapheme("👨‍👨‍👧‍👦"), Grapheme("👩‍👩‍👧‍👦"), Grapheme("👨‍👨‍👧‍👦"))
```
