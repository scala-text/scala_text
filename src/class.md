# クラス

これからScalaにおけるクラス定義に関して説明します。Javaのクラスがわかっている事を前提にします
が、ご了承ください。

## クラス定義

Scalaにおけるクラスは、記法を除けばJava言語のクラスと大して変わりません。Scalaのクラス定義は次のような形を取ります。

```scala
class <クラス名> '(' (<引数名1> : <引数型1>, <引数名2>: <引数型2> ...)? ')' {
  (<フィールド定義> | <メソッド定義> )*
}
```

たとえば、点を表すクラス`Point`を定義したいとします。`Point`はx座標を表すフィールド`x`（`Int`型）とフィールド`y`（`Int`型）からなるとします。このクラス`Point`をScalaで書くと次のようになります。

```scala mdoc:nest:silent
class Point(_x: Int, _y: Int) {
  val x = _x
  val y = _y
}
```

コンストラクタの引数と同名のフィールドを定義し、それを公開する場合は、以下のように短く書くこともできます。

```scala mdoc:nest:silent
class Point(val x: Int, val y: Int)
```

* クラス名の直後にコンストラクタ引数の定義がある
* val/varによって、コンストラクタ引数をフィールドとして公開することができる

点に注目してください。まず、最初の点ですが、Scalaでは1クラスに付き、基本的には1つのコンストラクタしか使いません。このコンストラクタを、Scalaでは*プライマリコンストラクタ*として特別に扱っています。文法上は複数のコンストラクタを定義できるようになっていますが、実際に使うことはほとんどありません。複数のオブジェクトの生成方法を提供したい場合、objectの `apply` メソッドとして定義することが多いです。

2番目の点ですが、プライマリコンストラクタの引数にval/varをつけるとそのフィールドは公開され、外部からアクセスできるようになります。なお、プライマリコンストラクタの引数のスコープはクラス定義全体におよびます。そのため、以下のようにメソッド定義の中から直接コンストラクタ引数を参照できます。

```scala mdoc:nest:silent
class Point(val x: Int, val y: Int) {
  def +(p: Point): Point = {
    new Point(x + p.x, y + p.y)
  }
  override def toString(): String = "(" + x + ", " + y + ")"
}
```


## メソッド定義

先ほど既にメソッド定義の例として`+`メソッドの定義が出てきましたが、一般的には、次のような形をとります。

```scala
(private([this | <パッケージ名>])? | protected([<パッケージ名>])?)? def <メソッド名> '('
  (<引数名> : 引数型 (, 引数名 : <引数型>)*)?
')': <返り値型> = <本体>
```

実際にはブロック式を使った以下の形式を取ることが多いでしょう。

```scala
(private([this | <パッケージ名>])? | protected([<パッケージ名>])?)? def <メソッド名> '('
  (<引数名> : 引数型 (, 引数名 : <引数型>)*)?
')': <返り値型> = {
  (<式> (; | <改行>)?)*
}
```

単にメソッド本体がブロック式からなる場合にこうなるというだけであって、メソッド定義を `{}` で囲む専用の構文があるわけではありません。

返り値型は省略しても特別な場合以外型推論してくれますが、読みやすさのために、返り値の型は明記する習慣を付けるようにしましょう。 `private` を付けるとそのクラス内だけから、 `protected` を付けると派生クラスからのみアクセスできるメソッドになります。  `private[this]` をつけると、同じオブジェクトからのみアクセス可能になります。また、 `private[パッケージ名]` を付けると同一パッケージに所属しているものからのみ、 `protected[パッケージ名]` をつけると、派生クラスに加えて追加で同じパッケージに所属しているもの全てからアクセスできるようになります。 `private` も `protected` も付けない場合、そのメソッドはpublicとみなされます。

先ほど定義した`Point`クラスをREPLから使ってみましょう。

```scala mdoc:nest
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

## 複数の引数リストを持つメソッド

メソッドは以下のように複数の引数リストを持つように定義することができます。

```scala
(private([this | <パッケージ名>])? | protected([<パッケージ名>])?)? def <メソッド名> '('
  (<引数名> : 引数型 (, 引数名 : <引数型>)*)?
