# implicitキーワード

Scalaには他の言語には見られないimplicitというキーワードで表現される機能があります。Scala 2ではimplicitという単一の機能によって複数の用途を賄うようになっていますが、1つの機能で色々な用途を表現できることがユーザーにとってわかりにくかったという反省もあり、Scala 3では用途別に異なるキーワードや構文を使う形になっています。

この章ではScala 2でのimplicitキーワードの4つの使い方を説明します。

## Implicit conversion

implicit conversionは暗黙の型変換をユーザが定義できる機能です。Scalaが普及し始めた時はこの機能が多用されたのですが、implicit conversionを多用するとプログラムが読みづらくなるということがわかったため、現在は積極的に使うことは推奨されていません。とはいえ、固定長整数から多倍長整数への変換など、標準ライブラリやサードパーティのライブラリで使われているケースもあるので知っておいて方が良いのは確かです。

implicit conversionは次のような形で定義します。 

```scala
  implicit def メソッド名(引数名: 引数の型): 返り値の型 = 本体
```

`implicit`というキーワードがついていることと引数が1つしかない[^implicit-arity]ことを除けば通常のメソッド定義同様です。implicit conversionでは引数の型と返り値の型に重要な意味があります。**引数の型**の式が現れたときに**返り値の型**を暗黙の型変換候補として登録することになるからです。

implicit conversionは次のようにして定義することができます。この例では、`Int`型から`Boolean`型への暗黙の型変換を定義しています。

```scala mdoc:nest
implicit def intToBoolean(arg: Int): Boolean = arg != 0

if(1) {
  println("1は真なり")
}
```

コンパイラは `if(1)` を見た時点で、本来`Boolean`が要求されているのに`Int`型の式である`1`が書かれていることがわかります。多くの静的型付き言語ではここで型エラーになります。しかし、Scalaでは引数が`Int`で返り値が`Boolean`である暗黙の型変換が定義されていないかを探索し、`intToBoolean`という暗黙の型変換を発見します。そして、以下のように`intToBoolean(1)`を挿入するのです。

```scala mdoc:nest
if(intToBoolean(1)) {
  println("1は真なり")
}
```

このようにして、`if`の条件式に`Int`を渡すことができるようになるわけです。ただし、暗黙の型変換のこのような使い方はあまり良いものではありません。if式の条件式に`Boolean`型の式しか渡せないようになっているのは間違いを防止するためなのに、そのチェックを通り抜けてしまえるわけですから。

`BigInt`や`BigDecimal`など一部のライブラリではScala標準の`Int`や`Double`と相互に変換するためにimplicit conversionを定義していますが、普通のユーザーが定義する必要があることは稀です。正当な理由を思いつかない限りは使わないようにしましょう。

Scala 3では`scala.Conversion`クラスのインスタンスを型クラス（後述）のインスタンスとして定義することで、implicit conversionを実現しています。しかし、Scala 2の場合と同様に利用するときは慎重になるべきです。

## Enrich my library

Enrich my libraryパターンと呼ばれるものがあります。C#やKotlinなどにある拡張メソッドと同等のもので、既存のクラスにメソッドを追加したようにみせかけることができます。Scala標準ライブラリの中にも利用例がありますし、サードパーティのライブラリでもよく見かけます。

たとえば、これまでみたプログラムの中には`(1 to 5)`という式がありましたが、本来`Int`型は`to`というメソッドを持っていません。

`to`メソッドはenrich my libraryパターンの典型的な利用例です。`Int`に対して`to`メソッドが定義されていないことがわかると、既存のimplicit conversionで定義されたメソッドの返り値型に`to`メソッドの定義がないか検索して、メソッドが見つかった場合に適切なimplicit conversionを挿入するのです。

この使い方では変換先の型は純粋にメソッドを追加するためだけに存在しているため、既存の型同士を変換するときのような混乱は起こりません。試しに、`String`の末尾に`":-)"`という文字列を追加して返すようにenrich my libraryパターンを使って

```scala mdoc:nest
class RichString(src: String) {
  def smile: String = src + ":-)"
}

implicit def enrichString(arg: String): RichString = new RichString(arg)

"Hi, ".smile
```

