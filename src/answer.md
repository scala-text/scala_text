# コレクションライブラリの演習問題の回答

## 練習問題

配列の`i`番目の要素と`j`番目の要素を入れ替える`swapArray`関数を定義してみましょう。`swapArray`関数の宣言は

```scala
def swapArray[T](arr: Array[T])(i: Int, j: Int): Unit
```

となります。ヒント：`swapArray`の結果は次のようになっていなければいけません。また、`i`と`j`が配列の範囲外である場合は特に考慮しなくて良いです。

```scala
scala> val arr = Array(1, 2, 3, 4, 5)
arr: Array[Int] = Array(1, 2, 3, 4, 5)

scala> swapArray(arr)(0, 4)

scala> arr
res1: Array[Int] = Array(5, 2, 3, 4, 1)

scala> swapArray(arr)(1, 3)

scala> arr
res3: Array[Int] = Array(5, 4, 3, 2, 1)
```

## 解答

```tut:silent
def swapArray[T](arr: Array[T])(i: Int, j: Int): Unit = {
  val tmp = arr(i)
  arr(i) = arr(j)
  arr(j) = tmp
}
```

<hr>

## 練習問題

`mkString`を使って、

```
1,2,3,4,5,6,7,8,9,10
```

となるような文字列を出力してみましょう（ヒント：`Range` にも`mkString`メソッドを使うことができる）

## 解答

```tut
(1 to 10).mkString(",")
```

<hr>

## 練習問題

`foldLeft`を用いて、`List`の要素を反転させるメソッド`reverse`を実装してみましょう。`reverse`は次のようにして呼びだすことが
できるものでなければいけません。

```
scala> reverse(List(1, 2, 3, 4, 5))
res0: List[Int] = List(5, 4, 3, 2, 1)
```

## 解答

```tut:silent
def reverse[A](lst: List[A]): List[A] = lst.foldLeft(Nil:List[A]){(a, b) => b::a}
```

<hr>

## 練習問題

`List`の全ての要素を足し合わせるメソッド`sum`を`foldRight`を用いて実装してみましょう。`sum`の宣言は次のようになります。
なお、`List`が空のときは0を返してください。

```scala
def sum(list: List[Int]): Int
```

## 解答

```tut:silent
def sum(list: List[Int]): Int = list match {
  case Nil => 1
  case x::xs => xs.foldRight(x){(x, y) => x + y}
}
```

<hr>

## 練習問題

`List`の全ての要素を掛け合わせるメソッド`mul`を`foldRight`を用いて実装してみましょう。`mul`の宣言は次のようになります。
なお、`List`が空のときは1を返してください。

```scala
def mul(list: List[Int]): Int
```

## 解答

```tut:silent
def mul(list: List[Int]): Int = list match {
  case Nil => 1
  case x::xs => xs.foldRight(x){(x, y) => y * x}
}
```

## 練習問題

