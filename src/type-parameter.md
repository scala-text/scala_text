# 型パラメータ（type parameter）（★★★）

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
class Cell[T](var value: T) {
  def put(newValue: T): Unit = {
    value = newValue
  }
  
  def get(): T = value
}
```

これをREPLで使ってみましょう。

```scala
scala> class Cell[T](var value: T) {
     |   def put(newValue: T): Unit = {
     |     value = newValue
     |   }
     |   
     |   def get(): T = value
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
class Pair[T1, T2](val t1: T1, val t2: T2) {
  override def toString(): String = "(" + t1 + "," + t2 + ")"
}
```

このクラス```Pair```の利用法としては、たとえば割り算の商と余りの両方を返すメソッド`divide`が挙げられます。`divide`の定義は次のようになります。

```tut:silent
def divide(m: Int, n: Int): Pair[Int, Int] = new Pair[Int, Int](m / n, m % n)
```

これらをREPLにまとめて流し込むと次のようになります。

```tut
class Pair[T1, T2](val t1: T1, val t2: T2) {
  override def toString(): String = "(" + t1 + "," + t2 + ")"
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

## 変位指定（variance）（★★）

この節では、型パラメータに関する性質である反変、共変について学びます。

## 共変（covariant）（★★）

Scalaでは、何も指定しなかった型パラメータは通常は非変（invariant）になります。非変というのは、型パラメータを持ったクラス`G`、型パラメータ`T1`と`T2`があったとき、`T1` = `T2`のときにのみ

```
val : G[T1] = G[T2]
```

というような代入が許されるという性質を表します。これは、違う型パラメータを与えたクラスは違う型になることを考えれば自然な性質です。ここであえて非変について言及したのは、Javaの組み込み配列クラスは標準で非変ではなく共変であるという設計ミスを犯しているからです。

ここでまだ共変について言及していなかったので、簡単に定義を示しましょう。共変というのは、型パラメータを持ったクラス`G`、型パラメータ`T1`と`T2`があったとき、`T1` が `T2` を継承しているときにのみ、

```
val : G[T2] = G[T1]
```

というような代入が許される性質を表します。Scalaでは、クラス定義時に

```tut:silent
class G[+T]
```
のように型パラメータの前に`+`を付けるとその型パラメータは（あるいはそのクラスは）共変になります。

このままだと定義が抽象的でわかりづらいかもしれないので、具体的な例として配列型を挙げて説明します。配列型はJavaでは共変なのに対してScalaでは非変であるという点において、面白い例です。まずはJavaの例です。`G` = 配列、 `T1` = `String`, `T2` = `Object`として読んでください。

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

さて、Scalaでは型パラメータを共変にした時点で、安全ではない操作はコンパイラがエラーを出してくれるので安心ですが、共変をどのような場合に使えるかを知っておくのは意味があります。たとえば、先ほど作成したクラス`Pair[T1, T2]`について考えてみましょう。`Pair[T1, T2]`は一度インスタンス化したら、変更する操作ができませんから、`ArrayStoreException`のような例外は起こり得ません。実際、`Pair[T1, T2]`は安全に共変にできるクラスで、`class Pair[+T1, +T2]`のようにしても問題が起きません。

```tut
class Pair[+T1, +T2](val t1: T1, val t2: T2) {
  override def toString(): String = "(" + t1 + "," + t2 + ")"
}

val pair: Pair[AnyRef, AnyRef] = new Pair[String, String]("foo", "bar")
```

ここで、`Pair`は作成時に値を与えたら後は変更できず、したがって`ArrayStoreException`のような例外が発生する余地がないことがわかります。一般的には、一度作成したら変更できない（immutable）などの型パラメータは共変にしても多くの場合問題がありません。

### 演習問題

次の*immutable*な*Stack*型の定義（途中）があります。`???`の箇所を埋めて、*Stack*の定義を完成させなさい。なお、`E >: T`は、`E`は`T`の継承元である、という制約を表しています。また、`Nothing`は全ての型のサブクラスであるような型を表現します。`Stack[T]`は共変なので、`Stack[Nothing]`はどんな型の`Stack`変数にでも格納することができます。

```tut:silent
trait Stack[+T] {
 def pop(): (T, Stack[T])
 def push[E >: T](e: E): Stack[E]
 def isEmpty(): Boolean
}

class NonEmptyStack[+T](private val top: T, private val rest: Stack[T]) extends Stack[T] {
  def push[E >: T](e: E): Stack[E] = ???
  def pop(): (T, Stack[T]) = ???
  def isEmpty(): Boolean = ???
}

case object EmptyStack extends Stack[Nothing] {
  def pop(): Nothing = throw new IllegalArgumentException("empty stack")
  def push[E >: Nothing](e: E): Stack[E] = new NonEmptyStack[E](e, this)
  def isEmpty(): Boolean = true
}

object Stack {
  def apply(): Stack[Nothing] = EmptyStack
}
```

## 反変（contravariant）（★）

次は共変とちょうど対になる性質である反変です。簡単に定義を示しましょう。反変というのは、型パラメータを持ったクラス`G`、型パラメータ`T1`と`T2`があったとき、`T1` が `T2` を継承しているときにのみ、

```
val : G[T1] = G[T2]
```

というような代入が許される性質を表します。Scalaでは、クラス定義時に

```tut:silent
class G[-T]
```
のように型パラメータの前に`-`を付けるとその型パラメータは（あるいはそのクラスは）反変になります。

反変の例として最もわかりやすいものの1つが関数の型です。たとえば、型`T1`と`T2`があったとき、

```scala
val x1: T1 => AnyRef = T2 => AnyRef型の値
x1(T1型の値)
```

というプログラムの断片が成功するためには、`T1`が`T2`を継承する必要があります。その逆では駄目です。仮に、`T1 = String`, `T2 = AnyRef` として考えてみましょう。

```scala
val x1: String => AnyRef = AnyRef => AnyRef型の値
x1(String型の値)
```

ここで`x1`に実際に入っているのは`AnyRef => AnyRef`型の値であるため、
引数として`String`型の値を与えても、`AnyRef`型の引数に`String`型の値を与えるのと同様であり、問題なく成功します。`T1`と`T2`が逆で、`T1 = AnyRef`, `T2 = String`の場合、`String`型の引数に`AnyRef`型の値を与えるのと同様になってしまうので、これは`x1`へ値を代入する時点でコンパイルエラーになるべきであり、実際にコンパイルエラーになります。

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
