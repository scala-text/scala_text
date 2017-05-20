# Future/Promiseについて

FutureとPromiseは非同期プログラミングにおいて、終了しているかどうかわからない処理結果を抽象化した型です。Futureは未来の結果を表す型です。Promiseは一度だけ、成功あるいは失敗を表す、処理または値を設定することでFutureに変換できる型です。

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


## Futureとは

[Future](http://www.scala-lang.org/api/current/index.html#scala.concurrent.Future)とは、
非同期に処理される結果が入ったOption型のようなものです。
mapやflatMapやfilter、for式の適用といったようなOptionやListでも利用できる性質を持っています。

ライブラリやフレームワークの処理が非同期主体となっている場合、
このFutureは基本的で重要な役割を果たすクラスとなります。

なおJavaにも[Future](http://docs.oracle.com/javase/jp/8/docs/api/java/util/concurrent/Future.html)というクラスがありますが、
こちらには関数を与えたり[^CompletableFuture]、Optionの持つ特性はありません。
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

  f.foreach { case s: String =>
    println(s)
  }

  println(f.isCompleted) // false

  Thread.sleep(5000) // Hello future!

  println(f.isCompleted) // true

  val f2: Future[String] = Future {
    Thread.sleep(1000)
    throw new RuntimeException("わざと失敗")
  }

  f2.failed.foreach { case e: Throwable =>
    println(e.getMessage)
  }

  println(f2.isCompleted) // false

  Thread.sleep(5000) // わざと失敗

  println(f2.isCompleted) // true
}
```

出力結果は、

```
false
Hello future!
true
false
わざと失敗
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

  f.foreach { case s: String =>
    println(s"[ThreadName] In Success: ${Thread.currentThread.getName}")
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
[ThreadName] In Success: ForkJoinPool-1-worker-5
Hello future!
```

となります。以上のコードではそれぞれのスレッド名を各箇所について出力してみました。
非常に興味深い結果ですね。`Future`と`foreach`に渡した関数に関しては、
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
なお先ほどと違ってこの度は、`foreach`ではなく`onComplete`を利用して成功と失敗の両方の処理を記述しました。

以上の実装のようにFutureは結果をOptionのように扱うことができるわけです。
無論mapも使えますがOptionがネストしている場合にflatMapを利用できるのと同様に、
flatMapもFutureに対して利用することもできます。
つまりmapの中での実行関数がさらにFutureを返すような場合も問題なくFutureを利用していけるのです。
`val futureSec: Future[Double] = futureMilliSec.map(i => i.toDouble / 1000)`を
上記のミリ秒を秒に変換する部分を100ミリ秒はかかる非同期のFutureにしてみた例は以下のとおりです。


```scala
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
    first <- futureFirst
    second <- futureSecond
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


## Promiseとは

[Promise](http://www.scala-lang.org/api/current/index.html#scala.concurrent.Promise)とは、

成功あるいは失敗を表す値を設定することによってFutureに変換することのできるクラスです。 実際にサンプルコードを示します。

```tut:silent
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Promise, Future}
import scala.concurrent.duration._
import scala.util.{Success, Failure}

object PromiseSample extends App {
  val promiseGetInt: Promise[Int] = Promise[Int]
  val futureByPromise: Future[Int] = promiseGetInt.future // PromiseからFutureを作ることが出来る

  // Promiseが解決されたときに実行される処理をFutureを使って書くことが出来る
  val mappedFuture = futureByPromise.map { i =>
    println(s"Success! i: ${i}")
  }

  // 別スレッドで何か重い処理をして、終わったらPromiseに値を渡す
  Future {
    Thread.sleep(300)
    promiseGetInt.success(1)
  }

  Await.ready(mappedFuture, 5000.millisecond)
}
```

この処理は必ず`Success! i: 1`という値を表示します。
このようにPromiseに値を渡すことで（Promiseから生成した）Futureを完了させることができます。

上の例はPromise自体の動作説明のためにFuture内でPromiseを使っています。通常はFutureの返り値を利用すればよいため、今の使い方ではあまりメリットがありません。
そこで今度はPromiseのよくある使い方の例として、callbackを指定するタイプの非同期処理をラップしてFutureを返すパターンを紹介します。

下記の例では、CallBackSomethingをラップしたFutureSomethingを定義しています。 `doSomething` の中でPromiseが使われていることに注目してください。

```tut:silent
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Random, Success}

class CallbackSomething {
  val random = new Random()

  def doSomething(onSuccess: Int => Unit, onFailure: Throwable => Unit): Unit = {
    val i = random.nextInt(10)
    if(i < 5) onSuccess(i) else onFailure(new RuntimeException(i.toString))
  }
}

class FutureSomething {
  val callbackSomething = new CallbackSomething

  def doSomething(): Future[Int] = {
    val promise = Promise[Int]
    callbackSomething.doSomething(i => promise.success(i), t => promise.failure(t))
    promise.future
  }
}

object CallbackFuture extends App {
  val futureSomething = new FutureSomething

  val iFuture = futureSomething.doSomething()
  val jFuture = futureSomething.doSomething()

  val iplusj = for {
    i <- iFuture
    j <- jFuture
  } yield i + j

  val result = Await.result(iplusj, Duration.Inf)
  println(result)
}
```

「Promiseには成功/失敗した時の値を設定できる」「PromiseからFutureを作ることが出来る」という2つの性質を利用して、
callbackをFutureにすることができました。

callbackを使った非同期処理は今回のような例に限らず、Httpクライアントで非同期リクエストを行う場合などで必要になることがあります。
柔軟なエラー処理が必要な場合、callbackよりFutureの方が有利な場面があるため、Promiseを使って変換可能であることを覚えておくとよいでしょう。

### 演習： カウントダウンラッチ

それでは、演習をやってみましょう。
FutureやPromiseの便利な特性を利用して、0〜1000ミリ秒間のランダムな時間を待つ8個のFutureを定義し、
そのうちの3つが終わり次第すぐにその3つの待ち時間を全て出力するという実装をしてみましょう。
なお、この動きは、Javaの並行処理のためのユーティリティである、
[CountDownLatch](http://docs.oracle.com/javase/jp/8/docs/api/java/util/concurrent/CountDownLatch.html)というクラスの
動きの一部を模したものとなります。

<!-- begin answer id="answer_ex1" style="display:none" -->

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
  futures.foreach { f => f.foreach {case waitMilliSec =>
    val index = indexHolder.getAndIncrement
    if(index < promises.length) {
      promises(index).success(waitMilliSec)
    }
  }}
  promises.foreach { p => p.future.foreach { case waitMilliSec => println(waitMilliSec)}}
  Thread.sleep(5000)
}
```

<!-- end answer -->

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

[^CompletableFuture]: ただし、Java 8から追加されたjava.util.concurrent.Futureのサブクラスである[CompletableFuture](http://docs.oracle.com/javase/jp/8/docs/api/java/util/concurrent/CompletableFuture.html)には、関数を引数にとるメソッドがあります。
[^concurrent]: 値の原始的な更新や同期の必要性などの並行処理に関する様々な話題の詳細な解説は本書の範囲をこえてしまうため割愛します。「Java Concurrency in Practice」ないしその和訳「Java並行処理プログラミング ー その「基盤」と「最新API」を究める」や「Effective Java」といった本でこれらの話題について学ぶことが出来ます。
