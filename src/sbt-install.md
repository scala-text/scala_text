# sbtをインストールする

```tut:invisible
import sbt._, Keys._
```

現実のScalaアプリケーションでは、Scalaプログラムを手動でコンパイル[^scalac]することは非常に稀で、
標準的なビルドツールである[sbt](https://www.scala-sbt.org/release/docs/ja/Setup.html)というツールを用いることになり
ます。ここでは、sbtのインストールについて説明します。

## Javaのインストール

Scala 2.13ではJava 8以降が必須なので、もしJavaがインストールされていなければ、まずJavaをインストールしましょう。
Javaのインストール方法の詳細はここでは省略します。
ScalaとJavaのそれぞれのバージョンの互換性に関しては、以下のScala公式サイトのページを見てください。

https://docs.scala-lang.org/overviews/jdk-compatibility/overview.html

## Mac OSの場合

Mac OSの場合、[Homebrew](https://brew.sh/index_ja.html)を用いて、

```
$ brew install sbt
```

でインストールするのが楽です。

## Windowsの場合

[chocolatey](https://chocolatey.org/)を用いるのが楽です。
chocolateyはWindows用のパッケージ マネージャで活発に開発が行われてます。chocolatey
のパッケージにはsbtのものもあるので、

```
> choco install sbt
```

とすればWindowsにsbtがインストールされます。

Windows/Mac OSの場合で、シェル環境でsbtと入力するとバイナリのダウンロードが
始まればインストールの成功です。sbtがないと言われる場合、環境変数へsbtへのPATHが
通っていないだけですので追加しましょう。Windowsでの環境変数編集ツールとしては、
[Rapid Environment Editor](http://www.rapidee.com/en/about)が非常に便利です。


## REPLとsbt

これからしばらく、REPL（Read Eval Print Loop）機能と呼ばれる対話的な機能を用いてScalaプログラムを
試していきますが、それは常に`sbt console`コマンドを経由して行います。

sbt consoleを起動するには、WindowsでもMacでも

```
$ sbt console
```

と入力すればOKです。成功すれば、

```
[info] Loading global plugins from /Users/.../.sbt/1.0/plugins
[info] Set current project to sandbox (in build file:/Users/.../sandbox/)
[info] Updating {file:/Users/.../sandbox/}sandbox...
[info] Resolving org.fusesource.jansi#jansi;1.4 ...
[info] Done updating.
[info] Starting scala interpreter...
[info] 
Welcome to Scala version 2.13.1 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_45).
Type in expressions to have them evaluated.
Type :help for more information.

scala> 

```

のように表示されます。sbt consoleを終了したい場合は、

```
scala> :quit
```

と入力します。なお、sbt consoleを立ち上げる箇所には仮のディレクトリを掘っておくことを
お勧めします。sbtはカレントディレクトリの下に_target_ディレクトリを生成してディレクトリ
空間を汚してしまうからです。

ちなみに、このとき起動されるScalaのREPLのバージョンは現在使っているsbtのデフォルトのバージョン
になってしまうので、こちらが指定したバージョンのScalaでREPLを起動したい場合は、同じディレクトリに
_build.sbt_というファイルを作成し、

```tut:silent
scalaVersion := "2.13.1"
```

としてやると良いです。この_*.sbt_がsbtのビルド定義ファイルになるのですが、今はREPLに慣れてもらう
段階なので、この_.sbt_ファイルの細かい書き方についての説明は省略します。


## sbtのバージョンについて

この“sbtのバージョンについて”は、最新版を正常にインストールできた場合は、読み飛ばしていただいて構いません。

sbtは`sbt --version`もしくは`sbt --launcher-version`とするとversionが表示されます[^hyphen]。
このテキストでは基本的にsbt 1.x[^latest]がインストールされている前提で説明していきます。
1.x系であれば基本的には問題ないはずですが、無用なトラブルを避けるため、
もし過去に少し古いバージョンのsbtをインストールしたことがある場合は、できるだけ最新版を入れておいたほうがいいでしょう。
また、もし0.13系以前のversion（0.13.16など）が入っている場合は、色々と動作が異なり不都合が生じるので、その場合は必ず1.x系の最新版を入れるようにしてください。


[^scalac]: ここで言う"手動で"とは、`scalac`コマンドを直接呼び出すという意味です

[^hyphen]: ハイフンは1つではなく2つなので注意。versionの詳細について知りたい場合は、こちらも参照。 https://github.com/scalajp/scala_text/issues/122

[^latest]: 具体的にはこれを書いている2019年9月時点の最新版である1.3.2。
