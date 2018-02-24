# ScalaText

[![Build Status](https://travis-ci.org/dwango/scala_text.svg?branch=master)](https://travis-ci.org/dwango/scala_text)

このテキストはドワンゴの新入社員Scala研修のために作成されたものである。

## 成果物

このテキストのコンパイル済み成果物は次の場所から入手できる。

- HTML版：https://dwango.github.io/scala_text/
- PDF版：https://dwango.github.io/scala_text_pdf/scala_text.pdf
- EPUB版：https://dwango.github.io/scala_text/scala_text.epub

## 目的

新入社員研修でScalaを習得してもらうために使用する。

## ターゲット

ドワンゴの新入社員。少なくとも1つの言語（Java以外でも）に触れた（学校の授業程度でも）経験があることを前提とする。

## 想定される研修期間

詳細は未定。2015年は、ScalaとPlayの研修を両方合わせて、1日8時間の研修を2〜3週間程度行った。

## 執筆スタイル

Markdownで記述し、GitBookで静的サイトにしてGitHub Pages上に公開している。
原稿ファイルは `src/` 以下に配置されている。

### 執筆への参加

以下のコマンドで初期設定を行うことができる。

```sh
git clone https://github.com/dwango/scala_text
cd scala_text
npm install
```

原稿のビルドは以下のように行う。
ビルドされたページは`gitbook/_book/index.html`から見ることができる。

```
sbt textBuildHtmlQuick
```

その他にも、テキストの校正、リンク切れの確認などを行うコマンドが定義してある。
コマンドには全てtextというprefixが付いているので、どのようなコマンドがあるかは補完から知ることができる。

```
# 日本語の校正
sbt textLint src/introduction.md

# リンク切れ確認
sbt textLinkTest

# tutキャッシュなしのビルド
sbt textBuildHtml

# 全ての検査を実行した後にビルド
sbt textBuildAllWithCheck
```

### tut

[tut](https://github.com/tpolecat/tut)という、Scalaコードを書くと、そのコードのチェックや
元のソースとなるmarkdownファイルから、実行後の出力を付け加えたmarkdownに変換してくれるツールを使用している。
Scalaのコード例をテキスト中に書く場合は、使用可能な箇所では出来る限りtutを使うこと。
tut自体の具体的な使用方法は、tutのREADMEなどを参照すること。

### gitbookで特別視されるファイルについて

book.jsonで記述されているreadme, summaryはgitbookのビルド時に特別視される。
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

GitBookではEPUB形式にもビルドすることが出来る。
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
- [scala_text](https://github.com/dwango/scala_text)のissue欄およびpull requestへ 
  
### 誤りとはいえないが改善して欲しい点や加筆して欲しい点に関して
- [scala_text](https://github.com/dwango/scala_text)のissue欄へ
- 特に１節を超えるレベルの加筆修正については、pull reqeust を送られても、文体の統一や文章の継続したメンテナンスの観点等から、対応するのが難しいのでご了承いただけると助かります。

### その他全体的な感想や改善要望
- [専用issue](https://github.com/dwango/scala_text/issues/235)へ

## ライセンス

本文書は、[CC BY-NC-SA 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/deed.ja)

![CC-BY-NC-SA](https://licensebuttons.net/l/by-nc-sa/3.0/88x31.png)

の元で配布されています。
