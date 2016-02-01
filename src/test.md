# テスト

この節ではテストについての話をします。
皆さんはこれまで期末試験や入社試験など多くのテストを受けてきたと思います。
ですが、これからはテストを受けさせることが多くなるでしょう（ただし相手はプログラムですが）。

プログラムが受けるテストは、そのプログラムが要求された正しい動作をするかを確認するために行います。
皆さんが受けてきたテストは一度解いた問題を二度解くことはほとんど無いことですが、
開発中のプログラムですと飽きることなく同じテストを解く（解かされる？）ことも多いでしょう。

ソフトウェアに対して行うテストのことをソフトウェアテストといいますが、
このソフトウェアテストにはいくつか種類があります。
よく使われるものを挙げてみましょう：

- ユニットテスト（Unit Test）
  - `単体テスト` とも呼びます。
  - プログラム全体ではなく小さな単位（例えば関数ごと）に実行されます。このテストの目的はその単体が正しく動作していることを確認することです。ユニットテスト用のフレームワークの種類はとても多くJavaのJUnitやPHPのPHPUnitなどxUnitと呼ばれることが多いです。
- 結合テスト・統合テスト (Integration Test)
  - プログラム全体が完成してから実際に動作するかを検証するために実行します。人力で行う（例えば機能を開発した本人）ものやSeleniumなどを使って自動で実行するものがあります。
- システムテスト・品質保証テスト (System Test)
  - 実際に利用される例に則した様々な操作を行い問題ないかを確認します。ウェブアプリケーションの場合ですと、例えばフォームに入力する値の境界値を超えた場合、超えない場合のレスポンスの検証を行ったり様々なウェブブラウザで正常に動作するかなどを確認します。

なおユニットテスト、結合テスト、システムテストなどのそれぞれのテストにおいて、
特にソフトウェアの機能要件を満たすためのテストのことを機能テストといいます。
ですが実際にはこれらの機能テスト以外にも、XSSやSQLインジェクションがないかを検査する脆弱性検査テストや
要求されたアクセス数を捌くことができるかをウェブアプリケーションに大量のアクセスをかけることで検査する
性能テストやストレステストなど多くのテストをくぐり抜ける必要があります。


## ユニットテスト

このたびは特にユニットテストの行い方についてフォーカスして解説していきます。
そもそもなぜユニットテストを行うのでしょうか？
最終的に機能を確認するのであれば結合テストやシステムテストだけすれば良いのではないか？ と思うかもしれません。

にもかかわらず、ユニットテストを行う理由は大きく3つあげられます。

1. 実装の前に満たすべき仕様をユニットテストとして定義し、実装を行うことで要件漏れのない機能を実装することができる
1. 全ての機能を実装する前に、単体でテストをすることができる
1. ユニットテストによって満たすべき仕様がテストされた状態ならば、安心してリファクタリングすることができる

以上が大きな理由です。

最初の理由であるテストコードをプロダクトコードよりも先に書くことをテストファーストと呼びます。
そして、失敗するテストを書いて実装を進めていく手法のことをテスト駆動開発（TDD: Test Driven Development）といいます。

しかしながらこのTDDを行うためには非常に高いプログラミングスキルが要求されます。
なぜならば技術的制約による仕様の変更が、テストを先に書いてしまうと行いにくいからです。
よくあることとして、実装してみてこれができないと気づいて仕様を変えなくてはならない、ということができないため、
仕様策定の段階で全ての技術的制約を理解していなくてはなりません。

