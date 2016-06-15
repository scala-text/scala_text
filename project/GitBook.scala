import sbt._
import sbt.IO
import tut.Plugin._

object GitBook extends NpmCliBase {
  val gitbookBin = nodeBin / cmd("gitbook")
  val gitbookVersion = "3.1.1"

  sealed trait Format {def command: String}
  object Format {
    case object Html extends Format { def command = s"build $bookJsonDir $bookDestDir/_book" }
    case object Epub extends Format { def command = s"epub  $bookJsonDir $bookDestDir/scala_text.epub" }
    case object Pdf extends Format { def command = s"pdf  $bookJsonDir $bookDestDir/scala_text.pdf" }
  }

  def buildBook(format: Format) = Def.inputTask[Unit] {
    val options = rawStringArg("<gitbook command>").parsed
    printRun(Process(s"$gitbookBin  ${format.command} --gitbook=$gitbookVersion $options"))

    if(format == Format.Html) {
      println("chrome48でcssの`text-rendering:optimizeLegibility`のレンダリングが高コストであるため該当cssを削除します")
      val cssPath = s"$bookDestDir/_book/gitbook/style.css"
      val orig = IO.read(file(cssPath), IO.utf8)
      val modified = orig.replace("text-rendering:optimizeLegibility;", "")
      IO.write(file(cssPath), modified, IO.utf8)
    }
  }

  lazy val textPluginInstall = taskKey[Unit]("install GitBook plugin")
  lazy val textHelpGitBook = taskKey[Unit]("help GitBook")
  lazy val textBuildOnly = inputKey[Unit]("build only specified html")
  lazy val textBuildHtml = inputKey[Unit]("build GitBook to html")
  lazy val textBuildEpub = inputKey[Unit]("build GitBook to epub")
  lazy val textBuildPdf = inputKey[Unit]("build GitBook to pdf")

  val settings = Seq(
    textPluginInstall := printRun(Process(s"$gitbookBin install")),
    textHelpGitBook := printRun(Process(s"$gitbookBin help")),
    textBuildHtml := buildBook(Format.Html).dependsOn(tut).evaluated,
    textBuildEpub := buildBook(Format.Epub).dependsOn(tut).evaluated,
    textBuildPdf := sys.error("pdf-convertで利用するcalibreがcentOS6で上手く動かないので停止中"),

    // tutを通さずビルドする（srcディレクトリからのコピーは行われないので tutOnly foo.md => textBuildOnly の順で実行する）
    textBuildOnly := buildBook(Format.Html).evaluated
  )
}
