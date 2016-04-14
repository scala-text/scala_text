# 演習問題の解答

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
