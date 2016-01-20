import sbt._

object LinkTest extends NpmCliBase {
  val eslintBin = nodeBin / "eslint"
  val mochaBin = nodeBin / "mocha"

  lazy val textEslint = taskKey[Unit]("lint js")
  lazy val textLinkTest = taskKey[Unit]("verify links")

  val settings = Seq(
    textEslint := printRun(Process(s"$eslintBin ${srcDir.listFiles("*.js")} ${testDir.listFiles("*.js")}")),
    textLinkTest := printRun(Process(s"$mochaBin"))
  )
}