`mkString`を実装してみましょう。`mkString`そのものを使ってはいけませんが、`foldLeft`や`foldRight`などの`List`に定義されている
他のメソッドは自由に使って構いません。[ListのAPIリファレンス](http://www.scala-lang.org/api/current/index.html#scala.collection.immutable.List)
を読めば必要なメソッドが載っています。実装する`mkString`の宣言は

```scala
def mkString[T](list: List[T])(sep: String): String
```

となります。残りの2つのバージョンの`mkString`は実装しなくても構いません。

## 解答

```tut:silent
def mkString[T](list: List[T])(sep: String): String = list match {
  case Nil => ""
  case x::xs => xs.foldLeft(x.toString){ case (x, y) => x + sep + y }
}
```

<hr>

## 練習問題

`map`メソッドを`foldLeft`と`reverse`を使って実装してみましょう。

## 解答

```scala
def map[T, U](list: List[T])(f: T => U): List[U] = {
  list.foldLeft(Nil:List[U]){(x, y) => f(y) :: x}.reverse
}
```

<hr>

## 練習問題

`filter`メソッドを`foldLeft`と`reverse`を使って実装してみましょう。

## 解答

```tut:silent
def filter[T](list: List[T])(f: T => Boolean): List[T] = {
  list.foldLeft(Nil:List[T]){(x, y) => if(f(y)) y::x else x}.reverse
}
```

## 練習問題

`count`メソッドを`foldLeft`を使って実装してみましょう。

## 解答

```tut:silent
def count(list: List[Int])(f: Int => Boolean): Int  = {
  list.foldLeft(0){(x, y) => if(f(y)) x + 1 else x}
}
```

## ケースクラスとパターンマッチングの演習問題の解答

## 練習問題

二分木（子の数が最大で2つであるような木構造）を表す型`Tree`と`Branch`, `Empty`を考えます：

```scala
sealed abstract class Tree
case class Branch(value: Int, left: Tree, right: Tree) extends Tree
case object Empty extends Tree
```

子が2つで左の子の値が`2`、右の子の値が`3`、自分自身の値が`1`の木構造はたとえば次のようにして定義することができます。

```scala
scala> val tree: Tree = Branch(1, Branch(2, Empty, Empty), Branch(3, Empty, Empty))
tree: Tree = Branch(1,Branch(2,Empty,Empty),Branch(3,Empty,Empty))
```

このような木構造に対して、

* 最大値を求める`max`メソッド：

```scala
scala> def max(tree: Tree): Int = ???
max: (tree: Tree)Int
```

* 最小値を求める`min`メソッド：

```scala
scala> def min(tree: Tree): Int = ???
min: (tree: Tree)Int
```

* 深さを求める`depth`メソッド：

```scala
scala> def depth(tree: Tree): Int = ???
depth: (tree: Tree)Int
```

をそれぞれ定義してみましょう。なお、

```scala
depth(Empty) == 0
depth(Branch(10, Empty, Empty)) = 1
```

です。

* 余裕があれば木構造を、

```
左の子孫の全ての値 <= 自分自身の値 < 右の子孫の全部の値 
```

となるような木構造に変換する`sort`メソッド：

```scala
scala> def sort(tree: Tree): Tree = ???
sort: (tree: Tree)Tree
```

を定義してみましょう。

## 解答

```tut:silent
object BinaryTree {
  sealed abstract class Tree
  case class Branch(value: Int, left: Tree, right: Tree) extends Tree
  case object Empty extends Tree

  def max(t: Tree): Int = t match {
    case Branch(v1, Branch(v2, Empty, Empty), Branch(v3, Empty, Empty)) =>
      val m = if(v1 <= v2) v2 else v1
      if(m <= v3) v3 else m
    case Branch(v1, Branch(v2, Empty, Empty), Empty) => if(v1 <= v2) v2 else v1
    case Branch(v1, Empty, Branch(v2, Empty, Empty)) => if(v1 <= v2) v2 else v1
    case Branch(v, l, r) => 
      val m1 = max(l)
      val m2 = max(r)
      val m3 = if(m1 <= m2) m2 else m1
      if(v <= m3) m3 else v
    case Empty => throw new RuntimeException
  }


  def min(t: Tree): Int = t match {
    case Branch(v1, Branch(v2, Empty, Empty), Branch(v3, Empty, Empty)) =>
      val m = if(v1 >= v2) v2 else v1
      if(m >= v3) v3 else m
    case Branch(v1, Branch(v2, Empty, Empty), Empty) => if(v1 >= v2) v2 else v1
    case Branch(v1, Empty, Branch(v2, Empty, Empty)) => if(v1 >= v2) v2 else v1
    case Branch(v, l, r) => 
      val m1 = min(l)
      val m2 = min(r)
      val m3 = if(m1 > m2) m2 else m1
      if(v >= m3) m3 else v
    case Empty => throw new RuntimeException
  }

  def depth(t: Tree): Int = t match {
    case Empty => 0
    case Branch(_, l, r) =>
      val ldepth = depth(l) 
      val rdepth = depth(r)
      (if(ldepth < rdepth) rdepth else ldepth) + 1
  }

  def sort(t: Tree): Tree = {
    def fromList(list: List[Int]): Tree = {
      def insert(value: Int, t: Tree): Tree = t match {
        case Empty => Branch(value, Empty, Empty)
        case Branch(v, l, r) =>
          if(value <= v) Branch(v, insert(value, l), r)
          else Branch(v, l, insert(value, r))
      }
      list.foldLeft(Empty:Tree){ case (t, v) => insert(v, t) }
    }
    def toList(tree: Tree): List[Int] = tree match {
      case Empty => Nil
      case Branch(v, l, r) => toList(l) ++ List(v) ++ toList(r)
    }
    fromList(toList(t))
  }

  def find(t: Tree, target: Int): Boolean = t match {
    case Branch(v, l, r) => if(v == target) true else (find(l, target) || find(r, target))
    case Empty => false
  }

  def findBinaryTree(t: Tree, target: Int): Boolean = t match {
    case Branch(v, l, r) => if(v == target) true else (if(target <= v) findBinaryTree(l, target) else findBinaryTree(r, target))
    case Empty => false
  }
}
```

## implicit conversion（暗黙の型変換）とimplicit parameter（暗黙のパラメータ）の演習問題の解答

## 練習問題

pimp my libraryパターンで、既存のクラスの利用を便利にするようなimplicit conversionを1つ定義してみてください。それはどのような場面で役に立つでしょうか？

## 解答

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

メソッドチェインの中でデバッグプリントをはさみたいときに役に立ちます。

## 練習問題

`m: Monoid[T]`と値`t1: T, t2: T, t3: T`は、次の条件（モノイド則）を満たす必要があります。

```scala
m.mplus(m.mzero, t1) == t1  // 単位元
m.mplus(t1, m.mzero) == t1  // 単位元
m.mplus(t1, m.mplus(t2, t3)) == m.mplus(m.mplus(t1, t2), t3) // 結合則
```
このような条件を満たす型`T`と単位元`mzero`、演算`mplus`を探し出し、`Monoid[T]`を定義してみましょう。この際、モノイド則が満たされていることをいくつかの入力に対して確認してみましょう。また、定義した`Monoid[T]`を`implicit`にして、`T`の合計値を先ほどの`sum`で計算できることを確かめてみましょう。

## 解答（一例です。他にも答えは無数にあります）

```tut:silent
object Monoids {
  trait Monoid[A] {
    def mplus(a: A, b: A): A
    def mzero: A
  }

  implicit object StringMonoid extends Monoid[String] {
    def mplus(a: String, b: String): String = a + b
    def mzero: String = ""
  }

  implicit object IntMonoid extends Monoid[Int] {
    def mplus(a: Int, b: Int): Int = a + b
    def mzero: Int = 0
  }

  case class Point(x: Int, y: Int)

  implicit object PointMonoid extends Monoid[Point] {
    def mplus(a: Point, b: Point): Point = Point(a.x + b.x, a.y + b.y)
    def mzero: Point = Point(0, 0)
  }

  def sum[A](lst: List[A])(implicit m: Monoid[A]) = lst.foldLeft(m.mzero)((x, y) => m.mplus(x, y))

  def main(args: Array[String]): Unit = {
    println(sum(List(Point(1, 1), Point(2, 2), Point(3, 3)))) // Point(6, 6)
    println(sum(List(Point(1, 2), Point(3, 4), Point(5, 6)))) // Point(9, 12)
  }
}
```


## エラー処理の演習問題の回答

## 練習問題
`map`と`flatten`を利用して、
`Some(2)`と`Some(3)`と`Some(5)`と`Some(7)`と`Some(11)`の値をかけて、`Some(2310)`を求めてみましょう。

## 解答

```tut:silent
val v1: Option[Int] = Some(2)
val v2: Option[Int] = Some(3)
val v3: Option[Int] = Some(5)
val v4: Option[Int] = Some(7)
val v5: Option[Int] = Some(11)
v1.map { i1 =>
    v2.map { i2 =>
        v3.map { i3 =>
            v4.map { i4 =>
                v5.map { i5 => i1 * i2 * i3 * i4 * i5 }
            }.flatten
        }.flatten
    }.flatten
}.flatten
```

## 練習問題
`flatMap`を利用して、`Some(2)`と`Some(3)`と`Some(5)`と`Some(7)`と`Some(11)`の値をかけて、`Some(2310)`を求めてみましょう。

## 解答

```tut:silent
val v1: Option[Int] = Some(2)
val v2: Option[Int] = Some(3)
val v3: Option[Int] = Some(5)
val v4: Option[Int] = Some(7)
val v5: Option[Int] = Some(11)
v1.flatMap { i1 =>
    v2.flatMap { i2 =>
        v3.flatMap { i3 =>
            v4.flatMap { i4 =>
                v5.map { i5 => i1 * i2 * i3 * i4 * i5 }
            }
        }
    }
}
```

## 練習問題
`for`を利用して、`Some(2)`と`Some(3)`と`Some(5)`と`Some(7)`と`Some(11)`の値をかけて、`Some(2310)`を求めてみましょう。

## 解答

```tut:silent
val v1: Option[Int] = Some(2)
val v2: Option[Int] = Some(3)
val v3: Option[Int] = Some(5)
val v4: Option[Int] = Some(7)
val v5: Option[Int] = Some(11)
for { i1 <- v1
      i2 <- v2
      i3 <- v3
      i4 <- v4
      i5 <- v5 } yield i1 * i2 * i3 * i4 * i5
```
