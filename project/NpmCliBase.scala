import sbt._
import sbt.complete.Parser
import scala.util.Properties

trait NpmCliBase {
  val nodeBin = file("node_modules/.bin/")

  // 執筆者の手により編集されるディレクトリ
  val srcDir = file("src")
  def srcMarkdowns = srcDir.listFiles("*.md").filterNot(f => f.getPath.contains("src/example_projects/"))

  // book.jsonがあるディレクトリ
  val bookJsonDir = file(".")

  // gitbookのビルドの起点/成果物が入るディレクトリ(gitbook/_book/index.html, gitbook/scala_text.epubが生成される)
  val bookDestDir = file("gitbook")

  // tutで処理済みのmarkdownファイルが入るディレクトリ。これがgitbook buildされる
  // book.jsonのrootにも指定されている。
  val compiledSrcDir = bookDestDir

  // リンク切れチェックのようなテストが入るディレクトリ
  val testDir = file("test")

  protected def rawStringArg(argLabel: String = "<arg>"): Parser[String] = {
    Def.spaceDelimited(argLabel).map(_.mkString(" "))
  }

  // exit codeが0でなければ例外を投げる
  def printRun(p: ProcessBuilder) : Unit = {
    p.lines foreach println
  }

  def cmd(name: String) =
    if(Properties.isWin) s"${name}.cmd" else name
}
