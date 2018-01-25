# 型クラスへの誘い

本章では[Implicitの章](./implicit.md)で、少しだけ触れた型クラスについて、より深く掘り下げます。

Implicitの章では、`Additive`という型クラスを定義することで、任意のコレクション（`Additive`が存在するもの）に対して、
要素の合計値を計算することができたのでした。本章では、このような仕組みを利用して、色々なアルゴリズムを共通化できる
ことを見ていきます。

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
