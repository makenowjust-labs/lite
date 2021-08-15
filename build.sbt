// Versions:

val scala2Version = "2.13.6"
val scala3Version = "3.0.1"

val munitVersion = "0.7.28"

val organizeImportsVersion = "0.5.0"
val scaluzziVersion = "0.1.18"

// Global settings:

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / organization := "codes.quine.labo"
ThisBuild / homepage := Some(url("https://github.com/MakeNowJust-Labo/lite"))
ThisBuild / licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))
ThisBuild / developers := List(
  Developer(
    "MakeNowJust",
    "TSUYUSATO Kitsune",
    "make.just.on@gmail.com",
    url("https://quine.codes/")
  )
)

ThisBuild / scalaVersion := scala2Version
ThisBuild / crossScalaVersions := Seq(scala3Version, scala2Version)
ThisBuild / scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-deprecation",
  "-Wunused"
)

// Scalafix config:

ThisBuild / scalafixScalaBinaryVersion := "2.13"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % organizeImportsVersion
ThisBuild / scalafixDependencies += "com.github.vovapolu" %% "scaluzzi" % scaluzziVersion

// Global project:

lazy val lite = project
  .in(file("."))
  .settings(commonGlobalSettings)
  .aggregate(rootJVM, rootJS, rootNative)

lazy val root = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(commonGlobalSettings)
  .aggregate(crazy, delta, gestalt, gimei, gitignore, grapheme, parser, pfix, romaji, show)

lazy val rootJVM = root.jvm
lazy val rootJS = root.js
lazy val rootNative = root.native

// Common settings:

lazy val commonGlobalSettings = Seq(
  sonatypeProfileName := "codes.quine",
  publish / skip := true,
  coverageEnabled := false
)

lazy val commonSettings = Seq(
  Compile / console / scalacOptions -= "-Wunused",
  Test / console / scalacOptions -= "-Wunused"
)

lazy val commonJSSettings = Seq(Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) })

lazy val commonNativeSettings = Seq(
  crossScalaVersions := Seq(scala2Version),
  coverageEnabled := false
)

lazy val useMunit = Seq(
  libraryDependencies += "org.scalameta" %%% "munit" % munitVersion % Test,
  testFrameworks += new TestFramework("munit.Framework")
)

// Modules:

lazy val crazy = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-crazy"))
  .settings(
    name := "lite-crazy",
    console / initialCommands += "import codes.quine.labo.lite.crazy._\n",
    commonSettings,
    useMunit
  )
  .jsSettings(commonJSSettings)
  .nativeSettings(commonNativeSettings)

lazy val crazyJVM = crazy.jvm
lazy val crazyJS = crazy.js
lazy val crazyNative = crazy.native

lazy val delta = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-delta"))
  .settings(
    name := "lite-delta",
    console / initialCommands += "import codes.quine.labo.lite.show._\n",
    console / initialCommands += "import codes.quine.labo.lite.show.Prettify.PrettifyGenOps\n",
    console / initialCommands += "\n",
    console / initialCommands += "import codes.quine.labo.lite.delta._\n",
    console / initialCommands += "import codes.quine.labo.lite.delta.Diff.DiffGenOps\n",
    console / initialCommands += "import codes.quine.labo.lite.delta.Key.KeyGenOps\n",
    commonSettings,
    useMunit
  )
  .jsSettings(commonJSSettings)
  .nativeSettings(commonNativeSettings)
  .dependsOn(gestalt, pfix, show)

lazy val deltaJVM = delta.jvm
lazy val deltaJS = delta.js
lazy val deltaNative = delta.native

lazy val gestalt = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-gestalt"))
  .settings(
    name := "lite-gestalt",
    console / initialCommands += "import codes.quine.labo.lite.gestalt._\n",
    commonSettings,
    useMunit
  )
  .jsSettings(commonJSSettings)
  .nativeSettings(commonNativeSettings)

lazy val gestaltJVM = gestalt.jvm
lazy val gestaltJS = gestalt.js
lazy val gestaltNative = gestalt.native

lazy val gimei = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-gimei"))
  .settings(
    name := "lite-gimei",
    console / initialCommands += "import codes.quine.labo.lite.gimei._\n",
    commonSettings,
    useMunit,
    coverageExcludedPackages := "<empty>;codes\\.quine\\.labo\\.lite\\.gimei\\.Data.*",
    useGimeiDataGenerator
  )
  .jsSettings(commonJSSettings)
  .nativeSettings(commonNativeSettings)
  .dependsOn(romaji)

lazy val gimeiJVM = gimei.jvm
lazy val gimeiJS = gimei.js
lazy val gimeiNative = gimei.native

lazy val useGimeiDataGenerator = {
  val generateData = taskKey[Seq[File]]("Generate data from YAML")
  Seq(
    Compile / sourceGenerators += generateData.taskValue,
    generateData / fileInputs += baseDirectory.value.toGlob / ".." / "data" / "*.yml",
    generateData / fileInputs += baseDirectory.value.toGlob / ".." / "scripts" / "gen.rb",
    generateData := {
      import scala.sys.process.Process
      val file = (Compile / sourceManaged).value / "codes" / "quine" / "labo" / "lite" / "gimei" / "Data.scala"
      val source = Process(Seq("ruby", (baseDirectory.value / ".." / "scripts" / "gen.rb").absolutePath)).!!
      IO.write(file, source)
      Seq(file)
    }
  )
}

