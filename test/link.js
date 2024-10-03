import fs from "fs";
import qfs from "q-io/fs";
import qht from "q-io/http";
import path from "path";
import sleep from "sleep";
import assert from "power-assert";
import cheerio from "cheerio";

// honkitで使っているmarkdownパーサー
import kramed from "kramed";

// === util ====
function merge(as, bs) {
  let as2 = [].concat(as);
  as2.push(...bs);
  return as2;
}

function flatten(arrayOfArray) {
  let a = arrayOfArray.reduce((acc, arr) => merge(acc, arr));
  return a;
}

function identity(a) {
  return a;
}

function map(arrayA, aToB) {
  return arrayA.map(aToB);
}

function flatMap(arrayA, aToArrayB) {
  return flatten(map(arrayA, aToArrayB));
}

function partition(arr, p) {
  let ok = [], ng = [];
  for ( let a of arr) {
    if (p(a) === true) {
      ok.push(a);
    } else {
      ng.push(a);
    }
  }
  return [ok, ng];
}

function range(from, to) {
  return Array(...Array(to - from + 1)).map((_, i) => i + from);
}

function repeatBlank(num) {
  return range(0, num - 1).map(() => []);
}

function groupBy(as, f) {
  let xs = {};
  for (let a of as) {
    let group = f(a);
    if (xs[group]) {
      xs[group].push(a);
    } else {
      xs[group] = [a];
    }
  }
  return xs;
}

// transpose array of array
// [[1,2,3,4],       [[1,5,8],
//  [5,6,7  ],  ==>   [2,6  ],
//  [8      ]]        [3,7  ],
//                    [4    ]]
function transpose(arrayOfArray) {
  let rows = arrayOfArray.length;
  let cols = Math.max.apply(null, arrayOfArray.map((as) => as.length));

  let result = repeatBlank(cols);
  for (let c of range(0, cols - 1)) {
    for (let r of range(0, rows - 1)) {
      if (arrayOfArray[r][c]) {
        result[c].push(arrayOfArray[r][c]);
      }
    }
  }
  return result;
}

// fを基準にグルーピングした配列を作り、
// グループごとに１つずつ要素を集めた配列を作る。
// ex: 先頭一文字でグルーピングした場合(f = s.substr(0, 1))
//     [facebook, twitter, twilio, github, gitbook, gitter]
// =>  [[facebook], [twitter, twilio], [github, gitbook, gitter]]
// =>  [[facebook, twitter, github], [twilio, gitbook], [gitter]]
function groupByAndTranpose(strs, f) {
  let obj = groupBy(strs, f);
  let arr = Object.values(obj);
  return transpose(arr);
}

describe("test utils test", () => {
  it("is merge", () => {
    let a = [1];
    let b = [3, 4];
    assert.deepEqual(merge(a, b), [1, 3, 4]);
    assert.deepEqual(a, [1]);
    assert.deepEqual(b, [3, 4]);
  });
  it("is merge(array of array)", () => {
    let a = [[1]];
    let b = [[3, 4]];
    assert.deepEqual(merge(a, b), [[1], [3, 4]]);
    assert.deepEqual(a, [[1]]);
    assert.deepEqual(b, [[3, 4]]);
  });
  it("is flatten", () => {
    let x = [[1, 2], [3]];
    assert.deepEqual(flatten(x), [1, 2, 3]);
    assert.deepEqual(x, [[1, 2], [3]]);
  });
  it("is identity", () => {
    assert(identity(1) === 1);
  });
  it("is map", () => {
    assert.deepEqual(map([1, 2, 3], (x) => 2 * x), [2, 4, 6]);
  });
  it("is flatMap", () => {
    assert.deepEqual(flatMap([1, 2, 3], (x) => [2 * x, 3 * x]), [2, 3, 4, 6, 6, 9]);
  });
  it("is partition", () => {
    assert.deepEqual(partition([1, 2, 3, 4], (i) => i % 2 === 1), [[1, 3], [2, 4]]);
  });
  it("is range", () => {
    assert.deepEqual(range(3, 6), [3, 4, 5, 6]);
  });
  it("is repeatBlank", () => {
    assert.deepEqual(repeatBlank(3), [[], [], []]);
  });
  it("is groupBy", () => {
    assert.deepEqual(
      groupBy(["aa", "ab", "ba", "bb"], (str) => str[0]),
      {"a": ["aa", "ab"], "b": ["ba", "bb"]}
    );
  });
  it("is transpose", () => {
    assert.deepEqual(
      transpose([[1, 2, 3, 4], [5, 6, 7], [8]]),
      [[1, 5, 8], [2, 6], [3, 7], [4]]
    );
  });
  it("is groupByAndTranpose", () => {
    let domains = ["facebook", "twitter", "twilio", "github", "gitbook", "gitter"];
    assert.deepEqual(
      groupByAndTranpose(domains, (s) => s.substr(0, 1)),
      [["facebook", "twitter", "github"], ["twilio", "gitbook"], ["gitter"]]
    );
  });
});

// === util ====

// markdownをパースしてhtmlにしたあと
// スクレイピングできるようcheerioオブジェクトにする
function parseFile(filePath) {
  let f = fs.readFileSync(path.join(__dirname, filePath), "utf8");
  return cheerio.load(kramed(f));
}

function isUrl(link) {
  return link.startsWith("http");
}

