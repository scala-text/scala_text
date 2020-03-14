import sbt._
import sbt.io.IO
import mdoc.MdocPlugin.autoImport._
import scala.sys.process.Process

object GitBook extends NpmCliBase {
  val gitbookBin = nodeBin / cmd("gitbook")
  val gitbookVersion = "3.2.2"

  sealed trait Format {def command: String}
  object Format {
    case object Html extends Format { def command = s"build $bookJsonDir $bookDestDir/_book" }
    case object Epub extends Format { def command = s"epub  $bookJsonDir $bookDestDir/scala_text.epub" }
    case object Pdf extends Format { def command = s"pdf  $bookJsonDir $bookDestDir/scala_text.pdf" }
  }

  def buildBook(format: Format) = Def.inputTask[Unit] {
    val options = rawStringArg("<gitbook command>").parsed
    printRun(Process(s"$gitbookBin  ${format.command} --gitbook=$gitbookVersion $options"))
  }

  lazy val textPluginInstall = taskKey[Unit]("install GitBook plugin")
  lazy val textHelpGitBook = taskKey[Unit]("help GitBook")
  lazy val textBuildHtml = inputKey[Unit]("build GitBook to html")
  lazy val textBuildEpub = inputKey[Unit]("build GitBook to epub")

  private[this] val mdocTask = mdoc.toTask("")

  val settings = Seq(
    textPluginInstall := printRun(Process(s"$gitbookBin install")),
    textHelpGitBook := printRun(Process(s"$gitbookBin help")),
    textBuildHtml := buildBook(Format.Html).dependsOn(mdocTask).evaluated,
    textBuildEpub := buildBook(Format.Epub).dependsOn(mdocTask).evaluated
  )
}
