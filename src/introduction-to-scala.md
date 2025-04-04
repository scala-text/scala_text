# Scalaとは

Scalaは2003年にスイス連邦工科大学ローザンヌ校（EPFL）のMartin Odersky教授によって開発されたプログラミング言語です。Scalaではオブジェクト指向と関数型プログラミングの
両方を行えるところに特徴があります。また、処理系はJVM上で動作するため[^not_only_jvm]、Java言語のライブラリのほとんどをシームレスに利用することができます。
ただし、Scalaはただのbetter Javaではないので、Scalaで効率的にプログラミングするためにはScalaの作法を知る必要があります。この文書がその一助になれば
幸いです。

## なぜ開発言語としてScalaを選ぶのか

なぜScalaを開発言語として選択するのでしょうか。ここではScalaの優れた点を見ていきたいと思います。

## オブジェクト指向プログラミングと関数型プログラミングの統合

Scalaという言語の基本的なコンセプトはオブジェクト指向と関数型の統合ですが、それはJavaをベースとしたオブジェクト指向言語の上に、関数型の機能を表現することで実現しています。

関数型プログラミングの第一の特徴は、関数を引数に渡したり、返り値にできたりする点ですが[^first_class]、Scalaの世界では関数はオブジェクトです。
一見メソッドを引数に渡しているように見えるところでも、そのメソッドを元に関数オブジェクトが生成されて渡されています。もちろんオブジェクト指向の世界でオブジェクトが第一級であることは自然なことです。
Scalaでは、関数をオブジェクトやメソッドと別の概念として導入するのではなく、関数を表現するオブジェクトとして導入することで「統合」しているわけです。

他にも、たとえばScalaの「case class」は、オブジェクト指向の視点ではValue Objectパターンに近いものと考えられますが、関数型の視点では代数的データ型として考えることができます。
また「implicit parameter」はオブジェクト指向の視点では暗黙的に型で解決される引数に見えますが、関数型の視点ではHaskellの型クラスに近いものと見ることができます。

このようにScalaという言語はオブジェクト指向言語に関数型の機能を足すのではなく、オブジェクト指向の概念で関数型の機能を解釈し取り込んでいます。
それにより必要以上に言語仕様を肥大化させることなく多様な関数型の機能を実現しています。
1つのプログラムをオブジェクト指向の視点と関数型の視点の両方で考えることはプログラミングに柔軟性と広い視野をもたらします。

### 関数型プログラミング

最近は関数リテラルを記述できるようにするなど関数型プログラミングの機能を取り込んだプログラミング言語が増えてきていますが、その中でもScalaの関数型プログラミングはかなり高機能であると言えるでしょう。

- case classによる代数的データ型
- 静的に網羅性がチェックされるパターンマッチ
- 型クラス
- forによるモナド構文
- モナドの型クラスの定義などに不可欠な高カインド型

以上のようにScalaでは単に関数が第一級であるだけに留まらず、本格的な関数型プログラミングをするための様々な機能があります。

また、Scalaはオブジェクトの不変性（immutability）を意識している言語です。
変数宣言は変更可能なvarと変更不可能なvalが分かれており、コレクションライブラリもmutableとimmutableでパッケージがわかれています[^mutable_and_immutable]。
case classもデフォルトではimmutableです。

不変性・参照透過性・純粋性は関数型プログラミングにおいて最も重要な概念と言われていますが、
近年、並行・並列プログラミングにおける利便性や性能特性の面で、不変性は関数型に限らず注目を集めており、研究も進んでいます。
その知見を応用できるのはScalaの大きな利点と言えるでしょう。

### オブジェクト指向プログラミング

Scalaが優れているのは関数型プログラミングの機能だけではありません。オブジェクト指向プログラミングにおいても様々な進化を遂げています。

- traitによるmixin
- 構造的部分型
- 型パラメータの変位（variance）指定
- self type annotationによる静的な依存性の注入
- implicit classあるいはextensionによる既存クラスの拡張
- Javaのプリミティブ型がラップされて、全ての値がオブジェクトとして扱える

以上のようにScalaではより柔軟なオブジェクト指向プログラミングが可能になっています。
Scalaのプログラミングでは特にtraitを使ったmixinによって、プログラムに高いモジュール性と、新しい設計の視点が得られるでしょう。

## Javaとの互換性

ScalaはJavaとの互換性を第一に考えられた言語です。Scalaの型やメソッド呼び出しはJavaと互換性があり、Javaのライブラリは普通にScalaから使うことができます。
大量にある既存のJavaライブラリを使うことができるのは大きな利点です。

またScalaのプログラムは基本的にJavaと同じようにバイトコードに変換され実行されるので、Javaと同等に高速で、性能予測が容易です。
コンパイルされたclassファイルをjavapコマンドを使ってどのようにコンパイルされたかを確認することもできます。

運用においてもJVM系のノウハウをそのまま使えることが多いです。実績があり、広く使われているJVMで運用できるのは利点になるでしょう。

## 非同期プログラミング、並行・分散プログラミング

Scalaでは非同期の計算を表現する[Future](https://www.scala-lang.org/api/current/scala/concurrent/Future.html)が標準ライブラリに含まれており、様々なライブラリで使われています。
非同期プログラミングにより、スレッド数を超えるようなクライアントの大量同時のアクセスに対応することができます。

また、他のシステムに問い合わせなければならない場合などにも、スレッドを占有することなく他のシステムの返答を待つことができます。
内部に多数のシステムがあり、外からの大量アクセスが見込まれる場合、Scalaの非同期プログラミングのサポートは大きなプラスになります。

[^not_only_jvm]: https://www.scala-native.org や https://www.scala-js.org/ といった、JVM以外の環境で動くものも存在します

[^first_class]: この特徴を関数が「第一級（first-class）」であると言います。

[^mutable_and_immutable]: https://docs.scala-lang.org/ja/overviews/collections/overview.html
