import sbt._
import sbt.io.IO
import mdoc.MdocPlugin.autoImport._
import scala.sys.process.Process

object Honkit extends NpmCliBase {
  val honkitBin = nodeBin / cmd("honkit")

  sealed trait Format {def command: String}
  object Format {
    case object Html extends Format { def command = s"build $bookJsonDir $bookDestDir/_book" }
    case object Epub extends Format { def command = s"epub  $bookJsonDir $bookDestDir/scala_text.epub" }
    case object Pdf extends Format { def command = s"pdf  $bookJsonDir $bookDestDir/scala_text.pdf" }
  }

  def buildBook(format: Format) = Def.inputTask[Unit] {
    val options = rawStringArg("<honkit command>").parsed
    printRun(Process(s"$honkitBin ${format.command} $options"))
  }

  lazy val textHelpHonkit = taskKey[Unit]("help Honkit")
  lazy val textBuildHtml = inputKey[Unit]("build Honkit to html")
  lazy val textBuildEpub = inputKey[Unit]("build Honkit to epub")

  private[this] val mdocTask = mdoc.toTask("")

  val settings = Seq(
    textHelpHonkit := printRun(Process(s"$honkitBin help")),
    textBuildHtml := buildBook(Format.Html).dependsOn(mdocTask).evaluated,
    textBuildEpub := buildBook(Format.Epub).dependsOn(mdocTask).evaluated
  )
}
