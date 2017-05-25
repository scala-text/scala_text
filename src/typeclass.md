# 型クラスの紹介

本章では[Implicitの章](./implicit.md)で説明した型クラスの具体例を紹介します。
本章で紹介する型クラスは、必ずしもScalaでのプログラミングに必要というわけではありません。
しかし、世の中に存在するScalaで実装されたライブラリやアプリケーションのいくつかでは、本章で紹介する型クラスなどを多用している場合があります。
そのようなライブラリやアプリケーションに出会った際にも臆さずコードリーディングができるよう、最低限の知識をつけることが本章の目的です。

本章で紹介する型クラスを絡めたScalaでのプログラミングについて詳しく知りたい場合は[Scala関数型デザイン＆プログラミング](http://book.impress.co.jp/books/1114101091)を読みましょう。

## Functor

前章に登場した`List`や`Option`には、`map`という関数が共通して定義されていました。
この`map`関数がある規則を満たす場合はFunctor型クラスとして抽象化できます[^hkind]。

```tut:silent
trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}
```

この型クラスが満たすべき規則は2つです。

```tut:silent
def identityLaw[F[_], A](fa: F[A])(implicit F: Functor[F]): Boolean =
  F.map(fa)(identity) == fa

def compositeLaw[F[_], A, B, C](fa: F[A], f1: A => B, f2: B => C)(implicit F: Functor[F]): Boolean =
  F.map(fa)(f2 compose f1) == F.map(F.map(fa)(f1))(f2)
```

なお、`identity`は次のように定義されます。

```tut:silent
def identity[A](a: A): A = a
```

例として、Option型でFunctor型クラスのインスタンスを定義し、前述の規則を満たすかどうか調べてみましょう。

```tut
import scala.language.higherKinds

trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}

def identityLaw[F[_], A](fa: F[A])(implicit F: Functor[F]): Boolean =
  F.map(fa)(identity) == fa

def compositeLaw[F[_], A, B, C](fa: F[A], f1: A => B, f2: B => C)(implicit F: Functor[F]): Boolean =
  F.map(fa)(f2 compose f1) == F.map(F.map(fa)(f1))(f2)

implicit object OptionFunctor extends Functor[Option] {
  def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa.map(f)
}

val n: Option[Int] = Some(2)
identityLaw(n)
compositeLaw(n, (i: Int) => i * i, (i: Int) => i.toString)
```

## Applicative Functor

複数の値が登場する場合にはFunctorでは力不足です。
そこで、複数の引数を持つ関数と値を組み合わせて1つの値を作りだせる機能を提供するApplicative Functorが登場します。

```tut:silent
trait Applicative[F[_]] {
  def point[A](a: A): F[A]
  def ap[A, B](fa: F[A])(f: F[A => B]): F[B]
}
```

Applicative FunctorはFunctorを一般化したものなので、Applicative Functorが持つ関数から`map`関数を定義できます。

```tut:silent
def map[F[_], A, B](fa: F[A])(f: A => B)(implicit F: Applicative[F]): F[B] =
  F.ap(fa)(F.point(f))
```

Applicative Functorが満たすべき規則は以下の通りです。

```tut:silent
def identityLaw[F[_], A](fa: F[A])(implicit F: Applicative[F]): Boolean =
  F.ap(fa)(F.point((a: A) => a)) == fa

def homomorphismLaw[F[_], A, B](f: A => B, a: A)(implicit F: Applicative[F]): Boolean =
  F.ap(F.point(a))(F.point(f)) == F.point(f(a))

def interchangeLaw[F[_], A, B](f: F[A => B], a: A)(implicit F: Applicative[F]): Boolean =
  F.ap(F.point(a))(f) == F.ap(f)(F.point((g: A => B) => g(a)))
```

また、`ap`と`point`を使って定義した`map`関数がFunctorのものと同じ振る舞いになることを確認する必要があります。

例として、Option型でApplicative Functorを定義してみましょう。

```tut
trait Applicative[F[_]] {
  def point[A](a: A): F[A]
  def ap[A, B](fa: F[A])(f: F[A => B]): F[B]
  def map[A, B](fa: F[A])(f: A => B): F[B] = ap(fa)(point(f))
}

def identityLaw[F[_], A](fa: F[A])(implicit F: Applicative[F]): Boolean =
  F.ap(fa)(F.point((a: A) => a)) == fa

def homomorphismLaw[F[_], A, B](f: A => B, a: A)(implicit F: Applicative[F]): Boolean =
  F.ap(F.point(a))(F.point(f)) == F.point(f(a))

def interchangeLaw[F[_], A, B](f: F[A => B], a: A)(implicit F: Applicative[F]): Boolean =
  F.ap(F.point(a))(f) == F.ap(f)(F.point((g: A => B) => g(a)))

implicit object OptionApplicative extends Applicative[Option] {
  def point[A](a: A): Option[A] = Some(a)
  def ap[A, B](fa: Option[A])(f: Option[A => B]): Option[B] = f match {
    case Some(g) => fa match {
      case Some(a) => Some(g(a))
      case None => None
    }
    case None => None
  }
}

val a: Option[Int] = Some(1)
val f: Int => String = { i => i.toString }
val af: Option[Int => String] = Some(f)
identityLaw(a)
homomorphismLaw(f, 1)
interchangeLaw(af, 1)
OptionApplicative.map(a)(_ + 1) == OptionFunctor.map(a)(_ + 1)
```

## Monad

ある値を受け取りその値を包んだ型を返す関数をApplicative Functorで扱おうとすると、型がネストしてしまい平坦化できません。
このネストする問題を解決するためにMonadと呼ばれる型クラスを用います。

```tut:silent
trait Monad[F[_]] {
  def point[A](a: A): F[A]
  def bind[A, B](fa: F[A])(f: A => F[B]): F[B]
}
```

`bind`はOptionやListで登場した`flatMap`を抽象化したものです。

Monadは以下の規則を満たす必要があります。

```tut:silent
def rightIdentityLaw[F[_], A](a: F[A])(implicit F: Monad[F]): Boolean =
  F.bind(a)(F.point(_)) == a

def leftIdentityLaw[F[_], A, B](a: A, f: A => F[B])(implicit F: Monad[F]): Boolean =
  F.bind(F.point(a))(f) == f(a)

def associativeLaw[F[_], A, B, C](fa: F[A], f: A => F[B], g: B => F[C])(implicit F: Monad[F]): Boolean =
  F.bind(F.bind(fa)(f))(g) == F.bind(fa)((a: A) => F.bind(f(a))(g))
```

MonadはApplicative Functorを特殊化したものなので、Monadが持つ関数から`point`関数と`ap`関数を定義できます。
`point`に関しては同じシグネチャなので自明でしょう。

```tut:silent
def ap[F[_], A, B](fa: F[A])(f: F[A => B])(implicit F: Monad[F]): F[B] =
  F.bind(f)((g: A => B) => F.bind(fa)((a: A) => F.point(g(a))))
```

それでは、Option型が前述の規則をみたすかどうか確認してみましょう。

```tut
trait Monad[F[_]] {
  def point[A](a: A): F[A]
  def bind[A, B](fa: F[A])(f: A => F[B]): F[B]
}

def rightIdentityLaw[F[_], A](a: F[A])(implicit F: Monad[F]): Boolean =
  F.bind(a)(F.point(_)) == a

def leftIdentityLaw[F[_], A, B](a: A, f: A => F[B])(implicit F: Monad[F]): Boolean =
  F.bind(F.point(a))(f) == f(a)

def associativeLaw[F[_], A, B, C](fa: F[A], f: A => F[B], g: B => F[C])(implicit F: Monad[F]): Boolean =
  F.bind(F.bind(fa)(f))(g) == F.bind(fa)((a: A) => F.bind(f(a))(g))

implicit object OptionMonad extends Monad[Option] {
  def point[A](a: A): Option[A] = Some(a)
  def bind[A, B](fa: Option[A])(f: A => Option[B]): Option[B] = fa match {
    case Some(a) => f(a)
    case None => None
  }
}

val fa: Option[Int] = Some(1)
val f: Int => Option[Int] = { n => Some(n + 1) }
val g: Int => Option[Int] = { n => Some(n * n) }
rightIdentityLaw(fa)
leftIdentityLaw(1, f)
associativeLaw(fa, f, g)
```

## Monoid

2つの同じ型を結合する機能を持ち、更にゼロ値を知る型クラスはMonoidと呼ばれています。

```tut:silent
trait Monoid[F] {
  def append(a: F, b: F): F
  def zero: F
}
```

前章で定義したAdditive型とよく似ていますが、Monoidは次の規則を満たす必要があります。

```tut:silent
def leftIdentityLaw[F](a: F)(implicit F: Monoid[F]): Boolean = a == F.append(F.zero, a)
def rightIdentityLaw[F](a: F)(implicit F: Monoid[F]): Boolean = a == F.append(a, F.zero)
def associativeLaw[F](a: F, b: F, c: F)(implicit F: Monoid[F]): Boolean = {
  F.append(F.append(a, b), c) == F.append(a, F.append(b, c))
}
```

Option[Int]型でMonoidインスタンスを定義してみましょう。

```tut
trait Monoid[F] {
  def append(a: F, b: F): F
  def zero: F
}

def leftIdentityLaw[F](a: F)(implicit F: Monoid[F]): Boolean = a == F.append(F.zero, a)
def rightIdentityLaw[F](a: F)(implicit F: Monoid[F]): Boolean = a == F.append(a, F.zero)
def associativeLaw[F](a: F, b: F, c: F)(implicit F: Monoid[F]): Boolean = {
  F.append(F.append(a, b), c) == F.append(a, F.append(b, c))
}

implicit object OptionIntMonoid extends Monoid[Option[Int]] {
  def append(a: Option[Int], b: Option[Int]): Option[Int] = (a, b) match {
    case (None, None) => None
    case (Some(v), None) => Some(v)
    case (None, Some(v)) => Some(v)
    case (Some(v1), Some(v2)) => Some(v1 + v2)
  }
  def zero: Option[Int] = None
}

val n: Option[Int] = Some(1)
val m: Option[Int] = Some(2)
val o: Option[Int] = Some(3)
leftIdentityLaw(n)
rightIdentityLaw(n)
associativeLaw(n, m, o)
```

型によっては結合方法が複数存在する場合があります。
その際は複数のMonoidインスタンスを定義しておき、状況に応じて使いたいMonoidインスタンスを選択できるようにしておきましょう。

[^hkind]: ここで出現する`F`は、通常の型ではなく、「何かの型を受け取って、型を返すもの」で、型構築子、型コンストラクタなどと呼びます。`List`や`Option`は型構築子の一種です。詳細については、[型システム入門 プログラミング言語と型の理論](http://www.amazon.co.jp/dp/4274069117)の第VI部「高階の型システム」を参照してください。
