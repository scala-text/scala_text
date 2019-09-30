# Javaとの相互運用

## ScalaとJava

ScalaはJVM(Java Virtual Machine)の上で動作するため、JavaのライブラリのほとんどをそのままScalaから呼びだすことが
できます。また、現状では、Scalaの標準ライブラリだけでは、どうしても必要な機能が足りず、Javaの機能を利用せざるを
得ないことがあります。ただし、Javaの機能と言っても、Scalaのそれとほとんど同じように利用することができます。

### import
Javaのライブラリをimportするためには、Scalaでほとんど同様のことを記述すればOKです。

```java
import java.util.*;
import java.util.ArrayList;
```
は 

```tut:silent
import java.util._
import java.util.ArrayList
```

と同じ意味になります。注意するべきは、Javaでのワイルドカードインポートが、`*`ではなく`_`になった程度です。

### インスタンスの生成

インスタンスの生成もJavaと同様にできます。Javaでの

```java
ArrayList<String> list = new ArrayList<>();
```

というコードはScalaでは

```tut
val list = new ArrayList[String]()
```

と記述することができます。

#### 練習問題

`java.util.HashSet`クラスのインスタンスを`new`を使って生成してみましょう。

<!-- begin answer id="answer_ex1" style="display:none" -->

```tut
import java.util.HashSet
val set = new HashSet[String]
```

<!-- end answer -->

### インスタンスメソッドの呼び出し

インスタンスメソッドの呼び出しも同様です。

```java
list.add("Hello");
list.add("World");
```

は

```tut
list.add("Hello")
list.add("World")
```

と同じです。

#### 練習問題

```java.lang.System``` クラスのフィールド `out` のインスタンスメソッド `println` を引数 `"Hello, World!"` として呼びだしてみましょう。

<!-- begin answer id="answer_ex2" style="display:none" -->

```tut
System.out.println("Hello, World!")
```

<!-- end answer -->

#### staticメソッドの呼び出し

staticメソッドの呼び出しもJavaの場合とほとんど同様にできますが、1つ注意点があります。それは、Scalaではstaticメソッドは継承されない
（というよりstaticメソッドという概念がない）ということです。これは、クラスAがstaticメソッドfooを持っていたとして、Aを継承したBに
対してB.foo()とすることはできず、A.foo()としなければならないという事を意味します。それ以外の点についてはJavaの場合とほぼ同じです。

