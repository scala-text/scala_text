# クラス

これからScalaにおけるクラス定義に関して説明します。Javaのクラスがわかっている事を前提にします
が、ご了承ください。

## クラス定義

Scalaにおけるクラスは、記法を除けばJava言語のクラスと大して変わりません。Scalaのクラス定義はおおまかには次のような形を取ります。

```scala
class ClassName(parameter1: Type1, parameter2: Type2, ...) {
  (a field or method definition)の0回以上の繰り返し
}
```

たとえば、点を表すクラス`Point`を定義したいとします。`Point`はx座標を表すフィールド`x`（`Int`型）とフィールド`y`（`Int`型）からなるとします。このクラス`Point`をScalaで書くと次のようになります。

```tut:silent
class Point(_x: Int, _y: Int) {
  val x = _x
  val y = _y
}
```

コンストラクタの引数と同名のフィールドを定義し、それを公開する場合は、以下のように短く書く
こともできます。

```tut:silent
class Point(val x: Int, val y: Int)
```

* クラス名の直後にコンストラクタ引数の定義がある
* val/varによって、コンストラクタ引数をフィールドとして公開することができる

点に注目してください。まず、最初の点ですが、Scalaでは1クラスに付き、基本的には1つのコンストラクタしか
使いません。このコンストラクタを、Scalaでは*プライマリコンストラクタ*として特別に扱っています。文法上は
複数のコンストラクタを定義できるようになっていますが、実際に使うことはほとんどありません。複数のオブジェクト
の生成方法を提供したい場合、objectの`apply`メソッドとして定義することが多いです。

2番目の点ですが、プライマリコンストラクタの引数にval/varをつけるとそのフィールドは公開され、外部からアクセス
できるようになります。なお、プライマリコンストラクタの引数のスコープはクラス定義全体におよびます。そのため、

```tut:silent
class Point(val x: Int, val y: Int) {
  def +(p: Point): Point = {
    new Point(x + p.x, y + p.y)
  }
  override def toString(): String = "(" + x + ", " + y + ")"
}
```

のように、メソッド定義の中から直接コンストラクタ引数を参照できます。

## メソッド定義

先ほど既にメソッド定義の例として`+`メソッドの定義が出てきましたが、一般的には、

```scala
(private[this/package名]/protected[package名]) def methodName(parameter1: Type1, parameter2: Type2, ...): ReturnType = ???
```

という形をとります。ただし、実際には `{}` 式を使った以下の形式を取ることが多いでしょう。

```scala
(private[this/package名]/protected[package名]) def methodName(parameter1: Type1, parameter2: Type2, ...): ReturnType = {
   ???
   ???
   ???
   ...
}
```

ここで、単に、メソッド本体が `{}` 式からなる場合にこうなる、というだけであって、メソッド定義を `{}` で
囲む専用の構文があるわけではないことに注意しましょう。

返り値の型は省略しても特別な場合以外型推論してくれますが、読みやすさのために、返り値の型は明記する習慣を付けるようにしましょう。`private`を付けるとそのクラス内だけから、
`protected`を付けると派生クラスからのみアクセスできるメソッドになります。 `private[this]` をつけると、同じオブジェクトからのみアクセス可能になります。また、
`private[package名]`を付けると同一パッケージに所属しているものからのみ、 `protected[package名]` をつけると、派生クラスに加えて追加で同じパッケージに所属しているもの
全てからアクセスできるようになります。`private`も`protected`も付けない場合、そのメソッドはpublicとみなされます。

先ほど定義した`Point`クラスをREPLから使ってみましょう。

```tut
class Point(val x: Int, val y: Int) {
  def +(p: Point): Point = {
    new Point(x + p.x, y + p.y)
  }
  override def toString(): String = "(" + x + ", " + y + ")"
}

val p1 = new Point(1, 1)

val p2 = new Point(2, 2)

p1 + p2
```

### 複数の引数リストを持つメソッド

メソッドは以下のように複数の引数リストを持つように定義することができます。

