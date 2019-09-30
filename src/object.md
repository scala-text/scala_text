# オブジェクト

Scalaでは、全ての値がオブジェクトです。また、全てのメソッドは何らかのオブジェクトに所属しています。
そのため、Javaのようにクラスに属するstaticフィールドやstaticメソッドといったものを作成することができません。その代わりに、`object`キーワードによって、同じ名前のシングルトンオブジェクトを現在の名前空間の下に1つ定義することが
できます。`object`キーワードによって定義したシングルトンオブジェクトには、そのオブジェクト固有のメソッドや
フィールドを定義することができます。

`object`構文の主な用途としては、

* ユーティリティメソッドやグローバルな状態の置き場所（Javaで言うstaticメソッドやフィールド）
* 同名クラスのオブジェクトのファクトリメソッド

が挙げられます。

objectの基本構文はクラスとおおむね同じで、

```
object <オブジェクト名> extends <クラス名> (with <トレイト名>)* {
  (<フィールド定義> | <メソッド定義>)*
}
```

となります。Scalaでは標準で[`Predef`という`object`](https://github.com/scala/scala/blob/v2.13.1/src/library/scala/Predef.scala)が定義・インポートされており、これは最初の使い方に当てはまります。 `println("Hello")` となにげなく
使っていたメソッドも実は` Predef` のメソッドなのです。 `extends` でクラスを継承、 `with` でトレイトをmix-in
可能になっているのは、オブジェクト名を既存のクラスのサブクラス等として振る舞わせたい場合があるからです。Scala
の標準ライブラリでは、 `Nil` という `object` がありますが、これは `List` の一種として振る舞わせたいため、
`List` を継承しています。一方、 `object` がトレイトをmix-inする事はあまり多くありませんが、クラスやトレイト
との構文の互換性のためにそうなっていると思われます。

一方、2番めの使い方について考えてみます。点を表す `Point` クラスのファクトリを
`object`で作ろうとすると、次のようになります。` apply` という名前のメソッドはScala処理系に
よって特別に扱われ、`Point(x)`のような記述があった場合で、`Point` `object`に`apply`という
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
公開し、`equals()`・`hashCode()`・`toString()`などのオブジェクトの基本的なメソッドをオーバーライドしたクラスを
生成し、また、そのクラスのインスタンスを生成するためのファクトリメソッドを生成するものです。たとえば、
`case class Point(x: Int, y: Int)`で定義した `Point` クラスは `equals()` メソッドを明示的に定義してはいませんが、

```scala
Point(1, 2).equals(Point(1, 2))
```
を評価した値は`true`になります。

## コンパニオンオブジェクト

クラスと同じファイル内、同じ名前で定義されたシングルトンオブジェクトは、コンパニオンオブジェクトと呼ばれます。
コンパニオンオブジェクトは対応するクラスに対して特権的なアクセス権を持っています。たとえば、
weightを`private`にした場合、

```scala
class Person(name: String, age: Int, private val weight: Int)

object Hoge {
  def printWeight(): Unit = {
    val taro = new Person("Taro", 20, 70)
    println(taro.weight)
  }
}
```

はNGですが、


```scala
class Person(name: String, age: Int, private val weight: Int)

object Person {
  def printWeight(): Unit = {
    val taro = new Person("Taro", 20, 70)
    println(taro.weight)
  }
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
  def printWeight(): Unit = {
    val taro = new Person("Taro", 20, 70)
    println(taro.weight)
  }
}

// Exiting paste mode, now interpreting.

defined class Person
defined object Person
```

### 練習問題

クラスを定義して、そのクラスのコンパニオンオブジェクトを定義してみましょう。コンパニオンオブジェクトが同名のクラスに対する
特権的なアクセス権を持っていることを、クラスのフィールドを`private`にして、そのフィールドへアクセスできることを通じて確認して
みましょう。また、クラスのフィールドを`private[this]`にして、そのフィールドへアクセスできないことを確認してみましょう。
