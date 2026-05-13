# Given/Using/Extension

Scalaには他の言語にはあまり見られない、暗黙的に値や変換をコンパイラに供給する仕組みがあります。Scala 2ではこれらは`implicit`という単一のキーワードで表現されていましたが、1つの機能で複数の用途を兼ねることがユーザーにとって分かりにくいという反省から、Scala 3では用途ごとに専用のキーワード・構文が用意されています。

| 用途 | Scala 2 | Scala 3 |
| --- | --- | --- |
| 暗黙の型変換 | `implicit def` | `given Conversion[A, B]` |
| 拡張メソッド（enrich my library） | `implicit class` | `extension (x: T)` |
| 文脈の暗黙引き渡し | `(implicit x: T)` 引数 | `(using x: T)` 引数 |
| 型クラスのインスタンス | `implicit val/object` | `given X: T with ...` |
| 暗黙値の取り出し | `implicitly[T]` | `summon[T]` |

この章ではScala 3でのこれら4つの使い方を順に説明します。Scala 2の`implicit`を見たことがある読者には、本章末尾の「Scala 2の`implicit`との対応」節も合わせて参照してもらえると、Scala 2のコードを読むときに役に立つはずです。

## 暗黙の型変換（Conversion）

暗黙の型変換は、ユーザーが「ある型から別の型への変換」をコンパイラに登録できる機能です。Scalaが普及し始めた時期にはこの機能が多用されたのですが、多用するとプログラムが読みづらくなることが分かったため、現在は積極的に使うことは推奨されていません。とはいえ、固定長整数から多倍長整数への変換など、標準ライブラリやサードパーティのライブラリで使われているケースもあるので知っておく価値はあります。

Scala 3で暗黙の型変換を定義するには、`scala.Conversion[A, B]`型の`given`インスタンスを定義します。

```scala
  given メソッド名: Conversion[引数の型, 返り値の型] = 引数 => 本体
```

引数の型の式が現れたときに、コンパイラが暗黙のうちに返り値の型への変換を挿入してくれます。次の例では、`Int`型から`Boolean`型への暗黙の型変換を定義しています。

```scala mdoc:nest
import scala.language.implicitConversions

given intToBoolean: Conversion[Int, Boolean] = arg => arg != 0

if 1 then {
  println("1は真なり")
}
```

コンパイラは`if 1 then ...`を見た時点で、本来`Boolean`が要求されているのに`Int`型の式である`1`が書かれていることに気づきます。多くの静的型付き言語ではここで型エラーになります。しかしScalaでは、引数が`Int`で返り値が`Boolean`の暗黙の型変換が定義されていないか探索し、上で定義した`intToBoolean`を発見します。そしてコンパイラはコードを次のように書き換えたかのように扱います。

```scala mdoc:nest
if intToBoolean(1) then {
  println("1は真なり")
}
```

これによって、`if`の条件式に`Int`を渡せるようになるわけです。ただし、暗黙の型変換のこのような使い方はあまり良いものではありません。if式の条件に`Boolean`しか渡せないようになっているのは間違いを防止するためなのに、そのチェックを通り抜けてしまえるわけですから。

なお、Scala 3では暗黙の型変換を使うために`import scala.language.implicitConversions`が必要です。これは「暗黙の型変換は強力すぎて誤用しやすいので、明示的にオプトインしてほしい」という意図に基づくものです。

`BigInt`や`BigDecimal`など一部のライブラリではScala標準の`Int`や`Double`と相互に変換するために暗黙の型変換を定義していますが、普通のユーザーが定義する必要があることは稀です。正当な理由を思いつかない限りは使わないようにしましょう。

## 拡張メソッド（Extension）

C#やKotlinなどにある「拡張メソッド」と同等の機能を、Scalaでは伝統的に *Enrich my library パターン* と呼んできました。既存のクラスにメソッドを追加したように見せかけることができ、Scala標準ライブラリの中にも利用例がありますし、サードパーティのライブラリでもよく見かけます。

たとえば、これまで見たプログラムの中には`(1 to 5)`という式がありましたが、本来`Int`型は`to`というメソッドを持っていません。`to`はまさに拡張メソッドの典型的な利用例で、`Int`に対して暗黙的にメソッドが追加されているのです。

