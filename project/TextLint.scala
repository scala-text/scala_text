import sbt._

import scala.util.control.NonFatal

object TextLint extends NpmCliBase {
  val textlintBin = nodeBin / cmd("textlint")

  lazy val textLint = inputKey[Unit]("lint text")

  val settings = Seq(
    textLint := {
      val args = rawStringArg(srcMarkdowns.mkString("\n")).parsed
      val options = if(args.isEmpty) s"$srcDir" else args

      printRun(Process(s"$textlintBin $options"))
    }
  )
}
