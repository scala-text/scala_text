# object（★★★）

Scalaでは、全ての値がオブジェクトです。また、全てのメソッドは何らかのオブジェクトに所属しています。
そのため、Javaのようにクラスに属するstaticフィールドやstaticメソッドといったものを作成することができません。
その代わりというと若干語弊があるのですが、`object`キーワードによって、同じ名前のシングルトンオブジェクトを
グローバルな名前空間に1つ定義することができます。`object`キーワードによって定義したオブジェクトもオブジェクト
であるため、メソッドやフィールドを定義することができます。

`object`構文の主な用途としては、

* ユーティリティメソッドやグローバルな状態の置き場所（Javaで言うstaticメソッドやフィールド）
* オブジェクトのファクトリメソッド
* Singletonパターン

3つが挙げられます。とはいえ、Singletonパターンを実現するために使われることはほとんどなく、もっぱら最初の
2つの用途で使われます。

objectの基本構文はクラスとほとんど同じで、

```
object オブジェクト名 extends クラス名 with トレイト名1 with トレイト名2 ... {
  本体
}
```

のようになります。Scalaでは標準で[`Predef`という`object`](https://github.com/scala/scala/blob/v2.11.8/src/library/scala/Predef.scala)が定義・インポート
されており、これは最初の使い方に当てはまります。たとえば、println()などとなにげなく
使っていたメソッドも実はPredef objectのメソッドなのです。

一方、2番めの使い方について考えてみます。点を表すPointクラスのファクトリを
`object`で作ろうとすると、次のようになります。applyという名前のメソッドはScala処理系に
よって特別に扱われ、`Point(x)`のような記述があった場合で、Point `object`に`apply`という
名前のメソッドが定義されていた場合、`Point.apply(x)`と解釈されます。これを利用してPoint objectの
`apply`メソッドでオブジェクトを生成するようにすることで、`Point(3, 5)`のような記述でオブジェクトを
生成できるようになります。

```tut
class Point(val x:Int, val y:Int)

object Point {
  def apply(x: Int, y: Int): Point = new Point(x, y)
}
```

これは、new Point()で直接Pointオブジェクトを生成するのに比べて、

* クラス（Point）の実装詳細を内部に隠しておける（インタフェースのみを外部に公開する）
* Pointではなく、そのサブクラスのインスタンスを返すことができる

といったメリットがあります。なお、上記の記述はケースクラスを用いてもっと簡単に

```tut
case class Point(x: Int, y: Int)
```

と書けます。ケースクラスは後述するパターンマッチのところでも出てきますが、ここではその使い方については
触れません。簡単に言うとケースクラスは、それをつけたクラスのプライマリコンストラクタ全てのフィールドを
公開し、`equqls()`・`hashCode()`・`toString()`などのオブジェクトの基本的なメソッドを持ったクラスを
生成し、また、そのクラスのインスタンスを生成するためのファクトリメソッドを生成するものです。たとえば、
`case class Point(x: Int, y: Int)`で定義した `Point` クラスは `equals()` メソッドを明示的に定義してはいませんが、

```scala
Point(1, 2).equals(Point(1, 2))
```
を評価した値は`true`になります。

## コンパニオンオブジェクト（★★★）

クラス名と同じ名前のシングルトンオブジェクトはコンパニオンオブジェクトと呼ばれます。
コンパニオンオブジェクトは対応するクラスに対して特権的なアクセス権を持っています。たとえば、
weightを`private`にした場合、

```scala
class Person(name: String, age: Int, private val weight: Int)

object Hoge {
  val taro = new Person("Taro", 20, 70)
  println(taro.weight)
}
```

はNGですが、


```scala
class Person(name: String, age: Int, private val weight: Int)

object Person {
  val taro = new Person("Taro", 20, 70)
  println(taro.weight)
}
```

はOKです。なお、コンパニオンオブジェクトでも、`private[this]`（そのオブジェクト内からのみアクセス可能）なクラスの
メンバーに対してはアクセスできません。単に`private`とした場合、コンパニオンオブジェクトからアクセスできるようになります。

上記のような、コンパニオンオブジェクトを使ったコードをREPLで試す場合は、REPLの`:paste`コマンドを使って、クラスとコンパニオン
オブジェクトを一緒にペーストするようにしてください。クラスとコンパニオンオブジェクトは同一ファイル中に置かれていなければ
ならないのですが、REPLで両者を別々に入力した場合、コンパニオン関係をREPLが正しく認識できないのです。

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

class Person(name: String, age: Int, private val weight: Int)

object Person {
  val taro = new Person("Taro", 20, 70)
  println(taro.weight)
}

// Exiting paste mode, now interpreting.

defined class Person
defined object Person
```

### 練習問題

クラスを定義して、そのクラスのコンパニオンオブジェクトを定義してみましょう。コンパニオンオブジェクトが同名のクラスに対する
特権的なアクセス権を持っていることを、クラスのフィールドを`private`にして、そのフィールドへアクセスできることを通じて確認して
みましょう。また、クラスのフィールドを`private[this]`にして、そのフィールドへアクセスできないことを確認してみましょう。
