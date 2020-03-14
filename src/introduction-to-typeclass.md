# 型クラスへの誘い

本章では[Implicitの章](./implicit.md)で、少しだけ触れた型クラスについて、より深く掘り下げます。

Implicitの章では、`Additive`という型クラスを定義することで、任意のコレクション（`Additive`が存在するもの）に対して、
要素の合計値を計算することができたのでした。本章では、このような仕組みを利用して、色々なアルゴリズムをライブラリ
化できることを見ていきます。

## averageメソッド

まず、`sum`に続いて、要素の平均値を計算するための`average`メソッドを作成することを考えます。`average`メソッドの
素朴な実装は次のようになるでしょう。 [^list-size]

```scala mdoc:nest
def average(list: List[Int]): Int = list.foldLeft(0)(_ + _) / list.size
```

これを、前章のように `Additive`を使ってみます。

```scala mdoc:nest
trait Additive[A] {
  def plus(a: A, b: A): A
  def zero: A
}
object Additive {
  implicit object IntAdditive extends Additive[Int] {
    def plus(a: Int, b: Int): Int = a + b
    def zero: Int = 0
  }
  implicit object DoubleAdditive extends Additive[Double] {
    def plus(a: Double, b: Double): Double = a + b
    def zero: Double = 0.0
  }
}
```

```scala
def average[A](lst: List[A])(implicit m: Additive[A]): A = {
  val length: Int = lst.length
  val sum: A = lst.foldLeft(m.zero)((x, y) => m.plus(x, y)) 
  sum / length
}
```

残念ながら、このコードはコンパイルを通りません。何故ならば、 `A` 型の値を `Int` 型の値で割ろうとしているのですが、その方法がわからないから
です。ここで、`Additive` をより一般化して、引き算、掛け算、割り算をサポートした型 `Num` を考えてみます。掛け算の
メソッドを `multiply` 、割り算のメソッドを `divide` とすると、 `Num` は次のようになるでしょう。
ここで、 `Nums` は、対話環境でコンパニオンクラス/オブジェクトを扱うために便宜的に作った名前空間であり、通常のScalaプログラムでは、コンパニオンクラス/オブジェクトを定義するときの作法に従えばよいです[^repl-companion]。

```scala mdoc:nest
object Nums {
  trait Num[A] {
    def plus(a: A, b: A): A
    def minus(a: A, b: A): A
    def multiply(a: A, b: A): A
    def divide(a: A, b: A): A 
    def zero: A
  }
  object Num{
    implicit object IntNum extends Num[Int] {
      def plus(a: Int, b: Int): Int = a + b
      def minus(a: Int, b: Int): Int = a - b
      def multiply(a: Int, b: Int): Int = a * b
      def divide(a: Int, b: Int): Int = a / b
      def zero: Int = 0
    }
    implicit object DoubleNum extends Num[Double] {
      def plus(a: Double, b: Double): Double = a + b
      def minus(a: Double, b: Double): Double = a - b
      def multiply(a: Double, b: Double): Double = a * b
      def divide(a: Double, b: Double): Double = a / b
      def zero: Double = 0.0
    }
  }
}
```

また、 `average` メソッドは、リストの長さ、つまり整数で割る必要があるので、整数を `A` 型に変換するための型
`FromInt` も用意します。 `FromInt` は次のようになります。 `to` は `Int` 型を対象の型に変換するメソッドです。

```scala mdoc:nest
object FromInts {
  trait FromInt[A] {
    def to(from: Int): A
  }
  object FromInt {
    implicit object FromIntToInt extends FromInt[Int] {
      def to(from: Int): Int = from
    }
    implicit object FromIntToDouble extends FromInt[Double] {
      def to(from: Int): Double = from
    }
  }
}
```

`Num` と `FromInt` を使うと、 `average` 関数は次のように書くことができます。

```scala mdoc:nest
import Nums._
import FromInts._
def average[A](lst: List[A])(implicit a: Num[A], b: FromInt[A]): A = {
  val length: Int = lst.length
  val sum: A  = lst.foldLeft(a.zero)((x, y) => a.plus(x, y))
  a.divide(sum, b.to(length))
}
```

この `average` 関数は次のようにして使うことができます。

```scala mdoc:nest
average(List(1, 3, 5))
average(List(1.5, 2.5, 3.5))
```

このようにして、複数の型クラスを組み合わせることで、より大きな柔軟性を手に入れることができました。ちなみに、

### context bounds

上記のコードは、context boundsというシンタックスシュガーを使うことで、次のように書き換えることもできます。

```scala mdoc:nest
import Nums._
import FromInts._
def average[A:Num:FromInt](lst: List[A]): A = {
  val a = implicitly[Num[A]]
  val b = implicitly[FromInt[A]]
  val length = lst.length
  val sum: A  = lst.foldLeft(a.zero)((x, y) => a.plus(x, y))
  a.divide(sum, b.to(length))
}
```

