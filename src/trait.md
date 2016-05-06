# トレイト

私たちの作るプログラムはしばしば数万行、多くなると数十万行やそれ以上に及ぶことがあります。
その全てを一度に把握することは難しいので、プログラムを意味のあるわかりやすい単位で分割しなければなりません。
さらに、その分割された部品はなるべく柔軟に組み立てられ、大きなプログラムを作れると良いでしょう。

プログラムの分割（モジュール化）と組み立て（合成）は、オブジェクト指向プログラミングでも関数型プログラミングにおいても重要な設計の概念になります。
そして、Scalaのオブジェクト指向プログラミングにおけるモジュール化の中心的な概念になるのがトレイトです。

この節ではScalaのトレイトの機能を一通り見ていきましょう。

## トレイトの基本（★★★）

Scalaのトレイトはクラスに比べて以下のような特徴があります。

- 複数のトレイトを1つのクラスやトレイトにミックスインできる
- 直接インスタンス化できない
- クラスパラメータ（コンストラクタの引数）を取ることができない

以下、それぞれの特徴の紹介をしていきます。

### 複数のトレイトを1つのクラスやトレイトにミックスインできる

Scalaのトレイトはクラスとは違い、複数のトレイトを1つのクラスやトレイトにミックスインすることができます。

```scala
trait TraitA

trait TraitB

class ClassA

class ClassB

// コンパイルできる
class ClassC extends ClassA with TraitA with TraitB

// コンパイルエラー！
// class ClassB needs to be a trait to be mixed in
class ClassD extends ClassA with ClassB
```

上記の例では`ClassA`と`TraitA`と`TraitB`を継承した`ClassC`を作ることはできますが`ClassA`と`ClassB`を継承した`ClassD`は作ることができません。
「class ClassB needs to be a trait to be mixed in」というエラーメッセージが出ますが、これは「`ClassB`をミックスインさせるためにはトレイトにする必要がある」という意味です。
複数のクラスを継承させたい場合はクラスをトレイトにしましょう。

### 直接インスタンス化できない

Scalaのトレイトはクラスと違い、直接インスタンス化できません。

```scala
trait TraitA

object ObjectA {
  // コンパイルエラー！
  // trait TraitA is abstract; cannot be instantiated
  val a = new TraitA
}
```

この制限は回避する方法がいくつかあります。1つはインスタンス化できるようにトレイトを継承したクラスを作ることです。
もう1つはトレイトに実装を与えてインスタンス化する方法です。

```tut:silent
trait TraitA

class ClassA extends TraitA

object ObjectA {
  // クラスにすればインスタンス化できる
  val a = new ClassA

  // 実装を与えてもインスタンス化できる
  val a2 = new TraitA {}
}
```

このように実際使う上では、あまり問題にならない制限でしょう。

### クラスパラメータ（コンストラクタの引数）を取ることができない

Scalaのトレイトはクラスと違いパラメータ（コンストラクタの引数）を取ることができないという制限があります[^trait-param-sip]。

```scala
// 正しいプログラム
class ClassA(name: String) {
  def printName() = println(name)
}

// コンパイルエラー！
// traits or objects may not have parameters
trait TraitA(name: String) {
  def printName: Unit = println(name)
}
```

これもあまり問題になることはありません。トレイトに抽象メンバーを持たせることで値を渡すことができます。
インスタンス化できない問題のときと同じようにクラスに継承させたり、
インスタンス化のときに抽象メンバーを実装をすることでトレイトに値を渡すことができます。

```tut:silent
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

### 「トレイト」という用語について（★）

この節では、トレイトやミックスインなどオブジェクトの指向の用語が用いられますが、他の言語などで用いられる用語とは少し違う意味を持つかもしれないので、注意が必要です。

トレイトはSchärliらによる2003年のECOOPに採択された論文『Traits: Composable Units of Behaviour』がオリジナルとされていますが、この論文中のトレイトの定義とScalaのトレイトの仕様は、合成時の動作や、状態変数の取り扱いなどについて、異なっているように見えます。

しかし、トレイトやミックスインという用語は言語によって異なるものであり、我々が参照しているScalaの公式ドキュメントや『Scalaスケーラブルプログラミング』でも「トレイトをミックスインする」という表現が使われていますので、ここではそれに倣いたいと思います。

## トレイトの様々な機能

### 菱形継承問題（★★）

以上見てきたようにトレイトはクラスに近い機能を持ちながら実質的な多重継承が可能であるという便利なものなのですが、
1つ考えなければならないことがあります。多重継承を持つプログラミング言語が直面する「菱形継承問題」というものです。

以下のような継承関係を考えてみましょう。
`greet`メソッドを定義した`TraitA`と、`greet`を実装した`TraitB`と`TraitC`、そして`TraitB`と`TraitC`のどちらも継承した`ClassA`です。

```scala
trait TraitA {
  def greet(): Unit
}

