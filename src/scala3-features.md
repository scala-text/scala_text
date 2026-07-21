# Scala 3で追加された主な機能

本テキストはScala 3 LTSを前提に書かれていますが、これまでの各章ではScala 2にもあった機能を中心に説明してきました。本章ではScala 3で新しく導入された、あるいは大きく変わった機能のうち、本テキストの他の章ではあまり踏み込んでこなかったものをまとめて紹介します。すでに別章で詳しく扱った機能（`enum`、`given`/`using`/`extension`、新しい制御構文など）はここでは概要に留めます。

新しく書くコードでこれらの機能を積極的に使うことで、Scala 2では実現しにくかった設計や、より簡潔な表現が可能になります。

## 本章で扱う機能の早見表

| 機能 | 用途 | 詳述章 |
| --- | --- | --- |
| `enum` | 代数的データ型・列挙の簡潔な定義 | [列挙型、ケースクラスとパターンマッチング](./case-class-and-pattern-matching.md) |
| `given`/`using`/`extension` | 型クラス、文脈引き渡し、拡張メソッド | [Given/Using/Extension](./contextual.md) |
| 新しい制御構文（`if then`、`while do`等） | 条件式の括弧を省略できる書き方 | [制御構文](./control-syntax.md) |
| トップレベル定義 | パッケージ直下に`def`や`val`を置ける | 本章 |
| `export` | 他オブジェクトのメンバーを再公開 | 本章 |
| `opaque type` | 抽象化された軽量な型ラッパー | 本章 |
| <code>&amp;</code> / <code>&#124;</code> 型 | 交差型・合併型 | 本章 |

## トップレベル定義

Scala 2では、メソッドや変数を定義するには必ず`object`、`class`、`trait`の中に置く必要があり、共有のユーティリティを集めたい場合は`package object`という特別な仕組みを使う必要がありました。Scala 3ではこの制約が緩和され、**パッケージ直下に`def`や`val`を直接書く**ことができます。

```scala
// utils.scala
package example.util

def square(x: Int): Int = x * x

val Greeting = "Hello, Scala 3!"
```

```scala
// 別ファイルから利用
package example

import example.util.*

@main def run(): Unit = {
  println(Greeting)
  println(square(7))
}
```

これにより、Scala 2の`package object`はほぼ不要になりました。共通ユーティリティをファイル単位で気軽にまとめられるため、ライブラリ作者・利用者の双方にとって取り回しがよくなっています。

なお、上記の例で使った`@main`はScala 3で導入された **エントリーポイント注釈** で、`main`メソッドを持つ`object`を書かずに済む簡易記法です。`@main def 名前(...): Unit = ...`と書くだけで、その関数がプログラムの開始点になります。

## `export`句

`export`句は、あるオブジェクトの中で、別オブジェクトのメンバーを **再公開（re-export）** するための機能です。Scala 2でこれを実現するには、メンバーごとに転送用のメソッドを手書きする必要がありました。Scala 3では1行で済みます。

```scala mdoc:nest:silent
class Logger {
  def info(msg: String): Unit = println(s"[INFO] $msg")
  def warn(msg: String): Unit = println(s"[WARN] $msg")
}

class App(logger: Logger) {
  export logger.{info, warn}
}
```

`export`したメンバーは、`App`の内部で使えるだけでなく、`App`自身のメソッドとして **外部にも公開** されます（内部で使うだけなら`import logger.*`でも足ります）。つまり`App`の利用者は、中に`Logger`がいることを意識せずに`app.info(...)`と呼び出せます。

```scala mdoc:nest
val app = App(Logger())
app.info("started")
app.warn("low memory")
```

`export`は **委譲（delegation）** を簡潔に書くための機能です。クラスを継承（`extends`）するほどではないが、特定のメンバーだけを外向きに公開したい、というケースで重宝します。`export logger.*`のようにワイルドカードで全メンバーを公開することもできます。

## `opaque type`

`opaque type`は、ある型を別の型として **コンパイル時にだけ区別** する仕組みです。実行時には元の型と同じ表現を持ち、追加のオブジェクト生成は発生しないため、性能を犠牲にせず型安全性を高められます。

たとえば「ユーザーID」と「商品ID」を、どちらも内部表現は`Long`のまま、混同を防ぎたいとしましょう。

```scala mdoc:nest:silent
object Ids {
  opaque type UserId = Long
  opaque type ProductId = Long

  object UserId {
    def apply(value: Long): UserId = value
    extension (id: UserId) def value: Long = id
  }
  object ProductId {
    def apply(value: Long): ProductId = value
    extension (id: ProductId) def value: Long = id
  }
}

import Ids.*

val u: UserId = UserId(1)
val p: ProductId = ProductId(2)

// コンパイルエラー: UserIdとProductIdは別の型として扱われる
// val mistake: UserId = p
```

`opaque type`の本体は、それを定義したオブジェクト（上の例では`Ids`）の **内側からは透過的**（`Long`そのものとして扱える）で、**外側からは不透明**（`UserId`という別の型として扱われる）になります。これにより、ケースクラスでラップする方法と比べて、ボックス化のコストを払わずに型レベルの区別を導入できます。

なお、上の例では拡張メソッド`value`をそれぞれのコンパニオンオブジェクトの中に定義しています。`UserId`と`ProductId`は実行時にはどちらも`Long`になるため、同じスコープに同名の拡張メソッドを並べるとシグネチャが衝突してコンパイルエラーになるからです。

## 交差型（`&`）と合併型（`|`）

Scala 3では、型レベルで「両方の型を満たす」「いずれかの型である」を表現する新しい型演算子が導入されました。

### 交差型（intersection types）

`A & B`は **「`A`であり、かつ`B`でもある」型** を表します。Scala 2の`A with B`に近い概念ですが、より一般的で、`B & A`と`A & B`が同じ型として扱われるなどの利点があります。

```scala mdoc:nest:silent
trait Resettable {
  def reset(): Unit
}

trait Growable[T] {
  def add(t: T): Unit
}

def f(x: Resettable & Growable[String]): Unit = {
  x.reset()
  x.add("hello")
}
```

### 合併型（union types）

`A | B`は **「`A`または`B`である」型** を表します。これまでScalaでは`Either[A, B]`のようなラッパーを使うか、共通のスーパータイプを定義する必要がありましたが、合併型を使うとそのままの型で「どちらか」を表現できます。

```scala mdoc:nest
def parseValue(s: String): Int | String = {
  s.toIntOption match {
    case Some(n) => n
    case None    => s
  }
}

val result: Int | String = parseValue("42")
result match {
  case n: Int    => println(s"整数: $n")
  case s: String => println(s"文字列: $s")
}
```

合併型は、引数や戻り値が「いくつかの具体的な型のうちのどれか」であることを型レベルで正確に表現したい場面で便利です。`null`の許容を`String | Null`のように表すこともでき、Scala 3のnull安全機能（explicit nulls）と組み合わせて使えます。ただしnull安全機能はデフォルトでは無効で、コンパイラオプションで有効化するオプトイン機能です。

## まとめ

Scala 3で導入された機能は、Scala 2で「ちょっと面倒だった」「コストが気になっていた」あれこれを、より直接的・低コストに書けるようにするものが中心です。本章で紹介した機能は、大規模なライブラリやアプリケーションの設計でこそ真価を発揮します。一方、本テキストの基礎章で扱うような入門レベルのコードでは、これらを意識しなくても十分に動くプログラムが書けます。

まずは基礎を固めたうえで、必要に応じて本章の機能を引き出しから取り出して使ってみてください。
