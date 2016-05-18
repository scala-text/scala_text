# エラー処理（★★★）

ここではScalaにおけるエラー処理の基本を学びます。Scalaでのエラー処理は例外を使う方法と、OptionやEitherやTryなどのデータ型を使う方法があります。この2つの方法はどちらか一方だけを使うわけではなく、状況に応じて使いわけることになります。

まずは私たちが扱わなければならないエラーとエラー処理の性質について確認しましょう。

## エラーとは

プログラムにとって、エラーというものにはどういったものがあるのか考えてみます。

### ユーザーからの入力

1つはユーザーから受け取る不正な入力です。たとえば以下のようなものが考えられます。

- 文字数が長すぎる
- 電話番号に文字列を使うなど、正しいフォーマットではない
- 既に登録されているユーザー名を使おうとしている

など色々な問題が考えられます。
また悪意のある攻撃者から攻撃を受けることもあります。

- アクセスを制限しているデータを見ようとしている
- ログインセッションのCookieを改変する
- 大量にアクセスをおこない、システムを利用不能にしようとする

基本的に外から受け取るデータはすべてエラーの原因となりえるので注意が必要です。

### 外部サービスのエラー

自分たちのプログラムが利用する外部サービスのエラーも考えられます。

- TwitterやFacebookに投稿しようとしても繋がらない
- iPhoneやAndroidと通信しようとしても回線の都合で切れてしまう
- ユーザーにメールを送信しようとしても失敗する

以上のように外部のサービスを使わなければならないような処理はすべて失敗することを想定したほうがいいでしょう。

### 内部のエラー

外的な要因だけではなく、内部の要因でエラーが発生することもあります。

- ライブラリのバグや自分たちのバグにより、プログラム全体が終了してしまう
- MySQLやRedisなどの内部で利用しているサーバーが終了してしまう
- メモリやディスク容量が足りない
- 処理に非常に大きな時間がかかってしまう

内部のエラーは扱うことが難しい場合が多いですが、起こりうることは念頭に置くべきです。

## エラー処理で実現しなければならないこと

以上のようなエラーに対して、私たちが行わなければいけないことを挙げてみます。

### 例外安全性

エラー処理の中の1つの例外処理には「例外安全性」という概念があります。
例外が発生してもシステムがダウンしたり、データの不整合などの問題が起きない場合、例外安全と言います。

この概念はエラー処理全般にもあてはまります。
私たちが作るプログラムを継続的に動作させたいと考えた場合、ユーザーの入力や外部サービスの問題により、システムダウンやデータの不整合が起きてはなりません。
これがエラー処理の第一の目的になります。

### 強い例外安全性

例外安全性にはさらに強い概念として「強い例外安全性」というものがあります。
これは例外が発生した場合、すべての状態が例外発生前に戻らなければならないという制約です。
一般的にはこの制約を満たすことは難しいのですが、たとえばユーザーがサービスに課金して、何らかのエラーが生じた場合、確実にエラーを検出し、課金処理を取り消さなければなりません。
どのような処理に強い例外安全性が求められるか判断し、どのように実現するかを考える必要があります。

## Javaにおけるエラー処理

Javaのエラー処理の方法はScalaにも適用できるものが多いです。
ここではJavaのエラー処理の注意点についていくつか復習しましょう。

### nullを返すことでエラーを表現する場合の注意点

