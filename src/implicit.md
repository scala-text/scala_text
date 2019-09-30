# implicit conversion（暗黙の型変換）とimplicit parameter（暗黙のパラメータ）

Scalaには、他の言語にはあまり見られない、implicit conversionとimplicit parameterという機能があります。
この2つの機能を上手く使いこなすことができれば、Scalaでのプログラミングの生産性は劇的に向上するでしょう。
なお、本当は、implicit conversionとimplicit parameterは相互に関係がある2つの機能なのですが、今回学習する
範囲では意識する必要がないと思われるので、2つの独立した機能として学びます。

## Implicit Conversion

implicit conversionは暗黙の型変換機能をユーザが定義できるようにする機能です。

implicit conversionは

```scala
  implicit def メソッド名(引数名: 引数の型): 返り値の型 = 本体
```

という形で定義します。`implicit`というキーワードがついていることと引数が1つしかない[^implicit-arity]ことを除けば通常のメソッド定義
と同様ですね。さて、implicit conversionでは、引数の型と返り値の型に重要な意味があります。それは、これが、
引数の型の式が現れたときに返り値の型を暗黙の変換候補として登録することになるからです。

定義したimplicit conversionは大きく分けて二通りの使われ方をします。1つは、新しく定義したユーザ定義の型などを
既存の型に当てはめたい場合です。たとえば、

```tut
implicit def intToBoolean(arg: Int): Boolean = arg != 0

if(1) {
  println("1は真なり")
}
```

といった形で、本来`Boolean`しか渡せないはずのif式に`Int`を渡すことができています。ただし、この使い方はあまり良いものではありません。上の例をみればわかる通り、implicit conversionを定義することで、コンパイラにより、本来はif式の条件式には`Boolean`型の式しか渡せないようにチェックしているのを通りぬけることができてしまうからです。一部のライブラリではそのライブラリ内のデータ型とScala標準のデータ型を相互に変換するために、そのようなimplicit conversionを定義している例がありますが、本当にそのような変換が必要かよく考える必要があるでしょう。

### pimp my library

もう1つの使い方は、pimp my libraryパターンと呼ばれ、既存のクラスにメソッドを追加して拡張する（ようにみせかける）使い方です。
Scala標準ライブラリの中にも大量の使用例があり、こちらが本来の使い方と言って良いでしょう。たとえば、
これまでみたプログラムの中に`(1 to 5)`という式がありましたが、本来Int型は`to`というメソッドを持っていません。
`to`メソッドはpimp my libraryパターンの使用例の最たるものです。コンパイラは、ある型に対するメソッド呼び出しを
見つけたとき、そのメソッドを定義した型がimplicit conversionの返り値の型にないか探索し、型が合ったらimplicit conversion
の呼び出しを挿入するのです。この使い方の場合、implicit conversionの返り値の型が他で使われるものでなければ安全に
implicit conversionを利用することができます。

試しに、`String`の末尾に`":-)"`という文字列を追加して返すimplicit conversionを定義してみましょう。

```tut
class RichString(val src: String) {
  def smile: String = src + ":-)"
}

implicit def enrichString(arg: String): RichString = new RichString(arg)

"Hi, ".smile
```

ちゃんと文字列の末尾に`":-)"`を追加する`smile`メソッドが定義できています。さて、ここでひょっとしたら気がついた方もいるかもしれませんが、implicit conversionはそのままでは、既存のクラスへのメソッド追加のために使用するには冗長であるということです。Scala 2.10からは、classにimplicit
というキーワードをつけることで同じようなことができるようになりました（皆さんが学習するのはScala 2.13なので気にする必要はありません。

### Implicit Class

上の定義は、Scala 2.10以降では、

```tut:reset
implicit class RichString(val src: String) {
  def smile: String = src + ":-)"
}

"Hi, ".smile
```

という形で書きなおすことができます。
implicit classはpimp my libraryパターン専用の機能であり、implicit defで既存型への変換した場合などによる混乱がないため、Scala 2.10以降でpimp my libraryパターンを使うときは基本的に後者の形式にすべきですが、
サードパーティのライブラリや標準ライブラリでも前者の形式になっていることがあるので、そのようなコードも読めるようにして
おきましょう。

### 練習問題 {#implicit_ex1}