trait TraitB extends TraitA {
  def greet(): Unit = println("Good morning!")
}

trait TraitC extends TraitA {
  def greet(): Unit = println("Good evening!")
}

class ClassA extends TraitB with TraitC
```

`TraitB`と`TraitC`の`greet`メソッドの実装が衝突しています。この場合`ClassA`の`greet`はどのような動作をすべきなのでしょうか？
`TraitB`の`greet`メソッドを実行すべきなのか、`TraitC`の`greet`メソッドを実行すべきなのか。
多重継承をサポートする言語はどれもこのようなあいまいさの問題を抱えており、対処が求められます。

ちなみに、上記の例をScalaでコンパイルすると以下のようなエラーが出ます。

```
ClassA.scala:13: error: class ClassA inherits conflicting members:
  method greet in trait TraitB of type ()Unit  and
  method greet in trait TraitC of type ()Unit
(Note: this can be resolved by declaring an override in class ClassA.)
class ClassA extends TraitB with TraitC
      ^
one error found
```

Scalaではoverride指定なしの場合メソッド定義の衝突はエラーになります。

この場合の1つの解法は、コンパイルエラーに「Note: this can be resolved by declaring an override in class ClassA.」とあるように`ClassA`で`greet`をoverrideすることです。

```scala
class ClassA extends TraitB with TraitC {
  override def greet(): Unit = println("How are you?")
}
```

このとき`ClassA`で`super`に型を指定してメソッドを呼びだすことで、`TraitB`や`TraitC`のメソッドを指定して使うこともできます。

```scala
class ClassB extends TraitB with TraitC {
  override def greet(): Unit = super[TraitB].greet()
}
```

実行結果は以下にようになります。

```scala
scala> (new ClassA).greet()
How are you?

scala> (new ClassB).greet()
Good morning!
```

では、`TraitB`と`TraitC`の両方のメソッドを呼び出したい場合はどうでしょうか？
1つの方法は上記と同じように`TraitB`と`TraitC`の両方のクラスを明示して呼びだすことです。

```scala
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

### 線形化（linearization）（★）

Scalaのトレイトの線形化機能とは、トレイトがミックスインされた順番をトレイトの継承順番と見做すことです。

次に以下の例を考えてみます。先程の例との違いは`TraitB`と`TraitC`の`greet`メソッド定義に`override`修飾子が付いていることです。

```tut:silent
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

```tut
(new ClassA).greet()
```

`ClassA`の`greet`メソッドの呼び出しで、`TraitC`の`greet`メソッドが実行されました。
これはトレイトの継承順番が線形化されて、後からミックスインした`TraitC`が優先されているためです。
つまりトレイトのミックスインの順番を逆にすると`TraitB`が優先されるようになります。
以下のようにミックスインの順番を変えてみます。

```tut:silent
class ClassB extends TraitC with TraitB
```

すると`ClassB`の`greet`メソッドの呼び出して、今度は`TraitB`の`greet`メソッドが実行されます。

```
scala> (new ClassB).greet()
Good morning!
```

`super`を使うことで線形化された親トレイトを使うこともできます

```tut:silent
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

```tut
(new ClassA).greet()

(new ClassB).greet()
```

線形化の機能によりミックスインされたすべてのトレイトの処理を簡単に呼び出せるようになりました。
このような線形化によるトレイトの積み重ねの処理をScalaの用語では積み重ね可能なトレイト（Stackable Trait）と呼ぶことがあります。

この線形化がScalaの菱形継承問題に対する対処法になるわけです。

### abstract override（★）