Javaでは、変数が未初期化である場合や、コレクションライブラリが空なのに要素を取得しようとした場合など、nullでエラーを表現することがあります。
Javaはプリミティブ型以外の参照型はすべてnullにすることができます。
この性質はエラー値を他に用意する必要がないという点では便利なのですが、
しばしば返り値をnullかどうかチェックするのを忘れて実行時エラーの[NullPointerException](https://docs.oracle.com/javase/jp/8/docs/api/java/lang/NullPointerException.html)（通称：ぬるぽ・NPE）を発生させてしまいます。
（「ぬるぽ」と「ガッ」というやりとりをする2chの文化の語源でもあります）

参照型がすべて`null`になりうるということは、メソッドが`null`が返されるかどうかはメソッドの型からはわからないので、Javaのメソッドで`null`を返す場合はドキュメントに書くようにしましょう。
そして、`null`をエラー値に使うエラー処理は暗黙的なエラー状態をシステムのいたるところに持ち込むことになり、発見困難なバグを生む要因になります。
後述しますが、ScalaではOptionというデータ構造を使うことでこの問題を解決します。

### 例外を投げる場合の注意点

Javaのエラー処理で中心的な役割を果たすのが例外です。
例外は今実行している処理を中断し、大域的に実行を移動できる便利な機能ですが、濫用することで処理の流れがわかりづらいコードにもなります。例外はエラー状態にのみ利用し、メソッドが正常な値を返す場合には使わないようにしましょう。

### チェック例外の注意点

Javaにはメソッドに`throws`節を付けることで、メソッドを使う側に例外を処理することを強制するチェック例外という機能もあります。
チェック例外は例外の発生を表現し、コンパイラにチェックさせるという点で便利な機能ですが、上げられた例外のcatch処理はわずらわしいものにもなりえます。
使う側が適切に処理できない例外を上げられた場合はあまり意味のないエラー処理コードを書かざるをえません。
よってチェック例外は利用側がcatchして適切にエラー状態から回復できる場合にのみ利用したほうがいいでしょう。

### 例外翻訳の注意点

Javaの例外は実装の変更により変化する場合があります。たとえば今までHTTPで取得していたデータをMySQLに保存したとしましょう。
その場合、今まではHTTPExceptionが投げられていたものが、SQLExceptionが投げられるようになるかもしれません。
すると、この例外をcatchする側もHTTPExceptionではなくSQLExceptionを扱うようにしなければなりません。
このように低レベルの実装の変更がプログラム全体に影響することがありえます。

そのような問題を防ぐために途中の層で一度例外をcatchし、適切な例外で包んでもう一度投げる手法があります。
このことを例外翻訳と呼びます。
例外翻訳は例外に対する情報を増やし、catchする側の影響も少なくする手法です。
ただし、この例外翻訳も乱用すると例外の種類が増えて例外処理が煩雑になる可能性もあるので注意が必要です。

### 例外をドキュメントに書く

例外はチェック例外でない場合、APIから読み取ることができません。
さらに後述しますがScalaではチェック例外がないので、メソッドの型からどんな例外を投げるかは判別できません。
そのためAPIドキュメントには発生しうる例外についても書いておいたほうが良いでしょう。

## 例外の問題点

Javaのエラー処理では例外が中心的な役割を担っていましたが、Scalaでも例外は多く使われます。
しかし、例外は便利な反面、様々な問題もあります。
ここで例外の問題点を把握し、適切に使えるようになりましょう。

### 例外を使うとコントロールフローがわかりづらくなる

先ほど述べたように例外は、適切に使えば正常系の処理とエラー処理を分離し、コードの可読性を上げ、エラー処理をまとめる効果があります。しかし、往々にして例外のcatch漏れが発生し、障害に繋がることがあります。逆に例外をcatchしているところで、どこで発生した例外をcatchしているのか判別できないために、コードの修正を阻害する場合もあります。

### 例外は非同期プログラミングでは使えない

例外の動作は送出されたらcatchされるまでコールスタックを遡っていくというものです。
ということは別スレッドや、別のイベントループなどで実行される非同期プログラミングとは相容れないものです。
特にScalaでは非同期プログラミングが多用されるので、例外をそのまま使えないことが多いです。

### 例外は型チェックできない

チェック例外を使わない限り、どんな例外が発生するのかメソッドの型としては表現されません。
またcatchする側でも間違った例外をキャッチしているかどうかは実行時にしかわかりません。
例外に頼りすぎると静的型付き言語の利点が損われます。

### チェック例外の問題点

チェック例外を使わないとコンパイル時に型チェックできないわけですが、ScalaではJavaとは違いチェック例外の機能はなくなりました。
これにはチェック例外の様々な問題点が理由としてあると思います

- 高階関数でチェック例外を扱うことが難しい
- ボイラープレートが増える
- 例外によるメソッド型の変更を防ぐために例外翻訳を多用せざるをえない

特にScalaでは1番目の問題が大きいと思います。
後述しますが、Scalaではチェック例外の代替手段として、エラーを表現するデータ型を使い、エラー処理を型安全にすることもできます。
それらを考えるとScalaでチェック例外をなくしたのは妥当な判断と言えるでしょう。

## エラーを表現するデータ型を使った処理

例外に問題があるとすれば、どのようにエラーを扱えばよいのでしょうか。
その答えの1つはエラーを例外ではなく、メソッドの返り値で返せるような値にすることです。

ここでは正常の値とエラー値のどちらかを表現できるデータ構造の紹介を通じて、Scalaの関数型のエラー処理の方法を見ていきます。

### [Option](https://github.com/scala/scala/blob/v2.11.8/src/library/scala/Option.scala)

OptionはScalaでもっとも多用されるデータ型の1つです。
前述のとおりJavaのnullの代替として使われることが多いデータ型です。

Option型は簡単に言うと、値を1つだけいれることのできるコンテナです。
ただし、Optionのまま様々なデータ変換処理ができるように便利な機能を持ちあわせています。

#### Optionの作り方と値の取得

では具体的にOptionの作り方と値の取得を見てみましょう。
Option型には具体的には

- `Some`
- `None`

以上2つの具体的な値が存在します。`Some`は何かしらの値が格納されている時の`Option`の型、
`None`は値が何も格納されていない時の`Option`の型です。

具体的な動きを見てみましょう。`Option`に具体的な値が入った場合は以下の様な動きをします。

```tut
val o: Option[String] = Option("hoge")

o.get

o.isEmpty

o.isDefined
```

今度は`null`を`Option`に入れるとどうなるでしょうか。

```tut
val o: Option[String] = Option(null)

o.isEmpty

o.isDefined
```

```tut:fail
o.get

```

Optionのコンパニオンオブジェクトのapplyには引数がnullであるかどうかのチェックが入っており、引数がnullの場合、値がNoneになります。
`get`メソッドを叩いた時に、`java.util.NoSuchElementException`という例外が起こっているので、
これがNPEと同じだと思うかもしれません。
しかしOptionには以下の様な便利メソッドがあり、それらを回避することができます。

```tut
o.getOrElse("")
```

以上は`Option[String]`の中身が`None`だった場合に、空文字を返すというコードになります。
値以外にも処理を書くこともできます。

```tut:fail
o.getOrElse(throw new RuntimeException("nullは受け入れられません"))
```

このように書くこともできるのです。


#### Optionのパターンマッチ

上記では、手続き的にOptionを処理しましたが、型を持っているため
パターンマッチを使って処理することもできます。

```tut
val s: Option[String] = Some("hoge")

s match {
  case Some(str) => println(str)
  case None => throw new RuntimeException
}
```

上記のようにSomeかNoneにパターンマッチを行い、Someにパターンマッチする場合には、
その中身の値を`str`という別の変数に束縛をすることも可能です。

中身を取りだすのではなく、中身を束縛するというテクニックは、`List`のパターンマッチでも
行うことができますが、全く同様のことがOptionでもできます。

#### Optionに関数を適用する

Optionには、コレクションの性質があると言いましたが、関数を内容の要素に適用できるという
性質もそのまま持ち合わせています。

```tut
Some(3).map(_ * 3)
```

このように、`map`で関数を適用する事もできます。なお、値が`None`の場合にはどうなるでしょうか。

```tut
val n: Option[Int] = None

n.map(_ * 3)
```

Noneのままだと型情報を持たないので一度、変数にしていますが、Noneに3をかけるという関数を
適用してもNoneのままです。この性質はとても便利で、その値がOptionの中身がSomeなのかNoneなのか
どちらであったとしても、同様の処理で記述でき、処理を分岐させる必要がないのです。

Java風に書くならば、

```tut:fail
if (n.isDefined) {
  n.get * 3
} else {
  throw new RuntimeException
}
```

きっと上記のように書くことになっていたでしょう。
ただ、よくよく考えると上記のJava風に書いた例とmapの例は異なることに気が付きます。
mapでは、値がSomeの場合は中身に関数を適用しますが、Noneの時には何も実行しません。
上記の例では例外を投げています。そして、値もInt型の値を返していることも異なっています。

このように、Noneの場合に実行し、値を返す関数を定義できるのが`fold`です。
`fold`の宣言を[ScalaのAPIドキュメント](http://www.scala-lang.org/api/current/index.html#scala.Option)から引用すると、
```scala
fold[B](ifEmpty: ⇒ B)(f: (A) ⇒ B): B
```
となります。

そして関数を適用した値を最終的に取得できます。

```tut:fail
n.fold(throw new RuntimeException)(_ * 3)
```

上記のように書くことで、Noneの際に実行する処理を定義し、かつ、関数を適用した中身の値を
取得することができます。

```tut
Some(3).fold(throw new RuntimeException)(_ * 3)
```

`Some(3)`を与えるとこのようにIntの9の値を返すことがわかります。


#### Optionの入れ子を解消する

実際の複雑なアプリケーションの中では、Optionの値が取得されることがよくあります。

たとえばキャッシュから情報を取得する場合は、キャッシュヒットする場合と、
キャッシュミスする場合があり、それらはScalaではよくOption型で表現されます。

このようなキャッシュ取得が連続して繰り返された場合はどうなるでしょうか。
例えば、1つ目と2つ目の整数の値がOptionで返ってきてそれをかけた値をもとめるような場合です。


```tut
val v1: Option[Int] = Some(3)

val v2: Option[Int] = Some(5)

v1.map(i1 => v2.map(i2 => i1 * i2))
```

mapだけを使ってシンプルに実装するとこんな風になってしまいます。
ウウッ…、悲しいことに`Option[Option[Int]]`のようにOptionが入れ子になってしまいます。

このような入れ子のoptionを解消するために用意されているのが、`flatten`です。

```tut
v1.map(i1 => v2.map(i2 => i1 * i2)).flatten
```

最後に`flatten`を実行することで、Optionの入れをを解消することができます。
なお、これはちゃんとv2がNoneである場合にも`flatten`は成立します。

```tut
val v1: Option[Int] = Some(3)

val v2: Option[Int] = None

v1.map(i1 => v2.map(i2 => i1 * i2)).flatten
```

つまり、キャッシュミスでSomeの値が取れなかった際も問題なくこの処理で動きます。

#### 練習問題 {#error_handling_ex1}

`map`と`flatten`を利用して、
`Some(2)`と`Some(3)`と`Some(5)`と`Some(7)`と`Some(11)`の値をかけて、`Some(2310)`を求めてみましょう。

<!-- begin answer id="answer_ex1" style="display:none" -->

```tut:silent
val v1: Option[Int] = Some(2)
val v2: Option[Int] = Some(3)
val v3: Option[Int] = Some(5)
val v4: Option[Int] = Some(7)
val v5: Option[Int] = Some(11)
v1.map { i1 =>
    v2.map { i2 =>
        v3.map { i3 =>
            v4.map { i4 =>
                v5.map { i5 => i1 * i2 * i3 * i4 * i5 }
            }.flatten
        }.flatten
    }.flatten
}.flatten
```

<!-- end answer -->

### flatMap

ここまでで、`map`と`flatten`を話しましたが、実際のプログラミングではこの両方を
組み合わせて使うということが多々あります。そのためその2つを適用してくれる
`flatMap`というメソッドがOptionには用意されています。
名前は`flatMap`なのですが、意味としてはOptionに`map`をかけて`flatten`を適用してくれます。

実際に先ほどの、`Some(3)`と`Some(5)`をかける例で利用してみると以下のようになります。

```tut
val v1: Option[Int] = Some(3)

val v2: Option[Int] = Some(5)

v1.flatMap(i1 => v2.map(i2 => i1 * i2))
```

ずいぶんシンプルに書くことができるようになります。

`Some(3)`と`Some(5)`と`Some(7)`をかける場合はどうなるでしょうか。

```tut
val v1: Option[Int] = Some(3)

val v2: Option[Int] = Some(5)

val v3: Option[Int] = Some(7)

v1.flatMap(i1 => v2.flatMap(i2 => v3.map(i3 => i1 * i2 * i3)))
```

無論これは、v1, v2, v3のいずれがNoneであった場合にも成立します。
その場合には`flatten`の時と同様に`None`が最終的な答えになります。

```tut
val v3: Option[Int] = None

v1.flatMap(i1 => v2.flatMap(i2 => v3.map(i3 => i1 * i2 * i3)))
```

以上のようになります。


#### 練習問題 {#error_handling_ex2}

`flatMap`を利用して、
`Some(2)`と`Some(3)`と`Some(5)`と`Some(7)`と`Some(11)`の値をかけて、`Some(2310)`を求めてみましょう。

<!-- begin answer id="answer_ex2" style="display:none" -->

```tut:silent
val v1: Option[Int] = Some(2)
val v2: Option[Int] = Some(3)
val v3: Option[Int] = Some(5)
val v4: Option[Int] = Some(7)
val v5: Option[Int] = Some(11)
v1.flatMap { i1 =>
    v2.flatMap { i2 =>
        v3.flatMap { i3 =>
            v4.flatMap { i4 =>
                v5.map { i5 => i1 * i2 * i3 * i4 * i5 }
            }
        }
    }
}
```

<!-- end answer -->

### forを利用したflatMapのリファクタリング

Optionはコレクションのようなものだという風に言いましたが、forをOptionに使うこともできます。
for式は実際には`flatMap`と`map`展開されて実行されるのです。

何をいっているのかわかりにくいと思いますので、先ほどの
`Some(3)`と`Some(5)`と`Some(7)`をflatMapでかけるという処理をforで書いてみましょう。

```tut
val v1: Option[Int] = Some(3)

val v2: Option[Int] = Some(5)

val v3: Option[Int] = Some(7)

for { i1 <- v1
      i2 <- v2
      i3 <- v3 } yield i1 * i2 * i3
```

実はこのfor式は先ほどの`flatMap`と`map`で書かれたものとまったく同じ動作をします。
`flatMap`と`map`を複数回使うような場合はfor式のほうがよりシンプルに書くことができていることがわかると思います。


#### 練習問題 {#error_handling_ex3}

`for`を利用して、
`Some(2)`と`Some(3)`と`Some(5)`と`Some(7)`と`Some(11)`の値をかけて、`Some(2310)`を求めてみましょう。

<!-- begin answer id="answer_ex3" style="display:none" -->

```tut:silent
val v1: Option[Int] = Some(2)
val v2: Option[Int] = Some(3)
val v3: Option[Int] = Some(5)
val v4: Option[Int] = Some(7)
val v5: Option[Int] = Some(11)
for { i1 <- v1
      i2 <- v2
      i3 <- v3
      i4 <- v4
      i5 <- v5 } yield i1 * i2 * i3 * i4 * i5
```

<!-- end answer -->

### [Either](https://github.com/scala/scala/blob/v2.11.8/src/library/scala/util/Either.scala)

Optionによりnullを使う必要はなくなりましたが、いっぽうでOptionでは処理が成功したかどうかしかわからないという問題があります。
Noneの場合、値が取得できなかったことはわかりますが、エラーの状態は取得できないので、使用できるのはエラーの種類が問題にならないような場合のみです。

そんなOptionと違い、エラー時にエラーの種類まで取得できるのがEitherです。
Optionが正常な値と何もない値のどちらかを表現するデータ型だったのに対して、Eitherは2つの値のどちらかを表現するデータ型です。
具体的には、Optionでは`Some`と`None`の2つの値を持ちましたが、Eitherは`Right`と`Left`の2つの値を持ちます。

```tut
val v1: Either[String, Int] = Right(123)

val v2: Either[String, Int] = Left("abc")
```

パターンマッチで値を取得できるのもOptionと同じです。

```tut
v1 match {
  case Right(i) => println(i)
  case Left(s)  => println(s)
}
```

#### Eitherでエラー値を表現する

一般的にEitherを使う場合、Left値をエラー値、Right値を正常な値とみなすことが多いです。
英語の"right"が正しいという意味なので、それにかけているという説があります。
そしてLeftに用いるエラー値ですが、これは代数的データ型（sealed traitとcase classで構成される一連のデータと型のこと）で定義するとよいでしょう。
パターンマッチの節で解説したように代数的データ型を用いることでエラーの処理が漏れているかどうかをコンパイラが検知してくれるようになります。
単に`Throwable`型をエラー型に使うのなら後述のTryで十分です。

例としてEitherを使ってログインのエラーを表現してみましょう。
Leftの値となる`LoginError`を定義します。
`sealed`を使って代数的データ型として定義するのがポイントです。

```scala
sealed trait LoginError
// パスワードが間違っている場合のエラー
case object InvalidPassword extends LoginError
// nameで指定されたユーザーが見つからない場合のエラー
case object UserNotFound extends LoginError
// パスワードがロックされている場合のエラー
case object PasswordLocked extends LoginError
```

ログインAPIの型は以下のようにします。

```scala
case class User(id: Long, name: String, password: String)

object LoginService {
  def login(name: String, password: String): Either[LoginError, User] = ???
}
```

`login`メソッドはユーザー名とパスワードをチェックして正しい組み合わせの場合は`User`オブジェクトをEitherのRightの値で返し、
エラーが起きた場合は`LoginError`をEitherのLeftの値で返します。

それでは、この`login`メソッドを使ってみましょう。

```scala
LoginService.login(name = "dwango", password = "password") match {
  case Right(user) => println(s"id: ${user.id}")
  case Left(InvalidPassword) => println(s"Invalid Password!")
}
```

とりあえず呼び出して、`println`を使って中身を表示しているだけです。
ここで注目していただきたいのが、Leftの値のパターンマッチです。
`InvalidPassword`の処理はしていますが、`UserNotFound`の場合と`PasswordLocked`の場合の処理が抜けてしまっています。
そのような場合でもエラー値に代数的データ型を用いているので、コンパイラがエラー処理漏れを検知してくれます。

試しに上のコードをコンパイルしてみると、

```scala
<console>:11: warning: match may not be exhaustive.
It would fail on the following inputs: Left(PasswordLocked), Left(UserNotFound)
              LoginService.login(name = "dwango", password = "password") match {
                                ^
```

のようにコンパイラが`Left(PasswordLocked)`と`Left(UserNotFound)`の処理が漏れていることをwarningで教えてくれます。
Eitherを使う場合はこのテクニックを覚えておいたほうがいいでしょう。

#### EitherのmapとflatMap

以上、見てきたように格納できるデータが増えているという点でEitherはOptionの拡張版に近いのですが、ScalaのEitherはmapとflatMapの動作にちょっと癖があります。
ScalaのEitherはOptionとは違い、直接mapやflatMapメソッドを持ちません。
Eitherオブジェクトのメソッドを見てみますと、

```scala
scala> val v: Either[String, Int] = Right(123)
v: Either[String,Int] = Right(123)

scala> v.
asInstanceOf   fold   isInstanceOf   isLeft   isRight   joinLeft   joinRight   left   right   swap   toString
```

`fold`や`isLeft`や`isRight`などはOptionで同じようなメソッドがありましたが、肝心のmapやflatMapがありません。
これではfor式を使って複数のEitherを組み合わせることができません。

これにはScalaのEitherが左右の型を平等で扱っているという理由があります。
たとえば自作のmapメソッドを作ることを考えてみましょう。
mapメソッドは関数をコンテナの中身に適用できるようにするというものでした。
Eitherの左右が平等に扱われると考えた場合、mapメソッドは左右のどちらの値に適用すればいいのでしょうか？
この答えには2つのアプローチが考えられます。

- 暗黙的に左右どちらかのうち片方を優先し、そちらに関数を適用する（たとえばRightが正常な値になることが多いならRightを暗黙的に優先するとか）
- 明示的に左右どちらを優先するか指定する

前者はHaskellのアプローチで、後者がScalaのアプローチになります。
上記のEitherのメソッドに`left`と`right`というものがありました。
これが左右どちらを優先するのか決めるメソッドです。

試しに`right`メソッドを使ってみましょう。

```scala
scala> val v: Either[String, Int] = Right(123)
v: Either[String,Int] = Right(123)

scala> val e = v.right
e: scala.util.Either.RightProjection[String,Int] = RightProjection(Right(123))

scala> e.
asInstanceOf   canEqual   copy   e   exists   filter   flatMap   forall   foreach   get   getOrElse   isInstanceOf   map   productArity   productElement   productIterator   productPrefix   toOption   toSeq   toString
```

Eitherに`right`メソッドを使ったら`RightProjection`というオブジェクトになりました。
そして、この`RightProjection`オブジェクトを見てみると、ようやくListやOptionで見慣れたようなメソッドが出てきました。
これらのメソッドはEitherのRightの値に対しておこなわれるメソッドということです。

ためしに`RightProjection`の`map`メソッドを使ってみましょう[^product-with-serializable]。

```tut
val v: Either[String, Int] = Right(123)

v.right.map(_ * 2)
```

これでmapを使って値を二倍にする関数をRightに適用できました。
ちなみにEitherがLeftの場合は何の処理もおこなわれません。
これはOptionでNoneに対してmapを使った場合に何の処理もおこなわれないという動作に似ていますね。

注意してほしいのはRightProjectionのmapメソッドの返り値はEitherであるという点です。
つまりmapやflatMapを連鎖して使う場合には毎回EitherをRightProjectionに変化させる必要があるということです。

Optionのときの掛け算の例をEitherで書いてみると、

```tut
val v1: Either[String, Int] = Right(3)

val v2: Either[String, Int] = Right(5)

val v3: Either[String, Int] = Right(7)

for {
  i1 <- v1.right
  i2 <- v2.right
  i3 <- v3.right
} yield i1 * i2 * i3
```

Rightが正常な値として用いられることが多いとすれば、ほとんどのEitherはRightProjectionに変化させて使うことになります。
このScalaのEitherの仕様はHaskellのEitherのような暗黙的にRightを優先するのに比べてわずらわしいと言われることもあります。
とりあえずEitherのmapやflatMapなどを使う場合はrightでRightProjectionに変化させると覚えてしまってよいと思います。

### [Try](https://github.com/scala/scala/blob/v2.11.8/src/library/scala/util/Try.scala)

ScalaのTryはEitherと同じように正常な値とエラー値のどちらかを表現するデータ型です。
Eitherとの違いは、2つの型が平等ではなく、エラー値がThrowableに限定されており、型引数を1つしか取らないことです。
具体的にはTryは以下の2つの値をとります。

- Success
- Failure

ここでSuccessは型変数を取り、任意の値を入れることができますが、FailureはThrowableしか入れることができません。
そしてTryには、コンパニオンオブジェクトのapplyで生成する際に、例外をcatchし、Failureにする機能があります。

```tut
import scala.util.Try

val v: Try[Int] = Try(throw new RuntimeException("to be caught"))
```

この機能を使って、例外が起こりそうな箇所を`Try`で包み、Failureにして値として扱えるようにするのがTryの特徴です。

またTryはEitherと違い、正常な値を片方に決めているのでmapやflatMapをそのまま使うことができます。

```tut
val v1 = Try(3)

val v2 = Try(5)

val v3 = Try(7)

for {
  i1 <- v1
  i2 <- v2
  i3 <- v3
} yield i1 * i2 * i3
```

#### [`NonFatal`](https://github.com/scala/scala/blob/v2.11.8/src/library/scala/util/control/NonFatal.scala)の例外

`Try.apply`がcatchするのはすべての例外ではありません。
NonFatalという種類の例外だけです。
NonFatalではないエラーはアプリケーション中で復旧が困難な非常に重度なものです。
なので、NonFatalではないエラーはcatchせずにアプリケーションを終了させて、外部から再起動などをしたほうがいいです。

Try以外でも、たとえば扱うことができる全ての例外をまとめて処理したい場合などに、

```tut:silent
import scala.util.control.NonFatal

try {
  ???
} catch {
  case NonFatal(e) => // 例外の処理
}
```

というパターンが実践的なコード中に出てくることがしばしばあるので覚えておくとよいと思います。

### OptionとEitherとTryの使い分け

ではエラー処理においてOptionとEitherとTryはどのように使い分けるべきなのでしょうか。

まず基本的にJavaでnullを使うような場面はOptionを使うのがよいでしょう。
コレクションの中に存在しなかったり、ストレージ中から条件に合うものを発見できなかったりした場合はOptionで十分だと考えられます。

次にEitherですが、Optionを使うのでは情報が不足しており、かつ、エラー状態が代数的データ型としてちゃんと定められるものに使うのがよいでしょう。
Javaでチェック例外を使っていたようなところで使う、つまり、復帰可能なエラーだけに使うという考え方でもよいです。
Eitherと例外を併用するのもアリだと思います。

TryはJavaの例外をどうしても値として扱いたい場合に用いるとよいです。
非同期プログラミングで使ったり、実行結果を保存しておき、あとで中身を参照したい場合などに使うことも考えられます。


## Optionの例外処理をEitherでリファクタする実例

Scalaでリレーショナルデータベースを扱う場合、関連をたどっていく中でどのタイミングで情報が
取得できなかったのかを返さねばならないことがあります。

Noneを盲目的に処理するのであれば、flatMapやfor式をつかえば畳み込んでスッキリかけるのですが、
関連を取得していくなかでどのタイミングでNoneが取得されてしまったのか返したい場合にはそうは行かず、
結局match caseの深いネストになってしまいます。

例を挙げます。

ユーザーとアドレスがそれぞれデータベースに格納されており、ユーザーIDを利用してそのユーザーを検索し、
ユーザーが持つアドレスIDでアドレスを検索し、さらにその郵便番号を取得するような場合を考えます。

失敗結果としては

- ユーザーがみつからない
- ユーザーがアドレスを持っていない
- アドレスがみつからない
- アドレスが郵便番号を持っていない

という4つの失敗パターンがあり、それらを結果オブジェクトとして返さなくてはなりません。

以下のようなコードになります。

```tut:silent
object MainBefore {

  case class Address(id: Int, name: String, postalCode: Option[String])
  case class User(id: Int, name: String, addressId: Option[Int])

  val userDatabase: Map[Int, User] = Map (
    1 -> User(1, "太郎", Some(1)),
    2 -> User(2, "二郎", Some(2)),
    3 -> User(3, "プー太郎", None)
  )

  val addressDatabase: Map[Int, Address] = Map (
    1 -> Address(1, "渋谷", Some("150-0002")),
    2 -> Address(2, "国際宇宙ステーション", None)
  )

  sealed abstract class PostalCodeResult
  case class Success(postalCode: String) extends PostalCodeResult
  abstract class Failure extends PostalCodeResult
  case object UserNotFound extends Failure
  case object UserNotHasAddress extends Failure
  case object AddressNotFound extends Failure
  case object AddressNotHasPostalCode extends Failure

  // どこでNoneが生じたか取得しようとするとfor式がつかえず地獄のようなネストになる
  def getPostalCodeResult(userId: Int): PostalCodeResult = {
    findUser(userId) match {
      case Some(user) =>
        user.addressId match {
          case Some(addressId) =>
            findAddress(addressId) match {
              case Some(address) =>
                address.postalCode match {
                  case Some(postalCode) => Success(postalCode)
                  case None => AddressNotHasPostalCode
                }
              case None => AddressNotFound
            }
          case None => UserNotHasAddress
        }
      case None => UserNotFound
    }
  }

  def findUser(userId: Int): Option[User] = {
    userDatabase.get(userId)
  }

  def findAddress(addressId: Int): Option[Address] = {
    addressDatabase.get(addressId)
  }

  def main(args: Array[String]): Unit = {
    println(getPostalCodeResult(1)) // Success(150-0002)
    println(getPostalCodeResult(2)) // AddressNotHasPostalCode
    println(getPostalCodeResult(3)) // UserNotHasAddress
    println(getPostalCodeResult(4)) // UserNotFound
  }
}
```

getPostalCodeResultが鬼のようなmatch caseのネストになっていることがわかります。
このような可読性の低いコードを、Eitherを使って書きなおすことができます。

以下のように全てのfindメソッドをEitherでFailureをLeftに、正常取得できた場合の値の型をRightにして書き直します。

findの各段階でFailureオブジェクトに引き換えるという動きをさせるわけです。

リファクタリングした結果は以下のようになります。

```tut:silent
object MainRefactored {

  case class Address(id: Int, name: String, postalCode: Option[String])
  case class User(id: Int, name: String, addressId: Option[Int])

  val userDatabase: Map[Int, User] = Map (
    1 -> User(1, "太郎", Some(1)),
    2 -> User(2, "二郎", Some(2)),
    3 -> User(3, "プー太郎", None)
  )

  val addressDatabase: Map[Int, Address] = Map (
    1 -> Address(1, "渋谷", Some("150-0002")),
    2 -> Address(2, "国際宇宙ステーション", None)
  )

  sealed abstract class PostalCodeResult
  case class Success(postalCode: String) extends PostalCodeResult
  abstract class Failure extends PostalCodeResult
  case object UserNotFound extends Failure
  case object UserNotHasAddress extends Failure
  case object AddressNotFound extends Failure
  case object AddressNotHasPostalCode extends Failure

  // 本質的に何をしているかわかりやすくリファクタリング
  def getPostalCodeResult(userId: Int): PostalCodeResult = {
    (for {
      user <- findUser(userId).right
      address <- findAddress(user).right
      postalCode <- findPostalCode(address).right
    } yield Success(postalCode)).merge
  }

  def findUser(userId: Int): Either[Failure, User] = {
    userDatabase.get(userId).toRight(UserNotFound)
  }

  def findAddress(user: User): Either[Failure, Address] = {
    for {
      addressId <- user.addressId.toRight(UserNotHasAddress).right
      address <- addressDatabase.get(addressId).toRight(AddressNotFound).right
    } yield address
  }

  def findPostalCode(address: Address): Either[Failure, String] = {
    address.postalCode.toRight(AddressNotHasPostalCode)
  }

  def main(args: Array[String]): Unit = {
    println(getPostalCodeResult(1)) // Success(150-0002)
    println(getPostalCodeResult(2)) // AddressNotHasPostalCode
    println(getPostalCodeResult(3)) // UserNotHasAddress
    println(getPostalCodeResult(4)) // UserNotFound
  }
}

```

以上のようになり、

```scala
  def getPostalCodeResult(userId: Int): PostalCodeResult = {
    (for {
      user <- findUser(userId).right
      address <- findAddress(user).right
      postalCode <- findPostalCode(address).right
    } yield Success(postalCode)).merge
  }
```
getPostalCodeResultが本質的に何をしているのかが非常にわかりやすいコードとなりました。何をしているかというと、
Eitherではfor式を直接つかえないので`.right`というメソッドで、RightProjectionという型にして、
for式が利用できる形に変換しています。そのあと、mergeメソッドにより中身を畳み込んで取得しています。

[^product-with-serializable]: Scala2.11以前だと`Product with Serializable with scala.util.Either`というような変な型になりますが、実用上問題ないので、ひとまず気にしなくてよいです。この変な型になる問題はScala2.12以降では修正されています。 https://github.com/scala/scala/pull/4355 https://issues.scala-lang.org/browse/SI-9173
