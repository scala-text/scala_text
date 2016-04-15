# 演習問題の解答

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

`m: Additive[T]`と値`t1: T, t2: T, t3: T`は、次の条件を満たす必要があります。

```scala
m.plus(m.zero, t1) == t1  // 単位元
m.plus(t1, m.zero) == t1  // 単位元
m.plus(t1, m.plus(t2, t3)) == m.plus(m.plus(t1, t2), t3) // 結合則
```
このような条件を満たす型`T`と単位元`zero`、演算`plus`を探し出し、`Additive[T]`を定義してみましょう。この際、条件が満たされていることをいくつかの入力に対して確認してみましょう。また、定義した`Additive[T]`を`implicit`にして、`T`の合計値を先ほどの`sum`で計算できることを確かめてみましょう。

## 解答（一例です。他にも答えは無数にあります）

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
