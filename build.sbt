import NpmCliBase._

val textLintAll = taskKey[Unit]("lint text, html")
val textTestAll = taskKey[Unit]("test scala, links")

ThisBuild / onChangedBuildSource := ReloadOnSourceChanges

name := "textbook"

scalaVersion := "2.13.2"

enablePlugins(MdocPlugin)

mdocIn := srcDir

mdocOut := compiledSrcDir

libraryDependencies ++= Seq(
  // TODO sbtがScala 2.13対応したら再び有効化
  // "org.scala-sbt" % "sbt" % sbtVersion.value,
  "org.mockito" % "mockito-core" % "3.6.0",
  "org.scalacheck" %% "scalacheck" % "1.15.1",
  "org.scalatest" %% "scalatest" % "3.2.3" // mdocで使うので、テストライブラリだが、わざとcompileスコープ
)

GitBook.settings

TextLint.settings

LinkTest.settings

textLintAll := Def.sequential(LinkTest.textEslint, TextLint.textLint.toTask("")).value

textTestAll := Def.sequential(compile in Test, LinkTest.textLinkTest).value

aggregateProjects(
  RootProject(file("src/example_projects/trait-example")),
  RootProject(file("src/example_projects/trait-refactored-example"))
)
