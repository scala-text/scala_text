# コレクションライブラリ（immutableとmutable）

Scalaには配列（`Array`）やリスト（`List`）、連想配列（`Map`）、集合（`Set`）を扱うための豊富なライブラリがあります。これを使いこなすことで、Scalaでの
プログラミングは劇的に楽になります。注意しなければならないのは、Scalaでは一度作成したら変更できない（immutable）なコレクションと
変更できる通常のコレクション（mutable）があることです。皆さんはmutableなコレクションに馴染んでいるかと思いますが、Scalaで関数型プログラ
ミングを行うためには、immutableなコレクションを活用する必要があります。

immutableなコレクションを使うのにはいくつものメリットがあります

* 関数型プログラミングで多用する再帰との相性が良い
* 高階関数を用いて簡潔なプログラムを書くことができる
* 一度作ったコレクションが知らない箇所で変更されていない事を保証できる
* 並行に動作するプログラムの中で、安全に受け渡しすることができる

mutableなコレクションを効果的に使えばプログラムの実行速度を上げることができますが、mutableなコレクションをどのような場面
で使えばいいかは難しい問題です。

この節では、Scalaのコレクションライブラリに含まれる以下のものについての概要を説明します。

- `Array`(mutable)
- `List`(immutable)
- `Map`(immutable)・`Map`(mutable)
- `Set`(immutable)・ `Set`(mutable)

## [Array](https://github.com/scala/scala/blob/v2.13.1/src/library/scala/Array.scala)

まずは大抵のプログラミング言語にある配列です。

```tut
val arr = Array(1, 2, 3, 4, 5)
```

これで1から5までの要素を持った配列が`arr`に代入されました。Scalaの配列は、他の言語のそれと同じように要素の中身を入れ替えることができます。配列の添字は0から始まります。なお、配列の型を指定しなくて良いのは、`Array(1, 2, 3, 4, 5)`の部分で、要素型が`Int`であるに違いないと
コンパイラが型推論してくれるからです。型を省略せずに書くと

```tut
val arr = Array[Int](1, 2, 3, 4, 5)
```

となります。ここで、`[Int]`の部分は型パラメータと呼びます。`Array`だけだとどの型かわからないので、`[Int]`を付けることでどの型の`Array`かを指定しているわけです。この型パラメータは型推論を補うために、色々な箇所で出てくるので覚えておいてください。しかし、この場面では、`Array`の要素型は`Int`だとわかっているので、冗長です。次に要素へのアクセスと代入です。

```tut
arr(0) = 7

arr

arr(0)
```

他の言語だと```arr[0]```のようにしてアクセスすることが多いので最初は戸惑うかもしれませんが、慣れてください。配列の`0`番目の要素がちゃんと`7`に入れ替わっていますね。

配列の長さは`arr.length`で取得することができます。

```tut
arr.length
```

`Array[Int]`はJavaでは`int[]`と同じ意味です。Scalaでは、配列などのコレクションの要素型を表記するとき
`Collection[ElementType]`のように一律に表記し、配列も同じように記述するのです。Javaでは配列型だけ特別扱い
するのに比べると統一的だと言えるでしょう。

ただし、あくまでも表記上はある程度統一的に扱えますが、実装上はJVMの配列であり、 **要素が同じでもequalsの結果がtrueにならない**, **生成する際にClassTagというものが必要** などのいくつかの罠があるので、Arrayはパフォーマンス上必要になる場合以外はあまり積極的に使うものではありません。


### 練習問題

配列の`i`番目の要素と`j`番目の要素を入れ替える`swapArray`メソッドを定義してみましょう。`swapArray`メソッドの宣言は

```tut:silent
def swapArray[T](arr: Array[T])(i: Int, j: Int): Unit = ???
```

となります。`i`と`j`が配列の範囲外である場合は特に考慮しなくて良いです。

<!-- begin answer id="answer_ex1" style="display:none" -->

```tut:silent
def swapArray[T](arr: Array[T])(i: Int, j: Int): Unit = {
  val tmp = arr(i)
  arr(i) = arr(j)
  arr(j) = tmp
}
```

```tut
val arr = Array(1, 2, 3, 4, 5)

swapArray(arr)(0, 4)

arr

swapArray(arr)(1, 3)

arr
```

