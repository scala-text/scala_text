# Scalaの基本

この節ではScalaの基本について、REPLを使った対話形式から始めて順を追って説明していきます。
ユーザはMac OS環境であることを前提に説明していきますが、Mac OS依存の部分はそれほど多くないのでWindows環境でもほとんどの場合同様に動作します。

## Scalaのインストール

これまででsbtをインストールしたはずですので、特に必要ありません。sbtが適当なバージョンのScalaをダウンロードしてくれます。

## REPLでScalaを対話的に試してみよう

Scalaプログラムは明示的にコンパイルしてから実行することが多いですが、REPL（Read Eval Print Loop）によって
対話的にScalaプログラムを実行することもできます。ここでは、まずREPLでScalaに触れてみることにしましょう。

先ほどのように、対話シェル（Windowsの場合はコマンドプロンプト）から

```
$ sbt console
```
とコマンドを打ってみましょう。

```
Welcome to Scala version 2.13.16 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_45).
Type in expressions to have them evaluated.
Type :help for more information.

scala> 
```

のように表示されるはずです。これでScalaの世界に入る準備ができました。
なお、`$`の部分はシェルプロンプトなので入力しなくて良いです[^Windows]。

## Hello, World!

まずは定番のHello, World!を表示させてみましょう。

```scala mdoc:nest
println("Hello, World!")
```

無事、Hello, World!が表示されるはずです。次にちょっと入力を変えて、`println()`をはずしてみましょう。
さて、どうなるでしょうか。

```scala mdoc:nest
"Hello, World!"
```

`res1: String = Hello, World` と、先ほどとは違う表示がされましたね。これは、`"Hello, World"`という式の
型が`String`であり、その値が`"Hello, World"`であることを示しています。これまで説明していませんでしたが、Scalaは
静的な型を持つ言語で、実行される前に型が合っていることがチェックされます。

### 練習問題

様々なリテラルをREPLで出力してみましょう。

- `0xff`
- `1e308`
- `9223372036854775807L`
- `9223372036854775808L`
- `9223372036854775807`
- `922337203685477580.7`
- `1.00000000000000000001 == 1`
- `"\u3042"`
- `"\ud842\udf9f"`

どのように表示されるでしょうか。

## 簡単な計算

次に簡単な足し算を実行してみましょう。

```
scala> 1 + 2
res1: Int = 3
```

3が表示され、その型が`Int`である事がわかりますね。なお、REPLの表示に出てくる`resN`というのは、REPLに何かの式（値を
返すもの）を入力したときに、その式の値にREPLが勝手に名前をつけたものです。この名前は次のように、その後も使うこと
ができます。

```
scala> res1
res2: Int = 3
```

この機能はREPLで少し長いプログラムを入力するときに便利ですので、活用していきましょう。Int型には他にも`+`,`-`,`*`,`/`といった
演算子が用意されており、次のようにして使うことができます。

```scala mdoc:nest
1 + 2

2 * 2

4 / 2

4 % 3
```

浮動小数点数の演算のためにも、ほぼ同じ演算子が用意されています。ただし、浮動小数点数には誤差があるためその点には注意が必要です。
見ればわかるように、`Double`という`Int`と異なる型が用意されています。`dbl.asInstanceOf[Int]`のようにキャストして型を変換することが
できますが、その場合、浮動小数点数の小数の部分が切り捨てられることに注意してください。

```scala mdoc:nest
1.0 + 2.0

2.2 * 2

4.5 / 2

4 % 3

4.0 % 2.0

4.0 % 3.0

(4.5).asInstanceOf[Int]

(4.0).asInstanceOf[Int]
```

### 練習問題

これまで出てきた、`+`,`-`,`*`,`/`,`%`の演算子を使って好きなように数式を打ち込んでみましょう。

- `2147483647 + 1`
- `9223372036854775807L + 1`
- `1e308 + 1`
- `1 + 0.0000000000000000001`
- `1 - 1`
- `1 - 0.1`
- `0.1 - 1`
- `0.1 - 0.1`
- `0.0000000000000000001 - 1`
- `0.1 * 0.1`
- `20 * 0.1`
- `1 / 3`
- `1.0 / 3`
- `1 / 3.0`
- `3.0 / 3.0`
- `1.0 / 10 * 1 / 10`
- `1 / 10 * 1 / 10.0`

どのような値になるでしょうか。また、その型は何になるでしょうか。

## 変数の基本

ここまでは変数を使わずにREPLに式をそのまま打ち込んでいましたが、長い式を入力するときにそれでは不便ですね。Scalaにも他の言語同様に変数があり、計算結果を格納することができます。注意しなければいけないのはScalaの変数には`val`と`var`の2種類
があり、前者は一度変数に値を格納したら変更不可能で、後者は通常の変数のように変更可能だということです。

それでは、変数を使ってみることにします。Scalaでは基本的に、`var`はあまり使わず`val`のみでプログラミングします。これは**Scalaでプログラミングをする上で大変重要なことなので**忘れない
ようにしてください。

```scala mdoc:nest
val x = 3 * 2
```

これは、変数xを定義し、それに`3 * 2`の計算結果を格納しています。特筆すべきは、`x`の型を宣言していないということです。 `3 * 2`の結果が`Int`なので、そこから`x`の型も`Int`に違いないとコンパイラが推論できるため、
変数の型を宣言する必要がないのです。Scalaのこの機能を（ローカル）型推論と呼びます。定義した変数の型は`Int`と
推論されたので、その後、`x`を別の型、たとえば`String`型として扱おうとしても、コンパイル時のエラーになります。
変数の型を宣言していなくてもちゃんと型が決まっているということです。

`val`を使った変数には値を再代入できず、型推論の効果がわかりにくいので、`var`を使った変数で実験してみます。
`var`を使った変数宣言の方法は基本的に`val`と同じです。

```
scala> var x = 3 * 3
x: Int = 9

scala> x = "Hello, World!"
<console>:8: error: type mismatch;
 found   : String("Hello, World!")
 required: Int
       x = "Hello, World!"
           ^

scala> x = 3 * 4
x: Int = 12
```

ポイントは、

* `var`を使って宣言した場合も型推論がはたらく（`3 * 3` → `Int`）
* `var`を使った場合、変数の値を変更することができる（`9` → `12`）

というところです。

ここまでは変数の型を宣言せずに型推論にまかせて来ましたが、明示的に変数の型を宣言することもできます。変数の型を宣言するには

```
（'val'|'var'）<変数名> : <型名> = <式>
```

のようにします。

```scala mdoc:nest
val x: Int = 3 * 3
```

ちゃんと変数の型を宣言できていますね。

### 練習問題

これまで出てきた変数と演算子を用いて、より複雑な数式を入力してみましょう。

* Q. ¥3,950,000を年利率2.3％の単利で8か月間借り入れた場合の利息はいくらか（円未満切り捨て）

<!-- begin answer id="answer_ex1" style="display:none" -->

A. ¥60,566

<!-- end answer -->

*  Q. 定価¥1,980,000の商品を値引きして販売したところ、原価1.6％にあたる¥26,400の損失となった。割引額は定価の何パーセントであったか

<!-- begin answer id="answer_ex2" style="display:none" -->

A. 18％

<!-- end answer -->

以上2つを、実際に変数を使ってScalaのコードで解いてみましょう。

[^Windows]: Windowsの場合は、`$`を`>`に読み替えてください。