通常のメソッドのオーバーライドで`super`を使ってスーパークラスのメソッドを呼びだす場合、
当然のことながら継承元のスーパークラスにそのメソッドの実装がなければならないわけですが、
Scalaには継承元のスーパークラスにそのメソッドの実装がない場合でもメソッドのオーバーライドが可能な`abstract override`という機能があります。

`abstract override`ではない`override`と`abstract override`を比較してみましょう。

```scala
trait TraitA {
  def greet(): Unit
}

// コンパイルエラー！
// method greet in trait TraitA is accessed from super. It may not be abstract unless it is overridden by a member declared `abstract' and `override'
trait TraitB extends TraitA {
  override def greet(): Unit = {
    super.greet()
    println("Good morning!")
  }
}

// コンパイルできる
trait TraitC extends TraitA {
  abstract override def greet(): Unit = {
    super.greet()
    println("Good evening!")
  }
}
```

`abstract`修飾子を付けていない`TraitB`はコンパイルエラーになってしまいました。エラーメッセージの意味は、`TraitA`の`greet`メソッドには実装がないので`abstract override`を付けない場合オーバーライドが許されないということです。

オーバーライドを`abstract override`にすることでスーパークラスのメソッドの実装がない場合でもオーバーライドすることができます。この特性は抽象クラスに対しても積み重ねの処理が書けるということを意味します。

しかし`abstract override`でも1つ制約があり、ミックスインされてクラスが作られるときにはスーパークラスのメソッドが実装されてなければなりません。

```scala
trait TraitA {
  def greet(): Unit
}

trait TraitB extends TraitA {
  def greet(): Unit =
    println("Hello!")
}

trait TraitC extends TraitA {
  abstract override def greet(): Unit = {
    super.greet()
    println("I like niconico.")
  }
}

// コンパイルエラー！
// class ClassA needs to be a mixin, since method greet in trait TraitC of type ()Unit is marked `abstract' and `override', but no concrete implementation could be found in a base
class ClassA extends TraitC

// コンパイルできる
class ClassB extends TraitB with TraitC
```

### 自分型（★★）

Scalaにはクラスやトレイトの中で自分自身の型にアノテーションを記述することができる機能があります。
これを自分型アノテーション（self type annotations）や単に自分型（self types）などと呼びます。

例を見てみましょう。

```tut:silent
trait Greeter {
  def greet(): Unit
}

trait Robot {
  self: Greeter =>

  def start(): Unit = greet()
}
```

このロボット（`Robot`）は起動（`start`）するときに挨拶（`greet`）するようです。
`Robot`は直接`Greeter`を継承していないのにもかかわらず`greet`メソッドを使えていることに注意してください。

このロボットのオブジェクトを実際に作るためには`greet`メソッドを実装したトレイトが必要になります。
REPLを使って動作を確認してみましょう。

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

trait HelloGreeter extends Greeter {
  def greet(): Unit = println("Hello!")
}

// Exiting paste mode, now interpreting.

defined trait HelloGreeter

scala> val r = new Robot with HelloGreeter
r: Robot with HelloGreeter = $anon$1@1e5756c0

scala> r.start()
Hello!
```

自分型を使う場合は、抽象トレイトを指定し、後から実装を追加するという形になります。
このように後から（もしくは外から）利用するモジュールの実装を与えることを依存性の注入（Dependency Injection）と呼ぶことがあります。
自分型を使われている場合、この依存性の注入のパターンが使われていると考えてよいでしょう。

ではこの自分型によるトレイトの指定は以下のように直接継承する場合と比べてどのような違いがあるのでしょうか。

```tut:silent
trait Greeter {
  def greet(): Unit
}

trait Robot2 extends Greeter {
  def start(): Unit = greet()
}
```

オブジェクトを生成するという点では変わりません。
`Robot2`も先程と同じように作成することができます。
ただし、このトレイトを利用する側や、継承したトレイトやクラスには`Greeter`トレイトの見え方に違いができます。

```scala
scala> val r: Robot = new Robot with HelloGreeter
r: Robot = $anon$1@10470bfa

scala> r.greet()
<console>:9: error: value greet is not a member of Robot
              r.greet()

scala> val r: Robot2 = new Robot2 with HelloGreeter
r: Robot = $anon$1@10470bfa

scala> r.greet()
Hello!
```

継承で作られた`Robot2`オブジェクトでは`Greeter`トレイトの`greet`メソッドを呼び出せてしまいますが、
自分型で作られた`Robot`オブジェクトでは`greet`メソッドを呼びだすことができません。

