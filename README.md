
# ScalaText
[![Discord](https://img.shields.io/badge/chat-on%20disord-darkslateblue.svg?logo=discord)](https://discord.gg/KHjnS5kQ8M)

このテキストは、Scala初学者がScalaを学ぶためのテキストである。ドワンゴの新入社員Scala研修のために作成されたものが、日本のScalaコミュニティに寄贈されたものとなる。

## 成果物

このテキストのコンパイル済み成果物は次の場所から入手可能である。

- HTML版：https://scala-text.github.io/scala_text/
- PDF版：https://scala-text.github.io/scala_text_pdf/scala_text.pdf
- EPUB版：https://scala-text.github.io/scala_text/scala_text.epub

## 目的

Scalaの初学者にScalaを習得してもらうために利用してもらうことを想定。

## ターゲット

Scalaの初学者で、少なくとも1つのプログラミング言語に触れた経験があることを前提としている。

## 執筆スタイル

Markdownで記述し、[HonKit](https://github.com/honkit/honkit)で静的サイトにしてGitHub Pages上に公開。原稿ファイルは `src/` 以下に配置。

### 執筆への参加

以下のコマンドで初期設定を行うことが可能。

```sh
git clone https://github.com/scala-text/scala_text
cd scala_text
npm install
```

原稿のビルドは以下のように行う。
ビルドされたページは`honkit/_book/index.html`から見ることができる。

```
sbt textBuildHtml
```

その他にも、テキストの校正、リンク切れの確認などを行うコマンドが定義してある。
コマンドには全てtextというprefixが付いているので、どのようなコマンドがあるかは補完から知ることができる。

```
# 日本語の校正
sbt textLint src/introduction.md

# リンク切れ確認
sbt textLinkTest

# mdocのビルド
sbt textBuildHtml

# 全ての検査を実行した後にビルド
sbt textBuildAllWithCheck
```

### mdoc

[mdoc](https://scalameta.org/mdoc/)という、Scalaコードを書くと、そのコードのチェックや
元のソースとなるmarkdownファイルから、実行後の出力を付け加えたmarkdownに変換してくれるツールを使用している。
Scalaのコード例をテキスト中に書く場合は、使用可能な箇所では出来る限りmdocを使うこと。
mdoc自体の具体的な使用方法は、mdocのREADMEなどを参照すること。

### honkitで特別視されるファイルについて

book.jsonで記述されているreadme, summaryはhonkitのビルド時に特別視される。
具体的には以下のような構成になっている。

- readme: Introductionとして本の先頭ページに配置される
- summary: 本のページ遷移情報に利用される

summaryを更新しないと**ページを追加しても本に反映されない**ので注意が必要。


## 校正ルール

textlintでルールを設定し原則としてこれに従う。
ルールの一例を以下に挙げる（自動でチェックされない項目は適宜レビューなどで修正を行う）。

### 見出しのレベル

この文書では次のように見出しを運用する。

- 記事のタイトルを`h1`として、タイトル以外では使わない
- 見出しレベル（`#`）は1つずつ増加させる
  - 例 `h2`の後に`h4`がきてはならない

### 括弧

地の文に現われる括弧は原則全角にする。
また、括弧の使用は例などなるべく短かいものにとどめ、
長くなる場合は脚注を使用する。

### ダブルクォート

強調などで半角の`"`を使うことがあるが、この記事では原則全角の`“`と`”`を用いる。

### ファイル名とディレクトリ名

ファイル名やディレクトリ名は原則_イタリック_にする。

### 句読点

- `「〜でした。」`のように、句点をカギ括弧の終端で使わず、`「〜でした」`とする
- 括弧を文章の終端で用いる場合、`〜です。（〜）`ではなく`〜です（〜）。`
  - 文章の最後が括弧になる場合、脚注を検討する
- 括弧内の終端では句点を打たない
  - 括弧内で句読点を使うような文章になる場合、括弧ではなく脚注を検討する

### 三点リーダ

- 三点リーダには`…`を用いて、原則2つ続けて使う
- 原則として、中黒`・`を三点リーダの用途で使ってはならない

## EPUBファイルの作成

[HonKit](https://github.com/honkit/honkit)ではEPUB形式にもビルドすることが出来る。
依存ライブラリとして[Calibre](https://calibre-ebook.com/)が必要なのでインストールする必要がある。
またcalibreの中の`ebook-convert`やnpmライブラリである`svgexport`にPATHが通っている必要があるので注意。

Macでのインストールは例えば以下のようになる。

```
brew cask install calibre
```

ビルドは以下のように行うことが出来る。

```
# svgexportにPATHが通っている必要がある(PATHを変更したくない場合はnpm install -g svgexportでも可)
PATH=node_modules/.bin:$PATH
sbt textBuildEpub
```

## フィードバック

### 誤字・脱字や技術的誤りの指摘・修正
- [scala_text](https://github.com/scala-text/scala_text)のissue欄およびpull requestへ 
  
### 誤りとはいえないが改善して欲しい点や加筆して欲しい点に関して
- [scala_text](https://github.com/scala-text/scala_text)のissue欄へ

### その他全体的な感想や改善要望
- [専用discussions](https://github.com/scala-text/scala_text/discussions/235)へ

## ライセンス

本文書は、[CC BY-NC-SA 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/deed.ja)

![CC-BY-NC-SA](https://licensebuttons.net/l/by-nc-sa/3.0/88x31.png)

の元で配布されています。ただし、CC-BY-NC-SAの「非営利」は[こちら](https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode.en#s3:~:text=NonCommercial%20provided%20there%20is%20no%20payment%20of%20monetary%20compensation%20in%20connection%20with%20the%20exchange.)にかかれている通り、

> NonCommercial means not primarily intended for or directed towards commercial advantage or monetary compensation. For purposes of this Public License, the exchange of the Licensed Material for other material subject to Copyright and Similar Rights by digital file-sharing or similar means is NonCommercial provided there is no payment of monetary compensation in connection with the exchange.

直接的な金銭のやり取りが発生するものであり、たとえば、社内の研修や社内での開発で利用する場合は営利に該当しません。

また、本資料を改変した場合の公開義務についても[こちら](https://wiki.creativecommons.org/wiki/ShareAlike_interpretation#:~:text=The%20ShareAlike%20condition%20only%20applies%20when%20a%20work%20is%20publicly%20shared)

> The ShareAlike condition only applies when a work is publicly shared. You are not obligated to share things you make from SA works--you may create remixes and adaptations that you do not publish. If you are using ShareAlike materials privately and not sharing them with others, you do not have to comply with the license conditions. For example, if you translate a ShareAlike work for internal use within your office, you do not have to license your translation under an SA or compatible license unless you plan to share it with others.

にある通りです。たとえば本資料を改変して社内のドキュメントのために使ったとしても、一般に公開しない限り（社内のみで利用するなど）改変部分について本ライセンスを継承する必要はなく自由に利用可能です。

これらの点について複数の問い合わせがあったため明記することにしました。