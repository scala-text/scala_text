import sbt._
import scala.sys.process.Process

object LinkTest extends NpmCliBase {
  val eslintBin = nodeBin / cmd("eslint")
  val mochaBin = nodeBin / cmd("mocha")

  lazy val textEslint = taskKey[Unit]("lint js")
  lazy val textLinkTest = taskKey[Unit]("verify links")

  val settings = Seq(
    textEslint := printRun(Process(s"$eslintBin ${srcDir.listFiles("*.js")} ${testDir.listFiles("*.js")}")),
    textLinkTest := printRun(Process(s"$mochaBin"))
  )
}
