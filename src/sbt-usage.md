# sbt の基本的な使い方

## About

sbt は Scala のコードで使われているデファクトのビルドツールです.

sbt は以下のような機能を担います.

- Scala のコンパイラの設定(バージョンやオプションなど)の管理・クロスビルド機能
- ライブラリをインストールしたりライブラリを公開したりするパッケージマネージャーとしての機能
- ビルドやテストの処理を定義・実行するための機能
- プラグインによるさまざまなタスクの自動化・簡略化の機能


## Scala のバージョン管理

### 単一の Scala バージョンを管理する場合

Scala のアプリケーションを書く場合、例えばウェブサーバーや CLI ツールを作る場合、特定の Scala のバージョンで書くのが一般的です.

例えば現在のディレクトリにある Scala アプリケーションの Scala バージョンを `3.2.0` に指定するには
`build.sbt` に以下のように書きます.


```scala
lazy val app = project.in(file("."))
  .settings(
    scalaVersion := "3.2.0"
  )
```

### 複数の Scala バージョンを管理する場合

他の Scala アプリケーションやライブラリから利用するライブラリを書く際は、しばしば複数の Scala バージョン向けに
ライブラリをビルドします.

例えば、Scala 2.13.8 と Scala 3.2.0 向けにライブラリをビルドするには次のように書きます.

```scala
lazy val lib = project.in(file("."))
  .settings(
    scalaVersion := "2.13.8",
    crossScalaVersions := Seq("2.13.8","3.2.0")
  )
```

sbt コンソールから `++2.13.8 <sbt command>` とすることで Scala のバージョンを指定して sbt のコマンドを実行したり、
`++<sbt command>` とすることで crossScalaVersions に指定したすべての Scala バージョンでコマンドを実行したりすることができます.


### コンパイラオプションの設定

sbt からは Java や Scala のオプションを設定できます.

例えば `build.sbt` に以下のように設定することで Scala　のコンパイラオプションを変更して deprecated な機能を使っている場合に警告を表示します.

```scala
scalacOptions ++= Seq(    
"-deprecation",
)
```


同様に Java のオプションを指定できます. 以下の例では比較的新しい Java で incubator モジュールを利用する設定を Java Options に渡しています. 
```scala
javaOptions ++= Seq(
  "--add-modules=jdk.incubator.foreign",
  "--enable-native-access=ALL-UNNAMED"
)
```


## パッケージの管理

### Scala ライブラリのインストール

現在のディレクトリにある Scala アプリケーションから Scala のライブラリを利用する場合、`build.sbt` に以下のように書きます.

`"パッケージのグループID" %% "アーティファクトID" % "バージョン"` という形式です.

```scala
lazy val app = project.in(file("."))
  .settings(
    scalaVersion := "3.2.0",
    libraryDependencies ++= Seq(
        "com.typelevel" %% "cats-core" % "2.7.0"
    )
  )
```

上の例では Scala 3 向けにビルドされた cats というライブラリのバージョン 2.7.0 を追加しています.

このように書けば `src/main/scala` 以下にある Scala ファイルから下のようにライブラリをインポートして利用できます.

```scala
import cats._
import cats.syntax._
import cats.implicits._
```

### Java ライブラリを追加する

Scala からは Java のライブラリを利用することができます. アプリケーションに Java ライブラリを追加する場合は以下のように書きます.
Scala のライブラリを追加する場合と違ってネームスペースとライブラリの間にある `%` が1つになっていることに注意してください.

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

Scala アプリケーションを JVM 向けだけでなく Scala.js や　Scala Native 向けにもビルドする場合はライブラリも Scala.js や Scala Native
向けにビルドされたものを利用する必要があります. 

以下の例は、現在のディレクトリにある Scala.js アプリケーションから Scala.js 向けにビルドされたライブラリを利用する例です.

`libraryDependencies` に与えるライブラリの `%%` が `%%%` になっていることに注意しましょう. 
このように書くことで Scala.js 向けにビルドする際は Scala.js 用のライブラリを Scala Native 向けにビルドする際は Scala Native 向けの
ライブラリを解決してくれます.

```scala
lazy val app = project.in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    libraryDependencies ++= Seq(
        "com.typelevel" %%% "cats-core" % "2.7.0"
    )
  )
```

### テスト時に利用するライブラリを指定する

アプリケーションやライブラリとしては利用しないがテストで利用するライブラリが必要な場合は次のように書きます.
ライブラリ名の後ろに `% Test` があることに注意してください. 
このように指定したライブラリはテスト用のコード(一般的には`src/test/scala` 以下にある Scala コード)からしか利用できず
リリースされるアプリケーションやライブラリには含まれません. JavaScript の package.json に含まれる
`DevDependencies` のようなものと考えるとわかりやすいかもしれません.

```scala
lazy val app = project.in(file("."))
  .settings(
    libraryDependencies ++= Seq(
        "com.typelevel" %% "cats-core" % "2.7.0",
        "org.scalameta" %% "munit" % "1.0.0-M6" % Test,
    )
  )
```

## sbt プラグイン

sbt ではプラグインを利用してさまざまなタスクを自動化できます. project/plugins.sbt ファイルからプラグインを追加できます.

試しに、Scala のコードフォーマッター scalafmt を sbt から利用するためのプラグインを project/plugins.sbt に追加してみましょう.

```scala
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
```

これで ターミナル または sbt コンソールから scalafmt, scalafmtSbt, scalafmtCheck, scalafmtAll などのコマンドが使えるようになります.

```scala
sbt scalafmt
```

他にも 
- ライブラリのリリースを自動化する `sbt-ci-release`
- アプリケーションをさまざまな形式でパッケージングする `sbt-native-packager`
- Scala.js や Scala Native のクロスビルドを簡単にする `sbt-crossproject` 

などさまざまなプラグインがあります.