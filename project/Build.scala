import sbt._
import sbt.Keys._
import tut.Plugin._

object build extends Build with NpmCliBase {
  lazy val textLintAll = taskKey[Unit]("lint text, html")
  lazy val textTestAll = taskKey[Unit]("test scala, links")

  lazy val root = (project in file(".")).
    settings(
      name := "textbook",
      scalaVersion := "2.11.8",
      tutSettings,
      tutSourceDirectory := srcDir,
      tutTargetDirectory := compiledSrcDir,
      libraryDependencies ++= Seq(
        "org.scala-sbt" % "sbt" % "1.0.0-M4",
        "org.mockito" % "mockito-core" % "1.10.19",
        "org.scalacheck" %% "scalacheck" % "1.12.5",
        "org.scalatest" %% "scalatest" % "2.2.6" // tutで使うので、テストライブラリだが、わざとcompileスコープ
      )
    ).settings(
      GitBook.settings ++ TextLint.settings ++ LinkTest.settings
    ).settings(
      textLintAll := Def.sequential(LinkTest.textEslint, TextLint.textLint.toTask("")).value,
      textTestAll := Def.sequential(compile in Test, LinkTest.textLinkTest).value
    ).aggregate(
      RootProject(file("src/example_projects/trait-example")),
      RootProject(file("src/example_projects/trait-refactored-example"))
    )
}