文字列の末尾に`":-)"`を追加する`smile`メソッドが定義できています。このとき、Scalaコンパイラは`enrichString("Hi, ")`の呼び出しを適切に挿入してくれます。

```scala mdoc:nest
enrichString("Hi, ").smile
```

しかし、拡張メソッドのためにimplicit conversionを毎回定義するのは冗長です。Scala 2.10以降ではclassにimplicitキーワードをつけることで簡潔な記述が可能になりました。上の定義は

```scala mdoc:reset
implicit class RichString(src: String) {
  def smile: String = src + ":-)"
}

"Hi, ".smile
```

という形で書きなおすことができます。implicit classはenrich my libraryパターン専用の機能なので、拡張メソッドを定義する意図を適切に表現できます。enrich my libraryパターンが必要なときは原則的にimplicit classを使うべきです。

しかし、サードパーティのライブラリや標準ライブラリではimplicit classが使われていないこともあるので、そのようなコードも読めるようにしておくのが良いでしょう。

さらに、Scala 3では以下のような拡張メソッドを定義するための専用構文が用意されました。

```scala mdoc:reset
extension (src: String) {
  def smile: String = src + ":-)"
}
```

### 練習問題 {#implicit_ex1}

`Int`から`BigInt`へのimplicit conversionのように、利用者にとって便利になるimplicit conversionを考えて定義してみてください。そのimplicit conversionにはどのような利点と欠点があるかを答えてください。

### 練習問題 {#implicit_ex2}

既存のクラスの利用を便利にするような形で、enrich my libraryパターンを適用してみましょう。どのような場面で役に立つでしょうか？

<!-- begin answer id="answer_ex1" style="display:none" -->

```scala mdoc:nest:silent
object Taps {
  implicit class Tap[T](self: T) {
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
import Taps._
Taps.main(Array())
```

定義した`tap()`メソッドはRubyなどの言語にありますが、メソッドチェインの中でデバッグプリントをはさみたいときに役に立ちます。

<!-- end answer -->

#### 練習問題 {#implicit_ex3}

