# implicitキーワード

Scalaには他の言語には見られないimplicitというキーワードで表現される機能があります。仕様上はimplicitという単一の機能によって複数の用途を賄うようになっていますが、一つのキーワードで色々な用途を表現できることがユーザーにとってわかりにくかったという反省もあり、Scala 3では用途別に異なるキーワードや構文を使う形になっています。

この章ではScala 2でのimplicitキーワードの4つの使い方について説明します。

## Implicit conversion

implicit conversionは暗黙の型変換をユーザが定義できる機能です。Scalaが普及し始めた時はこの機能が多用されたのですが、一方でimplicit conversionを多用するとプログラムが読みづらくなるということがわかったため、現在のScalaコミュニティでは積極的に使うことは推奨されていません。とはいえ、固定長整数から多倍長整数への変換など、ライブラリで使われているケースもあるので知っておいて損はありません。

implicit conversionは次のような形で定義します。 

```scala
  implicit def メソッド名(引数名: 引数の型): 返り値の型 = 本体
```

`implicit`というキーワードがついていることと引数が1つしかない[^implicit-arity]ことを除けば通常のメソッド定義同様です。さて、implicit conversionでは引数の型と返り値の型に重要な意味があります。何故なら、**引数の型**の式が現れたときに**返り値の型**を暗黙の型変換候補として登録することになるからです。

implicit conversionは例えば次のようにして定義することができます。この例では、`Int`型から`Boolean`型への暗黙の型変換を定義しています。

```scala mdoc:nest
implicit def intToBoolean(arg: Int): Boolean = arg != 0

if(1) {
  println("1は真なり")
}
```

コンパイラは `if(1)` を見た時点で、本来`Boolean`が要求されているのに`Int`型の式である1が書かれていることがわかります。多くの静的型付き言語ではここで型エラーになります。しかし、Scalaコンパイラ
は引数が`Int`で返り値が`Boolean`である暗黙の型変換が定義されていないか探索し、結果として`intToBoolean`という暗黙の型変換を発見します。そして、以下のように`intToBoolean(1)`を挿入するのです。

```scala mdoc:nest
if(intToBoolean(1)) {
  println("1は真なり")
}
```

こうして、`if`の条件式にInt`を渡すことができるようになるのです。ただし、暗黙の型変換のこのような使い方はあまり良いものではありません。上の例をみればわかる通り、if式の条件式に`Boolean`型の式しか渡せないようになっているのは間違いを防止するためなのに、そのチェックを通り抜けてしまえるのです。

`BigInt`や`BigDecimal`など一部のライブラリではScala標準の`Int`や`Double`と相互に変換するためにimplicit conversionを定義していますが、普通のユーザーが定義する必要があることは稀です。利用を正当化できる適切な理由を思いつかない限りは使わないようにしましょう。

Scala 3では`scala.Conversion`クラスのインスタンスを型クラス（後述）のインスタンスとして定義することで、implicit conversionを実現しています。しかし、Scala 2の場合と同様に利用するときは慎重になるべきです。

## Enrich my library

別の使い方として、enrich my libraryパターンと呼ばれるものがあります。最近の言語にある拡張メソッドと同等のもので、既存のクラスにメソッドを追加したようにみせかけることができます。Scala標準ライブラリの中にも大量の使用例がありますし、サードパーティのライブラリでも非常によく見かけます。

たとえば、これまでみたプログラムの中には`(1 to 5)`という式がありましたが、本来`Int`型は`to`というメソッドを持っていません。

`to`メソッドはenrich my libraryパターンの使用例の典型的なものです。コンパイラは、`Int`に対して`to`メソッドが定義されていないことを検出すると、implicit conversionで定義された返り値の型に`to`メソッドの定義がないか検索して、メソッドが見つかった場合に適切なimplicit conversionを挿入するのです。

この使い方では変換先の型は純粋にメソッドを追加するためだけに存在しているため、既存の型同士を変換するときのような混乱はあまり起こりません。Scala 3ではこのような定義の仕方が遠回り過ぎると判断されたのか、拡張メソッドを定義するための専用構文が用意されました。

しかし、Scala 3が実用で利用できるのはまだ先です。当面はenrich my libraryパターンを使うと考えておきましょう。試しに、`String`の末尾に`":-)"`という文字列を追加して返すようにenrich my libraryパターンを使ってみましょう。

```scala mdoc:nest
class RichString(val src: String) {
  def smile: String = src + ":-)"
}

implicit def enrichString(arg: String): RichString = new RichString(arg)

