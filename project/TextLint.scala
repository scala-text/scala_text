import sbt._

import scala.util.control.NonFatal
import scala.sys.process.Process

object TextLint extends NpmCliBase {
  val textlintBin = nodeBin / cmd("textlint")

  lazy val textLint = inputKey[Unit]("lint text")

  val settings = Seq(
    textLint := {
      val args = rawStringArg(srcMarkdowns.mkString("\n")).parsed
      val options = if(args.isEmpty) srcMarkdowns.mkString("\n") else args

      printRun(Process(s"$textlintBin $options"))
    }
  )
}
