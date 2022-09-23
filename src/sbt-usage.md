# sbtの基本的な使い方

## About

sbtはScalaのコードで使われているデファクトのビルドツールです。

sbtは以下のような機能を担います。

- Scalaのコンパイラの設定（バージョンやオプションなど）の管理・クロスビルド機能
- ライブラリをインストールしたりライブラリを公開したりするパッケージマネージャーとしての機能
- ビルドやテストの処理を定義・実行するための機能
- プラグインによるさまざまなタスクの自動化・簡略化の機能

以下ではsbtの基本的な機能を紹介しますが、詳細は[公式ドキュメント](https://www.scala-sbt.org/1.x/docs/ja/Getting-Started.html)を参照してください。

## Scalaのバージョン管理

### 単一のScalaバージョンを管理する場合

Scalaのアプリケーションを書く場合、例えばウェブサーバーやCLIツールを作る場合、特定のScalaのバージョンで書くのが一般的です。

例えば現在のディレクトリにあるScalaアプリケーションのScalaバージョンを`3.2.0`に指定するには
`build.sbt` に以下のように書きます。


```scala
lazy val app = project.in(file("."))
  .settings(
    scalaVersion := "3.2.0"
  )
```

### 複数の Scala バージョンを管理する場合

他のScalaアプリケーションやライブラリから利用するライブラリを書く際は、しばしば複数のScalaバージョン向けにライブラリをビルドします。

例えば、Scala 2.13.8とScala 3.2.0向けにライブラリをビルドするには次のように書きます。

```scala
lazy val lib = project.in(file("."))
  .settings(
    scalaVersion := "2.13.8",
    crossScalaVersions := Seq("2.13.8","3.2.0")
  )
```

sbtシェルから `++2.13.8 <sbt command>` とすることでScalaのバージョンを指定してsbtのコマンドを実行したり、
`+<sbt command>` とすることで`crossScalaVersions`に指定したすべてのScalaバージョンでコマンドを実行したりすることができます。


### コンパイラオプションの設定

sbtからはJavaやScalaのオプションを設定できます。

例えば `build.sbt` に以下のように設定することでScalaのコンパイラオプションを変更してdeprecatedな機能を使っている場合に警告を表示します。

```scala
scalacOptions ++= Seq(    
  "-deprecation",
)
```


同様にJavaのオプションを指定できます。以下の例では比較的新しいJavaでincubatorモジュールを利用する設定をJava Optionsに渡しています・
```scala
javaOptions ++= Seq(
  "--add-modules=jdk.incubator.foreign",
  "--enable-native-access=ALL-UNNAMED"
)
```


## パッケージの管理

### Scalaライブラリのインストール

現在のディレクトリにあるScalaアプリケーションからScalaのライブラリを利用する場合、`build.sbt` に以下のように書きます。

`"パッケージのグループID" %% "アーティファクトID" % "バージョン"` という形式です。

```scala
lazy val app = project.in(file("."))
  .settings(
    scalaVersion := "3.2.0",
    libraryDependencies ++= Seq(
      "com.typelevel" %% "cats-core" % "2.8.0"
    )
  )
```

上の例ではScala 3向けにビルドされたcatsというライブラリのバージョン2.8.0を追加しています。

このように書けば `src/main/scala` 以下にあるScalaファイルから下のようにライブラリをインポートして利用できます。

```scala
import cats.syntax.all._
```

### Javaライブラリを追加する

ScalaからはJavaのライブラリを利用することができます。 アプリケーションにJavaライブラリを追加する場合は以下のように書きます。
Scalaのライブラリを追加する場合と違ってネームスペースとライブラリの間にある `%` が1つになっていることに注意してください。

```scala
lazy val app = project.in(file("."))
  .settings(
    scalaVersion := "3.2.0",
    libraryDependencies ++= Seq(
      "log4j" % "log4j" % "1.2.17"
    )
  )
```

### JVM以外のプラットフォームからライブラリを利用する

ScalaアプリケーションをJVM向けだけでなくScala.jsやScala Native向けにもビルドする場合はライブラリもScala.jsやScala Native向けにビルドされたものを利用する必要があります。

以下の例は、現在のディレクトリにあるScala.jsアプリケーションからScala.js向けにビルドされたライブラリを利用する例です。

`libraryDependencies` に与えるライブラリの `%%` が `%%%` になっていることに注意しましょう。
このように書くことでScala.js向けにビルドする際はScala.js用のライブラリをScala Native向けにビルドする際はScala Native向けの
ライブラリを解決してくれます。

project/plugins.sbtにプラグインを追加します。

```scala
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.11.0")
```

build.sbtを次のように書きます。

```scala
lazy val app = project.in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "com.typelevel" %%% "cats-core" % "2.8.0"
    )
  )
```

### テスト時に利用するライブラリを指定する

アプリケーションやライブラリとしては利用しないがテストで利用するライブラリが必要な場合は次のように書きます。
ライブラリ名の後ろに `% Test` があることに注意してください。
このように指定したライブラリはテスト用のコード（一般的には`src/test/scala` 以下にあるScalaコード）からしか利用できず
リリースされるアプリケーションやライブラリには含まれません。JavaScriptのpackage.jsonに含まれる
`DevDependencies` のようなものと考えるとわかりやすいかもしれません。

```scala
lazy val app = project.in(file("."))
  .settings(
    libraryDependencies ++= Seq(
        "com.typelevel" %% "cats-core" % "2.8.0",
        "org.scalameta" %% "munit" % "1.0.0-M6" % Test,
    )
  )
```

## sbtプラグイン

sbtではプラグインを利用してさまざまなタスクを自動化できます。project/plugins.sbtファイルからプラグインを追加できます。

試しに、Scalaのコードフォーマッターscalafmtをsbtから利用するためのプラグインをproject/plugins.sbtに追加してみましょう。

```scala
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
```

これでターミナルまたはsbtシェルから`scalafmt`・`scalafmtSbt`・`scalafmtCheck`・`scalafmtAll`などのコマンドが使えるようになります。

```scala
sbt scalafmt
```

他にも 
- ライブラリのリリースを自動化する `sbt-ci-release`
- アプリケーションをさまざまな形式でパッケージングする `sbt-native-packager`
- Scala.jsやScala Nativeのクロスビルドを簡単にする `sbt-crossproject` 

などさまざまなプラグインがあります。