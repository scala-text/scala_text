# トレイト

私たちの作るプログラムはしばしば数万行、多くなると数十万行やそれ以上に及ぶことがあります。
その全てを一度に把握することは難しいので、プログラムを意味のあるわかりやすい単位で分割しなければなりません。
さらに、その分割された部品はなるべく柔軟に組み立てられ、大きなプログラムを作れると良いでしょう。

プログラムの分割（モジュール化）と組み立て（合成）は、オブジェクト指向プログラミングでも関数型プログラミングにおいても重要な設計の概念になります。
そして、Scalaのオブジェクト指向プログラミングにおけるモジュール化の中心的な概念になるのがトレイトです。

この節ではScalaのトレイトの機能を一通り見ていきましょう。

## トレイト定義

Scalaのトレイトは、クラスからコンストラクタを定義する機能を抜いたようなもので、おおまかには
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
- クラスパラメータ（コンストラクタの引数）を取ることができない

以下、それぞれの特徴の紹介をしていきます。

### 複数のトレイトを1つのクラスやトレイトにミックスインできる

Scalaのトレイトはクラスとは違い、複数のトレイトを1つのクラスやトレイトにミックスインすることができます。

```scala mdoc:nest:silent
trait TraitA

trait TraitB

class ClassA

class ClassB

// コンパイルできる
class ClassC extends ClassA with TraitA with TraitB
```

```scala
// コンパイルエラー！
class ClassD extends ClassA with ClassB
```

上記の例では`ClassA`と`TraitA`と`TraitB`を継承した`ClassC`を作ることはできますが`ClassA`と`ClassB`を継承した`ClassD`は作ることができません。
「class ClassB needs to be a trait to be mixed in」というエラーメッセージが出ますが、これは「`ClassB`をミックスインさせるためにはトレイトにする必要がある」という意味です。複数のクラスを継承させたい場合はクラスをトレイトにしましょう。

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
  val a = new ClassA

}
```

なお、`new Trait {}`という記法を使うと、一見トレイトをインスタンス化できているように見えます。
しかしこれは、`Trait`を継承した無名のクラスを作って、そのインスタンスを生成する構文なので、
トレイトそのものをインスタンス化できているわけではありません。

### クラスパラメータ（コンストラクタの引数）を取ることができない

Scalaのトレイトはクラスと違いパラメータ（コンストラクタの引数）を取ることができないという制限があります[^trait-param-dotty]。

```scala mdoc:nest:silent
// 正しいプログラム
class ClassA(name: String) {
  def printName() = println(name)
}
```

```scala
// コンパイルエラー！
trait TraitA(name: String)
```

これもあまり問題になることはありません。トレイトに抽象メンバーを持たせることで値を渡すことができます。
インスタンス化できない問題のときと同じようにクラスに継承させたり、
インスタンス化のときに抽象メンバーを実装をすることでトレイトに値を渡すことができます。

```scala mdoc:nest:silent
trait TraitA {
  val name: String
  def printName(): Unit = println(name)
}

// クラスにして name を上書きする
class ClassA(val name: String) extends TraitA

object ObjectA {
  val a = new ClassA("dwango")

  // name を上書きするような実装を与えてもよい
  val a2 = new TraitA { val name = "kadokawa" }
}
```

以上のようにトレイトの制限は実用上ほとんど問題にならないようなものであり、その他の点ではクラスと同じように使うことができます。
つまり実質的に多重継承と同じようなことができるわけです。
そしてトレイトのミックスインはモジュラリティに大きな恩恵をもたらします。是非使いこなせるようになりましょう。

### 「トレイト」という用語について

この節では、トレイトやミックスインなどオブジェクトの指向の用語が用いられますが、他の言語などで用いられる用語とは少し違う意味を持つかもしれないので、注意が必要です。

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
class ClassA extends TraitB with TraitC
```

`TraitB`と`TraitC`の`greet`メソッドの実装が衝突しています。この場合`ClassA`の`greet`はどのような動作をすべきなのでしょうか？
`TraitB`の`greet`メソッドを実行すべきなのか、`TraitC`の`greet`メソッドを実行すべきなのか。
多重継承をサポートする言語はどれもこのようなあいまいさの問題を抱えており、対処が求められます。

ちなみに、上記の例をScalaでコンパイルすると以下のようなエラーが出ます。