')'( '('
  (<引数名> : 引数型 (, 引数名 : <引数型>)*)?
')' )* : <返り値型> = <本体式>
```

複数の引数リストを持つメソッドには、Scalaの糖衣構文と組み合わせて流暢なAPIを作ったり、後述するimplicit parameterのために必要になったり、型推論を補助するために使われたりといった用途があります。複数の引数リストを持つ加算メソッドを定義してみましょう。

```scala mdoc:nest
class Adder {
  def add(x: Int)(y: Int): Int = x + y
}

val adder = new Adder()

adder.add(2)(3)

val fun = adder.add(2) _
fun(3)
```

複数の引数リストを持つメソッドは `obj.method(x, y)`の形式でなく `obj.method(x)(y)` の形式で呼びだすことになります。また、一番下の例のように最初の引数だけを適用して新しい関数を作る（部分適用）こともできます。

次のように、複数の引数リストを使わずに単に複数の引数を持つメソッドも作ることができます。

```scala mdoc:nest
class Adder {
  def add(x: Int, y: Int): Int = x + y
}

val adder = new Adder()

adder.add(2, 3)

val fun: Int => Int = adder.add(2, _)
fun(3)
```

## フィールド定義

フィールド定義は

```scala
(private([this | <パッケージ名>])? | protected([<パッケージ名>])?)? (val | var) <フィールド名>: <フィールド型> = <初期化式>
```

という形を取ります。`val`の場合は変更不能、`var`の場合は変更可能なフィールドになります。また、`private`を付けるとそのクラス内だけから、`protected`を付けるとそのクラスの派生クラスからのみアクセスできるフィールドになります。 `private[this]` を付加すると、同じオブジェクトからのみアクセス可能になります。さらに、`private[<パッケージ名>]`を付けると同一パッケージからのみ、 `protected[パッケージ名]` をつけると、派生クラスに加えて同じパッケージに所属しているもの全てからアクセスできるようになります。 `private`も`protected`も付けない場合、そのフィールドはpublicとみなされます。 `private[this]`を付けたフィールドへのアクセスは一般にJVMレベルでのフィールドへの直接アクセスになるため、若干高速です。細かいレベルでのパフォーマンスチューニングをする際は意識すると良いでしょう。

## 抽象メンバー

その時点では実装を書くことができず、後述する継承の際に、メソッドやフィールドの実装を与えたいということがあります。このような場合に対応するため、Scalaでは抽象メンバーを定義することができます。抽象メンバーは、メソッドの場合とフィールドの場合があり、メソッドの場合は次のようになります。

```scala
(private([this | <パッケージ名>])? | protected([<パッケージ名>])?)? def <メソッド名> '('
  (<引数名> : 引数型 (, 引数名 : <引数型>)*)?
')': <返り値型>
```

フィールドの定義は次のようになります。

```scala
(private([this | <パッケージ名>])? | protected([<パッケージ名>])?)? (val | var) <フィールド名>: <フィールド型>
```

メソッドやフィールドの中身がない以外は、通常のメソッドやフィールド定義と同じです。また、抽象メソッドを一個以上持つクラスは、抽象クラスとして宣言する必要があります。たとえば、`x`座標と`y`座標を持つ、抽象クラス`XY`は次のようにして定義します。クラスの前に`abstract` 修飾子をつける必要があるのがポイントです。

```scala mdoc:nest
abstract class XY {
  def x: Int
  def y: Int
}
```

## 継承

Scalaのクラスは、Javaのクラスと同様、継承することができます。継承には2つの目的があります。1つは継承によりスーパークラスの実装をサブクラスでも使うことで実装を再利用することです。もう1つは複数のサブクラスが共通のスーパークラスのインタフェースを継承することで処理を共通化することです[^subtyping_polymorphism]。実装の継承には複数の継承によりメソッドやフィールドの名前が衝突する場合の振舞いなどに問題があることが知られており、Javaでは実装継承が1つだけに限定されています。Java 8ではインタフェースにデフォルトの実装を持たせられるようになりましたが、変数は持たせられないという制約があります。Scalaではトレイトという仕組みで複数の実装の継承を実現していますが、トレイトについては別の節で説明します。

ここでは通常のScalaのクラスの継承について説明します。Scalaでのクラスの継承は次のような構文になります。

```scala
class <クラス名> <クラス引数> (extends <スーパークラス>)? (with <トレイト名>)* {
  (<フィールド定義> | <メソッド定義>)*
}
```

トレイト名はここでは使われませんが、後で出てくるトレイトの節で説明を行います。継承のはたらきはJavaのクラスと同様ですが、既存のメソッドをオーバーライドするときは`override`キーワードを使わなければならない点が異なります。 たとえば、次のようにすることができます。

```scala mdoc:nest
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