"Hi, ".smile
```

ちゃんと文字列の末尾に`":-)"`を追加する`smile`メソッドが定義できています。このとき、Scalaコンパイラは以下のように`enrichString("Hi, ")`の呼び出しを適切に挿入してくれます。

```scala mdoc:nest
enrichString("Hi, ").smile
```

しかし、拡張メソッドのためにimplicit conversionを毎回定義するのは冗長です。Scala 2.10以降ではclassにimplicitキーワードをつけることで同じようなことができるようになりました。上の定義はScala 2.10以降では、

```scala mdoc:reset
implicit class RichString(val src: String) {
  def smile: String = src + ":-)"
}

"Hi, ".smile
```

という形で書きなおすことができます。implicit classはenrich my libraryパターン専用の機能なので、拡張メソッドを定義するという意図を適切に表現できます。現在のScalaでenrich my libraryパターンが必要なときは原則的にimplicit classを使うべきです。

しかし、サードパーティのライブラリや標準ライブラリではimplicit classが使われていないこともあるので、そのようなコードも読めるようにしておくのが良いのでしょう。

### 練習問題 {#implicit_ex1}

`Int`から`BigInt`へのimplicit conversionのように、利用者にとって便利になるimplicit conversionを考えて定義してみてください。そのimplicit conversionにはどのような利点と欠点があるか考えてみてください。

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

メソッドチェインの中でデバッグプリントをはさみたいときに役に立ちます。

<!-- end answer -->

#### 練習問題 {#implicit_ex3}

[Scala標準ライブラリ](https://www.scala-lang.org/api/current/index.html)の中からenrich my libraryが使われている例を1つ以上見つけてください。それはどのような時に便利でしょうか？

## Implicit parameter（文脈引き渡し）

implicit parameterは主に2つの目的で使われます。1つ目の目的は、あちこちのメソッドに共通で引き渡されるオブジェクト（たとえば、ソケットやデータベースのコネクションなど）を明示的に引き渡すのを省略することです。

たとえば、データベースとのコネクションを表す`Connection`型があるとします。データベースと接続するメソッドには全てこのConnection型を引き渡さなければなりません。

```
def readRecordsFromTable(columnName: String, tableName: String, connection: Connection): List[Record]
def writeRecordsToTable(record: List[Record], tableName: String, connection: Connection): Unit
def readAllFromTable(tableName: String, connection: Connection): List[Row]
```

3つのメソッドは全て`Connection`型を引数に取るのに、呼びだす度に明示的に`Connection`オブジェクトを渡さなけれいけません。ここでimplicit parameterの出番です。上のメソッド定義を

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

こうすれば、最後の引数リストに暗黙に`Connection`オブジェクトを渡してくれるのです。たとえば、次のような呼び出しがあったとします。

```scala
val firstNames = readRecordsFromTable("first_name", "people")
```

この呼出しは次のように変換されます。

```scala
val firstNames = readRecordsFromTable("first_name", "people")(aConnection)
```

このように、implicit parameterを文脈を引き渡すための使い方はPlay FrameworkやScalaの各種O/Rマッパーで頻出します。

## Implicit parameter（型クラス）

implicit parameterのもう1つの使い方は少し風変わりです。Haskellなどの型クラスがある言語をご存知の人なら、型クラスそのものであると言う説明がわかりやすいかもしれません。ただし、多くの読者は型クラスについては知らないと思いますから、ここでは一から説明します。

まず、`List`の全ての要素の値を加算した結果を返す`sum`メソッドを定義したいとします。このような要求は頻繁にあるもので、定義できれば嬉しいことは間違いありません。問題は素直にはそのようなメソッドが定義できない点にあります。

何故でしょうか。ポイントは「何の」`List`か全くわかっていないことです。何のリストかわからないということは、整数や浮動小数点数の`+`メソッドをそのまま使うことはできないということです。このような時にimplicit parameterの出番です。

まず、2つの同じ型を足す（0の場合はそれに相当する値を返す）方法を知っている型を定義します。ここではその型を`Additive`とします。`Additive`の定義は次のようになります：

```scala mdoc:nest:silent
trait Additive[A] {
  def zero: A
  def plus(a: A, b: A): A
}
```

`Additive`の型パラメータ`A`は加算される`List`の要素の型を表しています。また、

* `zero`: 型パラメータAの**0**に相当する値を返す
* `plus()`: 型パラメータAを持つ2つの値を**加算して**返す

です。

次に`Additive`型を使って、`List`の全ての要素を合計するメソッドを定義します：

```scala mdoc:nest:silent
def sum[A](lst: List[A])(m: Additive[A]) = lst.foldLeft(m.zero)((x, y) => m.plus(x, y))
```

最後に、それぞれの型に応じた加算と0の定義を持ったobjectを定義します。ここでは`String`と`Int`について、`Additive[Int]`と`Additive[String]`を定義します。

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

def sum[A](lst: List[A])(m: Additive[A]) = lst.foldLeft(m.zero)((x, y) => m.plus(x, y))
```

