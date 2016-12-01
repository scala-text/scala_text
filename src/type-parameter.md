# 型パラメータ（type parameter）

クラスの節では触れませんでしたが、クラスは0個以上の型をパラメータとして取ることができます。これは、クラスを作る時点では何の型か特定できない場合（たとえば、コレクションクラスの要素の型）を表したい時に役に立ちます。型パラメータを入れたクラス定義の文法は
次のようになります

```scala
class クラス名[型パラメータ1, 型パラメータ2, ..., 型パラメータN](コンストラクタ引数1 :コンストラクタ引数1の型, コンストラクタ引数2 :コンストラクタ引数2の型, ...)
{
  0個以上のフィールドの定義またはメソッド定義
}
```

`型パラメータ1`から`型パラメータN`までは好きな名前を付け、クラス定義の中で使うことができます。とりあえず、簡単な例として、1個の要素を保持して、要素を入れる（`put`する）か取りだす（`get`する）操作ができるクラス`Cell`を定義してみます。`Cell`の定義は次のようになります。

```tut:silent
class Cell[A](var value: A) {
  def put(newValue: A): Unit = {
    value = newValue
  }

  def get(): A = value
}
```

これをREPLで使ってみましょう。

```scala
scala> class Cell[A](var value: A) {
     |   def put(newValue: A): Unit = {
     |     value = newValue
     |   }
     |   
     |   def get(): A = value
     | }
defined class Cell

scala> val cell = new Cell[Int](1)
cell: Cell[Int] = Cell@192aaffb

scala> cell.put(2)

scala> cell.get()
res1: Int = 2

scala> cell.put("something")
<console>:10: error: type mismatch;
 found   : String("something")
 required: Int
              cell.put("something")
                       ^
```

```tut
val cell = new Cell[Int](1)
```

で、型パラメータとして`Int`型を与えて、その初期値として1を与えています。型パラメータに`Int`を与えて`Cell`をインスタンス化したため、REPLでは`String`を`put`しようとして、コンパイラにエラーとしてはじかれています。`Cell`は様々な型を与えてインスタンス化したいクラスであるため、クラス定義時には特定の型を与えることができません。そういった場合に、型パラメータは役に立ちます。

次に、もう少し実用的な例をみてみましょう。メソッドから複数の値を返したい、という要求はプログラミングを行う上でよく発生します。そのような場合、型パラメータが無い言語では、

* 片方を返り値として、もう片方を引数を経由して返す
* 複数の返り値専用のクラスを必要になる度に作る

という選択肢しかありませんでした。しかし、前者は引数を返り値に使うという点で邪道ですし、後者の方法は多数の引数を返したい、あるいは解く問題上で意味のある名前の付けられるクラスであれば良いですが、ただ2つの値を返したいといった場合には小回りが効かず不便です。こういう場合、型パラメータを2つ取る`Pair`クラスを作ってしまいます。```Pair```クラスの定義は次のようになります。`toString`メソッドの定義は後で表示のために使うだけなので気にしないでください。

```tut:silent
class Pair[A, B](val a: A, val b: B) {
  override def toString(): String = "(" + a + "," + b + ")"
}
```

このクラス```Pair```の利用法としては、たとえば割り算の商と余りの両方を返すメソッド`divide`が挙げられます。`divide`の定義は次のようになります。

```tut:silent
def divide(m: Int, n: Int): Pair[Int, Int] = new Pair[Int, Int](m / n, m % n)
```

これらをREPLにまとめて流し込むと次のようになります。

```tut
class Pair[A, B](val a: A, val b: B) {
  override def toString(): String = "(" + a + "," + b + ")"
}

def divide(m: Int, n: Int): Pair[Int, Int] = new Pair[Int, Int](m / n, m % n)

divide(7, 3)
```

