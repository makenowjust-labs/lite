# lite-gimei

> A generator of Japanese dummy names and addresses with furigana.

It is also a port of Ruby's [`gimei`](https://github.com/willnet/gimei) library.

## Usage

`Gimei` is the frontend to generate a Japanese dummy data.

```scala
import codes.quine.labo.lite.gimei.Gimei
```

`Gimei.name()` generates a dummy name.

```scala
val name = Gimei.name()

name.toKanji
// res0: String = "藤村 史帆"
name.toHiragana
// res1: String = "ふじむら しほ"
name.toKatakana
// res2: String = "フジムラ シホ"
name.toRomaji
// res3: String => "Shiho Fujimura"
```

`Gimei.name()` result is `Name` object.
It consists some components.

```scala
name.gender
// res4: Gender = Female
name.firstName.toKanji
// res5: String = "史帆"
name.lastName.toKanji
// res6: String = "藤村"
```

`Gimei.address()` generates a dummy address.

```scala
val address = Gimei.address()

address.toKanji
// res7: String = "青森県南陽市温泉"
address.toHiragana
// res8: String = "あおもりけんなんようしおんせん"
address.toKatakana
// res9: String = "アオモリケンナンヨウシオンセン"
address.toRomaji
// res10: String = "Aomoriken Nanyoushi Onsen"
```

`Gimei.address()` result is `Address` object.
It  consists some components.

```scala
address.prefecture.toKanji
// res11: String = "青森県"
address.city.toKanji
// res12: String = "南陽市"
address.town.toKanji
// res13: String = "温泉"
```

## License

The original dictionary YAML file is generated from [naist-jdic](https://ja.osdn.net/projects/naist-jdic/).