[Scala標準ライブラリ](https://www.scala-lang.org/api/current/index.html)の中からenrich my libraryが使われている例を1つ以上見つけてください。どのような時に便利でしょうか？

## Implicit parameter（文脈引き渡し）

implicit parameterは主に2つの目的で使われます。1つ目の目的は、あちこちのメソッドに共通で渡されるオブジェクト（たとえば、ソケットやデータベースのコネクション）を明示的に引き渡すのを省略することです。

データベースとのコネクションを表す`Connection`型があるとします。データベースと接続するメソッドには全て`Connection`型を渡さなければなりません。

```
def readRecordsFromTable(columnName: String, tableName: String, connection: Connection): List[Record]
def writeRecordsToTable(record: List[Record], tableName: String, connection: Connection): Unit
def readAllFromTable(tableName: String, connection: Connection): List[Row]
```

3つのメソッドは全て`Connection`型を引数に取るのに、呼びだす度に明示的に`Connection`オブジェクトを渡さなければいけません。ここでimplicit parameterの出番です。上のメソッド定義を

```scala
def readRecordsFromTable(columnName: String, tableName: String)(implicit connection: Connection): List[Record]
def writeRecordsToTable(records: List[Record], tableName: String)(implicit connection: Connection): Unit
def readAllFromTable(tableName: String, connection: Connection)(implicit connection: Connection): List[Record]
```

と書き換えます。implicit修飾子は最後の引数リストに付けなければならないという制約があります。つまり、以下のようになっているのがポイントです。

```scala
(....)(implicit conn: Connection)
```

Scalaコンパイラは、このように定義されたメソッドが呼び出されると、現在の呼び出しスコープからたどって直近のimplicitとマークされた値を暗黙にメソッドに引き渡します。たとえば次のようにして、値をimplicitとしてマークします：

```scala
implicit val aConnection: Connection = connectDatabase(....)
```

こうすれば、最後の引数リストに暗黙に`Connection`オブジェクトを渡してくれるのです。のような呼び出しがあったとします。

```scala
val firstNames = readRecordsFromTable("first_name", "people")
```

この呼出しは次のように変換されます。

```scala
val firstNaemes = readRecordsFromTable("first_name", "people")(aConnection)
```

このような文脈を引き渡すためのimplicit parameterはPlay FrameworkやO/Rマッパーなどで出てきます。

## Implicit parameter（型クラス）

implicit parameterのもう1つの使い方は風変わりです。Haskellなどの型クラスがある言語をご存知の人なら、型クラスそのものであると言う説明がわかりやすいかもしれません。多くの読者は型クラスについては知らないと思いますから、ここでは一から説明します。

`List`の全ての要素の値を加算した結果を返す`sum`メソッドを定義したいとします。このような要求は頻繁にあるので、定義できれば嬉しいことは間違いありません。問題はそのようなメソッドを素直に定義できない点にあります。

ポイントは「何の」`List`か全くわかっていないことです。何のリストかわからないということは、整数や浮動小数点数の`+`メソッドをそのまま使うことはできないということです。このような時にimplicit parameterの出番です。

2つの同じ型を足す（0の場合はそれに相当する値を返す）方法を知っている型を定義します。ここではその型を`Additive`とします。`Additive`の定義は次のようになります：

```scala mdoc:nest:silent
trait Additive[A] {
  def zero: A
  def plus(a: A, b: A): A
}
```

`Additive`の型パラメータ`A`は加算される`List`の要素型を表しています。また、

* `zero`: 型パラメータ`A`の**0**に相当する値を返す
* `plus()`: 型パラメータ`A`を持つ2つの値を**加算して**返す

です。

次に`Additive`型を使って、`List`の全ての要素を合計するメソッドを定義します：

```scala mdoc:nest:silent
def sum[A](lst: List[A])(a: Additive[A]) = lst.foldLeft(a.zero)((x, y) => a.plus(x, y))
```

最後に、型に応じた`zero`と`plus()`の定義を持ったobjectを定義します。ここでは`String`と`Int`について、`Additive[Int]`と`Additive[String]`を定義します。

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

これで目的は果たすことはできますが、何の`List`の要素を合計するかは型チェックする時点ではわかっているのだから`IntAdditive`, `StringAdditive`を明示的に渡さずとも賢く推論してほしいものです。実は、まさにそれをimplicit parameterで実現することができるのです。

方法は簡単。`StringAdditive`と`IntAdditive`の定義の前にimplicitと付けることと、`sum`の最後の引数リストである`m`にimplicitを付けるだけです。implicit parameterを使った最終形は次のようになります。

```scala mdoc:nest
trait Additive[A] {
  def plus(a: A, b: A): A
  def zero: A
}

implicit object StringAdditive extends Additive[String] {
  def plus(a: String, b: String): String = a + b
  def zero: String = ""
}

implicit object IntAdditive extends Additive[Int] {
  def plus(a: Int, b: Int): Int = a + b
  def zero: Int = 0
}

def sum[A](lst: List[A])(implicit m: Additive[A]) = lst.foldLeft(m.zero)((x, y) => m.plus(x, y))

sum(List(1, 2, 3))

sum(List("A", "B", "C"))
```

任意のListの要素の合計値を求めるsumメソッドを自然な形で呼びだすことができています。

implicit parameterのこのような使い方はプログラミング言語Haskellから借りてきたもので、Haskellでは型クラスと呼ばれます。そのため、Scalaでも型クラスと呼ばれることも多々あります。Haskellの用語だと、`Additive`に相当する宣言を型クラスの宣言、`StringAdditive`と`IntAdditive`を`Additive`型クラスのインスタンスの定義と呼びます。

implicit parameterの型クラス的な用法は標準ライブラリにもあります。たとえば、

```scala mdoc:nest
List[Int]().sum

List(1, 2, 3, 4).sum

List(1.1, 1.2, 1.3, 1.4).sum
```

のように整数や浮動小数点数の合計値を計算することができます。これは、implicit parameterのおかげです。Scalaで型クラスを定義・使用する方法を覚えると設計の幅がグンと広がります。

### 練習問題 {#implicit_ex4}

`m: Additive[T]`と値`t1: T, t2: T, t3: T`は、次の条件を満たす必要があります。

```scala
m.plus(m.zero, t1) == t1  // 単位元
m.plus(t1, m.zero) == t1  // 単位元
m.plus(t1, m.plus(t2, t3)) == m.plus(m.plus(t1, t2), t3) // 結合則
```

条件を満たす型`T`と単位元`zero`、演算`plus`を探し出し、`Additive[T]`を定義しましょう。また、条件が満たされていることを確認してみましょう。定義した`Additive[T]`を`implicit`にして、`T`の合計値を先ほどの`sum`で計算できることも確かめてみましょう。

ヒント：条件を満たす型は無数にありますが、たとえば`x`座標と`y`座標からなる点を表すクラス`Point`を考えてみると良いでしょう。

<!-- begin answer id="answer_ex2" style="display:none" -->

```scala mdoc:silent

trait Additive[A] {
  def plus(a: A, b: A): A
  def zero: A
}

implicit object StringAdditive extends Additive[String] {
  def plus(a: String, b: String): String = a + b
  def zero: String = ""
}

implicit object IntAdditive extends Additive[Int] {
  def plus(a: Int, b: Int): Int = a + b
  def zero: Int = 0
}

case class Point(x: Int, y: Int)

implicit object PointAdditive extends Additive[Point] {
  def plus(a: Point, b: Point): Point = Point(a.x + b.x, a.y + b.y)
  def zero: Point = Point(0, 0)
}

def sum[A](lst: List[A])(implicit m: Additive[A]) = lst.foldLeft(m.zero)((x, y) => m.plus(x, y))
```

```scala mdoc:nest
println(sum(List(Point(1, 1), Point(2, 2), Point(3, 3)))) // Point(6, 6)
println(sum(List(Point(1, 2), Point(3, 4), Point(5, 6)))) // Point(9, 12)
```

<!-- end answer -->

### 練習問題 {#implicit_ex5}

`List[Int]` と `List[Double]` のsumを行うために、標準ライブラリでは何という型クラス（1つ）と型クラスのインスタンスを定義しているかを、[Scala標準ライブラリ](https://www.scala-lang.org/api/current/index.html)から探して挙げなさい。

<!-- begin answer id="answer_ex3" style="display:none" -->

型クラス：

* [Numeric[T]](https://www.scala-lang.org/api/3.8.1/scala/math/Numeric.html)

型クラスのインスタンス：

* [IntIsIntegral](https://www.scala-lang.org/api/3.8.1/scala/math/Numeric$$IntIsIntegral$.html)
* [DoubleIsFractional](https://www.scala-lang.org/api/3.8.1/scala/math/Numeric$$DoubleIsFractional$.html)

<!-- end answer -->

### implicitの探索範囲

implicit conversionやimplicit parameterの値が探索される範囲には、

- ローカルで定義されたもの
- importで指定されたもの
- スーパークラスで定義されたもの
- コンパニオンオブジェクトで定義されたもの

などがあります。
この中で注目していただきたいのが、コンパニオンオブジェクトでimplicitの値を定義するパターンです。

たとえば新しく`Rational`（有理数）型を定義したとして、コンパニオンオブジェクトに先ほど使った`Additive`型クラスのインスタンスを定義しておきます。

```scala
case class Rational(num: Int, den: Int)

object Rational {
  implicit object RationalAdditive extends Additive[Rational] {
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
res0: Rational = Rational(4,2)
```

新しくデータ型を定義し、型クラスインスタンスも一緒に定義したい場合によく出てくるパターンなので覚えておくとよいでしょう。

[^implicit-arity]: 引数が2つ以上あるimplicit defの定義も可能です。「implicit defのパラメーターにimplicitが含まれる」という型クラス的な使い方をする場合は実際にimplicit defに2つ以上のパラメーターが出現することがあります。ただしそういった定義は通常implicit conversionとは呼ばれません