// http://, https://を消す
function deleteProtocolFromUrl(url) {
  return url.replace(/https?:\/\//, "");
}

// 一番先頭のwwwを消す
function deleteFirstKUSA(url) {
  return url.replace(/^www\./, "");
}

/*
 * <a href="hoge.html">のような他の原稿に対するリンクがあるので、
 * src/hoge.mdに変換して存在確認を行えるようにする。
 * ただしexample_projects/.../mofu.scala.htmlのようなhtmlファイルを直接参照することがあるので
 * その場合は拡張子変換を行わない。
 */
function modifyLinkForTest(rawFilePath) {
  if (rawFilePath.includes("example_projects")) {
    return `src/${rawFilePath}`;
  }
  return `src/${rawFilePath}`.replace(/\.html$/, ".md");
}

/*
 * ファイル名を原稿用markdownがあるディレクトリへの相対パスに変換する。
 */
function relativePathFromTestDir(file) {
  return `../src/${file}`;
}

/*
 * localhost:9000などはserverを立てないと必ず404になるので
 * テスト対象から外す。
 */
function isBlackListedHost(url) {
  const BLACK_LIST = [
    "localhost",
    "127.0.0.1"
  ];
  let replaced = deleteProtocolFromUrl(url);
  return BLACK_LIST.map((b) => replaced.startsWith(b)).any(identity);
}

describe("Check links", () => {
  // src/以下の*.mdとREADME.mdの配列
  let targets = merge(
    ["../README.md"],
    fs.readdirSync("./src").filter((s) => s.endsWith(".md")).map(relativePathFromTestDir)
  );
  let parsed  = targets.map(parseFile);

  // <a href=と<img src=を検査。追加があったら↓に足す
  let hrefs = flatMap(parsed, ($) => $("a").get().map((a) => a.attribs.href));
  let imgs  = flatMap(parsed, ($) => $("img").get().map((i) => i.attribs.src));

  let links         = merge(hrefs, imgs);
  let [urls, files] = partition(links, isUrl);

  // 重複削除
  urls = [...new Set(urls)];

  // #から始まるリンクはページ内リンク（脚注）なので除外
  //
  // Scalaコードの一部をリンクとして検出してしまうので、特定の拡張子以外は除外
  let testFiles = files.filter((s) =>
    !s.startsWith("#") && (
      s.endsWith(".md") || s.endsWith(".scala") || s.endsWith(".sbt")
    )
  ).map(modifyLinkForTest);

  // src/../foo/barのようなリンクはビルド後は参照できなくなるのでそういったリンクがないことを確認する。
  it("should local files are in src directory", function() {
    testFiles.forEach((filePath) => {
      assert(qfs.join(filePath).startsWith("src"));
    });
  });

  it("should local files are exists", function(done) {
    this.timeout(2 * 1000);

    let promises  = testFiles.map((filePath) => {
      qfs.stat(filePath).then((stat) => {
        assert(stat.isFile());
        console.log(`ok: ${filePath}`);
      }, (e) => done(e));
    });

    Promise.all(promises).then(() => done(), (errors) => done(errors));
  });

  // 注意: HTTP通信するので外部のサーバーの状態に依存する。
  it("should url return successful response", function(done) {
    this.timeout(300 * 1000);

    // test対象のURL
    let testUrls =  urls.filter((u) => !isBlackListedHost(u));

    // 同時に同じドメインにアクセスするのを防ぐために
    // ドメインがばらけた配列を作る(先頭7文字が一致していたら同一ドメインとみなす)
    // 7文字 = akka.io/以下を同一ドメインとして認識できる、などの理由
    // 例:
    // 元の配列 [github/, github/, github/, twitter/ twitter/ facebeook/]
    // 結果    [[github/, twitter/ facebeook/],[github/, twitter/],[github/]]
    const DOMAIN_STR_NUM = 7;
    let testUrlArrays = groupByAndTranpose(
      testUrls, (s) => deleteFirstKUSA(deleteProtocolFromUrl(s)).substr(0, DOMAIN_STR_NUM)
    );

    // 先頭から順番にHTTPリクエストしてレスポンスを確かめる関数
    // 一度にMAX_CONNECTION個接続して、終わったら次のMAX_CONNECTION個を処理する。
    // Promise.allがすぐに制御を返すのでスタックはたまらないはず。
    // （testUrlArrays.length = 4000程度でもスタックオーバーフローしなかった）
    let totalSize = testUrls.length;
    function requestAsync(counter) {
      const MAX_CONNECTION = 35;

      // 今回処理する分を取得
      let requestUrls = testUrlArrays.shift();

      // 大量アクセスするとsocket hungするので、一定個数以上は元の配列に戻す
      if (requestUrls.length > MAX_CONNECTION) {
        let modosu = requestUrls.splice(MAX_CONNECTION);
        testUrlArrays.unshift(modosu);
      }

      let ps = requestUrls.map((u, index) => {
        let req = {url: u, timeout: 60 * 1000};
        return qht.request(req).then((res) => {
            // 失敗時にpower_assertに接続先URLを表示してもらうためにu &&をつけている
          assert(u && res.status >=  200);

            // redirectの先が見つからない可能性があるけど疲れたので省略
          assert(u && res.status <  400);

          console.log(`${counter + index}/${totalSize}: ${res.status} ${u}`);
        }, (e) => done(new Error(`${u}: ${e}`)));
      });
      return Promise.all(ps).then(() => {
        if (testUrlArrays.length > 0) {
          sleep.sleep(1);
          console.log("---");
          requestAsync(counter + requestUrls.length);
        } else {
          done();
        }
      }, (e) => done(e));
    }

    requestAsync(1);
  });
});
