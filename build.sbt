import NpmCliBase._

val textLintAll = taskKey[Unit]("lint text, html")
val textTestAll = taskKey[Unit]("test scala, links")

name := "textbook"

scalaVersion := "2.12.2"

enablePlugins(TutPlugin)

tutSourceDirectory := srcDir

tutTargetDirectory := compiledSrcDir

libraryDependencies ++= Seq(
  // sbt_2.12はしばらく出ない可能性が高いので下記PR内容をリバートしている
  // https://github.com/dwango/scala_text/pull/118
  // "org.scala-sbt" % "sbt" % "1.0.0-M4",
  "org.mockito" % "mockito-core" % "2.7.22",
  "org.scalacheck" %% "scalacheck" % "1.13.5",
  "org.scalatest" %% "scalatest" % "3.0.3" // tutで使うので、テストライブラリだが、わざとcompileスコープ
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