```scala
def methodName(parameter11: Type11, parameter12: Type12, ...)(...)(parameterN1: TypeN1, ..., parameterNM: TypeNM): RerurnType = ???
```

複数の引数リストを持つメソッドには、Scalaの糖衣構文と組み合わせて流暢なAPIを作ったり、後述するimplicit parameterのために必要になったり、
型推論を補助するために使われたりといった用途があります。
何はともあれ、複数の引数リストを持つ加算メソッドを定義してみましょう。

```tut
class Adder {
  def add(x: Int)(y: Int): Int = x + y
}

val adder = new Adder()

adder.add(2)(3)

adder.add(2) _
```

複数の引数リストを持つメソッドは`obj.m(x, y)`の形式でなく`obj.m(x)(y)`の形式で呼びだすことになります。また、一番下の例のように
最初の引数だけを適用して新しい関数を作る（部分適用）こともできます。

## フィールド定義

フィールド定義は

```scala
(private[this/package名]/protected[package名]) (val/var) fieldName: Type = Expression
```

という形を取ります。`val`の場合は変更不能、`var`の場合は変更可能なフィールドになります。また、`private`を付けるとその
クラス内だけから、`protected`を付けるとそのクラスの派生クラスからのみアクセスできるフィールドになります。 `private[this]` を
付加すると、同じオブジェクトからのみアクセス可能になります。さらに、`private[package名]`を付けると同一パッケージからのみ、
`protected[package名]` をつけると、派生クラスに加えて同じパッケージに所属しているもの全てからアクセスできるようになります。
`private`も`protected`も付けない場合、そのフィールドはpublicとみなされます。

`private[this]`を付けたフィールドへのアクセスは一般にJVMレベルでのフィールドへの直接アクセスになるため、若干高速です。細かいレベル
でのパフォーマンスチューニングをする際は意識すると良いでしょう。

## 継承

クラスのもう1つの機能は、継承です。
継承には2つの目的があります。
1つは継承によりスーパークラスの実装をサブクラスでも使うことで実装を再利用することです。
もう1つは複数のサブクラスが共通のスーパークラスのインタフェースを継承することで処理を共通化することです[^subtyping_polymorphism]。

実装の継承には複数の継承によりメソッドやフィールドの名前が衝突する場合の振舞いなどに問題があることが知られており、Javaでは実装継承が1つだけに限定されています。
Java 8ではインタフェースにデフォルトの実装を持たせられるようになりましたが、変数は持たせられないなどの制約があります。
Scalaではトレイトという仕組みで複数の実装の継承を実現していますが、トレイトについては別の節で説明します。

ここでは通常のScalaのクラスの継承について説明します。
Scalaでのクラスの継承は次のような構文になります。

```scala
class SubClass(....) extends SuperClass {
  ....
}
```

基本的に、継承のはたらきはJavaのクラスと同じですが、既存のメソッドをオーバーライドするときは`override`キーワードを
使わなければならない点が異なります。たとえば、

```tut
class APrinter() {
  def print(): Unit = {
    println("A")
  }
}

class BPrinter() extends APrinter {
  override def print(): Unit = {
    println("B")
  }
}

new APrinter().print

new BPrinter().print
```

のようにすることができます。ここで`override`キーワードをはずすと、

```tut:fail
class BPrinter() extends APrinter {
  def print(): Unit = {
    println("B")
  }
}
```

のようにメッセージを出力して、**コンパイルエラー**になります。Javaではしばしば、気付かずに既存のメソッドを
オーバーライドするつもりで新しいメソッドを定義してしまうというミスがありましたが、Scalaでは`override`キーワードを使って言語レベルでこの問題に対処しているのです。

[^subtyping_polymorphism]: このように継承などにより型に親子関係を作り、複数の型に共通のインタフェースを持たせることをサブタイピング・ポリモーフィズムと呼びます。Scalaでは他にも構造的部分型というサブタイピング・ポリモーフィズムの機能がありますが、実際に使われることが少ないため、このテキストでは説明を省略しています。