Scala 3では拡張メソッドを定義するための専用構文として`extension`が用意されています。試しに、`String`の末尾に`":-)"`という文字列を追加して返す`smile`メソッドを定義してみましょう。

```scala mdoc:reset
extension (src: String) {
  def smile: String = src + ":-)"
}

"Hi, ".smile
```

`extension (src: String)`は「`String`型に対してメソッドを追加する」という意図を端的に表しています。ブロック内に複数のメソッドを並べて書くこともできますし、型パラメータを取ることもできます。

```scala mdoc:reset
extension [A](self: A) {
  def tap[U](block: A => U): A = {
    block(self)
    self
  }

  def pipe[B](f: A => B): B = f(self)
}

"Hello, World".tap(println).reverse.tap(println)
"abc".pipe(_.toUpperCase)
```

`extension`は、拡張メソッドを定義したいという意図を構文レベルで表現できるため、Scala 2の`implicit class`より分かりやすく、誤用も起きにくくなっています。

### 練習問題 {#contextual_ex1}

`Int`から`BigInt`への暗黙の型変換のように、利用者にとって便利になる変換を1つ考えて`given Conversion[A, B]`として定義してみてください。その変換にはどのような利点と欠点があるかを答えてください。

### 練習問題 {#contextual_ex2}

既存のクラスに対し、`extension`構文を使って便利なメソッドを追加してみましょう。どのような場面で役に立つでしょうか？

<!-- begin answer id="answer_ex1" style="display:none" -->

```scala mdoc:nest:silent
object Taps {
  extension [T](self: T) {
    def tap[U](block: T => U): T = {
      block(self) //値は捨てる
      self
    }
  }

  def main(args: Array[String]): Unit = {
    "Hello, World".tap{s => println(s)}.reverse.tap{s => println(s)}
  }
}
```

```scala mdoc:nest
import Taps.*
Taps.main(Array())
```

定義した`tap`メソッドはRubyなどの言語にありますが、メソッドチェインの中でデバッグプリントをはさみたいときに役に立ちます。

<!-- end answer -->

#### 練習問題 {#contextual_ex3}

