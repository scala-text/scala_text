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
        "org.scalamock" %% "scalamock-scalatest-support" % "3.2",
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