最近ではTDDのために設計が悪くなってしまうという批判もあり、[TDD is dead](http://postd.cc/is-tdd-dead-part1/)
というコラムにおいてRailsの開発者のDHH(David Heinemeier Hansson)より問題が指摘されています。

以上の理由から、実際は2つ目以降の機能検証とリファクタリングのためにユニットテストが書かれることが多いです。
TDDはできれば素晴らしいことではありますが、この度は単体機能の検証とリファクタリングのための実践をここでは試します。

あと、3つ目に出てきたリファクタリングとはなんでしょうか？

リファクタリングとは、ソフトウェアの仕様を変えること無く、プログラムの構造を扱いやすく変化させることです。
マーチン・ファウラーの[リファクタリング](http://www.amazon.co.jp/dp/427405019X)によると、リファクタリングを行う理由として、

1. ソフトウェア設計を向上させるため
1. ソフトウェアを理解しやすくするため
1. バグを見つけやすくするため
1. 早くプログラミングできるようにするため

という4つの理由があげられています。また同書の中でTDDの提唱者であるケント・ベックは、

1. 読みにくいプログラムは変更しにくい
1. ロジックが重複しているプログラムは変更しにくい
1. 機能追加に伴い、既存のコード修正が必要になるプログラムは変更しにくい
1. 複雑な条件分岐の多いプログラムは変更しにくい

以上の経験則から、リファクタリングは有効であると述べています。

リファクタリングは中長期的な開発の効率をあげるための良い手段であり、
変更が要求されるソフトウェアでは切っても切ることができないプラクティスです。
このリファクタリングのために、ユニットテストは非常に有効なテスト手法ということができます。


## テスティングフレームワーク

実際にユニットテストのテストコードを書く際には、テスティングフレームワークを利用します。
ここではScalaで広く利用されているユニットテスト用のフレームワークには、

- [Specs2](http://etorreborre.github.io/specs2/)
- [ScalaTest](http://www.scalatest.org/)

というものがあります。

よく利用されるWebフレームワークであるPlay Frameworkの中では現在Specs2が採用されているのですが、
マクロによるpower assertが便利なため、今回はscalatestを利用します。

scalatestは、テスティングフレームワークの中でも
振舞駆動開発（BDD :Behavior Driven Development）をサポートしているフレームワークです。
BDDでは、テスト内にそのプログラムに与えられた機能的な外部仕様を記述させることで、
テストが本質的に何をテストしようとしているのかをわかりやすくする手法となります。
基本この書き方にそって書いていきます。


## テストができるsbtプロジェクトの作成

では、実際にユニットテストを書いてみましょう。
まずはプロジェクトを作成します。

適当な作業フォルダにて以下を実行します。ここでは、`scalatest_study`を作り、
さらに中に`src/main/scala`と`src/test/scala`の2つのフォルダを作りましょう。

`build.sbt`を用意して、以下を記述しておきます。

```scala
name := "scalatest_study"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.5" % "test"

```

その後、`scalatest_study`フォルダ内で、`sbt compile`を実行してみましょう。

```
[info] Set current project to scalatest_study (in build file:/Users/dwango/workspace/scalatest_study/scalatest_study/)
[info] Updating {file:/Users/dwango/workspace/scalatest_study/scalatest_study/}scalatest_study...
[info] Resolving jline#jline;2.12.1 ...
[info] downloading http://repo1.maven.org/maven2/org/scalatest/scalatest_2.11/2.2.1/scalatest_2.11-2.2.1.jar ...
[info] 	[SUCCESSFUL ] org.scalatest#scalatest_2.11;2.2.1!scalatest_2.11.jar(bundle) (10199ms)
[info] Done updating.
[success] Total time: 11 s, completed 2015/04/09 16:48:42
```

以上のように表示されれば、これで準備は完了です。

## Calcクラスとそのテストを実際に作る

それでは、具体的なテストを実装してみましょう。
このたびは、Calcクラスという以下の仕様のを満たすクラスを作成し、それらをテストしていきます。
Calcクラスの機能は以下のとおり、

- 整数の配列を取得し、それらを足し合わせた整数を返すことができるsum関数を持つ
- 整数を2つ受け取り、分子を分母で割った浮動小数点の値を返すことができるdiv関数を持つ
- 整数値を1つ受け取り、その値が素数であるかどうかのブール値を返すisPrime関数を持つ

実装すると`src/main/scala/Calc.scala`は、以下のようになります。

```tut:silent
class Calc {

  /**
   * 整数の配列を取得し、それらを出し合わせた整数を返す
   * Intの最大を上回った際にはオーバーフローする
   * @param seq
   * @return
   */
  def sum(seq: Seq[Int]): Int = seq.foldLeft(0)(_ + _)

  /**
   * 整数を2つ受け取り、分子を分母で割った浮動小数点の値を返す
   * 0で割ろうとした際には実行時例外が投げられる
   * @param numerator
   * @param denominator
   * @return
   */
  def div(numerator: Int, denominator: Int): Double = {
    if (denominator == 0) throw new ArithmeticException("/ by zero")
    numerator.toDouble / denominator.toDouble
  }

  /**
   * 整数値を一つ受け取り、その値が素数であるかどうかのブール値を返す
   * @param n
   * @return
   */
  def isPrime(n: Int): Boolean = {
    if (n < 2) false else !((2 to Math.sqrt(n).toInt) exists (n % _ == 0))
  }
}
```

次にテストケースについて考えます。

- sum関数
    - 整数の配列を取得し、それらを足し合わせた整数を返すことができる
    - Intの最大を上回った際にはオーバーフローする
- div関数
    - 整数を2つ受け取り、分子を分母で割った浮動小数点の値を返す
    - 0で割ろうとした際には実行時例外が投げられる
- isPrime関数
    - その値が素数であるかどうかのブール値を返す
    - 100万以下の値の素数判定を一秒以内で処理できる

以上のようにテストを行います。
基本的にテストの設計は、

1. 機能を満たすことをテストする
1. 機能が実行できる境界値に対してテストする
1. 例外やログがちゃんと出ることをテストする

以上の考えが重要です。

XP（エクストリームプログラミング）のプラクティスに、不安なところを徹底的にテストするという考えがあり、基本それに沿います。

ひとつ目の満たすべき機能が当たり前に動くは当たり前のこととして、
2つ目に不安な要素のある境界値をしっかりテストする、というのは
テストするケースを減らし、テストの正確性をあげるためのプラクティスの境界値テストとしてもしても知られています。
そして最後は、レアな事象ではあるけれど動かないと致命的な事象の取り逃しにつながる
例外やログについてテストも非常に重要なテストです。
このような例外やログにテストがないと例えば1か月に1度しか起こらないような
不具合に対する対処が原因究明できなかったりと大きな問題につながってしまいます。

最小のテストを書いてみます。`src/test/scala/CalcSpec.scala`を以下のように記述します。

```tut:silent
import org.scalatest._

class CalcSpec extends FlatSpec with DiagrammedAssertions {

  val calc = new Calc

  "sum関数" should "整数の配列を取得し、それらを足し合わせた整数を返すことができる" in {
    assert(calc.sum(Seq(1, 2, 3)) === 6)
    assert(calc.sum(Seq(0)) === 0)
    assert(calc.sum(Seq(-1, 1)) === 0)
    assert(calc.sum(Seq()) === 0)
  }

  it should "Intの最大を上回った際にはオーバーフローする" in {
    assert(calc.sum(Seq(Integer.MAX_VALUE, 1)) === Integer.MIN_VALUE)
  }
}
```

テストクラスに`DiagrammedAssertions`をミックスインし、`assert`メソッドの引数に期待する条件を記述していきます[^predef-assert]。
`DiagrammedAssertions`を使うことで、覚えるべきAPIを減らしつつテスト失敗時に多くの情報を表示できるようになります。

テストを実装したら`sbt test`でテストを実行してください。
以下のような実行結果が表示されます。

```
[info] Loading project definition from /Users/dwango/workspace/scalatest_study/project
[info] Set current project to scalatest_study (in build file:/Users/dwango/workspace/scalatest_study/)
[info] Compiling 1 Scala source to /Users/dwango/workspace/scalatest_study/target/scala-2.11/classes...
[info] Compiling 1 Scala source to /Users/dwango/workspace/scalatest_study/target/scala-2.11/test-classes...
[info] CalcSpec:
[info] sum関数
[info] - should 整数の配列を取得し、それらを足し合わせた整数を返すことができる
[info] - should Intの最大を上回った際にはオーバーフローする
[info] Run completed in 570 milliseconds.
[info] Total number of tests run: 2
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 2, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 12 s, completed 2015/12/25 1:25:56
```

実行結果から、すべてのテストに成功したことを確認できます[^power-assert]。
なお、わざと失敗した場合にはどのように表示されるのか確認してみましょう。

```
[info] Loading project definition from /Users/dwango/workspace/scalatest_study/project
[info] Set current project to scalatest_study (in build file:/Users/dwango/workspace/scalatest_study/)
[info] Compiling 1 Scala source to /Users/dwango/workspace/scalatest_study/target/scala-2.11/test-classes...
[info] CalcSpec:
[info] sum関数
[info] - should 整数の配列を取得し、それらを足し合わせた整数を返すことができる *** FAILED ***
[info]   assert(calc.sum(Seq(1, 2, 3)) === 7)
[info]          |    |  ||   |  |  |   |   |
[info]          |    6  ||   1  2  3   |   7
[info]          |       |List(1, 2, 3) false
[info]          |       6
[info]          Calc@e72a964 (CalcSpec.scala:8)
[info] - should Intの最大を上回った際にはオーバーフローする
[info] Run completed in 288 milliseconds.
[info] Total number of tests run: 2
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 1, failed 1, canceled 0, ignored 0, pending 0
[info] *** 1 TEST FAILED ***
[error] Failed tests:
[error] 	CalcSpec
[error] (test:test) sbt.TestsFailedException: Tests unsuccessful
[error] Total time: 7 s, completed 2015/12/25 1:39:59
```

どこがどのように間違ったのかを指摘してくれます。

次に、例外が発生をテストする場合について記述してみましょう。
div関数までテストの実装を進めます。

```tut:silent
import org.scalatest._

class CalcSpec extends FlatSpec with DiagrammedAssertions {

  val calc = new Calc

  // ...

  "div関数" should "整数を2つ受け取り、分子を分母で割った浮動小数点の値を返す" in {
    assert(calc.div(6, 3) === 2.0)
    assert(calc.div(1, 3) === 0.3333333333333333)
  }

  it should "0で割ろうとした際には実行時例外が投げられる" in {
    intercept[ArithmeticException] {
      calc.div(1, 0)
    }
  }
}
```

上記では最後の部分でゼロ除算の際に投げられる例外をテストしています。
`intercept[Exception]`という構文で作ったスコープ内で投げられる例外がある場合には成功となり、
例外がない場合には逆にテストが失敗します。

最後にパフォーマンスを保証するテストを書きます。
なお、本来ユニットテストは時間がかかるテストを書くべきではありませんが、
できるだけ短い時間でそれを判定できるように実装します。

```tut:silent
import org.scalatest._
import org.scalatest.concurrent.Timeouts
import org.scalatest.time.SpanSugar._

class CalcSpec extends FlatSpec with DiagrammedAssertions with Timeouts {

  val calc = new Calc

  // ...

  "isPrime関数" should "その値が素数であるかどうかのブール値を返す" in {
    assert(calc.isPrime(0) === false)
    assert(calc.isPrime(-1) === false)
    assert(calc.isPrime(2))
    assert(calc.isPrime(17))
  }

  it should "100万以下の値の素数判定を一秒以内で処理できる" in {
    failAfter(1000 millis) {
      assert(calc.isPrime(9999991))
    }
  }
}
```

`Timeouts`というトレイトを利用することで`failAfter`という処理時間を
テストする機能を利用できるようになります。

最終的に全てのテストをまとめて`sbt test`で実行すると以下の様な出力が得られます。

```
[info] Loading project definition from /Users/dwango/workspace/scalatest_study/project
[info] Set current project to scalatest_study (in build file:/Users/dwango/workspace/scalatest_study/)
[info] Compiling 1 Scala source to /Users/dwango/workspace/scalatest_study/target/scala-2.11/test-classes...
[info] CalcSpec:
[info] sum関数
[info] - should 整数の配列を取得し、それらを足し合わせた整数を返すことができる
[info] - should Intの最大を上回った際にはオーバーフローする
[info] div関数
[info] - should 整数を2つ受け取り、分子を分母で割った浮動小数点の値を返す
[info] - should 0で割ろうとした際には実行時例外が投げられる
[info] isPrime関数
[info] - should その値が素数であるかどうかのブール値を返す
[info] - should 100万以下の値の素数判定を一秒以内で処理できる
[info] Run completed in 280 milliseconds.
[info] Total number of tests run: 6
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 6, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 8 s, completed 2015/12/25 1:43:22
```

以上が基本的なテストを実装するための機能の紹介でした。
BDDでテストを書くことによってテストによってどのような仕様が満たされた状態であるのかというのが
わかりやすい状況になっていることがわかります。

## モック

モックとは、テストをする際に必要となるオブジェクトを偽装して用意できる機能です。
以下の様なモックライブラリが存在しています。

- [ScalaMock](http://scalamock.org/)
- [EasyMock](http://easymock.org/)
- [JMock](http://www.jmock.org/)
- [Mockito](https://code.google.com/p/mockito/)

ここでは、ScalaTestで最初に紹介されているScalaMockを利用してみましょう。`build.sbt`に

```scala
libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test"
```

以上を追記することで利用可能になります。
せっかくなので、先ほど用意したCalcクラスのモックを用意して、モックにsumの振る舞いを仕込んで見ましょう。

```scala
import org.scalamock.scalatest.MockFactory
import org.scalatest._

class CalcSpec extends FlatSpec with DiagrammedAssertions with Timeouts with MockFactory {

  // ...

  "Calcのモックオブジェクト" should "振る舞いを偽装することができる" in {
    val mockCalc = mock[Calc]
    (mockCalc.sum _).expects(Seq(3, 4, 5)).returning(12)
    asserr(mockCalc.sum(Seq(3, 4, 5)) === 12)
  }
}
```

`MockFactory`というトレイトをミックスインすることで、このモックの機能が利用できるようになります。
`val mockCalc = mock[Calc]`でモックオブジェクトを作成し、
`(mockCalc.sum _).expects(Seq(3, 4, 5)).returning(12)`で振る舞いを作成しています。

そして最後に、`assert(mockCalc.sum(Seq(3, 4, 5)) === 12)`でモックに仕込んだ偽装された振る舞いをテストしています。

以上のようなモックの機能は、実際には時間がかかってしまう通信などの部分を高速に動かすために利用されています。
より詳しい、モックの利用方法については、[ScalaMock User Guide](http://scalamock.org/user-guide/)をご覧ください。


## コードカバレッジの測定

テストを行った際に、テストが機能のどれぐらいを網羅できているのかを知る方法として、
コードカバレッジを図るという方法があります。
ここでは、[scoverage](https://github.com/scoverage/scalac-scoverage-plugin)を利用します。

過去、[SCCT](http://mtkopone.github.io/scct/)というプロダクトがあったのですが紆余曲折あり、
今はあまりメンテンナンスされていません。

`project/plugins.sbt` に以下のコードを記述します。

```scala
resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.3")
```

その後、`sbt clean coverage test`を実行することで、`target/scala-2.11/scoverage-report/index.html`にレポートが出力されます。


![Scoverage Code Coverage](img/scoverage_code_coverage.png)


以上のような出力となり、今回のテストはカバレッジ率100％であることがわかります。

ただし実際の開発においては、殆ど存在しないような例に対しての防御的な実装もあるため、カバレッジを100％にすることは難しいこともあります。
その際には重要なビジネスロジックだけは、カバレッジが100％となるように工夫するのが良いとされています。


## コードスタイルチェック

なおテストとは直接は関係ありませんが、ここまでで紹介したテストは、
実際にはJenkinsなどの継続的インテグレーションツール（CIツール）で実施され、
リグレッションを検出するためにつかわれます。
その際に、CIの一環として一緒に行われることが多いのがコードスタイルチェックです。

ここでは、[ScalaStyle](http://www.scalastyle.org/sbt.html)を利用します。

使い方は、`project/plugins.sbt` に以下のコードを記述します。

```scala
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.6.0")
```

その後、`sbt scalastyleGenerateConfig`を一度だけ実施後、`sbt scalastyle`を実行します。

実行すると、

```
[info] Loading project definition from /Users/dwango/workspace/scalatest_study/project
[info] Set current project to scalatest_study (in build file:/Users/dwango/workspace/scalatest_study/)
[info] scalastyle using config /Users/dwango/workspace/scalatest_study/scalastyle-config.xml
[warn] /Users/dwango/workspace/scalatest_study/src/main/scala/Calc.scala:1: Header does not match expected text
[info] Processed 1 file(s)
[info] Found 0 errors
[info] Found 1 warnings
[info] Found 0 infos
[info] Finished in 12 ms
[success] created output: /Users/dwango/workspace/scalatest_study/target
[success] Total time: 1 s, completed 2015/04/09 22:17:40
```

以上のように警告の表示をおこなってくれます。

これはヘッダに特定のテキストが入っていないというルールに対して警告を出してくれているもので、
`scalastyle-config.xml`でルール変更を行うことができます。
なおデフォルトの設定では、Apacheライセンスの記述を入れなくては警告を出す設定になっています。
これらもテスティングフレームワークやカバレッジ測定と同時に導入、設定してしまいましょう。

[^predef-assert]: Scalaには`Predef`にも`assert`が存在しますが、基本的に使うことはありません
[^power-assert]: 渡された条件式の実行過程をダイアグラムで表示する`assert`は、一般に“power assert”と呼ばれています

