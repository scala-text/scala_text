# 型クラスへの誘い

本章では[Implicitの章](./implicit.md)で、少しだけ触れた型クラスについて、より深く掘り下げます。

Implicitの章では、 `Additive` という型クラスを定義することで、任意のコレクション（`Additive`が存在するもの）
に対して、要素の合計値を計算することができたのでした。本章では、このような仕組みを利用して、色々なアルゴリズム
を共通化できることを見ていきます。

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
です。ここで、`A` / `A`を計算するための型 `Dividable` を考えてみます。`Dividable` は次のようになるでしょう。

```tut
trait Dividable[A] {
  def div(a: A, b: Int): A 
}
object  Dividable {
  implicit object IntDividable extends Dividable[Int] {
    def div(a: Int, b: Int): Int = a / b
  }
  implicit object DoubleDividable extends Dividable[Double] {
    def div(a: Double, b: Int): Double = a / b
  }
}
```

また、 `Additive` に1に相当する値を計算するためのメソッド `one` を追加で導入します。

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


`Additive` と `Dividable` を使うと、 `average` 関数は次のように書くことができます。

```tut
def average[A](lst: List[A])(implicit a: Additive[A], d: Dividable[A]): A = {
  val length = lst.length
  d.div(lst.foldLeft(a.zero)((x, y) => a.plus(x, y)), length)
}
```

この `average` 関数は次のようにして使うことができます。

```tut
average(List(1, 3, 5))
average(List(1.5, 2.5, 3.5))
```

このようにして、複数の型クラスを組み合わせることで、より大きな柔軟性を手に入れることができました。
