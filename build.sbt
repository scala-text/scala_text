import NpmCliBase._

@transient
val textLintAll = taskKey[Unit]("lint text, html")
@transient
val textTestAll = taskKey[Unit]("test scala, links")
@transient
val textBuildAll = taskKey[Unit]("build html for both Scala 3 LTS and Scala 2 archived")

name := "textbook"

scalaVersion := "3.8.4"

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
  "org.scala-sbt" % "sbt" % sbtVersion.value,
  "org.mockito" % "mockito-core" % "5.23.0",
  "org.scalacheck" %% "scalacheck" % "1.19.0",
  "org.scalatest" %% "scalatest-flatspec" % "3.2.20", // mdocで使うので、テストライブラリだが、わざとcompileスコープ
  "org.scalatest" %% "scalatest-diagrams" % "3.2.20"
)

Honkit.settings

TextLint.settings

LinkTest.settings

textLintAll := Def.sequential(LinkTest.textEslint, TextLint.textLint.toTask("")).value

textTestAll := Def.sequential(Test / compile, LinkTest.textLinkTest).value

textBuildAll := Def.sequential(Honkit.textBuildHtml.toTask(""), Honkit.textBuildHtmlScala2.toTask("")).value

aggregateProjects(
  RootProject(file("src/example_projects/trait-example")),
  RootProject(file("src/example_projects/trait-refactored-example"))
)
