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

ThisBuild / scalaVersion := "2.13.6"
ThisBuild / crossScalaVersions := Seq("3.0.1", "2.13.6")
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
ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"
ThisBuild / scalafixDependencies += "com.github.vovapolu" %% "scaluzzi" % "0.1.18"

val crossProjectNames = Seq("crazy", "delta", "gestalt", "gimei", "grapheme", "pfix", "romaji", "show")
val platformSuffices = Seq("JVM", "JS", "Native")
platformSuffices.flatMap { platform =>
  addCommandAlias(s"test$platform", crossProjectNames.map(name => s"$name$platform/test").mkString("; "))
}

lazy val root = project
  .in(file("."))
  .settings(
    sonatypeProfileName := "codes.quine",
    publish / skip := true,
    coverageEnabled := false
  )
  .aggregate(crazyJVM, crazyJS, crazyNative)
  .aggregate(deltaJVM, deltaJS, deltaNative)
  .aggregate(gestaltJVM, gestaltJS, gestaltNative)
  .aggregate(gimeiJVM, gimeiJS, gimeiNative)
  .aggregate(graphemeJVM, graphemeJS, graphemeNative)
  .aggregate(romajiJVM, romajiJS, romajiNative)
  .aggregate(pfixJVM, pfixJS, pfixNative)
  .aggregate(showJVM, showJS, showNative)

lazy val crazy = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-crazy"))
  .settings(
    name := "lite-crazy",
    console / initialCommands :=
      """|import codes.quine.labo.lite.crazy._
         |""".stripMargin,
    Compile / console / scalacOptions -= "-Wunused",
    Test / console / scalacOptions -= "-Wunused",
    // Set URL mapping of scala standard API for Scaladoc.
    apiMappings ++= scalaInstance.value.libraryJars
      .filter(file => file.getName.startsWith("scala-library") && file.getName.endsWith(".jar"))
      .map(_ -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"))
      .toMap,
    // Settings for test:
    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.27" % Test,
    testFrameworks += new TestFramework("munit.Framework")
  )
  .jsSettings(Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) })
  .nativeSettings(
    crossScalaVersions := Seq("2.13.6"),
    coverageEnabled := false
  )

lazy val crazyJVM = crazy.jvm
lazy val crazyJS = crazy.js
lazy val crazyNative = crazy.native

lazy val delta = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-delta"))
  .settings(
    name := "lite-delta",
    console / initialCommands :=
      """|import codes.quine.labo.lite.show._
         |import codes.quine.labo.lite.show.Prettify.PrettifyGenOps
         |
         |import codes.quine.labo.lite.delta._
         |import codes.quine.labo.lite.delta.Diff.DiffGenOps
         |import codes.quine.labo.lite.delta.Key.KeyGenOps
         |""".stripMargin,
    Compile / console / scalacOptions -= "-Wunused",
    Test / console / scalacOptions -= "-Wunused",
    // Set URL mapping of scala standard API for Scaladoc.
    apiMappings ++= scalaInstance.value.libraryJars
      .filter(file => file.getName.startsWith("scala-library") && file.getName.endsWith(".jar"))
      .map(_ -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"))
      .toMap,
    // Settings for test:
    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.27" % Test,
    testFrameworks += new TestFramework("munit.Framework")
  )
  .jsSettings(Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) })
  .nativeSettings(
    crossScalaVersions := Seq("2.13.6"),
    coverageEnabled := false
  )
  .dependsOn(gestalt, pfix, show)

lazy val deltaJVM = delta.jvm
lazy val deltaJS = delta.js
lazy val deltaNative = delta.native

lazy val gestalt = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-gestalt"))
  .settings(
    name := "lite-gestalt",
    console / initialCommands :=
      """|import codes.quine.labo.lite.gestalt._
         |""".stripMargin,
    Compile / console / scalacOptions -= "-Wunused",
    Test / console / scalacOptions -= "-Wunused",
    // Set URL mapping of scala standard API for Scaladoc.
    apiMappings ++= scalaInstance.value.libraryJars
      .filter(file => file.getName.startsWith("scala-library") && file.getName.endsWith(".jar"))
      .map(_ -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"))
      .toMap,
    // Settings for test:
    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.27" % Test,
    testFrameworks += new TestFramework("munit.Framework")
  )
  .jsSettings(Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) })
  .nativeSettings(
    crossScalaVersions := Seq("2.13.6"),
    coverageEnabled := false
  )

lazy val gestaltJVM = gestalt.jvm
lazy val gestaltJS = gestalt.js
lazy val gestaltNative = gestalt.native

