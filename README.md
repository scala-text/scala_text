# ScalaText

このテキストはドワンゴの新入社員Scala研修のために作成されたものである。

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
npm install
```

原稿のビルドは以下のように行う。
ビルドされたページは`gitbook/_book/index.html`から見ることができる。

```
sbt textBuildHtml
```

その他にも、テキストの校正、リンク切れの確認などを行うコマンドが定義してある
コマンドには全てtextというprefixが付いているので、どのようなコマンドがあるかは補完から知ることができる。

```
# 日本語の校正
sbt textLint src/introduction.md

# リンク切れ確認
sbt textLinkTest

# 全ての検査を実行した後にビルド
sbt textBuildAllWithCheck
```

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

## EPUB, PDF

gitbookではepubやpdf形式にもビルドすることが出来る。
依存ライブラリとして[Calibre](http://calibre-ebook.com/)が必要なのでインストールする必要がある。
またcalibreの中の`ebook-convert`やnpmライブラリである`svgexport`にPATHが通っている必要があるので注意。

macでのインストールは例えば以下のようになる。

```
brew cask install calibre
ln -s ~/Applications/calibre.app/Contents/console.app/Contents/MacOS/ebook-convert /usr/local/bin/ebook-convert
```

ビルドは以下のように行うことが出来る。

```
# svgexportにPATHが通っている必要がある(PATHを変更したくない場合はnpm install -g svgexportでも可)
PATH=node_modules/.bin:$PATH
sbt textBuildEpub
sbt textBuildPdf # 現在pdfはjenkins上でのビルドに問題があるので無効化しています。
```

## ライセンス

本文書は、[CC BY-NC-SA 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/deed.ja)

![CC-BY-NC-SA](https://licensebuttons.net/l/by-nc-sa/3.0/88x31.png)

の元で配布されています。