<!-- end answer -->

### [Range](https://github.com/scala/scala/blob/v2.13.1/src/library/scala/collection/immutable/Range.scala)

`Range`は範囲を表すオブジェクトです。`Range`は直接名前を指定して生成するより、`to`メソッドと`until`メソッドを用いて呼びだすことが多いです。また、`toList`メソッドを用いて、その範囲の数値の列を後述する`List`に変換することができます。では、早速REPLで`Range`を使ってみましょう。

```tut
1 to 5

(1 to 5).toList

1 until 5

(1 until 5).toList
```

`to`は右の被演算子を含む範囲を、`until`は右の被演算子を含まない範囲を表していることがわかります。また、`Range`は`toList`で後述する`List`に変換することができることもわかります。

### [List](https://github.com/scala/scala/blob/v2.13.1/src/library/scala/collection/immutable/List.scala)

さて、導入として大抵の言語にある`Array`を出しましたが、Scalaでは`Array`を使うことはそれほど多くありません。代わりに`List`や
`Vector`といったデータ構造をよく使います（`Vector`については後述します）。`List`の特徴は、一度作成したら中身を
変更できない（immutable）ということです。中身を変更できないデータ構造（永続データ構造とも呼びます）はScalaがサポートしている
関数型プログラミングにとって重要な要素です。それでは`List`を使ってみましょう。

```tut
val lst = List(1, 2, 3, 4, 5)
```

```tut:fail
lst(0) = 7
```

見ればわかるように、`List`は一度作成したら値を更新することができません。しかし、`List`は値を更新することができませんが、
ある`List`を元に新しい`List`を作ることができます。これが値を更新することの代わりになります。以降、`List`に対して組み込みで用意されている各種操作をみていくことで、`List`の値を更新することなく色々な操作ができることがわかるでしょう。

### Nil：空のList

