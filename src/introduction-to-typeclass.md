# 型クラスへの誘い

本章では[Implicitの章](./implicit.md)で、少しだけ触れた型クラスについて、より深く掘り下げます。

Implicitの章では、`Additive`という型クラスを定義することで、任意のコレクション（`Additive`が存在するもの）に対して、
要素の合計値を計算することができたのでした。本章では、このような仕組みを利用して、色々なアルゴリズムをライブラリ
化できることを見ていきます。

まず、`sum`に続いて、要素の平均値を計算するための`average`メソッドを作成することを考えます。`average`メソッドの
素朴な実装は次のようになるでしょう。

```tut
def average(list: List[Int]): Int = list.foldLeft(0)(_ + _) / list.size
```

ここで、`List`の`size`を計算するのは若干効率が悪いですが、その点については気にしないことにします。これを、前章のように
`Additive`を使ってみます。

```tut
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
  val length = lst.foldLeft(m.zero)((x, _) => m.plus(x, 1))
  lst.foldLeft(m.zero)((x, y) => m.plus(x, y)) / length
}
```

残念ながら、このコードはコンパイルを通りません。何故ならば、 `A` 型の値を `A` 型で割ろうとしているのですが、その方法がわからないから
です。ここで、`Additive` をより一般化して、引き算、掛け算、割り算をサポートした型 `Num` を考えてみます。掛け算の
メソッドを `multiply` 、割り算のメソッドを `divide` とすると、 `Num` は次のようになるでしょう。
ここで、 `Nums` は、対話環境でコンパニオンクラス/オブジェクトを扱うために便宜的に作った名前空間であり、通常のScalaプログラムでは必要ありません。

```tut
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
      def plus(a: Double, b: Double): Double = a / b
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

```tut
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

```tut
import Nums._
import FromInts._
def average[A](lst: List[A])(implicit a: Num[A], b: FromInt[A]): A = {
  val length = lst.length
  a.divide(lst.foldLeft(a.zero)((x, y) => a.plus(x, y)), b.to(length))
}
```

この `average` 関数は次のようにして使うことができます。

```tut
average(List(1, 3, 5))
average(List(1.5, 2.5, 3.5))
```

このようにして、複数の型クラスを組み合わせることで、より大きな柔軟性を手に入れることができました。ちなみに、
上記のコードは、context boundsというシンタックスシュガーを使うことで、次のように書き換えることもできます。

```tut
import Nums._
import FromInts._
def average[A:Num:FromInt](lst: List[A]): A = {
  val a = implicitly[Num[A]]
  val b = implicitly[FromInt[A]]
  val length = lst.length
  a.divide(lst.foldLeft(a.zero)((x, y) => a.plus(x, y)), b.to(length))
}
```

implicit parameterの名前 `a` と `b` が引数から見えなくなりましたが、 `implicitly[Type]` とすることで、
`Type` 型のimplicit paramerterの値を取得することができます。

別のアルゴリズムをライブラリ化した例を、Scalaの標準ライブラリから紹介します。コレクションから
最大値を取得する `max` と最小値を取得する `min` です。これらは、次のようにして使うことができます。

```tut
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

さらに、別のアルゴリズムをライブラリ化してみます。リストの中央値を求めるメソッド `median` を定義する
ことを考えます。中央値は、要素数が奇数の場合、リストをソートした場合のちょうど真ん中の値を、偶数の
場合、真ん中の2つの値を足して2で割ったものになります。ここで、中央値の最初のケースには `Ordering` が、
2番目のケースにはそれに加えて、先程定義した `Num` と `FromInt` があれば良さそうです。

この3つを使って、 `median` メソッドを定義してみます。先程出てきたcontext boundsを使って、シグネチャ
が見やすいようにしています。

```tut
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

```tut
assert(2 == median(List(1, 3, 2)))
assert(2.5 == median(List(1.5, 2.5, 3.5)))
assert(3 == median(List(1, 3, 4, 5)))
```

次はより複雑な例を考えてみます。次のように、オブジェクトをシリアライズする
メソッド `string` を定義したいとします。

```scala
import Serializer.string
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
* 要素がシリアライズ可能なリストをシリアライズ可能
* 文字列をシリアライズ可能

であり、自分で作成したクラスについては、次のトレイト `Serializer` を
継承して `serialize` メソッドを実装するオブジェクトをimplicitにすることで、
シリアライズ可能にできます。

```scala
trait Serializer[A] {
  def serialize(obj: A): String
}
```


