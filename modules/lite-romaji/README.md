# lite-romaji

>  A romaji-kana bi-directional transliterator.

[![Maven Central](https://img.shields.io/maven-central/v/codes.quine.labo/lite-romaji_2.13?logo=scala&style=for-the-badge)](https://search.maven.org/artifact/codes.quine.labo/lite-romaji_2.13)

## Install

Insert the following to your `build.sbt`.
`
```sbt
libraryDependencies += "codes.quine.labo" %% "lite-romaji" % "0.3.0"
```

## Usage

`Romaji.toKana` converts romaji text to katakana text.

```scala
import codes.quine.labo.lite.romaji.Romaji

Romaji.toKana("Indo Jin Wo Migi He")
// res0: String = "インド ジン ヲ ミギ ヘ"
```

`Kana.toRomaji` converts katakana text to romaji text.

```scala
import codes.quine.labo.lite.romaji.Kana

Kana.toRomaji("インド ジン ヲ ミギ ヘ")
// res1: String = "indo jin wo migi he"
```