現在時刻をミリ秒単位で取得する[`System.currentTimeMillis()`](https://docs.oracle.com/javase/jp/8/docs/api/java/lang/System.html#currentTimeMillis--)をScalaから呼び出してみましょう。

```
scala> System.currentTimeMillis()
res0: Long = 1416357548906
```

表示される値はみなさんのマシンにおける時刻に合わせて変わりますが、問題なく呼び出せているはずです。

##### 練習問題

`java.lang.System`クラスのstaticメソッド`exit()`を引数 `0`  として呼びだしてみましょう。どのような結果になるでしょうか。

<!-- begin answer id="answer_ex3" style="display:none" -->

```scala
System.exit(0)
```

実行中のScalaプログラム（プロセス）が終了する。

<!-- end answer -->

#### staticフィールドの参照

staticフィールドの参照もJavaの場合と基本的に同じですが、staticメソッドの場合と同じ注意点が当てはまります。つまり、staticフィールドは
継承されない、ということです。たとえば、Javaでは [`JFrame.EXIT_ON_CLOSE`](https://docs.oracle.com/javase/jp/8/docs/api/javax/swing/JFrame.html#EXIT_ON_CLOSE) が継承されることを利用して、

```java
import javax.swing.JFrame;

public class MyFrame extends JFrame {
  public MyFrame() {
    setDefaultCloseOperation(EXIT_ON_CLOSE); //JFrameを継承しているので、EXIT_ON_CLOSEだけでOK
  }
}
```

のようなコードを書くことができますが、Scalaでは同じように書くことができず、

```scala
scala> import javax.swing.JFrame

class MyFrame extends JFrame {
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) //JFrame.を明示しなければならない
}
```

のように書く必要があります。

現実のプログラミングでは、Scalaの標準ライブラリだけでは必要なライブラリが不足している場面に多々遭遇しますが、そういう場合は既にある
サードパーティのScalaライブラリかJavaライブラリを直接呼びだすのが基本になります。

##### 練習問題

ScalaでJavaのstaticフィールドを参照しなければならない局面を1つ以上挙げてみましょう。

<!-- begin answer id="answer_ex4" style="display:none" -->

`java.lang.System`クラスのstaticフィールド`err`を参照する場合

<!-- end answer -->

#### Scalaの型とJavaの型のマッピング

Javaの型は適切にScalaにマッピングされます。たとえば、`System.currentTimeMillis()`が返す型はlong型ですが、Scalaの標準の型
である`scala.Long`にマッピングされます。Scalaの型とJavaの型のマッピングは次のようになります。

<table class="table table-bordered">
<caption>Javaのプリミティブ型とScalaの型のマッピング</caption>
<thead>
  <tr>
    <td>Javaの型</td> <td>Scalaの型</td>
  </tr>
</thead>
<tbody>
  <tr>
    <td>void (厳密にはJavaでvoidは<strong>型ではなく</strong>ただのキーワードとして扱われていますが、ここでは便宜上型としています)</td> <td>scala.Unit</td>
  </tr>
  <tr>
    <td>boolean </td> <td>scala.Boolean</td>
  </tr>
  <tr>
    <td>byte </td> <td>scala.Byte</td>
  </tr>
  <tr>
    <td>short</td> <td>scala.Short</td>
  </tr>
  <tr>
    <td>int</td> <td>scala.Int</td>
  </tr>
  <tr>
    <td>long</td> <td>scala.Long</td>
  </tr>
  <tr>
    <td>char</td> <td>scala.Char</td>
  </tr>
  <tr>
    <td>float</td> <td>scala.Float</td>
  </tr>
  <tr>
    <td>double</td> <td>scala.Double</td>
  </tr>
  <tr>
    <td>java.lang.Object（プリミティブ型ではありませんが特別な型なので載せました）</td> <td>scala.AnyRef</td>
  </tr>
  <tr>
    <td>java.lang.String</td> <td>java.lang.String</td>
  </tr>
</tbody>
</table>

Javaのすべてのプリミティブ型に対応するScalaの型が用意されていることがわかりますね！　また、`java.lang`パッケージにあるクラスは全てScalaからimport無しに使えます。

また、参照型についてもJava同様にクラス階層の中に組み込まれています。たとえば、Javaで
言う`int[]`は`Array[Int]`と書きますが、これは`AnyRef`のサブクラスです。ということは、Scala
で`AnyRef`と書くことで`Array[Int]`を`AnyRef`型の変数に代入可能です。ユーザが定義した
クラスも同様で、基本的に`AnyRef`を継承していることになっています。
（ただし、value classというものがあり、それを使った場合は少し事情が異なりますがここでは詳細には触れません）

#### nullとOption

Scalaの世界ではnullを使うことはなく、代わりにOption型を使います。一方で、Javaのメソッドを呼び出したりすると、
返り値としてnullが返ってくることがあります。Scalaの世界ではできるだけnullを取り扱いたくないのでこれは少し困ったこと
です。幸いにも、Scalaでは`Option(value)`とすることで、`value`がnullのときは`None`が、nullでないときは`Some(value)`
を返すようにできます。

java.util.Mapを使って確かめてみましょう。

```tut
val map = new java.util.HashMap[String, Int]()

map.put("A", 1)

map.put("B", 2)

map.put("C", 3)

Option(map.get("A"))

Option(map.get("B"))

Option(map.get("C"))

Option(map.get("D"))
```

ちゃんとnullがOptionにラップされていることがわかります。Scalaの世界からJavaのメソッドを呼びだすときは、返り値をできるだけ
Option()でくるむように意識しましょう。

#### JavaConverters 

JavaのコレクションとScalaのコレクションはインタフェースに互換性がありません。これでは、ScalaのコレクションをJavaのコレクション
に渡したり、逆に返ってきたJavaのコレクションをScalaのコレクションに変換したい場合に不便です。そのような場合に便利なのがJavaConverters
です。使い方はいたって簡単で、

```tut:silent
import scala.collection.JavaConverters._
```

とするだけです。これで、JavaとScalaのコレクションのそれぞれにasJava()やasScala()といったメソッドが追加されるのでそのメソッドを以下のように
呼び出せば良いです。

```tut
import scala.collection.JavaConverters._
import java.util.ArrayList

val list = new ArrayList[String]()

list.add("A")

list.add("B")

val scalaList = list.asScala
```

BufferはScalaの変更可能なリストのスーパークラスですが、ともあれ、asScalaメソッドによってJavaのコレクションをScalaのそれに変換することができている
ことがわかります。そのほかのコレクションについても同様に変換できますが、詳しくは[APIドキュメント](https://www.scala-lang.org/api/current/scala/collection/JavaConverters$.html)を参照してください。

##### 練習問題

[`scala.collection.mutable.ArrayBuffer`](https://www.scala-lang.org/api/current/scala/collection/mutable/ArrayBuffer.html)型の値を生成してから、JavaConvertersを使って[java.util.List](https://docs.oracle.com/javase/jp/8/docs/api/java/util/List.html)型に変換してみましょう。なお、`ArrayBuffer`には1つ以上の要素を入れておくこととします。

<!-- begin answer id="answer_ex5" style="display:none" -->

```tut
import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConverters._
val buffer = new ArrayBuffer[String]
buffer += "A"
buffer += "B"
buffer += "C"
val list = buffer.asJava
```

<!-- end answer -->

#### ワイルドカードと存在型

Javaでは、

```java
import java.util.List;
import java.util.ArrayList;
List<? extends Object> objects = new ArrayList<String>();
```

のようにして、クラス宣言時には不変であった型パラメータを共変にしたり、


```java
import java.util.Comparator;
Comparator<? super String> cmp = new Comparator<Object>() {
  public int compare(Object o1, Object o2) {
    return o1.hashCode() - o2.hashCode();
  }
};
```

のようにして反変にすることができます。ここで、`? extends Object` の部分を共変ワイルドカード、
`? super String`の部分を反変ワイルドカードと呼びます。より一般的には、このような機能を、利用側で
変位指定するという意味でuse-site varianceと呼びます。

この機能に対応するものとして、Scalaには存在型があります。上記のJavaコードは、Scalaでは次のコードで表現することができます。

```tut
import java.util.{List => JList, ArrayList => JArrayList}

val objects: JList[_ <: Object] = new JArrayList[String]()
```

```tut
import java.util.{Comparator => JComparator}

val cmp: JComparator[_ >: String] = new JComparator[Any] {
  override def compare(o1: Any, o2: Any): Int = {
    o1.hashCode() - o2.hashCode()
  }
}
```

より一般的には、`G<? extends T>` は `G[_ <: T]`に、`G<? super T>`は `G[_ >: T]` に置き換えることができます。Scalaのプログラム
開発において、Javaのワイルドカードを含んだ型を扱いたい場合は、この機能を使いましょう。一方で、Scalaプログラムでは定義側の変位指定、
つまりdeclaration-site varianceを使うべきであって、Javaと関係ない部分においてこの機能を使うのはプログラムをわかりにくくするため、
避けるべきです。

#### SAM変換

Scala 2.12でSAM(Single Abstract Method)変換が導入され[^sam]、Java 8のラムダ式を想定したライブラリを簡単に利用できるようになりました。
Java 8におけるラムダ式とは、関数型インタフェースと呼ばれる、メソッドが1つしかないようなインタフェースに対して無名クラスを簡単に記述できる構文です[^lam]。
例えば、10の階乗を例にすると以下のように簡潔に書くことができます。

```java
import java.util.stream.IntStream;
int factorial10 = IntStream.rangeClosed(1, 10).reduce(1, (i1, i2) -> i1 * i2);
```

ちなみに、これをラムダ式を使わずに書くと、以下のようにとても大変です。

```java
import java.util.stream.IntStream;
import java.util.function.IntBinaryOperator;
int factorial10 = IntStream.rangeClosed(1, 10).reduce(1,
  new IntBinaryOperator() {
    @Override public int applyAsInt(int left, int right) {
      return left * right;
    }
  });
```

関数の章で説明したように、元々Scalaにもラムダ式に相当する無名関数という構文があります。
しかし、以前のScalaでは`FunctionN`型が期待される箇所に限定されており、Javaにおいてラムダ式が期待される箇所の大半において使用することができませんでした。
例えば、10の階乗の例は`IntBinaryOperator`型が期待されているので以下のように無名クラスを使う必要がありました。

```tut
import java.util.stream.IntStream;
import java.util.function.IntBinaryOperator;
val factorial10 = IntStream.rangeClosed(1, 10).reduce(1,
  new IntBinaryOperator {
    def applyAsInt(left: Int, right: Int) = left * right;
  });
```

SAM変換を利用すると以下のようにここにも無名関数を利用できるようになります。

```tut
import java.util.stream.IntStream;
val factorial10 = IntStream.rangeClosed(1, 10).reduce(1, _ * _);
```

[^sam]: 正確には`-Xexperimetal`オプションにより、Scala 2.11でもSAM変換を有効にすることができます。
[^lam]: 厳密に言うと、無名クラスを用いたコードとラムダ式もしくは無名関数を用いたコードの間には、JavaとScalaいずれにおいても細かな違いが存在します。
例えば、スコープや出力されるバイトコードなどです。より詳しくは言語仕様などを当たってみてください。