`Int`から`Boolean`へのimplicit conversionのように利用者を混乱させるようなものを考えて、定義してみてください。また、そのimplicit
conversionにはどのような危険があるかを考えてください。

### 練習問題 {#implicit_ex2}

pimp my libraryパターンで、既存のクラスの利用を便利にするようなimplicit conversionを1つ定義してみてください。それはどのような
場面で役に立つでしょうか？

<!-- begin answer id="answer_ex1" style="display:none" -->

```tut:silent
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

```tut
import Taps._
Taps.main(Array())
```

メソッドチェインの中でデバッグプリントをはさみたいときに役に立ちます。

<!-- end answer -->

#### 練習問題 {#implicit_ex3}

[Scala標準ライブラリ](https://www.scala-lang.org/api/current/index.html)の中からpimp my libraryが使われている例を（先ほど挙げた
ものを除いて）1つ以上見つけてください。

## Implicit Parameter

implicit parameterは主として2つの目的で使われます。1つの目的は、あちこちのメソッドに共通で引き渡されるオブジェクト（たとえば、
ソケットやデータベースのコネクションなど）を明示的に引き渡すのを省略するために使うものです。これは例で説明すると非常に簡単に
わかると思います。

まず、データベースとのコネクションを表す`Connection`型があるとします。データベースと接続するメソッドは全てこのConnection型を引き
渡さなければなりません。

```
def useDatabase1(...., conn: Connection)
def useDatabase2(...., conn: Connection)
def useDatabase3(...., conn: Connection)
```

この3つのメソッドは共通して`Connection`型を引数に取るのに、呼びだす度に明示的に`Connection`オブジェクトを渡さなければならず面倒で仕方ありません。
ここでimplicit parameterの出番です。上のメソッド定義を

```scala
def useDatabase1(....)(implicit conn: Connection)
def useDatabase2(....)(implicit conn: Connection)
def useDatabase3(....)(implicit conn: Connection)
```

のように書き換えます。implicit修飾子は引数の先頭の要素に付けなければならないという制約があり、implicit parameterを使うにはカリー化されたメソッド定義が必要になります。最後の引数リストが

```scala
(implicit conn: Connection)
```

とあるのがポイントです。Scalaコンパイラは、このようにして定義されたメソッドが呼び出されると、現在のスコープからたどって直近のimplicitと
マークされた値を暗黙にメソッドに引き渡します。値をimplicitとしてマークするとは、たとえば次のようにして行います：

```scala
implicit val connection: Connection = connectDatabase(....)
```

このようにすることで、最後の引数リストに暗黙に`Connection`オブジェクトを渡してくれるのです。このようなimplicit parameterの使い方はPlay 2 FrameworkやScalaの各種O/Rマッパーで頻出します。

implicit parameterのもう1つの使い方は、少々変わっています。まず、`List`の全ての要素の値を加算した結果を返す`sum`メソッドを定義したいとします。
このメソッドはどのような定義になるでしょうか。ポイントは、「何の」`List`か全くわかっていないことで、整数の`+`メソッドをそのまま使ったりという
ことはそのままではできないということです。このような場合、2つの手順を踏みます。

まず、2つの同じ型を足す（0の場合はそれに相当する値を返す）方法を知っている型を定義します。ここではその型を`Additive`とします。
`Additive`の定義は次のようになります：

```tut:silent
trait Additive[A] {
  def plus(a: A, b: A): A
  def zero: A
}
```

ここで、`Additive`の型パラメータ`A`は加算される`List`の要素の型を表しています。また、

* `zero`: 型パラメータAの**0**に相当する値を返す
* `plus`: 型パラメータAを持つ2つの値を**加算して**返す

です。

次に、この`Additive`型を使って、`List`の全ての要素を合計するメソッドを定義します：

```tut:silent
def sum[A](lst: List[A])(m: Additive[A]) = lst.foldLeft(m.zero)((x, y) => m.plus(x, y))
```

後は、それぞれの型に応じた加算と0の定義を持ったobjectを定義します。ここでは`String`と`Int`について定義をします。

```tut:silent
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

