# lite-romaji

>  A romaji-kana bi-directional transliterator.

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
