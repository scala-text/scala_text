# トレイト

私たちの作るプログラムはしばしば数万行、多くなると数十万行やそれ以上に及ぶことがあります。
その全てを一度に把握することは難しいので、プログラムを意味のあるわかりやすい単位で分割しなければなりません。
さらに、その分割された部品はなるべく柔軟に組み立てられ、大きなプログラムを作れると良いでしょう。

プログラムの分割（モジュール化）と組み立て（合成）は、オブジェクト指向プログラミングでも関数型プログラミングにおいても重要な設計の概念になります。
そして、Scalaのオブジェクト指向プログラミングにおけるモジュール化の中心的な概念になるのがトレイトです。

この節ではScalaのトレイトの機能を一通り見ていきましょう。

## トレイト定義

Scalaのトレイトは、クラスとよく似たもので、おおまかには
次のように定義することができます。

```scala
trait <トレイト名> {
  (<フィールド定義> | <メソッド定義>)*
}
```

フィールド定義とメソッド定義は本体がなくても構いません。トレイト名で指定した名前がトレイトとして
定義されます。

## トレイトの基本

Scalaのトレイトはクラスに比べて以下のような特徴があります。

- 複数のトレイトを1つのクラスやトレイトにミックスインできる
- 直接インスタンス化できない
- パラメータを取れる（Scala 2では取れなかったが、Scala 3で導入された *trait parameters*）

以下、それぞれの特徴の紹介をしていきます。

### 複数のトレイトを1つのクラスやトレイトにミックスインできる

Scalaのトレイトはクラスとは違い、複数のトレイトを1つのクラスやトレイトにミックスインすることができます。Scala 3ではミックスインを **カンマ区切り** で書きます（Scala 2では `with` キーワードを使っていました）。

```scala mdoc:nest:silent
trait TraitA

trait TraitB

class ClassA

class ClassB

// コンパイルできる
class ClassC extends ClassA, TraitA, TraitB
```

`with` 記法も引き続き利用できるので、既存のScala 2コードを読むときには両方の形に慣れておきましょう。次の2つは同じ意味です。

```scala
class ClassC extends ClassA, TraitA, TraitB    // Scala 3 流
class ClassC extends ClassA with TraitA with TraitB   // Scala 2 流（Scala 3でも動く）
```

```scala
// コンパイルエラー！
class ClassD extends ClassA, ClassB
```

上記の例では`ClassA`と`TraitA`と`TraitB`を継承した`ClassC`を作ることはできますが`ClassA`と`ClassB`を継承した`ClassD`は作ることができません。
「class ClassB is not a trait」というエラーメッセージが出ますが、これは「`ClassB`はトレイトではない（のでミックスインできない）」という意味です。複数のクラスを継承させたい場合はクラスをトレイトにしましょう。

### 直接インスタンス化できない

Scalaのトレイトはクラスと違い、直接インスタンス化できません。

```scala mdoc:nest
trait TraitA
```

```scala
object ObjectA {
  // コンパイルエラー！
  val a = new TraitA
}
```

これは、トレイトが単体で使われることをそもそも想定していないための制限です。トレイトを使うときは、通常、それを継承した
クラスを作ります。

```scala mdoc:nest:silent
trait TraitA

class ClassA extends TraitA

object ObjectA {
  // クラスにすればインスタンス化できる
  val a = ClassA()

}
```

なお、`new Trait {}`という記法を使うと、一見トレイトをインスタンス化できているように見えます。
しかしこれは、`Trait`を継承した無名のクラスを作って、そのインスタンスを生成する構文なので、
トレイトそのものをインスタンス化できているわけではありません。

### パラメータを取れる（trait parameters）

Scala 3のトレイトはクラスと同じように **パラメータ** を取ることができます。これは *trait parameters* と呼ばれるScala 3の新機能です（Scala 2までのトレイトはパラメータを取れませんでした）。

```scala mdoc:nest:silent
trait Greeter(name: String) {
  def greet(): Unit = println(s"Hello, $name!")
}

class JapaneseGreeter extends Greeter("こうた")
class EnglishGreeter  extends Greeter("Kota")
```

`trait Greeter(name: String)` のように書くと、`Greeter` を継承するクラスは引数を渡して初期化する必要があります。

ただし、トレイトを直接インスタンス化することはできません（パラメータを取れるようになっても、インスタンス化禁止という制限は変わらないため）。また、`trait Greeter(name: String)` にパラメータを渡せるのは **クラス** か **オブジェクト** だけで、**継承チェーンの中で1度だけ** 渡します（トレイトは引数を渡さずに継承だけできます）。

Scala 2では同じことを実現するために、トレイトに抽象メンバーを持たせて、サブクラス側で値を上書きする方法が使われていました。今でもこのパターンは有効です。

```scala mdoc:nest:silent
trait TraitA {
  val name: String
  def printName(): Unit = println(name)
}

// クラスにして name を上書きする
class ClassA(val name: String) extends TraitA

object ObjectA {
  val a = ClassA("dwango")

  // name を上書きするような実装を与えてもよい
  val a2 = new TraitA { val name = "kadokawa" }
}
```

