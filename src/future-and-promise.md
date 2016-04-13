# Future/Promiseについて（★★★）

FutureとPromiseは非同期プログラミングにおいて、終了しているかどうかわからない処理結果を抽象化した型です。
Futureは未来の結果を表す型であり、Promiseは一度だけ成功と定義した値または値と定義した処理を与えることでFutureに変換できる型です。

JVM系の言語では、マルチスレッドで並行処理を使った非同期処理を行うことが多々あります。
無論ブラウザ上のJavaScriptのようなシングルスレッドで行うような非同期処理もありますが、
マルチスレッドで行う非同期処理は定義した処理群が随時行われるのではなく、
マルチコアのマシンならば大抵の場合、複数のCPUで別々に実行されることとなります。

具体的に非同期処理が行われている例としては、UIにおける読み込み中のインジゲーターなどがあげられます。
読み込み中のインジゲーターがアニメーションしている間も、ダイアログを閉じたり、
別な操作をすることができるのは、読み込み処理が非同期でおこなわれているからです。

なお、このような特定のマシンの限られたリソースの中で、
マルチスレッドやマルチプロセスによって順不同もしくは同時に処理を行うことを、
並行（Concurrent）処理といいます。
マルチスレッドの場合はプロセスとメモリ空間とファイルハンドラを複数のスレッドで共有し、
マルチプロセスの場合はメモリ管理は別ですがCPUリソースを複数のプロセスで共有しています。
（注、スレッドおよびプロセスのような概念については知っているものとみなして説明していますのでご了承ください）

リソースが共有されているかどうかにかかわらず、完全に同時に処理を行っていくことを
、並列（Parallel）処理といいます。
大抵の場合、複数のマシンで分散実行させるような分散系を利用したスケールするような処理を並列処理系と呼びます。

このたびはこのような並行処理を使った非同期処理を行った場合に、
とても便利なFutureとPromiseというそれぞれのクラスの機能と使い方について説明を行います。


## Futureとは（★★★）