[Scala標準ライブラリ](https://www.scala-lang.org/api/current/index.html)の中から拡張メソッド（`extension`または`implicit class`）が使われている例を1つ以上見つけてください。どのような時に便利でしょうか？

## using引数（文脈の暗黙引き渡し）

`using`引数は、メソッド呼び出しのたびに毎回書きたくない「文脈オブジェクト」を、コンパイラに自動で引き渡してもらう仕組みです。たとえば、ソケットやデータベースのコネクションのように、あちこちのメソッドに共通で渡されるオブジェクトに使うと便利です。

データベースとのコネクションを表す`Connection`型があるとします。データベースと接続するメソッドには全て`Connection`型を渡さなければなりません。

```
def readRecordsFromTable(columnName: String, tableName: String, connection: Connection): List[Record]
def writeRecordsToTable(record: List[Record], tableName: String, connection: Connection): Unit
def readAllFromTable(tableName: String, connection: Connection): List[Row]
```

3つのメソッドは全て`Connection`型を引数に取るのに、呼び出すたびに明示的に`Connection`オブジェクトを渡さなければいけません。ここで`using`引数の出番です。上のメソッド定義を

```scala
def readRecordsFromTable(columnName: String, tableName: String)(using connection: Connection): List[Record]
def writeRecordsToTable(records: List[Record], tableName: String)(using connection: Connection): Unit
def readAllFromTable(tableName: String)(using connection: Connection): List[Record]
```

と書き換えます。`using`修飾子は最後の引数リストに付けます。つまり、以下のようになっているのがポイントです。

```scala
(....)(using conn: Connection)
```

`using`引数は名前を省略することもできます。メソッド内部で使う必要がなければ型だけ書けば十分です。

```scala
def readRecordsFromTable(columnName: String, tableName: String)(using Connection): List[Record]
```

このように定義されたメソッドが呼び出されると、Scalaコンパイラは現在の呼び出しスコープからたどって、適切な型の`given`値を探索し、暗黙にメソッドへ引き渡します。たとえば次のようにして、値を`given`としてマークします。

```scala
given aConnection: Connection = connectDatabase(....)
```

こうすれば、最後の引数リストに暗黙のうちに`Connection`オブジェクトを渡してくれます。以下のような呼び出しがあったとします。

```scala
val firstNames = readRecordsFromTable("first_name", "people")
```

この呼び出しは次のように変換されます。

```scala
val firstNames = readRecordsFromTable("first_name", "people")(using aConnection)
```

呼び出し側で明示的に`using`を書きたい場合は、`readRecordsFromTable("first_name", "people")(using aConnection)`のように指定できます。

このような文脈を引き渡すための`using`引数は、Play FrameworkやO/Rマッパー、あるいは`ExecutionContext`（`Future`の章で詳述）のような実行コンテキストを必要とするライブラリでよく出てきます。

## 型クラスとgiven/using/summon

`using`引数のもう1つの使い方は風変わりです。Haskellなどの型クラスがある言語をご存知の人なら、型クラスそのものであると言う説明が分かりやすいかもしれません。多くの読者は型クラスについては知らないと思いますから、ここでは一から説明します。

`List`の全ての要素の値を加算した結果を返す`sum`メソッドを定義したいとします。このような要求は頻繁にあるので、定義できれば嬉しいことは間違いありません。問題はそのようなメソッドを素直に定義できない点にあります。

ポイントは「何の」`List`か全く分かっていないことです。何のリストか分からないということは、整数や浮動小数点数の`+`メソッドをそのまま使うことはできないということです。このような時に`using`引数の出番です。

2つの同じ型を足す（0の場合はそれに相当する値を返す）方法を知っている型を定義します。ここではその型を`Additive`とします。`Additive`の定義は次のようになります。

```scala mdoc:nest:silent
trait Additive[A] {
  def zero: A
  def plus(a: A, b: A): A
}
```

`Additive`の型パラメータ`A`は加算される`List`の要素型を表しています。また、

* `zero`：型パラメータ`A`の**0**に相当する値を返す
* `plus()`：型パラメータ`A`を持つ2つの値を**加算して**返す

です。

次に`Additive`型を使って、`List`の全ての要素を合計するメソッドを定義します。

```scala mdoc:nest:silent
def sum[A](lst: List[A])(a: Additive[A]) = lst.foldLeft(a.zero)((x, y) => a.plus(x, y))
```

最後に、型に応じた`zero`と`plus()`の定義を持ったオブジェクトを定義します。ここでは`String`と`Int`について、`Additive[Int]`と`Additive[String]`を定義します。

```scala mdoc:nest:silent
object StringAdditive extends Additive[String] {
  def plus(a: String, b: String): String = a + b
  def zero: String = ""
}

object IntAdditive extends Additive[Int] {
  def plus(a: Int, b: Int): Int = a + b
  def zero: Int = 0
}
```

まとめると次のようになります。

```scala mdoc:nest:silent
trait Additive[A] {
  def plus(a: A, b: A): A
  def zero: A
}

object StringAdditive extends Additive[String] {
  def plus(a: String, b: String): String = a + b
  def zero: String = ""
}

object IntAdditive extends Additive[Int] {
  def plus(a: Int, b: Int): Int = a + b
  def zero: Int = 0
}

def sum[A](lst: List[A])(a: Additive[A]) = lst.foldLeft(a.zero)((x, y) => a.plus(x, y))
```

`List[Int]`型と`List[String]`型のどちらでも、要素の合計を計算できる汎用的な`sum`メソッドができました。

実際に呼び出したいときには、

```scala mdoc:nest
sum(List(1, 2, 3))(IntAdditive)
sum(List("A", "B", "C"))(StringAdditive)
```

とすれば良いだけです。

これで目的を果たすことはできますが、何の`List`の要素を合計するかは型チェックする時点では分かっているのだから、`IntAdditive`や`StringAdditive`を明示的に渡さずとも賢く推論してほしいものです。実は、まさにそれを`using`引数と`given`定義で実現することができるのです。

方法は簡単。`StringAdditive`と`IntAdditive`を`given`として定義することと、`sum`の最後の引数リストにある`m`を`using`引数にするだけです。`given`を使った最終形は次のようになります。

```scala mdoc:nest
trait Additive[A] {
  def plus(a: A, b: A): A
  def zero: A
}

given StringAdditive: Additive[String] with {
  def plus(a: String, b: String): String = a + b
  def zero: String = ""
}

given IntAdditive: Additive[Int] with {
  def plus(a: Int, b: Int): Int = a + b
  def zero: Int = 0
}

def sum[A](lst: List[A])(using m: Additive[A]) = lst.foldLeft(m.zero)((x, y) => m.plus(x, y))

sum(List(1, 2, 3))

sum(List("A", "B", "C"))
```

任意のListの要素の合計値を求めるsumメソッドを自然な形で呼び出すことができています。

`given X: T with { ... }`はScala 3で型クラスのインスタンスを定義する標準的な形です。`with`の後ろのブロックに、その型クラスが要求するメソッドを実装します。インスタンスに名前を付けたくない場合は、`given Additive[Int] with { ... }`のように匿名で書くこともできます。

このような`using`引数と型クラスの組み合わせはプログラミング言語Haskellから借りてきたもので、Haskellでは型クラスと呼ばれます。そのため、Scalaでも型クラスと呼ばれることがよくあります。Haskellの用語だと、`Additive`に相当する宣言を **型クラスの宣言**、`StringAdditive`と`IntAdditive`を `Additive`型クラスの **インスタンスの定義** と呼びます。

なお、メソッドの本体から型クラスインスタンスを参照したいだけで、引数名を付けるのが冗長な場合は、`using`引数の名前を省略して`summon`で取り出すこともできます。

```scala mdoc:nest:silent
def sum2[A](lst: List[A])(using Additive[A]): A = {
  val m = summon[Additive[A]]
  lst.foldLeft(m.zero)((x, y) => m.plus(x, y))
}
```

`summon[T]`はScala 2の`implicitly[T]`に対応するもので、現在のスコープから型`T`の`given`値を取り出します。

さらに、型パラメータの直後で`using`引数を要求する短縮記法として **コンテキスト境界（context bound）** があります。`def sum[A: Additive](lst: List[A]): A`と書けば、暗黙のうちに`Additive[A]`を要求する`using`引数が追加されたかのように扱われます。

```scala mdoc:nest
def sum3[A: Additive](lst: List[A]): A = {
  val m = summon[Additive[A]]
  lst.foldLeft(m.zero)((x, y) => m.plus(x, y))
}

sum3(List(1, 2, 3))
sum3(List("A", "B", "C"))
```

型クラスを使った設計は標準ライブラリにも見られます。たとえば、

```scala mdoc:nest
List[Int]().sum

List(1, 2, 3, 4).sum

List(1.1, 1.2, 1.3, 1.4).sum
```

のように整数や浮動小数点数の合計値を計算することができます。これは、後述する`Numeric`型クラスのインスタンスが標準ライブラリに用意されているおかげです。Scalaで型クラスを定義・使用する方法を覚えると、設計の幅がグンと広がります。

### 練習問題 {#contextual_ex4}

`m: Additive[T]`と値`t1: T, t2: T, t3: T`は、次の条件を満たす必要があります。

```scala
m.plus(m.zero, t1) == t1  // 単位元
m.plus(t1, m.zero) == t1  // 単位元
m.plus(t1, m.plus(t2, t3)) == m.plus(m.plus(t1, t2), t3) // 結合則
```

条件を満たす型`T`と単位元`zero`、演算`plus`を探し出し、`Additive[T]`を定義しましょう。また、条件が満たされていることを確認してみましょう。定義した`Additive[T]`を`given`として宣言して、`T`の合計値を先ほどの`sum`で計算できることも確かめてみましょう。

ヒント：条件を満たす型は無数にありますが、たとえば`x`座標と`y`座標からなる点を表すクラス`Point`を考えてみると良いでしょう。

<!-- begin answer id="answer_ex2" style="display:none" -->

```scala mdoc:silent

trait Additive[A] {
  def plus(a: A, b: A): A
  def zero: A
}

given StringAdditive: Additive[String] with {
  def plus(a: String, b: String): String = a + b
  def zero: String = ""
}

given IntAdditive: Additive[Int] with {
  def plus(a: Int, b: Int): Int = a + b
  def zero: Int = 0
}

case class Point(x: Int, y: Int)

given PointAdditive: Additive[Point] with {
  def plus(a: Point, b: Point): Point = Point(a.x + b.x, a.y + b.y)
  def zero: Point = Point(0, 0)
}

def sum[A](lst: List[A])(using m: Additive[A]) = lst.foldLeft(m.zero)((x, y) => m.plus(x, y))
```

```scala mdoc:nest
println(sum(List(Point(1, 1), Point(2, 2), Point(3, 3)))) // Point(6, 6)
println(sum(List(Point(1, 2), Point(3, 4), Point(5, 6)))) // Point(9, 12)
```

<!-- end answer -->

### 練習問題 {#contextual_ex5}

`List[Int]` と `List[Double]` のsumを行うために、標準ライブラリでは何という型クラス（1つ）と型クラスのインスタンスを定義しているかを、[Scala標準ライブラリ](https://www.scala-lang.org/api/current/index.html)から探して挙げなさい。

<!-- begin answer id="answer_ex3" style="display:none" -->

型クラス：

* [Numeric[T]](https://www.scala-lang.org/api/3.8.3/scala/math/Numeric.html)

型クラスのインスタンス：

* [IntIsIntegral](https://www.scala-lang.org/api/3.8.3/scala/math/Numeric$$IntIsIntegral$.html)
* [DoubleIsFractional](https://www.scala-lang.org/api/3.8.3/scala/math/Numeric$$DoubleIsFractional$.html)

<!-- end answer -->

### givenの探索範囲

`given`値の探索範囲には、

- ローカルで`given`定義されたもの、または`using`引数として受け取っているもの
- importで取り込まれたもの（`import obj.given`で全ての`given`を取り込めます）
- スーパークラスやスーパートレイトで定義されたもの
- コンパニオンオブジェクトで定義されたもの

などがあります。
この中で注目していただきたいのが、コンパニオンオブジェクトに`given`値を定義するパターンです。

たとえば新しく`Rational`（有理数）型を定義したとして、コンパニオンオブジェクトに先ほど使った`Additive`型クラスのインスタンスを定義しておきます。

```scala
case class Rational(num: Int, den: Int)

object Rational {
  given RationalAdditive: Additive[Rational] with {
    def plus(a: Rational, b: Rational): Rational = {
      if (a == zero) {
        b
      } else if (b == zero) {
        a
      } else {
        Rational(a.num * b.den + b.num * a.den, a.den * b.den)
      }
    }
    def zero: Rational = Rational(0, 0)
  }
}
```

importをしていないのに、Additive型クラスのインスタンスを使うことができます。

```scala
scala> sum(List(Rational(1, 1), Rational(2, 2)))
val res0: Rational = Rational(4, 2)
```

新しくデータ型を定義し、型クラスインスタンスも一緒に定義したい場合によく出てくるパターンなので覚えておくとよいでしょう。

## Scala 2の`implicit`との対応

既存のScala 2コードや古い解説記事を読む際の手助けとして、Scala 2の`implicit`構文とScala 3での対応する書き方を整理しておきます。

```scala
// 暗黙の型変換
// Scala 2:
implicit def intToBoolean(arg: Int): Boolean = arg != 0
// Scala 3:
given intToBoolean: Conversion[Int, Boolean] = arg => arg != 0
```

```scala
// 拡張メソッド
// Scala 2:
implicit class RichString(src: String) {
  def smile: String = src + ":-)"
}
// Scala 3:
extension (src: String) {
  def smile: String = src + ":-)"
}
```

```scala
// 文脈の引き渡し（using引数）
// Scala 2:
def query(sql: String)(implicit conn: Connection): Result
// Scala 3:
def query(sql: String)(using conn: Connection): Result
```

```scala
// 型クラスインスタンスの定義
// Scala 2:
implicit object IntAdditive extends Additive[Int] { ... }
implicit val intAdditive: Additive[Int] = new Additive[Int] { ... }
// Scala 3:
given IntAdditive: Additive[Int] with { ... }
given Additive[Int] with { ... }  // 匿名
```

```scala
// 暗黙値の取り出し
// Scala 2:
implicitly[Additive[Int]]
// Scala 3:
summon[Additive[Int]]
```

Scala 3でも`implicit`キーワードは互換性のために引き続き利用できますが、新しく書くコードでは原則として`given`・`using`・`extension`を使うようにしましょう。

[^implicit-arity]: 引数が2つ以上ある`Conversion`相当の定義も技術的には可能ですが、暗黙の型変換としては引数1つの形が基本です。