new APrinter().print()

new BPrinter().print()
```


ここで`override`キーワードをはずすと、次のようにメッセージを出力して、**コンパイルエラー**になります。 

```scala
class BPrinter() extends APrinter {
  def print(): Unit = {
    println("B")
  }
}
```

Javaではしばしば、気付かずに既存のメソッドをオーバーライドするつもりで新しいメソッドを定義してしまうというミスがありますが、Scalaでは`override`キーワードを使って言語レベルでこの問題に対処しているのです。

### 練習問題 {#class_ex1}

全てが `Int` 型の `x` 、 `y` 、 `z` という名前を持った、3次元座標を表す `Point3D` クラスを定義してください。 `Point3D` クラスは次のようにして使うことができなければいけません。

```scala
val p = new Point3D(10, 20, 30)
println(p.x) // 10
println(p.y) // 20
println(p.z) // 30
```

<!-- begin answer id="answer_ex1" style="display:none" -->

```scala mdoc:nest:silent
class Point3D(val x: Int, val y: Int, val z: Int)
```

#### 解説

プライマリコンストラクタの引数として座標の値を渡し、それをそのまま取り出しているので、プライマリコンストラクタの引数に `val` を付けるのが最も簡単です。別解として、以下のように別途 `val` でフィールドを定義することも可能ですが、今回あえてそうする意味は少ないでしょう。 

```scala mdoc:nest:silent
class Point3D(x_ : Int, y_ : Int, z_ : Int) {
  val x: Int = x_
  val y: Int = y_
  val z: Int = z_
}
```

<!-- end answer -->

### 練習問題 {#class_ex2}

次の抽象クラス `Shape` を継承して、 `Rectangle` クラス（長方形クラス）と`Circle` クラス（円クラス）を定義してください。また、`area` メソッドをオーバーライドして、ただしく面積が計算できるように定義してください。なお、長方形の面積は幅を`w`、高さを`h`とすると、`w * h`で求めることができます。円の面積は、半径を`r`とすると、`r * r * math.Pi` で求めることができます。

```scala
abstract class Shape {
  def area: Double
}
/*
 * RectangleとCircleの定義
 */
var shape: Shape = new Rectangle(10.0, 20.0)
println(shape.area)
shape = new Circle(2.0)
println(shape.area)
```

<!-- begin answer id="answer_ex2" style="display:none" -->

```scala mdoc:nest:silent
abstract class Shape {
  def area: Double
}
class Rectangle(val width: Double, val height: Double) extends Shape {
  override def area: Double = width * height
}
class Circle(val radius: Double) extends Shape {
  override def area: Double = radius * radius * math.Pi
}
```

#### 解説

`Rectangle`と`Circle`クラスが`Shape`クラスを継承して、それぞれで`area: Double`メソッドをオーバーライドしています。この場合、`Shape`の`area`は抽象メソッドなので`override`は必須ではありませんが、つけた方が良いでしょう。

<!-- end answer -->


[^subtyping_polymorphism]: このように継承などにより型に親子関係を作り、複数の型に共通のインタフェースを持たせることをサブタイピング・ポリモーフィズムと呼びます。Scalaでは他にも構造的部分型というサブタイピング・ポリモーフィズムの機能がありますが、実際に使われることが少ないため、このテキストでは説明を省略しています。
