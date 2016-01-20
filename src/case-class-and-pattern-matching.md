# ケースクラスとパターンマッチング（★★★）

パターンマッチングは、Scalaを初めとする関数型言語に一般的な機能です。CやJavaのswitch文に似ていますが、より
強力な機能です。しかし、パターンマッチングの真価を発揮するには、標準ライブラリまたはユーザが定義したケース
クラス（case class）によるデータ型の定義が必要になります。

簡単なケースクラスによるデータ型を定義してみます。

```tut:silent
sealed abstract class DayOfWeek
case object Sunday extends DayOfWeek
case object Monday extends DayOfWeek
case object Tuesday extends DayOfWeek
case object Wednesday extends DayOfWeek
case object Thursday extends DayOfWeek
case object Friday extends DayOfWeek
case object Saturday extends DayOfWeek
```

これは、一週間の曜日を表すデータ型です。CやJavaの`enum`に似ていますね。実際、同じように使うことができます。
たとえば、以下のように`DayOfWeek`型の変数に`Sunday`を代入することができます。

```tut:silent
val x: DayOfWeek = Sunday
```

`object`またはその他のデータ型は、パターンマッチングのパターンを使うことができます。この例では、この`DayOfWeek`型を継承した
各`object`をパターンマッチングのパターンを使うことができます。パターンマッチングの構文は再度書くと、

```scala
式 match {
  case pat1 =>
  case pat2 =>
  ...
}
```

のようになります。`DayOfWeek`の場合、次のようにして使うことができます。

```tut
x match {
  case Sunday => 1
  case Monday => 2
  case Tuesday => 3
  case Wednesday => 4
  case Thursday => 5
  case Friday => 6
}
```

これは、xが`Sunday`なら1を、`Monday`なら2を…返すパターンマッチです。ここで、パターンマッチで
漏れがあった場合、コンパイラが警告してくれます。この警告は、`sealed`修飾子をスーパークラス/トレイトに付ける
ことによって、その（直接の）サブクラス/トレイトは同じファイル内にしか定義できないという性質を利用して実現されています。
この用途以外で`sealed`はめったに使われないので、ケースクラスのスーパークラス/トレイトには`sealed`を付けるものだと
覚えておけば良いでしょう。

これだけだと、CやJavaの列挙型とあまり変わらないように見えますが、それらと異なるのは各々の
データは独立してパラメータを持つことができることです。また、パターンマッチの際はそのデータ型
の種類によって分岐するだけでなく、データを分解することができることが特徴です。

例として四則演算を表す構文木を考えてみます。各ノード`Exp`を継承し（つまり、全て
のノードは式である）、二項演算を表すノードはそれぞれの子として`lhs`（左辺）、`rhs`（右辺）を持つこと
とします。葉ノードとして整数リテラル（`Lit`）も入れます。これはIntの値を取るものとします。また、
二項演算の結果として小数が現れた場合は小数部を切り捨てることとします。これを表すデータ型
をScalaで定義すると次のようになります。

```tut:silent
sealed abstract class Exp
case class Add(lhs: Exp, rhs: Exp) extends Exp
case class Sub(lhs: Exp, rhs: Exp) extends Exp
case class Mul(lhs: Exp, rhs: Exp) extends Exp
case class Div(lhs: Exp, rhs: Exp) extends Exp
case class Lit(value: Int) extends Exp
```

全てのデータ型に`case`修飾子がついているので、これらのデータ型はパターンマッチングのパターンとして使うことができます。
この定義から、`1 + ((2 * 3) / 2)`という式を表すノードを構築します。

```tut
val example = Add(Lit(1), Div(Mul(Lit(2), Lit(3)), Lit(2)))
```

この`example`ノードを元に四則演算を定義する関数を定義してみます。関数の定義の
詳細は後ほど説明しますが、ここでは雰囲気だけをつかんでください。

```tut
def eval(exp: Exp): Int = exp match {
  case Add(l, r) => eval(l) + eval(r)
  case Sub(l, r) => eval(l) - eval(r)
  case Mul(l, r) => eval(l) * eval(r)
  case Div(l, r) => eval(l) / eval(r)
  case Lit(v) => v
}
```

この定義をREPLに読み込ませて、`eval(example)`として、

```tut
eval(example)
```

のように表示されれば成功です。きちんと、`1 + ((2 * 3) / 2)`という式の計算結果が
出ていますね。ここで注目すべきは、パターンマッチングによって、

1. ノードの種類と構造によって分岐する
2. ネストしたノードを分解する
3. ネストしたノードを分解した結果を変数に束縛する

という3つの動作が同時に行えていることです。 これがケースクラスを使ったデータ型と
パターンマッチングの組み合わせの強力さです。また、この`match`式の中で、たとえば
`case Lit(v) => v`の行を書き忘れた場合、`DayOfWeek`の例と同じように、

```scala
<console>:16: warning: match may not be exhaustive.
It would fail on the following input: Lit(_)
       def eval(exp: Exp): Int = exp match {
```

記述漏れがあることを指摘してくれますから、ミスを防ぐこともできます。

## 変数宣言におけるパターンマッチング

`match`式中のパターンマッチングのみを扱ってきましたが、実は変数宣言でもパターンマッチングを行うことができます。

たとえば、次のようなケースクラス`Point`があったとします。

```tut
case class Point(x: Int, y: Int)
```

このケースクラス`Point`に対して、

```tut
val Point(x, y) = Point(10, 20)
```

とすると、`x`に`10`が、`y`に`20`が束縛されます。もしパターンにマッチしなかった場合は、例外 `scala.MatchError` が発生してしまうので、変数宣言におけるパターンマッチングは、それが必ず成功すると型情報から確信できる場合にのみ使うようにしましょう。

## 練習問題

`DayOfWeek`型を使って、ある日の次の曜日を返すメソッド`nextDayOfWeek`

```tut:silent
def nextDayOfWeek(d: DayOfWeek): DayOfWeek = ???
```

をパターンマッチを用いて定義してみましょう。

## 練習問題

二分木（子の数が最大で2つであるような木構造）を表す型`Tree`と`Branch`, `Empty`を考えます：

```tut:silent
sealed abstract class Tree
case class Branch(value: Int, left: Tree, right: Tree) extends Tree
case object Empty extends Tree
```

子が2つで左の子の値が`2`、右の子の値が`3`、自分自身の値が`1`の木構造はたとえば次のようにして定義することができます。

```tut
val tree: Tree = Branch(1, Branch(2, Empty, Empty), Branch(3, Empty, Empty))
```

このような木構造に対して、

1. 最大値を求める`max`メソッド：
2. 最小値を求める`min`メソッド：
3. 深さを求める`depth`メソッド：

```tut:silent
def max(tree: Tree): Int = ???
def min(tree: Tree): Int = ???
def depth(tree: Tree): Int = ???
```

をそれぞれ定義してみましょう。なお、

```scala
depth(Empty) == 0
depth(Branch(10, Empty, Empty)) = 1
```

です。

余裕があれば木構造を、

```
左の子孫の全ての値 <= 自分自身の値 < 右の子孫の全部の値
```

となるような木構造に変換する`sort`メソッド：

```tut:silent
def sort(tree: Tree): Tree = ???
```

を定義してみましょう。なお、`sort`メソッドは、葉ノードでないノードの個数と値が同じであれば元の構造と同じでなくても良いものとします。