```tut:silent
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
これで、`Int`型の`List`も`String`型の`List`のどちらの要素の合計も計算できる汎用的な`sum`メソッドができました。
実際に呼び出したいときには、

```tut
sum(List(1, 2, 3))(IntAdditive)
sum(List("A", "B", "C"))(StringAdditive)
```

とすれば良いだけです。さて、これで目的は果たすことはできましたが、何の`List`の要素を合計するかは型チェックする時点ではわかって
いるのだからいちいち`IntAdditive`, `StringAdditive`を明示的に渡さずとも賢く推論してほしいものです。そして、まさにそれをimplicit
parameterで実現することができます。方法は簡単で、`StringAdditive`と`IntAdditive`の定義の前にimplicitと付けることと、`sum`の最後の引数リストの`m`にimplicitを付けるだけです。implicit parameterを使った最終形は次のようになります。

```tut
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
実は、implicit parameterのこのような使い方はプログラミング言語Haskellから借りてきたもので、型クラス（を使った計算）と言われます。Haskellの用語だと、`Additive`を型クラス、`StringAdditive`と`IntAdditive`を`Additive`型クラスのインスタンスと
呼びます。

このimplicit parameterの用法は標準ライブラリにもあって、たとえば、

```tut
List[Int]().sum

List(1, 2, 3, 4).sum

List(1.1, 1.2, 1.3, 1.4).sum
```

のように整数や浮動小数点数の合計値を特に気にとめることなく計算することができています。Scalaにおいて型クラスを定義・
使用する方法を覚えると、設計の幅がグンと広がります。

### 練習問題 {#implicit_ex4}

`m: Additive[T]`と値`t1: T, t2: T, t3: T`は、次の条件を満たす必要があります。

```scala
m.plus(m.zero, t1) == t1  // 単位元
m.plus(t1, m.zero) == t1  // 単位元
m.plus(t1, m.plus(t2, t3)) == m.plus(m.plus(t1, t2), t3) // 結合則
```
このような条件を満たす型`T`と単位元`zero`、演算`plus`を探し出し、`Additive[T]`を定義しましょう。この際、条件が満たされていることをいくつかの入力に対して確認してみましょう。また、定義した`Additive[T]`を`implicit`にして、`T`の合計値を先ほどの`sum`で計算できることを確かめてみましょう。

ヒント：このような条件を満たすものは無数にありますが、思いつかない人はたとえば`x`座標と`y`座標からなる点を表すクラス`Point`を考えてみると良いでしょう。

<!-- begin answer id="answer_ex2" style="display:none" -->

```tut:silent

object Additives {
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
}
```

```tut
import Additives._
println(sum(List(Point(1, 1), Point(2, 2), Point(3, 3)))) // Point(6, 6)
println(sum(List(Point(1, 2), Point(3, 4), Point(5, 6)))) // Point(9, 12)
```

<!-- end answer -->

### 練習問題 {#implicit_ex5}

`List[Int]` と `List[Double]` のsumを行うために、標準ライブラリでは何という型クラス（1つ）と型クラスのインスタンス
（2つ）を定義しているかを、[Scala標準ライブラリ](https://www.scala-lang.org/api/current/index.html)から
探して挙げなさい。

<!-- begin answer id="answer_ex3" style="display:none" -->

型クラス：

* [Numeric[T]](https://www.scala-lang.org/api/2.13.1/scala/math/Numeric.html)

型クラスのインスタンス：

* [IntIsIntegral](https://www.scala-lang.org/api/2.13.1/scala/math/Numeric$$IntIsIntegral$.html)
* [DoubleAsIfIntegral](https://www.scala-lang.org/api/2.13.1/scala/math/Numeric$$DoubleAsIfIntegral$.html)

<!-- end answer -->

### implicitの探索範囲

implicit defやimplicit parameterの値が探索される範囲には、

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

すると、importをしていないのに、このAdditive型クラスのインスタンスを使うことができます。

```scala
scala> sum(List(Rational(1, 1), Rational(2, 2)))
res0: Rational = Rational(4,2)
```

新しくデータ型を定義し、型クラスインスタンスも一緒に定義したい場合によく出てくるパターンなので覚えておくとよいでしょう。

[^implicit-arity]: 引数が2つ以上あるimplicit defの定義も可能です。「implicit defのパラメーターにimplicitが含まれる」という型クラス的な使い方をする場合は実際にimplicit defに2つ以上のパラメーターが出現することがあります。ただしそういった定義は通常implicit conversionとは呼ばれません
