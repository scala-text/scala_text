import sbt._
import tut.Plugin._

object GitBook extends NpmCliBase {
  val gitbookBin = nodeBin / "gitbook"

  sealed trait Format {def command: String}
  object Format {
    case object Html extends Format { def command = "build" }
    case object Epub extends Format { def command = "epub" }
    case object Pdf extends Format { def command = "pdf" }
  }

  def buildBook(format: Format) = Def.inputTask[Unit] {
    val options = rawStringArg("<gitbook command>").parsed
    printRun(Process(s"$gitbookBin  ${format.command} $bookBuildDir $options"))
  }

  lazy val textPluginInstall = taskKey[Unit]("install GitBook plugin")
  lazy val textHelpGitBook = taskKey[Unit]("help GitBook")
  lazy val textBuildOnly = inputKey[Unit]("build only specified html")
  lazy val textBuildHtml = inputKey[Unit]("build GitBook to html")
  lazy val textBuildEpub = inputKey[Unit]("build GitBook to epub")
  lazy val textBuildPdf = inputKey[Unit]("build GitBook to pdf")
  lazy val textBuildAll = taskKey[Unit]("build GitBook to all format")

  val settings = Seq(
    textPluginInstall := printRun(Process(s"$gitbookBin install")),
    textHelpGitBook := printRun(Process(s"$gitbookBin help")),
    textBuildHtml := buildBook(Format.Html).dependsOn(tut).evaluated,
    /*
     foo.mdだけ更新があるときに単純にsbt buildを実行すると
     tutで全ファイルがコンパイル -> gitbook build が走ってしまいそれなりに時間がかかる。
     その対策のために現状はtutOnly foo.md -> buildOnlyとすれば
     foo.mdだけコンパイルされて更新される -> gitbook buildがかかるようになっている。

     本当は buildOnly foo.md とだけやれば
     tutOnly foo.md -> gitbook build
     が自動でh実行されるようにしたかったが書き方がよくわからず挫折した。
    */
    textBuildOnly := buildBook(Format.Html).evaluated,
    textBuildEpub := buildBook(Format.Epub).dependsOn(tut).evaluated,
    textBuildPdf := sys.error("pdf-convertで利用するcalibreがcentOS6で上手く動かないので停止中"),
    // 全部パラレルにやっていいのか不明なので逐次実行
    // pdfはエラーになるので実行していない
    textBuildAll <<= textBuildEpub.toTask("") dependsOn textBuildHtml.toTask("")
  )
}