implicit parameterの名前 `a` と `b` が引数から見えなくなりましたが、 `implicitly[Type]` とすることで、
`Type` 型のimplicit paramerterの値を取得することができます。

## maxメソッドとminメソッド

別のアルゴリズムをライブラリ化した例を、Scalaの標準ライブラリから紹介します。コレクションから
最大値を取得する `max` と最小値を取得する `min` です。これらは、次のようにして使うことができます。

```scala mdoc:nest
List(1, 3, 4, 2).max
List(1, 3, 2, 4).min
```

比較できない要素のリストに対して `max` 、 `min` を求めようとするとコンパイルエラーになります。この `max`
と `min` も型クラスで実現されています。

`max` と `min` のシグネチャは次のようになっています。

```scala
def max[B >: A](implicit cmp: Ordering[B]): A
```

```scala
def min[B >: A](implicit cmp: Ordering[B]): A
```

`B >: A` の必要性についてはおいておくとして、ポイントは、 `Ordering[B]` 型のimplicit parameterを要求
するところです。 `Ordering[B]` のimplicitなインスタンスがあれば、 `B` 型同士の大小関係を比較できるため、最大値
と最小値を求めることができます。

## medianメソッド

さらに、別のアルゴリズムをライブラリ化してみます。リストの中央値を求めるメソッド `median` を定義する
ことを考えます。中央値は、要素数が奇数の場合、リストをソートした場合のちょうど真ん中の値を、偶数の
場合、真ん中の2つの値を足して2で割ったものになります。ここで、中央値の最初のケースには `Ordering` が、
2番目のケースにはそれに加えて、先程定義した `Num` と `FromInt` があれば良さそうです。

この3つを使って、 `median` メソッドを定義してみます。先程出てきたcontext boundsを使って、シグネチャ
が見やすいようにしています。

```scala mdoc:nest
import Nums._
import FromInts._
def median[A:Num:Ordering:FromInt](lst: List[A]): A = {
  val num = implicitly[Num[A]]
  val ord = implicitly[Ordering[A]]
  val int = implicitly[FromInt[A]]
  val size = lst.size
  require(size > 0)
  val sorted = lst.sorted
  if(size % 2 == 1) {
    sorted(size / 2)
  } else {
    val fst = sorted((size / 2) - 1)
    val snd = sorted((size / 2))
    num.divide(num.plus(fst, snd), int.to(2))
  }
}
```

このメソッドは次のようにして使うことができます。

```scala mdoc:nest
assert(2 == median(List(1, 3, 2)))
assert(2.5 == median(List(1.5, 2.5, 3.5)))
assert(3 == median(List(1, 3, 4, 5)))
```

## オブジェクトのシリアライズ

次はより複雑な例を考えてみます。次のように、オブジェクトをシリアライズする
メソッド `string` を定義したいとします。

```scala
import Serializers.string
string(List(1, 2, 3)) // [1,2,3]
string(List(List(1),List(2),List(3)) // [[1],[2],[3]]
string(1) // 1
string("Foo") // Foo
class MyClass(val x: Int)
string(new MyClass(1)) // Compile Error!
class MyKlass(val x: Int)
implicit object MyKlassSerializer extends Serializer[MyKlass] {
  def serialize(klass: MyKlass): String = s"MyKlass(${klass.x})"
}
string(new MyKlass(1)) // OK
```

この `string` メソッドは、

* 整数をシリアライズ可能
* 文字列をシリアライズ可能
* 要素がシリアライズ可能なリストをシリアライズ可能

であり、自分で作成したクラスについては、次のトレイト `Serializer` を
継承して `serialize` メソッドを実装するオブジェクトをimplicitにすることで、
シリアライズ可能にできます。

```scala
trait Serializer[A] {
  def serialize(obj: A): String
}
```

これを仮に `Serializer` 型クラスと呼びます。

この `string` メソッドのシグニチャをまず考えてみます。このメソッドは
`Serializer` 型クラスを必要としているので、 `Serializer[A]` のような
implicit parameterを必要としているはずです。また、引数は `A` 型の値
で、返り値は `String` なので、結果として次のようになります。

```scala
def string[A:Serializer](obj: A): String = ???
```

次に実装ですが、 `Serializer` 型クラスを要求しているということは、
`Serializer` の `serialize` メソッドを呼びだせばいいだけなので、
次のようになります。

```scala mdoc:nest
object Serializers {
  trait Serializer[A] {
    def serialize(obj: A): String
  }
  def string[A:Serializer](obj: A): String = {
    implicitly[Serializer[A]].serialize(obj)
  }
}
```

`Serializers` という `object` を作っていますが、これをimportすることで：