`Robot`が利用を宣言するためにある`Greeter`のメソッドが外から呼び出せてしまうことはあまり良いことではありません。
この点で自分型を使うメリットがあると言えるでしょう。逆に単に依存性を注入できればよいという場合には、この動作は煩わしく感じられるかもしれません。

もう1つ自分型の特徴としては型の循環参照を許す点です。

自分型を使う場合は以下のようなトレイトの相互参照を許しますが、

```scala
// コンパイルできる
trait Greeter {
  self: Robot =>

  def greet(): Unit = println(s"My name is $name")
}

trait Robot {
  self: Greeter =>

  def name: String

  def start(): Unit = greet()
}
```

これを先ほどのように継承に置き換えることではできません。

```scala
// コンパイルエラー
// illegal cyclic reference involving trait Greeter
trait Greeter extends Robot {
  def greet(): Unit = println(s"My name is $name")
}

trait Robot extends Greeter {
  def name: String

  def start(): Unit = greet()
}
```

しかし、このように循環するような型構成を有効に使うのは難しいかもしれません。

依存性の注入を使う場合、継承を使うか、自分型を使うかというのは若干悩ましい問題かもしれません。
機能的には継承があればよいと言えますが、上記のような可視性の問題がありますし、
自分型を使うことで依存性の注入を利用しているとわかりやすくなる効果もあります。
利用する場合はチームで相談するとよいかもしれません。

### 落とし穴：トレイトの初期化順序（★★）

Scalaのトレイトの`val`の初期化順序はトレイトを使う上で大きな落とし穴になります。
以下のような例を考えてみましょう。トレイト`A`で変数`foo`を宣言し、トレイト`B`が`foo`を使って変数`bar`を作成し、クラス`C`で`foo`に値を代入してから`bar`を使っています。

```tut:silent
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

```tut
(new C).printBar()
```

`nullWorld`と表示されてしまいました。クラス`C`で`foo`に代入した値が反映されていないようです。
どうしてこのようなことが起きるかというと、Scalaのクラスおよびトレイトはスーパークラスから順番に初期化されるからです。
この例で言えば、クラス`C`はトレイト`B`を継承し、トレイト`B`はトレイト`A`を継承しています。
つまり初期化はトレイト`A`が一番先におこなわれ、変数`foo`が宣言され、中身は何も代入されていないので、nullになります。
次にトレイト`B`で変数`bar`が宣言され`null`である`foo`と"World"という文字列から`"nullWorld"`という文字列が作られ、変数`bar`に代入されます。
先ほど表示された文字列はこれになります。

このような簡単な例なら気づきやすいのですが、似たような形の大規模な例もあります。
先ほど自分型で紹介した「依存性の注入」は、上位のトレイトで宣言したものを、中間のトレイトで使い、最終的にインスタンス化するときにミックスインするという手法です。
ここでもうっかりすると同じような罠を踏んでしまいます。
Scala上級者でもやってしまうのが`val`の初期化順の罠なのです。

### トレイトの`val`の初期化順序の回避方法（★★）

では、この罠はどうやれば回避できるのでしょうか。上記の例で言えば、使う前にちゃんと`foo`が初期化されるように、`bar`の初期化を遅延させることです。
処理を遅延させるには`lazy val`か`def`を使います。

具体的なコードを見てみましょう。

```tut:silent
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

```tut
(new C).printBar()
```

`lazy val`は`val`に比べて若干処理が重く、[複雑な呼び出しでデッドロックが発生](https://gist.github.com/xuwei-k/7b2be9957222bcb8f184)する場合があります。
`val`のかわりに`def`を使うと毎回値を計算してしまうという問題があります。
しかし、両方とも大きな問題にならない場合が多いので、特に`val`の値を使って`val`の値を作り出すような場合は`lazy val`か`def`を使うことを検討しましょう。

トレイトの`val`の初期化順序を回避するもう1つの方法としては事前定義（Early Definitions）を使う方法もあります。
事前定義というのはフィールドの初期化をスーパークラスより先におこなう方法です。

```tut:silent
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


[^trait-param-sip]: 将来のバージョンでは、パラメータを取れるようになるかもしれないという話があります http://docs.scala-lang.org/sips/pending/trait-parameters.html