`List[Int]`型と`List[String]`型のどちらでも、要素の合計を計算できる汎用的な`sum`メソッドができました。

実際に呼び出したいときには、

```scala mdoc:nest
sum(List(1, 2, 3))(IntAdditive)
sum(List("A", "B", "C"))(StringAdditive)
```

とすれば良いだけです。

これで目的は果たすことはできますが、何の`List`の要素を合計するかは型チェックする時点ではわかっているのだから`IntAdditive`, `StringAdditive`を明示的に渡さずとも賢く推論してほしいものです。まさにそれをimplicit parameterで実現することができるのです。

方法は簡単で、`StringAdditive`と`IntAdditive`の定義の前にimplicitと付けることと、`sum`の最後の引数リストである`m`にimplicitを付けるだけです。implicit parameterを使った最終形は次のようになります。

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

任意のListの要素の合計値を求めるsumメソッドを自然な形（`sum(List(1, 2, 3))`）で呼びだすことができています。

implicit parameterのこのような使い方はプログラミング言語Haskellから借りてきたもので、型クラス（あるいは型クラスを使った計算）と言われます。Haskellの用語だと、`Additive`を型クラス、`StringAdditive`と`IntAdditive`を`Additive`型クラスのインスタンスと呼びます。

implicit parameterの型クラス的用法は標準ライブラリにもあります。たとえば、

```scala mdoc:nest
List[Int]().sum

List(1, 2, 3, 4).sum

List(1.1, 1.2, 1.3, 1.4).sum
```

のように整数や浮動小数点数の合計値を特に気にとめることなく計算することができます。これは、implicit parameterのおかげです。Scalaで型クラスを定義・使用する方法を覚えると設計の幅がグンと広がります。

### 練習問題 {#implicit_ex4}

`m: Additive[T]`と値`t1: T, t2: T, t3: T`は、次の条件を満たす必要があります。

```scala
m.plus(m.zero, t1) == t1  // 単位元
m.plus(t1, m.zero) == t1  // 単位元
m.plus(t1, m.plus(t2, t3)) == m.plus(m.plus(t1, t2), t3) // 結合則
```

条件を満たす型`T`と単位元`zero`、演算`plus`を探し出し、`Additive[T]`を定義しましょう。また、条件が満たされていることを確認してみましょう。定義した`Additive[T]`を`implicit`にして、`T`の合計値を先ほどの`sum`で計算できることも確かめてみましょう。

ヒント：条件を満たす型は無数にありますが、たとえば`x`座標と`y`座標からなる点を表すクラス`Point`とそれに対応する`implicit object PointAdditive`を考えてみると良いでしょう。

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

* [Numeric[T]](https://www.scala-lang.org/api/2.13.8/scala/math/Numeric.html)

型クラスのインスタンス：

* [IntIsIntegral](https://www.scala-lang.org/api/2.13.8/scala/math/Numeric$$IntIsIntegral$.html)
* [DoubleIsFractional](https://www.scala-lang.org/api/2.13.8/scala/math/Numeric$$DoubleIsFractional$.html)

<!-- end answer -->

### implicitの探索範囲

implicit conversionやimplicit parameterの値が探索される範囲には次のようなものがあります。

- メソッドローカルに定義されたもの
- importされたobject内に定義されたもの
- スーパークラスで定義されたもの
- コンパニオンオブジェクト内に定義されたもの

この中で注目していただきたいのが、コンパニオンオブジェクトの中でimplicitな値を定義するパターンです。何故ならば、コンパニオンオブジェクトにimplicitな値を定義しておくことでScalaコンパイラが自動的に探索してくれるからです。

新しく`Rational`（有理数）型を定義したとして、コンパニオンオブジェクトに先ほど使った`Additive`型クラスのインスタンスを定義しておきます。

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

このとき、importをしていないのに、このAdditive型クラスのインスタンスを使うことができます。

```scala
scala> sum(List(Rational(1, 1), Rational(2, 2)))
res0: Rational = Rational(4,2)
```

このように、コンパニオンオブジェクトにimplicit objectを定義しておくことで、新しいデータ型を定義すると同時に型クラスのインスタンスも定義することができます。よく出てくるパターンなので覚えておくとよいでしょう。

[^implicit-arity]: 引数が2つ以上あるimplicit defの定義も可能です。「implicit defのパラメーターにimplicitが含まれる」という型クラス的な使い方をする場合は実際にimplicit defに2つ以上のパラメーターが出現することがあります。ただしそういった定義は通常implicit conversionとは呼ばれません