このように、抽象メンバーを使う方法は柔軟でデフォルト実装も書きやすいので、状況に応じてtrait parametersと使い分けてください。
トレイトのミックスインはモジュラリティに大きな恩恵をもたらします。是非使いこなせるようになりましょう。

### 「トレイト」という用語について

この節では、トレイトやミックスインなどオブジェクト指向の用語が用いられますが、他の言語などで用いられる用語とは少し違う意味を持つかもしれないので、注意が必要です。

トレイトはSchärliらによる2003年のECOOPに採択された論文『Traits: Composable Units of Behaviour』がオリジナルとされていますが、この論文中のトレイトの定義とScalaのトレイトの仕様は、合成時の動作や、状態変数の取り扱いなどについて、異なっているように見えます。

しかし、トレイトやミックスインという用語は言語によって異なるものであり、我々が参照しているScalaの公式ドキュメントや『Scalaスケーラブルプログラミング』でも「トレイトをミックスインする」という表現が使われていますので、ここではそれに倣いたいと思います。

## トレイトの様々な機能

### 菱形継承問題

以上見てきたようにトレイトはクラスに近い機能を持ちながら実質的な多重継承が可能であるという便利なものなのですが、
1つ考えなければならないことがあります。多重継承を持つプログラミング言語が直面する「菱形継承問題」というものです。

以下のような継承関係を考えてみましょう。
`greet`メソッドを定義した`TraitA`と、`greet`を実装した`TraitB`と`TraitC`、そして`TraitB`と`TraitC`のどちらも継承した`ClassA`です。

```scala mdoc:nest:silent
trait TraitA {
  def greet(): Unit
}

trait TraitB extends TraitA {
  def greet(): Unit = println("Good morning!")
}

trait TraitC extends TraitA {
  def greet(): Unit = println("Good evening!")
}
```

```scala
class ClassA extends TraitB, TraitC
```

`TraitB`と`TraitC`の`greet`メソッドの実装が衝突しています。この場合`ClassA`の`greet`はどのような動作をすべきなのでしょうか？
`TraitB`の`greet`メソッドを実行すべきなのか、`TraitC`の`greet`メソッドを実行すべきなのか。
多重継承をサポートする言語はどれもこのようなあいまいさの問題を抱えており、対処が求められます。

ちなみに、上記の例をScalaでコンパイルすると以下のようなエラーが出ます。

```scala
scala> class ClassA extends TraitB, TraitC
-- [E164] Declaration Error: ---------------------------------------------------
1 |class ClassA extends TraitB, TraitC
  |      ^
  |error overriding method greet in trait TraitB of type (): Unit;
  |  method greet in trait TraitC of type (): Unit class ClassA inherits conflicting members:
  |  method greet in trait TraitB of type (): Unit  and
  |  method greet in trait TraitC of type (): Unit
  |(Note: this can be resolved by declaring an override in class ClassA.)
1 error found
```

Scalaではoverride指定なしの場合メソッド定義の衝突はエラーになります。

この場合の1つの解法は、コンパイルエラーに「Note: this can be resolved by declaring an override in class ClassA.」とあるように`ClassA`で`greet`をoverrideすることです。

```scala mdoc:nest:silent
class ClassA extends TraitB, TraitC {
  override def greet(): Unit = println("How are you?")
}
```

このとき`ClassA`で`super`に型を指定してメソッドを呼びだすことで、`TraitB`や`TraitC`のメソッドを指定して使うこともできます。

```scala mdoc:nest:silent
class ClassB extends TraitB, TraitC {
  override def greet(): Unit = super[TraitB].greet()
}
```

実行結果は以下のようになります。

```scala mdoc:nest
ClassA().greet()

ClassB().greet()
```

では、`TraitB`と`TraitC`の両方のメソッドを呼び出したい場合はどうでしょうか？
1つの方法は上記と同じように`TraitB`と`TraitC`の両方のクラスを明示して呼びだすことです。

```scala mdoc:nest:silent
class ClassA extends TraitB, TraitC {
  override def greet(): Unit = {
    super[TraitB].greet()
    super[TraitC].greet()
  }
}
```

しかし、継承関係が複雑になった場合にすべてを明示的に呼ぶのは大変です。
また、コンストラクタのように必ず呼び出されるメソッドもあります。

Scalaのトレイトにはこの問題を解決するために「線形化（linearization）」という機能があります。

### 線形化（linearization）

Scalaのトレイトの線形化機能とは、トレイトがミックスインされた順番をトレイトの継承順番と見做すことです。

次に以下の例を考えてみます。先程の例との違いは`TraitB`と`TraitC`の`greet`メソッド定義に`override`修飾子が付いていることです。

```scala mdoc:nest:silent
trait TraitA {
  def greet(): Unit
}

trait TraitB extends TraitA {
  override def greet(): Unit = println("Good morning!")
}

trait TraitC extends TraitA {
  override def greet(): Unit = println("Good evening!")
}

class ClassA extends TraitB, TraitC
```