* `string` メソッドを使える
* `Serializer` 型クラスが公開される

ようになります。

さて、これでシグネチャの部分はできたので実装に入ります。とはいっても、今回の範囲内では、ほとんど
オブジェクトを `toString` するだけのものなのですが…。

```scala
implicit object IntSerializer extends Serializer[Int] {
  def serialize(obj: Int): String = obj.toString
}
implicit object StringSerializer extends Serializer[String] {
  def serialize(obj: String): String = obj
}
```

以上は、整数と文字列の `Serializer` です。単に `toString` を呼び出しているか、自身を返しているだけなのが
わかります。次が少しわかりにくいです。要素がシリアライズ可能なときだけ、リストがシリアライズ可能でなければ
いけないのですから、単純に以下のようにしてもだめです。

```scala
implicit def ListSerializer[A]: Serializer[List[A]] = {
  def serialize(obj: List[A]): String = ???
}
```

この定義では `A` にどのような操作が可能なのかわからないため、中身を単純に `toString` するくらいしか
実装しようがないですし、また、そのような実装では要素型の `Serializer` の実装と整合性が取れません。
これを解決するには、 `ListSerializer` がimplicit parameterを取るようにします。

```scala
implicit def ListSerializer[A](implicit serializer: Serializer[A]): Serializer[List[A]] = {
  def serialize(obj: List[A]): String = {
    val serializedList = obj.map{o => serializer.serialize(o)}
    serializedList.mkString("[",",","]")
  }
}
```

このように定義したとき、コンパイラは、要素型 `A` がシリアライズ可能でない場合（あらかじめimplicit def/objectで
そう定義されていない場合）コンパイルエラーにしてくれます。つまり、型安全にオブジェクトをシリアライズできるの
です。

ここまでで、一通りの実装ができたので、定義を一箇所にまとめて実行結果を確認してみましょう。この節の最初の
方の入力例を使って動作確認をします。

```scala mdoc:nest
object Serializers {
  trait Serializer[A] {
    def serialize(obj: A): String
  }
  def string[A:Serializer](obj: A): String = {
    implicitly[Serializer[A]].serialize(obj)
  }
  implicit object IntSerializer extends Serializer[Int] {
    def serialize(obj: Int): String = obj.toString
  }
  implicit object StringSerializer extends Serializer[String] {
    def serialize(obj: String): String = obj
  }
  implicit def ListSerializer[A](implicit serializer: Serializer[A]): Serializer[List[A]] = new Serializer[List[A]]{
    def serialize(obj: List[A]): String = {
      val serializedList = obj.map{o => serializer.serialize(o)}
      serializedList.mkString("[",",","]")
    }
  }
}
import Serializers._
string(List(1, 2, 3)) // [1,2,3]
string(List(List(1),List(2),List(3))) // [[1],[2],[3]]
string(1) // 1
string("Foo") // Foo
// class MyClass(val x: Int)
// string(new MyClass(1)) // Compile Error!
class MyKlass(val x: Int)
implicit object MyKlassSerializer extends Serializer[MyKlass] {
  def serialize(klass: MyKlass): String = s"MyKlass(${klass.x})"
}
string(new MyKlass(1)) // OK
```

行コメントに書いた想定通りの動作をしていることがわかります。ここで重要なのは、 `MyClass` に
対しては `Serializer` を定義していないので、コンパイルエラーになる点です。多くの言語のシリアライズ
ライブラリでは、リフレクションを駆使してシリアライズしようとするため、実行時になって初めてエラーが
わかることが多いです。特に、静的型付き言語では、そのような場合、型によって安全を保証できないのは
デメリットです。一方、今回用いた手法では、後付けでシリアライズする型を追加でき、かつコンパイル時に
その正当性を検査できるのです。

## まとめ

型クラス（≒implicit parameter）は、うまく使うと、後付けのデータ型に対して
既存のアルゴリズムを型安全に適用するのに使うことができます。この特徴は、特にライブラリ設計のときに
重要になってきます。ライブラリ設計時点で定義されていないデータ型に対していかにしてライブラリのアルゴリズム
を適用するか、つまり、拡張性が高いように作るかというのは、なかなか難しい問題です。簡潔に書けることを重視すると、
拡張性が狭まりがちですし、拡張性が高いように作ると、デフォルトの動作でいいところを毎回書かなくてはいけなくて
利用者にとって不便です。型クラスを使ったライブラリを提供することによって、この問題をある程度緩和することが
できます。皆さんも、型クラスを使って、既存の問題をより簡潔に、拡張性が高く解決できないか考えてみてください。

[^list-size]: ここで、`List`の`size`を計算するのは若干効率が悪いですが、その点については気にしないことにします。
[^repl-companion]: つまり、単に `Nums` を削除して同一ファイルに両方の定義を置けば良いです