[Future](http://www.scala-lang.org/api/current/index.html#scala.concurrent.Future)とは、
非同期に処理される結果が入ったOption型のようなものです。
その処理が終わっているのかどうかや（isCompleted）、正しく終わった時の処理を適用する（onSuccess）、
例外が起こったときの処理を適用する（onFailure）といったことが可能な他、
flatMapやfilter、for式の適用といったようなOptionやListでも利用できる性質も持ち合わせています。

ライブラリやフレームワークの処理が非同期主体となっている場合、
このFutureは基本的で重要な役割を果たすクラスとなります。

なおJavaにも[Future](http://docs.oracle.com/javase/jp/8/docs/api/java/util/concurrent/Future.html)というクラスがありますが、
こちらには関数を与えたり、Optionの持つ特性はありません。
また、ECMAScript 6にある[Promise](https://developer.mozilla.org/ja/docs/Web/JavaScript/Reference/Global_Objects/Promise)
という機能がありますが、そちらの方がScalaのFutureの機能に似ています。
このECMAScript 6のPromiseとScalaのPromiseは、全く異なる機能であるため注意が必要です。

実際のコード例を見てみましょう。

```tut:silent
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object FutureSample extends App {

  val s = "Hello"
  val f: Future[String] = Future {
    Thread.sleep(1000)
    s + " future!"
  }

  f.onSuccess { case s: String =>
    println(s)
  }

  println(f.isCompleted) // false

  Thread.sleep(5000) // Hello future!

  println(f.isCompleted) // true
}
```

出力結果は、

```
false
Hello future!
true
```

のようになります。

以上はFuture自体の機能を理解するためのサンプルコードです。
非同期プログラミングは、sbt consoleで実装するのが難しいのでファイルに書かせてもらいました。
Futureシングルトンは関数を与えるとその関数を非同期に与える`Future[+T]`を返します。
上記の実装例ではまず、1000ミリ秒待機して、`"Hello"`と`" future!"`を文字列結合するという処理を非同期に処理します。
そして成功時の処理を定義した後futureが処理が終わっているかを確認し、
futureの結果取得を5000ミリ秒間待つという処理を行った後、
その結果がどうなっているのかをコンソールに出力するという処理をします。

なお以上のように5000ミリ秒待つという他に、そのFuture自体の処理を待つという書き方もすることができます。
`Thread.sleep(5000)`を`Await.ready(f, 5000 millisecond)`とすることで、
Futureが終わるまで最大5000ミリ秒を待つという書き方となります。
ただし、この書き方をする前に、

```tut:silent
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
```

以上をimport文に追加する必要があります。さらにこれらがどのように動いているのかを、スレッドの観点から見てみましょう。
以下のようにコードを書いてみます。

```tut:silent
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object FutureSample extends App {

  val s = "Hello"
  val f: Future[String] = Future {
    Thread.sleep(1000)
    println(s"[ThreadName] In Future: ${Thread.currentThread.getName}")
    s + " future!"

  }

  f.onSuccess { case s: String =>
    println(s"[ThreadName] In onSuccess: ${Thread.currentThread.getName}")
    println(s)
  }

  println(f.isCompleted) // false

  Await.ready(f, 5000 millisecond) // Hello future!

  println(s"[ThreadName] In App: ${Thread.currentThread.getName}")
  println(f.isCompleted) // true
}
```

この実行結果については、

```
false
[ThreadName] In Future: ForkJoinPool-1-worker-5
[ThreadName] In App: main
true
[ThreadName] In onSuccess: ForkJoinPool-1-worker-5
Hello future!
```

となります。以上のコードではそれぞれのスレッド名を各箇所について出力してみました。
非常に興味深い結果ですね。`Future`と`onSuccess`に渡した関数に関しては、
`ForkJoinPool-1-worker-5`というmainスレッドとは異なるスレッドで実行されています。

つまりFutureを用いることで知らず知らずのうちのマルチスレッドのプログラミングが実行されていたということになります。
また、`Await.ready(f, 5000 millisecond)`で処理を書いたことで、`isCompleted`の確認処理のほうが、
`"Hello future!"`の文字列結合よりも先に出力されていることがわかります。
これは文字列結合の方が値参照よりもコストが高いためこのようになります。

ForkJoinPoolに関しては、Javaの並行プログラミングをサポートする`ExecutorService`というインタフェースを被ったクラスとなります。
内部的にスレッドプールを持っており、スレッドを使いまわすことによって、スレッドを作成するコストを低減し高速化を図っています。

Futureについての動きがわかった所で、FutureがOptionのように扱えることも説明します。

```tut:silent
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Random, Success}

object FutureOptionUsageSample extends App {
  val random = new Random()
  val waitMaxMilliSec = 3000

  val futureMilliSec: Future[Int] = Future {
    val waitMilliSec = random.nextInt(waitMaxMilliSec);
    if(waitMilliSec < 1000) throw new RuntimeException(s"waitMilliSec is ${waitMilliSec}" )
    Thread.sleep(waitMilliSec)
    waitMilliSec
  }

  val futureSec: Future[Double] = futureMilliSec.map(i => i.toDouble / 1000)

  futureSec onComplete {
    case Success(waitSec) => println(s"Success! ${waitSec} sec")
    case Failure(t) => println(s"Failure: ${t.getMessage}")
  }

  Thread.sleep(3000)
 }
```

出力例としては、`Success! 1.538 sec`や`Failure: waitMilliSec is 971`というものになります。
この処理では、3000ミリ秒を上限としたランダムな時間を待ってその待ったミリ秒を返すFutureを定義しています。
ただし、1000ミリ秒未満しか待たない場合には失敗とみなし例外を投げます。
この最初にえられるFutureを`futureMilliSec`としていますが、その後、`map`メソッドを利用して`Int`のミリ秒を`Doubule`の秒に変換しています。
なお先ほどと違ってこの度は、`onSuccess`ではなく`onComplete`を利用して成功と失敗の両方の処理を記述しました。

以上の実装のようにFutureは結果をOptionのように扱うことができるわけです。
無論mapも使えますがOptionがネストしている場合にflatMapを利用できるのと同様に、
flatMapもFutureに対して利用することもできます。
つまりmapの中での実行関数がさらにFutureを返すような場合も問題なくFutureを利用していけるのです。
`val futureSec: Future[Double] = futureMilliSec.map(i => i.toDouble / 1000)`を
上記のミリ秒を秒に変換する部分を100ミリ秒はかかる非同期のFutureにしてみた例は以下のとおりです。


```
  val futureSec: Future[Double] = futureMilliSec.flatMap(i => Future {
    Thread.sleep(100)
    i.toDouble / 1000
  })
```

mapで適用する関数でOptionがとれてきてしまうのをflattenできるという書き方と同じように、
Futureに適用する関数の中でさらにFutureが取得できるような場合では、flatMapが適用できます。
この書き方のお陰で非常に複雑な非同期処理を、比較的シンプルなコードで表現してやることができるようになります。


### Futureを使って非同期に取れてくる複数の結果を利用して結果を作る

さて、flatMapが利用できるということは、for式も利用できます。
これらはよく複数のFutureを組み合わせて新しいFutureを作成するのに用いられます。
実際に実装例を見てみましょう。


```tut:silent
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success, Random}

object CompositeFutureSample extends App {
  val random = new Random()
  val waitMaxMilliSec = 3000

  def waitRandom(futureName: String): Int = {
    val waitMilliSec = random.nextInt(waitMaxMilliSec);
    if(waitMilliSec < 500) throw new RuntimeException(s"${futureName} waitMilliSec is ${waitMilliSec}" )
    Thread.sleep(waitMilliSec)
    waitMilliSec
  }

  val futureFirst: Future[Int] = Future { waitRandom("first") }
  val futureSecond: Future[Int] = Future { waitRandom("second") }

  val compositeFuture: Future[(Int, Int)] = for {
    first: Int <- futureFirst
    second: Int <- futureSecond
  } yield (first, second)

  compositeFuture onComplete  {
    case Success((first, second)) => println(s"Success! first:${first} second:${second}")
    case Failure(t) => println(s"Failure: ${t.getMessage}")
  }

  Thread.sleep(5000)
 }
```

先ほど紹介した例に似ていますが、ランダムで生成した最大3秒間待つ関数を用意し、500ミリ秒未満しか待たなかった場合は失敗とみなします。
その関数を実行する関数をFutureとして2つ用意し、それらをfor式で畳み込んで新しいFutureを作っています。
そして最終的に新しいFutureに対して成功した場合と失敗した場合を出力します。

出力結果としては、`Success! first:1782 second:1227`や`Failure: first waitMilliSec is 412`や
`Failure: second waitMilliSec is 133`といったものとなります。

なおFutureにはfilterの他、様々な並列実行に対するメソッドが存在しますので、
[APIドキュメント](http://www.scala-lang.org/api/current/index.html#scala.concurrent.Future)を見てみてください。
また複数のFuture生成や[並列実行に関してのまとめられた日本語の記事](http://qiita.com/mtoyoshi/items/297f6acdfe610440c719)もありますので、
複雑な操作を試してみたい際にはぜひ参考にしてみてください。


## Promiseとは（★）
[Promise](http://www.scala-lang.org/api/current/index.html#scala.concurrent.Promise)とは、
一度だけ成功と定義した値か失敗と定義した値かTryオブジェクトやFutureなどを与えることによってFutureに変換することのできるクラスです。
そのため、Promiseはそれ自体が、一瞬可変オブジェクトのような振る舞いをします。
なかなかわかりにくいかと思いますので、実際にサンプルコードを示します。

```tut:silent
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Promise, Future}
import scala.util.{Success, Failure, Random}

object PromiseSample extends App {
  val random = new Random()
  val promiseGetInt: Promise[Int] = Promise[Int]

  val futureGetInt: Future[Int] = promiseGetInt.success(1).future

  futureGetInt.onComplete {
    case Success(i) => println(s"Success! i: ${i}")
    case Failure(t) => println(s"Failure! t: ${t.getMessage}")
  }

  Thread.sleep(1000)
}
```

この処理は必ず`Success! i: 1`という値を返します。
`promiseGetInt.success(1).future`を`promiseGetInt.future`のように成功結果を与えないような処理にした場合には、
onCompleteが呼ばれることはないため、何も出力されません。

なおこの1度だけしか結果が適用されないという特性を活かして、Futureを組み合わせてものが実装できます。
複数successが定義される場合には、successメソッドの場合にtrySuccessというPromiseのメソッドを利用します。
successを利用して複数回成功した値を定義した場合には、例外`IllegalStateException`が投げられます。
では早速実装例を見てみましょう。


```tut:silent
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Random, Success}

object PromiseFutureCompositionSample extends App {
  val random = new Random()
  val promiseGetInt: Promise[Int] = Promise[Int]

  val firstFuture: Future[Int] = Future {
    Thread.sleep(100)
    1
  }
  firstFuture.onSuccess{ case i => promiseGetInt.trySuccess(i)}

  val secondFuture: Future[Int] = Future {
    Thread.sleep(200)
    2
  }
  secondFuture.onSuccess{ case i => promiseGetInt.trySuccess(i)}

  val futureGetInt: Future[Int] = promiseGetInt.future

  futureGetInt.onComplete {
    case Success(i) => println(s"Success! i: ${i}")
    case Failure(t) => println(s"Failure! t: ${t.getMessage}")
  }

  Thread.sleep(1000)
}
```

結果は必ず、`Success! i: 1`が表示されます。
100ミリ秒待って1を返すfirstFutureと、200ミリ秒待って2を返す`secondFuture`が定義されています。
時系列的に、`firstFuture`がほとんどの場合promiseのfutureを完成させる役割をします。そのため必ず出力結果は1となるわけです。
Promiseは、このように非同期の結果を受け取ったり組み合わせたりするためのプレースホルダとしての部品の役割を果たしています。


### 演習： カウントダウンラッチ

それでは、演習をやってみましょう。
FutureやPromiseの便利な特性を利用して、0〜1000ミリ秒間のランダムな時間を待つ8個のFutureを定義し、
そのうちの3つが終わり次第すぐにその3つの待ち時間を全て出力するという実装をしてみましょう。
なお、この動きは、Javaの並行処理のためのユーティリティである、
[CountDownLatch](http://docs.oracle.com/javase/jp/8/docs/api/java/util/concurrent/CountDownLatch.html)というクラスの
動きの一部を模したものとなります。


### 解答例： カウントダウンラッチ

解答例は以下のとおりです。

```tut:silent
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Promise, Future}
import scala.util.Random

object CountDownLatchSample extends App {
  val indexHolder = new AtomicInteger(0)
  val random = new Random()
  val promises: Seq[Promise[Int]] = for {i <- 1 to 3} yield Promise[Int]
  val futures: Seq[Future[Int]] = for {i <- 1 to 8} yield Future[Int] {
    val waitMilliSec = random.nextInt(1000)
    Thread.sleep(waitMilliSec)
    waitMilliSec
  }
  futures.foreach { f => f.onSuccess {case waitMilliSec =>
    val index = indexHolder.getAndIncrement
    if(index < promises.length) {
      promises(index).success(waitMilliSec)
    }
  }}
  promises.foreach { p => p.future.onSuccess{ case waitMilliSec => println(waitMilliSec)}}
  Thread.sleep(5000)
}
```

上記のコードを簡単に説明すると、指定された処理を行うFutureの配列を用意し、それらがそれぞれ成功した時に
AtomicIntegerで確保されているindexをアトミックにインクリメントさせながら、
Promiseの配列のそれぞれに成功結果を定義しています。
そして、最後にPromiseの配列から作り出した全てのFutureに対して、コンソールに出力をさせる処理を定義します。
基本的なFutureとPromiseを使った処理で表現されていますが、ひとつ気をつけなくてはいけないのはAtomicIntegerの部分です。
これはFutureに渡した関数の中では、同じスレッドが利用されているとは限らないために必要となる部分です。
別なスレッドから変更される値に関しては、値を原子的に更新するようにコードを書かなければなりません。
プリミティブな値に関して原子的な操作を提供するのが
[AtomicInteger](http://docs.oracle.com/javase/jp/8/docs/api/java/util/concurrent/atomic/AtomicInteger.html)というJavaのクラスとなります。[^concurrent]
以上が解答例でした。

ちなみに、このような複雑なイベント処理は既にJavaの[concurrentパッケージ](http://docs.oracle.com/javase/jp/8/docs/api/java/util/concurrent/package-summary.html)に
いくつか実装があるので実際の利用ではそれらを用いることもできます。
またもっと複雑なイベントの時間毎の絞込みや合成、分岐などをする際には
[RxScala](http://reactivex.io/rxscala/)というイベントストリームを専門に
取り扱うライブラリを利用することができます。
このRxは元々はC#で生まれたReactive Extensionsというライブラリで、
現在では[様々な言語にポーティング](https://github.com/Reactive-Extensions)が行われています。

まだ実装時間に余裕がある場合は、[Semaphore](http://docs.oracle.com/javase/jp/8/docs/api/java/util/concurrent/Semaphore.html)（セマフォ）という常に特定個数ずつしか実行されない処理を実装してみましょう。

[^concurrent]: 値の原始的な更新や同期の必要性などの並行処理に関する様々な話題の詳細な解説は本書の範囲をこえてしまうため割愛します。「Java Concurrency in Practice」ないしその和訳「Java並行処理プログラミング ー その「基盤」と「最新API」を究める」や「Effective Java」といった本でこれらの話題について学ぶことが出来ます。