まず最初に紹介するのは`Nil`です。Scalaで空の`List`を表すには`Nil`というものを使います。Rubyなどでは`nil`は言語上かなり特別な意味を持ちますが、Scalaではデフォルトでスコープに入っているということ以外は特別な意味はなく[単にobjectです](https://github.com/scala/scala/blob/v2.13.1/src/library/scala/collection/immutable/List.scala#L591)。Nilは単体では意味がありませんが、次に説明する`::`と合わせて用いることが多いです。

### [:: - Listの先頭に要素をくっつける](https://github.com/scala/scala/blob/v2.13.1/src/library/scala/collection/immutable/List.scala#L97)

`::`（コンスと読みます）は既にある`List`の先頭に要素をくっつけるメソッドです。これについては、REPLで結果をみた方が早いでしょう。

```tut
val a1 = 1 :: Nil
val a2 = 2 :: a1
val a3 = 3 :: a2
val a4 = 4 :: a3
val a5 = 5 :: a3
```

付け足したい要素を`::`を挟んで`List`の前に書くことで`List`の先頭に要素がくっついていることがわかります。ここで、`::`はやや特別な呼び出し方をするメソッドであることを説明しなければなりません。まず、Scalaでは1引数のメソッドは中置記法で書くことができます。それで、`1 :: Nil` のように書くことができるわけです。次に、メソッド名の最後が`:`で終わる場合、被演算子の前と後ろをひっくり返して右結合で呼び出します。たとえば、

```tut
1 :: 2 :: 3 :: 4 :: Nil
```

は、実際には、

```tut
Nil.::(4).::(3).::(2).::(1)
```

のように解釈されます。`List`の要素が演算子の前に来て、一見数値のメソッドのように見えるのに`List`のメソッドとして呼び出せるのはそのためです。

### ++：List同士の連結

`++`はList同士を連結するメソッドです。これもREPLで見た方が早いでしょう。

```tut
List(1, 2) ++ List(3, 4)

List(1) ++ List(3, 4, 5)

List(3, 4, 5) ++ List(1)
```

`++`は1引数のメソッドなので、中置記法で書いています。また、末尾が`:`で終わっていないので、たとえば、

```tut
List(1, 2) ++ List(3, 4)
```

は

```tut
List(1, 2).++(List(3, 4))
```

と同じ意味です。大きな`List`同士を連結する場合、計算量が大きくなるのでその点には注意した方が良いです。

### mkString：文字列のフォーマッティング

このメソッドはScalaで非常に*頻繁に使用されます。*皆さんも、Scalaを使って
いく上で使う機会が多いであろうメソッドです。このメソッドは引数によって多重定義されており、3バージョンあるので
それぞれを紹介します。

#### mkString

引数なしバージョンです。このメソッドは、単に`List`の各要素を左から順に繋げた文字列を返します。

```tut
List(1, 2, 3, 4, 5).mkString
```

注意しなければならないのは、引数なしメソッドの`mkString`は`()`を付けて呼びだすことが**できない**
という点です。たとえば、以下のコードは、若干分かりにくいエラーメッセージがでてコンパイルに失敗します。

```tut:fail
List(1, 2, 3, 4, 5).mkString()
```

Scalaの`0`引数メソッドは`()`なしと
`()`を使った定義の二通りあって、前者の形式で定義されたメソッドは`()`を付けずに呼び出さなければいけません。
逆に、`()`を使って定義されたメソッドは、`()`を付けても付けなくても良いことになっています。このScalaの
仕様は混乱しやすいので注意してください。


#### mkString(sep: String)

引数にセパレータ文字列`sep`を取り、`List`の各要素を`sep`で区切って左から順に繋げた文字列を返します。

```tut
List(1, 2, 3, 4, 5).mkString(",")
```

#### `mkString(start: String, sep: String, end: String)`

`mkString(sep)`とほとんど同じですが、`start`と`end`に囲まれた文字列を返すところが異なります。

```tut
List(1, 2, 3, 4, 5).mkString("[", ",", "]")
```

#### 練習問題

`mkString`を使って、最初の数`start`と最後の数`end`を受け取って、

```
start,(start+1),(start+2)...,end
```

となるような文字列を返すメソッド`joinByComma`を定義してみましょう（ヒント：`Range` にも`mkString`メソッドはあります）。

例

```scala
joinByComma(1,5)  // 1,2,3,4,5
```

```tut:silent
def joinByComma(start: Int, end: Int): String = {
  ???
}
```

<!-- begin answer id="answer_ex2" style="display:none" -->

```tut:silent
def joinByComma(start: Int, end: Int): String = {
  (start to end).mkString(",")
}
```

```tut
joinByComma(1, 10)
```

`(start to end)` で、 `start` から `end` までの列を作って、 `mkString(",")` を使って間に `,` を挟んでいます。

<!-- end answer -->

### foldLeft：左からの畳み込み

`foldLeft`メソッドは`List`にとって非常に基本的なメソッドです。他の様々なメソッドを`foldLeft`を使って実装することができます。`foldLeft`の宣言を[ScalaのAPIドキュメント](https://www.scala-lang.org/api/current/scala/collection/immutable/List.html)から引用すると、

```scala
def foldLeft[B](z: B)(f: (B, A) ⇒ B): B
```

となります。`z`が`foldLeft`の結果の初期値で、リストを左からたどりながら`f`を適用していきます。`foldLeft`について
はイメージが湧きにくいと思いますので、`List(1, 2, 3).foldLeft(0)((x, y) => x + y)`の結果を図示します。

```
       +
      / \
     +   3
    / \
   +   2
  / \
 0   1
```

この図で、

```
   +
  / \
 0   1
```

は`+`に0と1を与えて適用するということを意味します。リストの要素を左から順にfを使って「畳み込む」（fold
は英語で畳み込むという意味を持ちます）状態がイメージできるでしょうか。`foldLeft`は汎用性の高いメソッドで、
たとえば、`List`の要素の合計を求めたい場合は

```tut
List(1, 2, 3).foldLeft(0)((x, y) => x + y)
```

`List`の要素を全て掛けあわせた結果を求めたい場合は

```tut
List(1, 2, 3).foldLeft(1)((x, y) => x * y)
```

とすることで求める結果を得ることができます[^fold-sum-product]。その他にも様々な処理を`foldLeft`を用いて実装することができます。

さて、節の最後に、実用上の補足を少ししておきます。
少し恣意的ですが1つの例として、「リストのリスト」をリストに変換する（平らにする）処理というのを考えてみます。
`List(List(1), List(2 ,3))`を`List(1, 2, 3)`に変換するのが目標です。安直に書くとこうなるでしょうか：

```scala
scala> List(List(1), List(2, 3), List(4)).foldLeft(Nil)(_ ++ _) 
<console>:12: error: type mismatch;
 found   : List[Int]
 required: scala.collection.immutable.Nil.type
       List(List(1), List(2, 3), List(4)).foldLeft(Nil)(_ ++ _)
                                                          ^

```

しかしコンパイルが通りません。
エラーメッセージの意味としては、今回の`Nil`は`List[Int]`型と見なされてほしいわけですが、期待したように型推論できていないようです。
`Nil`に明示的に型注釈を付けることで、コンパイルできるようになります。

```tut
List(List(1), List(2, 3), List(4)).foldLeft(Nil: List[Int])(_ ++ _)
```

このように、`Nil`が混ざった処理はそのままだとうまくコンパイルが通ってくれないことがあります。
そういう場合は型注釈を試すとよい、と頭の片隅に入れておいてください。

#### 練習問題

`foldLeft`を用いて、`List`の要素を反転させる次のシグニチャを持ったメソッド`reverse`を実装してみましょう：

```tut:silent
def reverse[T](list: List[T]): List[T] = ???
```

<!-- begin answer id="answer_ex3" style="display:none" -->

```tut:silent
def reverse[T](list: List[T]): List[T] = list.foldLeft(Nil: List[T])((a, b) => b :: a)
```

```tut
reverse(List(1, 2, 3, 4, 5))
```

`foldLeft` の初期値に `Nil` を与えて、そこから後ろにたどる毎に、「前に」要素を追加していくことで、
逆順のリストを作ることができています。

```tut:invisible
import org.scalacheck._, Arbitrary.arbitrary

Testing.test(arbitrary[List[Int]]){ list =>
  reverse(list) == list.reverse
}
```

<!-- end answer -->

### foldRight：右からの畳み込み

`foldLeft`が`List`の左からの畳み込みだったのに対して、`foldRight`は右からの畳込みです。`foldRight`の宣言を
[ScalaのAPIドキュメントから](https://www.scala-lang.org/api/current/scala/collection/immutable/List.html)参照すると、

```scala
def foldRight[B](z: B)(op: (A, B) ⇒ B): B
```

となります。`foldRight`に与える関数である`op`の引数の順序が`foldLeft`の場合と逆になっている事に注意してください。
`foldRight`を`List(1, 2, 3).foldRight(0)((y, x) => y + x)`とした場合の様子を図示すると次のようになります。

```
   +
  / \
 1   +   
    / \
   2   +   
      / \
     3   0
```

ちょうど`foldLeft`と対称になっています。`foldRight`も非常に汎用性の高いメソッドで、多くの処理を`foldRight`を
用いて実装することができます。

#### 練習問題

`List`の全ての要素を足し合わせるメソッド`sum`を`foldRight`を用いて実装してみましょう。`sum`の宣言は次のようになります。
なお、`List`が空のときは0を返してみましょう。

```tut:silent
def sum(list: List[Int]): Int = ???
```

<!-- begin answer id="answer_ex4" style="display:none" -->

```tut:silent
def sum(list: List[Int]): Int = list.foldRight(0){(x, y) => x + y}
```

```tut
sum(List(1, 2, 3, 4, 5))
```

<!-- end answer -->

#### 練習問題

`List`の全ての要素を掛け合わせるメソッド`mul`を`foldRight`を用いて実装してみましょう。`mul`の宣言は次のようになります。
なお、`List`が空のときは1を返してみましょう。

```tut
def mul(list: List[Int]): Int = ???
```

<!-- begin answer id="answer_ex5" style="display:none" -->

```tut:silent
def mul(list: List[Int]): Int = list.foldRight(1){(x, y) => x * y}
```

```tut
mul(List(1, 2, 3, 4, 5))
```

<!-- end answer -->

#### 練習問題

`mkString`を実装してみましょう。`mkString`そのものを使ってはいけませんが、`foldLeft`や`foldRight`などの`List`に定義されている
他のメソッドは自由に使って構いません。[ListのAPIリファレンス](https://www.scala-lang.org/api/current/scala/collection/immutable/List.html)
を読めば必要なメソッドが載っています。実装する`mkString`の宣言は

```tut:silent
def mkString[T](list: List[T])(sep: String): String = ???
```

となります。残りの2つのバージョンの`mkString`は実装しなくても構いません。

<!-- begin answer id="answer_ex6" style="display:none" -->

```tut:silent
def mkString[T](list: List[T])(sep: String): String = list match {
  case Nil => ""
  case x::xs => xs.foldLeft(x.toString){(x, y) => x + sep + y}
}
```

<!-- end answer -->

### map：各要素を加工した新しい`List`を返す

`map`メソッドは、1引数の関数を引数に取り、各要素に関数を適用した結果できた要素からなる新たな`List`を返します。
ためしに`List(1, 2, 3, 4, 5)`の各要素を2倍してみましょう。

```tut
List(1, 2, 3, 4, 5).map(x => x * 2)
```
`x => x * 2`の部分は既に述べたように、無名関数を定義するための構文です。メソッドの引数に与える短い関数を定義するときは、
Scalaでは無名関数をよく使います。`List`の全ての要素に何らかの処理を行い、その結果を加工するという処理は頻出するため、`map`は
Scalaのコレクションのメソッドの中でも非常によく使われるものになっています。

#### 練習問題

次のシグニチャを持つ`map`メソッドを`foldLeft`と`reverse`を使って実装してみましょう：

```tut:silent
def map[T, U](list: List[T])(f: T => U): List[U] = ???
```

<!-- begin answer id="answer_ex7" style="display:none" -->

```tut:silent
def map[T, U](list: List[T])(f: T => U): List[U] = {
  list.foldLeft(Nil:List[U]){(x, y) => f(y) :: x}.reverse
}
```

<!-- end answer -->

### filter：条件に合った要素だけを抽出した新しい`List`を返す

`filter`メソッドは、`Boolean`型を返す1引数の関数を引数に取り、各要素に関数を適用し、`true`になった要素のみを抽出した
新たな`List`を返します。`List(1, 2, 3, 4, 5)`から奇数だけを抽出してみましょう。

```tut
List(1, 2, 3, 4, 5).filter(x => x % 2 == 1)
```

#### 練習問題

次のシグニチャを持つ`filter`メソッドを`foldLeft`と`reverse`を使って実装してみましょう：

```tut:silent
def filter[T](list: List[T])(f: T => Boolean): List[T] = ???
```

<!-- begin answer id="answer_ex8" style="display:none" -->

```tut:silent
def filter[T](list: List[T])(f: T => Boolean): List[T] = {
  list.foldLeft(Nil:List[T]){(x, y) => if(f(y)) y::x else x}.reverse
}
```

<!-- end answer -->

### find：条件に合った最初の要素を返す

`find`メソッドは、`Boolean`型を返す1引数の関数を引数に取り、各要素に前から順番に関数を適用し、最初にtrueになった要素を
`Some`でくるんだ値を`Option`型として返します。1つの要素もマッチしなかった場合`None`を`Option`型として返します。
`List(1, 2, 3, 4, 5)`から最初の奇数だけを抽出してみましょう

```tut
List(1, 2, 3, 4, 5).find(x => x % 2 == 1)
```

後で説明されることになりますが、`Option`型はScalaプログラミングの中で重要な要素であり頻出します。

### takeWhile：先頭から条件を満たしている間を抽出する

`takeWhile`メソッドは、`Boolean`型を返す1引数の関数を引数に取り、前から順番に関数を適用し、結果が`true`の間のみからなる`List`を返します。`List(1, 2, 3, 4, 5)`の5より前の4要素を抽出してみます。

```tut
List(1, 2, 3, 4, 5).takeWhile(x => x != 5)
```

### count：`List`の中で条件を満たしている要素の数を計算する

`count`メソッドは、`Boolean`型を返す1引数の関数を引数に取り、全ての要素に関数を適用して、`true`が返ってきた要素の数を計算します。例として`List(1, 2, 3, 4, 5)`の中から偶数の数（2になるはず）を計算してみます。

```tut
List(1, 2, 3, 4, 5).count(x => x % 2 == 0)
```

#### 練習問題

次のシグニチャを持つ`count`メソッドを`foldLeft`を使って実装してみましょう：

```tut:silent
def count[T](list: List[T])(f: T => Boolean): Int = ???
```

<!-- begin answer id="answer_ex9" style="display:none" -->

```tut:silent
def count[T](list: List[T])(f: T => Boolean): Int  = {
  list.foldLeft(0){(x, y) => if(f(y)) x + 1 else x}
}
```

<!-- end answer -->

### flatMap：`List`をたいらにする

`flatMap`は一見少し変わったメソッドですが、後々重要になってくるメソッドなので説明しておきます。flatMapの
宣言は[ScalaのAPIドキュメントから](https://www.scala-lang.org/api/current/scala/collection/immutable/List.html)参照すると、

```scala
final def flatMap[B](f: (A) ⇒ GenTraversableOnce[B]): List[B]
```

となります。ここで、`GenTraversableOnce[B]`という変わった型が出てきていますが、ここではあらゆるコレクション（要素の型はB型である）を入れることが
できる型程度に考えてください。さて、flatMapの引数fの型は`(A) => GenTraversableOnce[B]`です。`flatMap`はこれを使って、各
要素にfを適用して、結果の要素からなるコレクションを分解してListの要素にします。これについては、実際に見た方が早いでしょう。

```tut
List(List(1, 2, 3), List(4, 5)).flatMap{e => e.map{g => g + 1}}
```

ネストした`List`の各要素に`flatMap`の中で`map`を適用して、`List`の各要素に1を足したものをたいらにしています。これだけだとありがたみがわかりにくいですが、ちょっと形を変えてみると非常に面白い使い方ができます：

```tut
List(1, 2, 3).flatMap{e => List(4, 5).map(g => e * g)}
```

`List(1, 2, 3)`と`List(4, 5)`の2つの`List`についてループし、各々の要素を掛けあわせた要素からなる`List`を抽出しています。実は、
for-comprehension

```scala
for(x <- col1; y <- col2;) yield z
```

は

```scala
col1.flatMap{x => col2.map{y => z}}
```

のシンタックスシュガーだったのです。すなわち、ある自分で定義したデータ型に`flatMap`と`map`を（適切に）実装すれば
for構文の中で使うことができるのです。

#### Listの性能特性

`List`の性能特性として、`List`の先頭要素へのアクセスは高速にできる反面、要素へのランダムアクセスや末尾へのデータの追加は
`List`の長さに比例した時間がかかってしまうということが挙げられます。`List`は関数型プログラミング言語で最も基本的なデータ
構造で、どの関数型プログラミング言語でもたいていは`List`がありますが、その性能特性には十分注意して扱う必要があります。
特に他の言語のプログラマはうっかり`List`の末尾に要素を追加するような遅いプログラムを書いてしまうことがあるので注意する必要
があります。

```tut
List(1, 2, 3, 4)

5 :: List(1, 2, 3, 4) // Listの先頭のセルに新しいをくっつける

List(1, 2, 3, 4) :+ 5 // 注意！末尾への追加は、Listの要素数分かかる
```

### 紹介したメソッドについて

`mkString`をはじめとした`List`の色々なメソッドを紹介してきましたが、実はこれらの大半は`List`特有ではなく、既に紹介した`Range`や`Array`、これから紹介する他のコレクションでも同様に使うことができます。何故ならばこれらの操作の大半は特定のコレクションではなく、コレクションのスーパータイプである共通のトレイト中に宣言されているからです。もちろん、`List`に要素を加える処理と`Set`に要素を加える処理（`Set`に既にある要素は加えない）のように、中で行われる処理が異なることがあるので、その点は注意する必要があります。詳しくは[ScalaのAPIドキュメント](https://www.scala-lang.org/api/current/index.html)を探索してみましょう。

### [Vector](https://github.com/scala/scala/blob/v2.13.1/src/library/scala/collection/immutable/Vector.scala)

`Vector`は少々変わったデータ構造です。`Vector`は一度データ構造を構築したら変更できないimmutableなデータ構造
です。要素へのランダムアクセスや長さの取得、データの挿入や削除、いずれの操作も十分に高速にできる比較的
万能なデータ構造です。immutableなデータ構造を使う場合は、まず`Vector`を検討すると良いでしょう。

```tut
Vector(1, 2, 3, 4, 5) //どの操作も「ほぼ」一定の時間で終わる

6 +: Vector(1, 2, 3, 4, 5)

Vector(1, 2, 3, 4, 5) :+ 6

Vector(1, 2, 3, 4, 5).updated(2, 5)
```

## Map

`Map`はキーから値へのマッピングを提供するデータ構造です。他の言語では辞書や連想配列と呼ばれたりします。
Scalaでは`Map`として一度作成したら変更できないimmutableな`Map`と変更可能なmutableな`Map`の2種類を提供しています。

### [`scala.collection.immutable.Map`](https://github.com/scala/scala/blob/v2.13.1/src/library/scala/collection/immutable/Map.scala)

Scalaで何も設定せずにただ`Map`と書いた場合、`scala.collection.immutable.Map`が使われます。その名の通り、一度
作成すると変更することはできません。内部の実装としては主に[`scala.collection.immutable.HashMap`](https://github.com/scala/scala/blob/v2.13.1/src/library/scala/collection/immutable/HashMap.scala)と
[`scala.collection.immutable.TreeMap`](https://github.com/scala/scala/blob/v2.13.1/src/library/scala/collection/immutable/TreeMap.scala)の2種類がありますが、通常は`HashMap`が使われます。

```tut
val m = Map("A" -> 1, "B" -> 2, "C" -> 3)

m.updated("B", 4) //一見元のMapを変更したように見えても

m // 元のMapはそのまま
```

### `scala.collection.mutable.Map`

Scalaの変更可能な`Map`は`scala.collection.mutable.Map`にあります。実装としては、`scala.collection.mutable.HashMap`、
`scala.collection.mutable.LinkedHashMap`、リストをベースにした`scala.collection.mutable.ListMap`がありますが、通常は
`HashMap`が使われます。

```tut
import scala.collection.mutable

val m = mutable.Map("A" -> 1, "B" -> 2, "C" -> 3)

m("B") = 5 // B -> 5 のマッピングに置き換える

m // 変更が反映されている
```

## Set

`Set`は値の集合を提供するデータ構造です。`Set`の中では同じ値が2つ以上存在しません。たとえば、`Int`の`Set`の中には1が2つ以上含まれていてはいけません。REPLで`Set`を作成するための式を入力すると、

```tut
Set(1, 1, 2, 3, 4)
```

重複した1が削除されて、1が1つだけになっていることがわかります。


### `scala.collection.immutable.Set`

Scalaで何も設定せずにただ`Set`と書いた場合、`scala.collection.immutable.Set`が使われます。immutableな`Map`の場合と
同じく、一度作成すると変更することはできません。内部の実装としては、主に [`scala.collection.immutable.HashSet`](https://github.com/scala/scala/blob/v2.13.1/src/library/scala/collection/immutable/HashSet.scala) と
[`scala.collection.immutable.TreeSet`](https://github.com/scala/scala/blob/v2.13.1/src/library/scala/collection/immutable/TreeSet.scala) の2種類がありますが、通常は`HashSet`が使われます。

```tut
val s = Set(1, 2, 3, 4, 5)

s - 5 // 5を削除した後も

s // 元のSetはそのまま
```

### `scala.collection.mutable.Set`

Scalaの変更可能な`Set`は`scala.collection.mutable.Set`にあります。主な実装としては、[`scala.collection.mutable.HashSet`](https://github.com/scala/scala/blob/v2.13.1/src/library/scala/collection/mutable/HashSet.scala) 、
[`scala.collection.mutable.TreeSet`](https://github.com/scala/scala/blob/v2.13.1/src/library/scala/collection/mutable/TreeSet.scala)がありますが、通常は`HashSet`が使われます。

```tut
import scala.collection.mutable

val s = mutable.Set(1, 2, 3, 4, 5)

s -= 5 // 5 を削除したら

s // 変更が反映される
```

### その他資料

さらにコレクションライブラリについて詳しく知りたい場合は、以下の公式のドキュメントなどを読みましょう
<https://docs.scala-lang.org/ja/overviews/collections/introduction.html>

[^fold-sum-product]: ただし、これはあくまでもfoldLeftの例であって、要素の和や積を求めたい場合に限って言えばもっと便利なメソッドが標準ライブラリに存在するので、実際にはこの例のような使い方はしません