```scala
scala> class ClassA extends TraitB with TraitC
<console>:13: error: class ClassA inherits conflicting members:
  method greet in trait TraitB of type ()Unit  and
  method greet in trait TraitC of type ()Unit
(Note: this can be resolved by declaring an override in class ClassA.)
       class ClassA extends TraitB with TraitC
             ^
```

Scalaではoverride指定なしの場合メソッド定義の衝突はエラーになります。

この場合の1つの解法は、コンパイルエラーに「Note: this can be resolved by declaring an override in class ClassA.」とあるように`ClassA`で`greet`をoverrideすることです。

```scala mdoc:nest:silent
class ClassA extends TraitB with TraitC {
  override def greet(): Unit = println("How are you?")
}
```

このとき`ClassA`で`super`に型を指定してメソッドを呼びだすことで、`TraitB`や`TraitC`のメソッドを指定して使うこともできます。

```scala mdoc:nest:silent
class ClassB extends TraitB with TraitC {
  override def greet(): Unit = super[TraitB].greet()
}
```

実行結果は以下にようになります。

```scala mdoc:nest
(new ClassA).greet()

(new ClassB).greet()
```

では、`TraitB`と`TraitC`の両方のメソッドを呼び出したい場合はどうでしょうか？
1つの方法は上記と同じように`TraitB`と`TraitC`の両方のクラスを明示して呼びだすことです。

```scala mdoc:nest:silent
class ClassA extends TraitB with TraitC {
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

class ClassA extends TraitB with TraitC
```

この場合はコンパイルエラーにはなりません。では`ClassA`の`greet`メソッドを呼び出した場合、いったい何が表示されるのでしょうか？
実際に実行してみましょう。

```scala mdoc:nest
(new ClassA).greet()
```

`ClassA`の`greet`メソッドの呼び出しで、`TraitC`の`greet`メソッドが実行されました。
これはトレイトの継承順番が線形化されて、後からミックスインした`TraitC`が優先されているためです。
つまりトレイトのミックスインの順番を逆にすると`TraitB`が優先されるようになります。
以下のようにミックスインの順番を変えてみます。

```scala mdoc:nest:silent
class ClassB extends TraitC with TraitB
```

すると`ClassB`の`greet`メソッドの呼び出しで、今度は`TraitB`の`greet`メソッドが実行されます。

```
scala> (new ClassB).greet()
Good morning!
```

`super`を使うことで線形化された親トレイトを使うこともできます

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

class ClassA extends TraitB with TraitC
class ClassB extends TraitC with TraitB
```

この`greet`メソッドの結果もまた継承された順番で変わります。

```scala mdoc:nest
(new ClassA).greet()

(new ClassB).greet()
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
(new C).printBar()
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
(new C).printBar()
```

`lazy val`は`val`に比べて若干処理が重く、[複雑な呼び出しでデッドロックが発生](https://gist.github.com/xuwei-k/7b2be9957222bcb8f184)する場合があります。
`val`のかわりに`def`を使うと毎回値を計算してしまうという問題があります。
しかし、両方とも大きな問題にならない場合が多いので、特に`val`の値を使って`val`の値を作り出すような場合は`lazy val`か`def`を使うことを検討しましょう。

トレイトの`val`の初期化順序を回避するもう1つの方法としては事前定義（Early Definitions）を使う方法もあります。
事前定義というのはフィールドの初期化をスーパークラスより先におこなう方法です。
ただし、この機能はScala 3では無くなりました。

```scala
trait A {
  val foo: String
}

trait B extends A {
  val bar = foo + "World" // valのままでよい
}

class C extends {
  val foo = "Hello" // スーパークラスの初期化の前に呼び出される
} with B {
  def printBar(): Unit = println(bar)
}
```

上記の`C`の`printBar`を呼び出しても正しく`HelloWorld`と表示されます。

この事前定義は利用側からの回避方法ですが、
この例の場合はトレイト`B`のほうに問題がある（普通に使うと初期化の問題が発生してしまう）ので、
トレイト`B`のほうを修正したほうがいいかもしれません。

トレイトの初期化問題は継承されるトレイト側で解決したほうが良いことが多いので、
この事前定義の機能は実際のコードではあまり見ることはないかもしれません。


[^trait-param-dotty]: Scala 3では、[トレイトがパラメータを取る](https://docs.scala-lang.org/scala3/reference/other-new-features/trait-parameters.html)ことができます。
