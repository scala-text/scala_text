# ケースクラスとパターンマッチング

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

<!-- begin answer id="answer_ex1" style="display:none" -->

```tut:silent
def nextDayOfWeek(d: DayOfWeek): DayOfWeek = d match {
  case Sunday => Monday
  case Monday => Tuesday
  case Tuesday => Wednesday
  case Wednesday => Thursday
  case Thursday => Friday
  case Friday => Saturday
  case Saturday => Sunday
}
```

```tut
nextDayOfWeek(Sunday)
nextDayOfWeek(Monday)
nextDayOfWeek(Saturday)
```

<!-- end answer -->

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
depth(Branch(10, Empty, Empty)) == 1
depth(Branch(10, Branch(20,
                    Empty,
                    Empty
                 ), Empty)) == 2
// 右のBranchの方が、左のBranchよりも深い
depth(Branch(10, Branch(20,
                    Empty,
                    Empty
                 ), Branch(30,
                    Branch(40,
                        Empty,
                        Empty
                    ),
                 Empty))) == 3
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

<!-- begin answer id="answer_ex2" style="display:none" -->

```tut:silent
object BinaryTree {
  sealed abstract class Tree
  case class Branch(value: Int, left: Tree, right: Tree) extends Tree
  case object Empty extends Tree

  def max(t: Tree): Int = t match {
    case Branch(v, Empty, Empty) =>
      v
    case Branch(v, Empty, r) =>
      val x = max(r)
      if(v > x) v else x
    case Branch(v, l, Empty) =>
      val x = max(l)
      if(v > x) v else x
    case Branch(v, l, r) =>
      val x = max(l)
      val y = max(r)
      if(v > x) {
        if(v > y) v else y
      } else {
        if(x > y) x else y
      }
    case Empty =>
      throw new RuntimeException
  }


  def min(t: Tree): Int = t match {
    case Branch(v, Empty, Empty) =>
      v
    case Branch(v, Empty, r) =>
      val x = min(r)
      if(v < x) v else x
    case Branch(v, l, Empty) =>
      val x = min(l)
      if(v < x) v else x
    case Branch(v, l, r) =>
      val x = min(l)
      val y = min(r)
      if(v < x) {
        if(v < y) v else y
      } else {
        if(x < y) x else y
      }
    case Empty =>
      throw new RuntimeException
  }

  def depth(t: Tree): Int = t match {
    case Empty => 0
    case Branch(_, l, r) =>
      val ldepth = depth(l) 
      val rdepth = depth(r)
      (if(ldepth < rdepth) rdepth else ldepth) + 1
  }

  def toList(tree: Tree): List[Int] = tree match {
    case Empty => Nil
    case Branch(v, l, r) => toList(l) ++ List(v) ++ toList(r)
  }

  def sort(t: Tree): Tree = {
    def fromList(list: List[Int]): Tree = {
      def insert(value: Int, t: Tree): Tree = t match {
        case Empty => Branch(value, Empty, Empty)
        case Branch(v, l, r) =>
          if(value <= v) Branch(v, insert(value, l), r)
          else Branch(v, l, insert(value, r))
      }
      list.foldLeft(Empty:Tree){ case (t, v) => insert(v, t) }
    }
    fromList(toList(t))
  }

  def find(t: Tree, target: Int): Boolean = t match {
    case Branch(v, l, r) => if(v == target) true else (find(l, target) || find(r, target))
    case Empty => false
  }

  def findBinaryTree(t: Tree, target: Int): Boolean = t match {
    case Branch(v, l, r) => if(v == target) true else (if(target <= v) findBinaryTree(l, target) else findBinaryTree(r, target))
    case Empty => false
  }
}
```

```tut:invisible
import org.scalacheck._, Arbitrary.arbitrary

val nonEmptyTreeGen: Gen[BinaryTree.Tree] = {

  lazy val branchGen: Gen[BinaryTree.Tree] = for{
    x <- arbitrary[Int]
    l <- treeGen
    r <- treeGen
  } yield BinaryTree.Branch(x, l, r)

  lazy val treeGen: Gen[BinaryTree.Tree] =
    Gen.oneOf(
      branchGen,
      Gen.const(BinaryTree.Empty)
    )

  branchGen
}

Testing.test(nonEmptyTreeGen){ tree =>
  BinaryTree.max(tree) == BinaryTree.toList(tree).max
}

Testing.test(nonEmptyTreeGen){ tree =>
  BinaryTree.min(tree) == BinaryTree.toList(tree).min
}
```

<!-- end answer -->


## 部分関数

これまでの説明の中で、無名関数とパターンマッチングについて説明してきましたが、この2つの機能を組み合わせた
部分関数（PartialFunction）がScalaには存在します。説明の前に、まず、具体的なユースケースを挙げます：

```tut
List(1, 2, 3, 4, 5).collect { case i if i % 2 == 1 => i * 2 }
```

ここで、`collect`メソッドは `pf: PartialFunction[A, B]` を引数に取り、`pf.isDefinedAt(i)` が `true` になる
要素のみを残し、さらに、`pf.apply(i)` の結果の値を元にした新しいコレクションを返します。

`isDefinedAt` は

```scala
i % 2 == 1
```

の部分から自動的に生成され、パターンがマッチするときのみ真になるように定義されます。`collect`はこの`isDefinedAt`
メソッドを使うことで、`filter` と `map` に相当する処理を同時に行うことができています。

`PartialFunction` は、自分でクラスを継承して作ることも可能ですが、一般的には、

```scala
{
  case pat1 => exp1
  case pat2 => exp2
  ...
  case patn => expn
}
```

という形の式から、自動的に生成されます。`isDefinedAt`が真になる条件は、`pat1 ... patn`のいずれかの条件にマッチすることです。

`PartialFunction`を使う機会は実用的にはそれほど多いわけではありませんが、`collect`やサードパーティのライブラリで使うことが
しばしばあるので、覚えておくと良いでしょう。

注意点として、 `{ case .... }`　という形式は、あくまで `PartialFunction` 型が要求されているときにのみ `PartialFunction`
が生成されるのであって、通常の `FunctionN` 型が要求されたときには、違う意味を持つということです。たとえば、以下のような
定義があったとします。

```tut
val even: Int => Boolean = {
  case i if i % 2 == 0 => true
  case _ => false
}
```

このとき、この定義は、無名関数とパターンマッチを組み合わせたものと同じ意味になります。この点にだけ注意してください。

```tut
val even: Int => Boolean = (x => x match {
  case i if i % 2 == 0 => true
  case _ => false
})
```