lazy val gitignore = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-gitignore"))
  .settings(
    name := "lite-gitignore",
    console / initialCommands += "import java.nio.file.Files\n",
    console / initialCommands += "import java.nio.file.Path\n",
    console / initialCommands += "import java.nio.file.Paths\n",
    console / initialCommands += "\n",
    console / initialCommands += "import codes.quine.labo.lite.gitignore._\n",
    commonSettings,
    useMunit
  )
  .jsSettings(commonJSSettings)
  .nativeSettings(commonNativeSettings)
  .dependsOn(parser)

lazy val gitignoreJVM = gitignore.jvm
lazy val gitignoreJS = gitignore.js
lazy val gitignoreNative = gitignore.native

lazy val grapheme = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-grapheme"))
  .settings(
    name := "lite-grapheme",
    console / initialCommands += "import codes.quine.labo.lite.grapheme._\n",
    commonSettings,
    useMunit,
    coverageExcludedPackages := "<empty>;codes\\.quine\\.labo\\.lite\\.grapheme\\.Data.*",
    useGraphemeDataGenerator,
    useGraphemeTestGenerator
  )
  .jsSettings(commonJSSettings)
  .nativeSettings(commonNativeSettings)

lazy val graphemeJVM = grapheme.jvm
lazy val graphemeJS = grapheme.js
lazy val graphemeNative = grapheme.native

lazy val useGraphemeDataGenerator = {
  val generateData = taskKey[Seq[File]]("Generate data from UCD text")
  Seq(
    Compile / sourceGenerators += generateData.taskValue,
    generateData / fileInputs += baseDirectory.value.toGlob / ".." / "data" / "*.txt",
    generateData / fileInputs += baseDirectory.value.toGlob / ".." / "scripts" / "gen.rb",
    generateData := {
      import scala.sys.process.Process
      val file = (Compile / sourceManaged).value / "codes" / "quine" / "labo" / "lite" / "grapheme" / "Data.scala"
      val source = Process(Seq("ruby", (baseDirectory.value / ".." / "scripts" / "gen.rb").absolutePath)).!!
      IO.write(file, source)
      Seq(file)
    }
  )
}

lazy val useGraphemeTestGenerator = {
  val generateTest = taskKey[Seq[File]]("Generate test from UCD text")
  Seq(
    Test / sourceGenerators += generateTest.taskValue,
    generateTest / fileInputs += baseDirectory.value.toGlob / ".." / "data" / "*.txt",
    generateTest / fileInputs += baseDirectory.value.toGlob / ".." / "scripts" / "gen-test.rb",
    generateTest := {
      import scala.sys.process.Process
      val file =
        (Compile / sourceManaged).value / "codes" / "quine" / "labo" / "lite" / "grapheme" / "GraphemeBreakTestSuite.scala"
      val source = Process(Seq("ruby", (baseDirectory.value / ".." / "scripts" / "gen-test.rb").absolutePath)).!!
      IO.write(file, source)
      Seq(file)
    }
  )
}

lazy val parser = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-parser"))
  .settings(
    name := "lite-parser",
    console / initialCommands += "import codes.quine.labo.lite.parser._\n",
    commonSettings,
    useMunit
  )
  .jsSettings(commonJSSettings)
  .nativeSettings(commonNativeSettings)

lazy val parserJVM = parser.jvm
lazy val parserJS = parser.js
lazy val parserNative = parser.native

lazy val pfix = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-pfix"))
  .settings(
    name := "lite-pfix",
    console / initialCommands += "import codes.quine.labo.lite.pfix._\n",
    commonSettings,
    useMunit
  )
  .jsSettings(commonJSSettings)
  .nativeSettings(commonNativeSettings)

lazy val pfixJVM = pfix.jvm
lazy val pfixJS = pfix.js
lazy val pfixNative = pfix.native

lazy val romaji = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-romaji"))
  .settings(
    name := "lite-romaji",
    console / initialCommands += "import codes.quine.labo.lite.romaji._\n",
    commonSettings,
    useMunit
  )
  .jsSettings(commonJSSettings)
  .nativeSettings(commonNativeSettings)

lazy val romajiJVM = romaji.jvm
lazy val romajiJS = romaji.js
lazy val romajiNative = romaji.native

lazy val show = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-show"))
  .settings(
    name := "lite-show",
    console / initialCommands += "import codes.quine.labo.lite.show._\n",
    console / initialCommands += "import codes.quine.labo.lite.show.Prettify.PrettifyGenOps\n",
    commonSettings,
    useMunit
  )
  .jsSettings(commonJSSettings)
  .nativeSettings(commonNativeSettings)
  .dependsOn(pfix)

lazy val showJVM = show.jvm
lazy val showJS = show.js
lazy val showNative = show.native
