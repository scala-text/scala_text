import NpmCliBase._

val textLintAll = taskKey[Unit]("lint text, html")
val textTestAll = taskKey[Unit]("test scala, links")

ThisBuild / onChangedBuildSource := ReloadOnSourceChanges

name := "textbook"

scalaVersion := "3.8.2"

enablePlugins(MdocPlugin)

mdocIn := srcDir

mdocOut := compiledSrcDir

cleanFiles += compiledSrcDir

libraryDependencySchemes ++= Seq(
  "util-interface",
  "compiler-interface",
  "util-relation_3",
  "util-logging_3",
  "util-control_3"
).map("org.scala-sbt" % _ % "always")

libraryDependencies ++= Seq(
  "org.scala-sbt" % "sbt" % "2.0.0-RC9",
  "org.mockito" % "mockito-core" % "5.21.0",
  "org.scalacheck" %% "scalacheck" % "1.19.0",
  "org.scalatest" %% "scalatest-flatspec" % "3.2.19", // mdocで使うので、テストライブラリだが、わざとcompileスコープ
  "org.scalatest" %% "scalatest-diagrams" % "3.2.19"
)

Honkit.settings

TextLint.settings

LinkTest.settings

textLintAll := Def.sequential(LinkTest.textEslint, TextLint.textLint.toTask("")).value

textTestAll := Def.sequential(Test / compile, LinkTest.textLinkTest).value

aggregateProjects(
  RootProject(file("src/example_projects/trait-example")),
  RootProject(file("src/example_projects/trait-refactored-example"))
)