この場合はコンパイルエラーにはなりません。では`ClassA`の`greet`メソッドを呼び出した場合、いったい何が表示されるのでしょうか？
実際に実行してみましょう。

```scala mdoc:nest
ClassA().greet()
```

`ClassA`の`greet`メソッドの呼び出しで、`TraitC`の`greet`メソッドが実行されました。
これはトレイトの継承順番が線形化されて、後からミックスインした`TraitC`が優先されているためです。
つまりトレイトのミックスインの順番を逆にすると`TraitB`が優先されるようになります。
以下のようにミックスインの順番を変えてみます。

```scala mdoc:nest:silent
class ClassB extends TraitC, TraitB
```

すると`ClassB`の`greet`メソッドの呼び出しで、今度は`TraitB`の`greet`メソッドが実行されます。

```
scala> ClassB().greet()
Good morning!
```

`super`を使うことで線形化された親トレイトを使うこともできます。

```scala mdoc:nest:silent
trait TraitA {
  def greet(): Unit = println("Hello!")
}

trait TraitB extends TraitA {
  override def greet(): Unit = {
    super.greet()
    println("My name is Terebi-chan.")
  }
}

trait TraitC extends TraitA {
  override def greet(): Unit = {
    super.greet()
    println("I like niconico.")
  }
}

class ClassA extends TraitB, TraitC
class ClassB extends TraitC, TraitB
```

この`greet`メソッドの結果もまた継承された順番で変わります。

```scala mdoc:nest
ClassA().greet()

ClassB().greet()
```

線形化の機能によりミックスインされたすべてのトレイトの処理を簡単に呼び出せるようになりました。
このような線形化によるトレイトの積み重ねの処理をScalaの用語では積み重ね可能なトレイト（Stackable Trait）と呼ぶことがあります。

この線形化がScalaの菱形継承問題に対する対処法になるわけです。

### 落とし穴：トレイトの初期化順序

Scalaのトレイトの`val`の初期化順序はトレイトを使う上で大きな落とし穴になります。
以下のような例を考えてみましょう。トレイト`A`で変数`foo`を宣言し、トレイト`B`が`foo`を使って変数`bar`を作成し、クラス`C`で`foo`に値を代入してから`bar`を使っています。

```scala mdoc:nest:silent
trait A {
  val foo: String
}

trait B extends A {
  val bar = foo + "World"
}

class C extends B {
  val foo = "Hello"

  def printBar(): Unit = println(bar)
}
```

REPLでクラス`C`の`printBar`メソッドを呼び出してみましょう。

```scala mdoc:nest
C().printBar()
```

`nullWorld`と表示されてしまいました。クラス`C`で`foo`に代入した値が反映されていないようです。
どうしてこのようなことが起きるかというと、Scalaのクラスおよびトレイトはスーパークラスから順番に初期化されるからです。
この例で言えば、クラス`C`はトレイト`B`を継承し、トレイト`B`はトレイト`A`を継承しています。
つまり初期化はトレイト`A`が一番先におこなわれ、変数`foo`が宣言され、中身は何も代入されていないので、nullになります。
次にトレイト`B`で変数`bar`が宣言され`null`である`foo`と"World"という文字列から`"nullWorld"`という文字列が作られ、変数`bar`に代入されます。
先ほど表示された文字列はこれになります。

### トレイトの`val`の初期化順序の回避方法

では、この罠はどうやれば回避できるのでしょうか。上記の例で言えば、使う前にちゃんと`foo`が初期化されるように、`bar`の初期化を遅延させることです。
処理を遅延させるには`lazy val`か`def`を使います。

具体的なコードを見てみましょう。

```scala mdoc:nest:silent
trait A {
  val foo: String
}

trait B extends A {
  lazy val bar = foo + "World" // もしくは def bar でもよい
}

class C extends B {
  val foo = "Hello"

  def printBar(): Unit = println(bar)
}
```

先ほどの`nullWorld`が表示されてしまった例と違い、`bar`の初期化に`lazy val`が使われるようになりました。
これにより`bar`の初期化が実際に使われるまで遅延されることになります。
その間にクラス`C`で`foo`が初期化されることにより、初期化前の`foo`が使われることがなくなるわけです。

今度はクラス`C`の`printBar`メソッドを呼び出してもちゃんと`HelloWorld`と表示されます。

```scala mdoc:nest
C().printBar()
```

`lazy val`は`val`に比べて若干処理が重く、[複雑な呼び出しでデッドロックが発生](https://gist.github.com/xuwei-k/7b2be9957222bcb8f184)する場合があります。
`val`のかわりに`def`を使うと毎回値を計算してしまうという問題があります。
しかし、両方とも大きな問題にならない場合が多いので、特に`val`の値を使って`val`の値を作り出すような場合は`lazy val`か`def`を使うことを検討しましょう。