7割る3の商と余りが`res0`に入っていることがわかります。なお、ここでは`new Pair[Int, Int](m / n, m % n)`としましたが、引数の型から型パラメータの型を推測できる場合、省略できます。この場合、`Pair`のコンストラクタに与える引数は`Int`と`Int`なので、`new Pair(m / n, m % n)`としても同じ意味になります。この`Pair`は2つの異なる型（同じ型でも良い）を返り値として返したい全ての場合に使うことができます。このように、どの型でも同じ処理を行う場合を抽象化できるのが型パラメータの利点です。

ちなみに、この`Pair`のようなクラスはScalaではよく使われるため、`Tuple1`から`Tuple22`(`Tuple`の後の数字は要素数）があらかじめ用意されています。また、インスタンス化する際も、

```tut
val m = 7
val n = 3
new Tuple2(m / n, m % n)
```

などとしなくても、

```tut
val m = 7
val n = 3
(m / n, m % n)
```

とすれば良いようになっています。

## 変位指定（variance）

この節では、型パラメータに関する性質である反変、共変について学びます。

## 共変（covariant）

Scalaでは、何も指定しなかった型パラメータは通常は非変（invariant）になります。非変というのは、型パラメータを持ったクラス`G`、型パラメータ`A`と`B`があったとき、`A` = `B`のときにのみ

```
val : G[A] = G[B]
```

というような代入が許されるという性質を表します。これは、違う型パラメータを与えたクラスは違う型になることを考えれば自然な性質です。ここであえて非変について言及したのは、Javaの組み込み配列クラスは標準で非変ではなく共変であるという設計ミスを犯しているからです。

ここでまだ共変について言及していなかったので、簡単に定義を示しましょう。共変というのは、型パラメータを持ったクラス`G`、型パラメータ`A`と`B`があったとき、`A` が `B` を継承しているときにのみ、

```
val : G[B] = G[A]
```

というような代入が許される性質を表します。Scalaでは、クラス定義時に

```tut:silent
class G[+A]
```
のように型パラメータの前に`+`を付けるとその型パラメータは（あるいはそのクラスは）共変になります。

このままだと定義が抽象的でわかりづらいかもしれないので、具体的な例として配列型を挙げて説明します。配列型はJavaでは共変なのに対してScalaでは非変であるという点において、面白い例です。まずはJavaの例です。`G` = 配列、 `A` = `String`, `B` = `Object`として読んでください。

```java
Object[] objects = new String[1];
objects[0] = 100;
```

このコード断片はJavaのコードとしてはコンパイルを通ります。ぱっと見でも、`Object`の配列を表す変数に`String`の配列を渡すことができるのは理にかなっているように思えます。しかし、このコードを実行すると例外 [`java.lang.ArrayStoreException`](https://docs.oracle.com/javase/jp/8/docs/api/java/lang/ArrayStoreException.html) が発生します。これは、`objects`に入っているのが実際には`String`の配列（`String`のみを要素として持つ）なのに、2行目で`int`型（ボクシング変換されて`Integer`型）の値である`100`を渡そうとしていることによります。

一方、Scalaでは同様のコードの一行目に相当するコードをコンパイルしようとした時点で、次のようなコンパイルエラーが出ます（`Any`は全ての型のスーパークラスで、`AnyRef`に加え、`AnyVal`（値型）の値も格納できます）。

```scala
scala> val arr: Array[Any] = new Array[String](1)
<console>:7: error: type mismatch;
 found   : Array[String]
 required: Array[Any]
```
このような結果になるのは、Scalaでは配列は非変だからです。静的型付き言語の型安全性とは、コンパイル時により多くのプログラミングエラーを捕捉するものであるとするなら、配列の設計はScalaの方がJavaより型安全であると言えます。

さて、Scalaでは型パラメータを共変にした時点で、安全ではない操作はコンパイラがエラーを出してくれるので安心ですが、共変をどのような場合に使えるかを知っておくのは意味があります。たとえば、先ほど作成したクラス`Pair[A, B]`について考えてみましょう。`Pair[A, B]`は一度インスタンス化したら、変更する操作ができませんから、`ArrayStoreException`のような例外は起こり得ません。実際、`Pair[A, B]`は安全に共変にできるクラスで、`class Pair[+A, +B]`のようにしても問題が起きません。

```tut
class Pair[+A, +B](val a: A, val b: B) {
  override def toString(): String = "(" + a + "," + b + ")"
}

val pair: Pair[AnyRef, AnyRef] = new Pair[String, String]("foo", "bar")
```

ここで、`Pair`は作成時に値を与えたら後は変更できず、したがって`ArrayStoreException`のような例外が発生する余地がないことがわかります。一般的には、一度作成したら変更できない（immutable）などの型パラメータは共変にしても多くの場合問題がありません。

### 演習問題

次の*immutable*な*Stack*型の定義（途中）があります。`???`の箇所を埋めて、*Stack*の定義を完成させなさい。なお、`E >: A`は、`E`は`A`の継承元である、という制約を表しています。

```tut:silent
trait Stack[+A] {
  def push[E >: A](e: E): Stack[E]
  def top: A
  def pop: Stack[A]
  def isEmpty: Boolean
}

class NonEmptyStack[+A](private val first: A, private val rest: Stack[A]) extends Stack[A] {
  def push[E >: A](e: E): Stack[E] = ???
  def top: A = ???
  def pop: Stack[A] = ???
  def isEmpty: Boolean = ???
}

case object EmptyStack extends Stack[Nothing] {
  def push[E >: Nothing](e: E): Stack[E] = new NonEmptyStack[E](e, this)
  def top: Nothing = throw new IllegalArgumentException("empty stack")
  def pop: Nothing = throw new IllegalArgumentException("empty stack")
  def isEmpty: Boolean = true
}

object Stack {
  def apply(): Stack[Nothing] = EmptyStack
}
```

また、`Nothing`は全ての型のサブクラスであるような型を表現します。`Stack[A]`は共変なので、`Stack[Nothing]`はどんな型の`Stack`変数にでも格納することができます。
例えば`Stack[Nothing]`型である`EmptyStack`は、`Stack[Int]`型の変数と`Stack[String]`型の変数の両方に代入することができます。

```tut
val intStack: Stack[Int] = Stack()
val stringStack: Stack[String] = Stack()
```

<!-- begin answer id="answer_ex1" style="display:none" -->

```tut:silent
class NonEmptyStack[+A](private val first: A, private val rest: Stack[A]) extends Stack[A] {
  def push[E >: A](e: E): Stack[E] = new NonEmptyStack[E](e, this)
  def top: A = first
  def pop: Stack[A] = rest
  def isEmpty: Boolean = false
}
```

<!-- end answer -->

## 反変（contravariant）

次は共変とちょうど対になる性質である反変です。簡単に定義を示しましょう。反変というのは、型パラメータを持ったクラス`G`、型パラメータ`A`と`B`があったとき、`A` が `B` を継承しているときにのみ、

```
val : G[A] = G[B]
```

というような代入が許される性質を表します。Scalaでは、クラス定義時に

```tut:silent
class G[-A]
```
のように型パラメータの前に`-`を付けるとその型パラメータは（あるいはそのクラスは）反変になります。

反変の例として最もわかりやすいものの1つが関数の型です。たとえば、型`A`と`B`があったとき、

```scala
val x1: A => AnyRef = B => AnyRef型の値
x1(T1型の値)
```

というプログラムの断片が成功するためには、`A`が`B`を継承する必要があります。その逆では駄目です。仮に、`A = String`, `B = AnyRef` として考えてみましょう。

```scala
val x1: String => AnyRef = AnyRef => AnyRef型の値
x1(String型の値)
```

ここで`x1`に実際に入っているのは`AnyRef => AnyRef`型の値であるため、
引数として`String`型の値を与えても、`AnyRef`型の引数に`String`型の値を与えるのと同様であり、問題なく成功します。`A`と`B`が逆で、`A = AnyRef`, `B = String`の場合、`String`型の引数に`AnyRef`型の値を与えるのと同様になってしまうので、これは`x1`へ値を代入する時点でコンパイルエラーになるべきであり、実際にコンパイルエラーになります。

実際にREPLで試してみましょう。

```scala
scala> val x1: AnyRef => AnyRef = (x: String) => (x:AnyRef)
<console>:7: error: type mismatch;
 found   : String => AnyRef
 required: AnyRef => AnyRef
       val x1: AnyRef => AnyRef = (x: String) => (x:AnyRef)
                                              ^

scala> val x1: String => AnyRef = (x: AnyRef) => x
x1: String => AnyRef = <function1>
```

このように、先ほど述べたような結果になっています。

## 型パラメータの境界（bounds）

型パラメータ`T`に対して何も指定しない場合、その型パラメータ`T`は、どんな型でも入り得ることしかわかりません。そのため、
何も指定しない型パラメータ`T`に対して呼び出せるメソッドは`Any`に対するもののみになります。しかし、たとえば、順序がある
要素からなるリストをソートしたい場合など、`T`に対して制約を書けると便利な場合があります。そのような場合に使えるのが、
型パラメータの境界（bounds）です。型パラメータの境界には2種類あります。

### 上限境界（upper bounds）

1つ目は、型パラメータがどのような型を継承しているかを指定する上限境界（upper bounds）です。上限境界では、型パラメータ
の後に、`<:`を記述し、それに続いて制約となる型を記述します。以下では、`show`によって文字列化できるクラス`Show`を定義した
うえで、`Show`であるような型のみを要素として持つ`ShowablePair`を定義しています。

```tut:silent
abstract class Show {
  def show: String
}
class ShowablePair[A <: Show, B <: Show](val a: A, val b: B) extends Show {
  override def show: String = "(" + a.show + "," + b.show + ")"
}
```

ここで、型パラメータ`A`、`B`ともに上限境界として`Show`が指定されているため、`a`と`b`に対して`show`を呼び出すことが
できます。なお、上限境界を明示的に指定しなかった場合、`Any`が指定されたものとみなされます。

### 下限境界（lower bounds）

2つ目は、型パラメータがどのような型のスーパータイプであるかを指定する下限境界（lower bounds）です。下限境界は、
共変パラメータと共に用いることが多い機能です。実際に例を見ます。

まず、共変の練習問題であったような、イミュータブルな`Stack`クラスを定義します。この`Stack`は共変にしたいとします。

```tut:fail:silent
abstract class Stack[+A]{
  def push(element: A): Stack[A]
  def top: A
  def pop: Stack[A]
  def isEmpty: Boolean
}
```

しかし、この定義は、以下のようなコンパイルエラーになります。

```
error: covariant type A occurs in contravariant position in type A of value element
         def push(element: A): Stack[A]
                           ^
```

このコンパイルエラーは、共変な型パラメータ`A`が反変な位置（反変な型パラメータが出現できる箇所）に出現したということを
言っています。一般に、引数の位置に共変型パラメータ`E`の値が来た場合、型安全性が壊れる可能性があるため、このようなエラーが
出ます。しかし、この`Stack`は配列と違ってイミュータブルであるため、本来ならば型安全性上の問題は起きません。この問題に
対処するために型パラメータの下限境界を使うことができます。型パラメータ`E`を`push`に追加し、その下限境界として、`Stack`
の型パラメータ`A`を指定します。

```tut:silent
abstract class Stack[+A]{
  def push[E >: A](element: E): Stack[E]
  def top: A
  def pop: Stack[A]
  def isEmpty: Boolean
}
```

このようにすることによって、コンパイラは、`Stack`には`A`の任意のスーパータイプの値が入れられる可能性があることがわかるように
なります。そして、型パラメータ`E`は共変ではないため、どこに出現しても構いません。このようにして、下限境界を利用して、型安全な
`Stack`と共変性を両立することができます。