lazy val gimei = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-gimei"))
  .settings(
    name := "lite-gimei",
    console / initialCommands :=
      """|import codes.quine.labo.lite.gimei._
         |""".stripMargin,
    Compile / console / scalacOptions -= "-Wunused",
    Test / console / scalacOptions -= "-Wunused",
    // Set URL mapping of scala standard API for Scaladoc.
    apiMappings ++= scalaInstance.value.libraryJars
      .filter(file => file.getName.startsWith("scala-library") && file.getName.endsWith(".jar"))
      .map(_ -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"))
      .toMap,
    // Settings for test:
    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.27" % Test,
    testFrameworks += new TestFramework("munit.Framework"),
    coverageExcludedPackages := "<empty>;codes\\.quine\\.labo\\.lite\\.gimei\\.Data.*",
    // Generators:
    {
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
  )
  .jsSettings(Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) })
  .nativeSettings(
    crossScalaVersions := Seq("2.13.6"),
    coverageEnabled := false
  )
  .dependsOn(romaji)

lazy val gimeiJVM = gimei.jvm
lazy val gimeiJS = gimei.js
lazy val gimeiNative = gimei.native

lazy val grapheme = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-grapheme"))
  .settings(
    name := "lite-grapheme",
    console / initialCommands :=
      """|import codes.quine.labo.lite.grapheme._
         |""".stripMargin,
    Compile / console / scalacOptions -= "-Wunused",
    Test / console / scalacOptions -= "-Wunused",
    // Set URL mapping of scala standard API for Scaladoc.
    apiMappings ++= scalaInstance.value.libraryJars
      .filter(file => file.getName.startsWith("scala-library") && file.getName.endsWith(".jar"))
      .map(_ -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"))
      .toMap,
    // Settings for test:
    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.27" % Test,
    testFrameworks += new TestFramework("munit.Framework"),
    coverageExcludedPackages := "<empty>;codes\\.quine\\.labo\\.lite\\.grapheme\\.Data.*",
    // Generators:
    {
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
    }, {
      val generateTest = taskKey[Seq[File]]("Generate text from UCD text")
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
  )
  .jsSettings(Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) })
  .nativeSettings(
    crossScalaVersions := Seq("2.13.6"),
    coverageEnabled := false
  )

lazy val graphemeJVM = grapheme.jvm
lazy val graphemeJS = grapheme.js
lazy val graphemeNative = grapheme.native

lazy val romaji = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-romaji"))
  .settings(
    name := "lite-romaji",
    console / initialCommands :=
      """|import codes.quine.labo.lite.romaji._
         |""".stripMargin,
    Compile / console / scalacOptions -= "-Wunused",
    Test / console / scalacOptions -= "-Wunused",
    // Set URL mapping of scala standard API for Scaladoc.
    apiMappings ++= scalaInstance.value.libraryJars
      .filter(file => file.getName.startsWith("scala-library") && file.getName.endsWith(".jar"))
      .map(_ -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"))
      .toMap,
    // Settings for test:
    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.27" % Test,
    testFrameworks += new TestFramework("munit.Framework")
  )
  .jsSettings(Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) })
  .nativeSettings(
    crossScalaVersions := Seq("2.13.6"),
    coverageEnabled := false
  )

lazy val romajiJVM = romaji.jvm
lazy val romajiJS = romaji.js
lazy val romajiNative = romaji.native

lazy val pfix = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-pfix"))
  .settings(
    name := "lite-pfix",
    console / initialCommands :=
      """|import codes.quine.labo.lite.pfix._
         |""".stripMargin,
    Compile / console / scalacOptions -= "-Wunused",
    Test / console / scalacOptions -= "-Wunused",
    // Set URL mapping of scala standard API for Scaladoc.
    apiMappings ++= scalaInstance.value.libraryJars
      .filter(file => file.getName.startsWith("scala-library") && file.getName.endsWith(".jar"))
      .map(_ -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"))
      .toMap,
    // Settings for test:
    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.27" % Test,
    testFrameworks += new TestFramework("munit.Framework")
  )
  .jsSettings(Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) })
  .nativeSettings(
    crossScalaVersions := Seq("2.13.6"),
    coverageEnabled := false
  )

lazy val pfixJVM = pfix.jvm
lazy val pfixJS = pfix.js
lazy val pfixNative = pfix.native

lazy val show = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("modules/lite-show"))
  .settings(
    name := "lite-show",
    console / initialCommands :=
      """|import codes.quine.labo.lite.show._
         |import codes.quine.labo.lite.show.Prettify.PrettifyGenOps
         |""".stripMargin,
    Compile / console / scalacOptions -= "-Wunused",
    Test / console / scalacOptions -= "-Wunused",
    // Set URL mapping of scala standard API for Scaladoc.
    apiMappings ++= scalaInstance.value.libraryJars
      .filter(file => file.getName.startsWith("scala-library") && file.getName.endsWith(".jar"))
      .map(_ -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"))
      .toMap,
    // Settings for test:
    libraryDependencies += "org.scalameta" %%% "munit" % "0.7.27" % Test,
    testFrameworks += new TestFramework("munit.Framework")
  )
  .jsSettings(Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) })
  .nativeSettings(
    crossScalaVersions := Seq("2.13.6"),
    coverageEnabled := false
  )
  .dependsOn(pfix)

lazy val showJVM = show.jvm
lazy val showJS = show.js
lazy val showNative = show.native
