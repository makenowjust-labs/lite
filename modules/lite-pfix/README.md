# lite-pfix

> A partially defined fixpoint combinator.

[![Maven Central](https://img.shields.io/maven-central/v/codes.quine.labo/lite-pfix_2.13?logo=scala&style=for-the-badge)](https://search.maven.org/artifact/codes.quine.labo/lite-pfix_2.13)

## Install

Insert the following to your `build.sbt`.

```sbt
libraryDependencies += "codes.quine.labo" %% "lite-pfix" % "<latest version>"
```

## Usage

`PFix` is a partially defined fixpoint combinator.

Using this library, we can build a recursive function combining with multiple partial functions. 
The following is FizzBuzz example:

```scala
import codes.quine.labo.pfix.PFix

val fizzbuzz = PFix[Int, List[String]](rec => { case n if n > 0 && n % 15 == 0 => rec(n - 1) ++ List("FizzBuzz") })
val fizz = PFix[Int, List[String]](rec => { case n if n > 0 && n % 3 == 0 => rec(n - 1) ++ List("Fizz") })
val buzz = PFix[Int, List[String]](rec => { case n if n > 0 && n % 5 == 0 => rec(n - 1) ++ List("Buzz") })
val other = PFix[Int, List[String]](rec => { case n if n > 0 => rec(n - 1) ++ List(n.toString) })

val f = fizzbuzz.orElse(fizz).orElse(buzz).orElse(other).toFunction(_ => List.empty)
f(15)
// List("1", "2", "Fizz", "4", "Buzz", "Fizz", "7", "8", "Fizz", "Buzz", "11", "Fizz", "13", "14", "FizzBuzz")
```
