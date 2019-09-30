# sbtでプログラムをコンパイル・実行する

```tut:invisible
import sbt._, Keys._
```

前節まででは、REPLを使ってScalaのプログラムを気軽に実行してみました。この節ではScalaのプログラムをsbtでコンパイルして実行する方法を学びましょう。
まずはREPLの時と同様にHello, World!を表示するプログラムを作ってみましょう。その前に、REPLを抜けましょう。REPLを抜けるには、REPLから以下のように
入力します[^repl-quit]。

```
>:quit
```

Scala 2.10までは`exit`、Scala 2.11以降は`sys.exit`で終了することができますが、これらはREPL専用のコマンドではなく、今のプロセス自体を
終了させる汎用的なメソッドなのでREPLを終了させる時には使用しないようにしましょう。

```tut:silent
object HelloWorld {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
  }
}
```

`object`や`def`といった新しいキーワードが出てきましたね。これらの詳しい意味はあとで説明するので、ここでは、`scalac`でコンパイルする
プログラムはこのような形で定義するものと思ってください。`{}`で囲まれている部分はREPLの場合と同じ、```println("Hello, World!")``` ですね。 これを_HelloWorld.scala_という名前のファイルに保存します。

上記のプログラムはsbtでコンパイルし、実行することができます。ここでは_sandbox_というディレクトリを作成し、そこにプログラムを置くことにしましょう。

```
sandbox
├── HelloWorld.scala
└── build.sbt
```

以上のようにファイルを置きます。

今回の_build.sbt_にはScalaのバージョンと一緒に`scalac`の警告オプションも有効にしてみましょう。

```tut:silent
// build.sbt
scalaVersion := "2.13.1"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint")
```

この記述を加えることで`scalac`が

- 今後廃止の予定のAPIを利用している（`-deprecation`）
- 明示的に使用を宣言しないといけない実験的な機能や注意しなければならない機能を利用している（`-feature`）
- 型消去などでパターンマッチが有効に機能しない場合（`-unchecked`）
- その他、望ましい書き方や落とし穴についての情報（`-Xlint`）

などの警告の情報を詳しく出してくれるようになります。
コンパイラのメッセージが親切になるので付けるようにしましょう。

さて、このようにファイルを配置したら_sandbox_ディレクトリに入り、sbtを起動します。sbtを起動するには対話シェルから以下のようにコマンドを打ちます。

```
$ sbt
```

するとsbtのプロンプトが出て、sbtのコマンドが入力できるようになります。
今回はHelloWorldのプログラムを実行するために`run`コマンドを入力してみましょう。

```scala
> run
[info] Compiling 1 Scala source to ...
[info] Running HelloWorld
Hello, World!
[success] Total time: 1 s, completed 2015/02/09 15:44:44
```

HelloWorldプログラムがコンパイルされ、さらに実行されて`Hello, World!`と表示されました。
`run`コマンドでは`main`メソッドを持っているオブジェクトを探して実行してくれます。

またsbtの管理下のScalaプログラムは`console`コマンドでREPLから呼び出せるようになります。
_HelloWorld.scala_と同じ場所に_User.scala_というファイルを作ってみましょう

```tut:silent
// User.scala
class User(val name: String, val age: Int)

object User {
  def printUser(user: User) = println(user.name + " " + user.age)
}
```

この_User.scala_には`User`クラスと`User`オブジェクトがあり、`User`オブジェクトには`User`の情報を表示する`printUser`メソッドがあります（クラスやオブジェクトの詳細についてはこの後の節で説明します）。

```
sandbox
├── HelloWorld.scala
├── User.scala
└── build.sbt
```

この状態で`sbt console`でREPLを起動すると、REPLで`User`クラスや`User`オブジェクトを利用することができます。

```scala
scala> val u = new User("dwango", 13)
u: User = User@20daebd4

scala> User.printUser(u)
dwango 13
```

今後の節では様々なサンプルコードが出てきますが、このようにsbtを使うと簡単に自分で試してみることができるので、活用してみてください。

[^repl-quit]: `:quit`ではなく`:q`のみでも終了できます。
