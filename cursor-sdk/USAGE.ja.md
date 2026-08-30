# 脆弱性オーケストレータ（使い方）

English: [USAGE.md](./USAGE.md)

構成とファイルの役割は [ARCHITECTURE.ja.md](./ARCHITECTURE.ja.md) を参照。
シーケンスは [SEQUENCE.ja.md](./SEQUENCE.ja.md) を参照。

GitLab CI / GitHub Actions の Trivy レポートを受け取り、Cursor SDK でトリアージ → 方針判定 → 修正 MR / PR まで進めるプロトタイプです。スキャンジョブ自体は CRITICAL が出ても失敗しません。修正は非同期です。

どちらのホスト上かは CI の環境変数（`GITLAB_CI` / `GITHUB_ACTIONS`）から自動判定するので、CLI は同じものが動きます。変わるのは repo URL、開始 ref、プロンプト内の MR / PR という呼び方だけです。

## 全体像

```
Trivy レポート
    → Agent.create（トリアージ、読み取り中心）
    → policy.ts（コードで判定。エージェントには任せない）
    → Agent.create（修正。cloud なら autoCreatePR）
    → Agent.create（影響分析。MR / PR / Issue 本文に英語で追記。コードは触らない）
    → CI 失敗時は Agent.resume（同じエージェントの続き）
```

SDK を呼ぶのはこのディレクトリの CLI だけです。Policy 判定とレポート整形は通常の TypeScript です。

## 必要要件

- Node.js 22.13 以上
- [Trivy](https://trivy.dev/)（ローカルスキャン用。macOS なら `brew install trivy`）
- [Cursor Dashboard](https://cursor.com/dashboard/api) の `CURSOR_API_KEY`（ユーザーキーまたはサービスアカウントキー）
- cloud ランタイムを使う場合: Cursor からこのリポジトリ（GitLab / GitHub）へアクセスできること

## インストール

```bash
cd cursor-sdk
npm ci
```

## どこで何を実行するか

ローカルは早く知るため、CI は正式なスキャン結果から MR / PR を出すため、です。手元の Trivy は CI と一致するとは限らないので、ローカルでは MR も PR も開きません。

| 場所 | やること | やらないこと |
| --- | --- | --- |
| ローカル | Trivy → トリアージ → Policy。ログと state ファイル | MR / PR を開く（`--skip-remediate`。`--runtime local`） |
| CI | 同じフローのあと、Policy を通ったものだけ cloud で修正 MR（GitLab）/ PR（GitHub）。続けて影響分析を英語でその本文に追記 | スキャン失敗でパイプラインを落とすこと |

リポジトリルート、または `cursor-sdk/` のどちらからでも実行できます。`--cwd` を省略すると `pom.xml` のあるリポジトリルートを探します。

### ローカルで実行するコマンド

`CURSOR_API_KEY` はシェルに export しておく（`~/.zshrc` など）。値はリポジトリに置かない。

スキャンだけ:

```bash
cd cursor-sdk
npm run scan
```

CRITICAL / HIGH / MEDIUM、Trivy ネイティブ JSON、検出しても exit 0。自動修正は Policy が CRITICAL だけ通す。`cursor-sdk/` は対象外。レポートはリポジトリルートの `gl-trivy-fs-report.json`。依存解決を省略するなら `bash scripts/trivy-scan.sh --skip-resolve`。

スキャンからトリアージまで（推奨）:

```bash
cd cursor-sdk
npm run scan:run
```

中身は次と同じです。`--runtime local` と `--skip-remediate` が付くので、修正エージェントも MR も動きません。

```bash
cd cursor-sdk
npx tsx src/cli.ts run \
  --report ../gl-trivy-fs-report.json \
  --runtime local \
  --skip-remediate
```

サンプル JSON で CLI だけ試す:

```bash
cd cursor-sdk
npx tsx src/cli.ts run \
  --report fixtures/gl-trivy-fs-report.sample.json \
  --runtime local \
  --skip-remediate
```

出力はターミナルの Policy 行と `.cursor/vuln-orchestrator-state.json` です。作業ツリーは変わりません。

### CI で実行されるコマンド

人が手元で `--runtime cloud` を叩く必要はありません。`CURSOR_API_KEY` が入った push パイプラインで、オーケストレータジョブが次を実行します。

GitLab CI — `.gitlab-ci.yml` の `sdk-vuln-orchestrator`:

```bash
cd cursor-sdk
npm ci
npx tsx src/cli.ts run \
  --report "$CI_PROJECT_DIR/gl-trivy-fs-report.json" \
  --runtime cloud
```

GitHub Actions — `.github/workflows/security-scan.yml` の `sdk-vuln-orchestrator`:

```bash
cd cursor-sdk
npm ci
npx tsx src/cli.ts run \
  --report "$GITHUB_WORKSPACE/gl-trivy-fs-report.json" \
  --runtime cloud
```

コマンドは同じで、ワークスペースを指す変数だけが違います。`--skip-remediate` は付けません。入力は同じパイプライン / run の Trivy レポート成果物です。`CURSOR_REPO_URL` と `CURSOR_STARTING_REF` はホスト側の変数から取ります（GitLab は `$CI_PROJECT_URL` / `$CI_COMMIT_SHA`、GitHub は `$GITHUB_SERVER_URL/$GITHUB_REPOSITORY` / `$GITHUB_REF_NAME`）。cloud かつ Policy 通過時だけ `autoCreatePR` で MR（GitLab）または PR（GitHub）が開き、続けて影響分析エージェントが Release Notes と利用箇所を見て、英語で **MR / PR 本文**（クローズ対象の Issue があればその本文にも）追記します。コメントは付けません。分析に失敗しても修正 MR / PR は残します。

オーケストレータが落ちてもマージは止まりません（GitLab は `allow_failure: true`、GitHub は `continue-on-error: true`）。state は成果物 `.cursor/vuln-orchestrator-state.json` に残ります。

GitLab でのセットアップ:

1. **Settings → CI/CD → Variables** に `CURSOR_API_KEY` を追加（Masked。Protected にするなら protected ブランチの push だけが対象）
2. 既存の Cursor Automation（`CURSOR_TRIVY_WEBHOOK_URL` / `CURSOR_TRIVY_API_KEY`）は無効化。両方有効だと MR が二重になる
3. `main` へ push する

GitHub でのセットアップ:

1. **Settings → Secrets and variables → Actions → New repository secret** に `CURSOR_API_KEY` を追加。未設定ならオーケストレータジョブのステップはスキップされ、ワークフローは成功のまま終わる
2. このリポジトリを Cursor に接続する。修正ブランチを push して PR を開くのは `GITHUB_TOKEN` ではなく Cloud Agent 自身なので、Cursor の GitHub App に書き込み権限が必要
3. 任意: **Settings → Code security** で Code scanning を有効にすると Trivy の SARIF が Security タブに出る。無効でもアップロードのステップは `continue-on-error: true` なので失敗扱いにはならない
4. `main` へ push する

GitHub 側だけの挙動:

- `push` トリガーは `cursor/**` を除外している。エージェントが作った修正ブランチでオーケストレータを再度回さないため。修正 PR の build / scan は `pull_request` トリガーで走る
- オーケストレータジョブは `push` と手動の **Run workflow**（`workflow_dispatch`）で動く（`pull_request` では動かない）
- Trivy が `trivy-results.sarif` も出し、`github/codeql-action/upload-sarif` で Code scanning へ上げる（GitLab 側に SARIF のステップはない）
- スキャンジョブは JAR 成果物を取り込まない。`--skip-dirs target` で対象外だから意味がなく、効くのは `./mvnw dependency:resolve` で埋めた `~/.m2` の方
- 開始 ref はコミット SHA ではなくブランチ名（`github.ref_name`）。Cursor の ref 検証が push 直後の SHA を `Failed to verify existence of commit ...` で弾くことがあり、ブランチ名なら通るため。冪等キーは `GITHUB_SHA` のままなので、同じコミットで二重に起動することはない
- 修正 PR の作成者は使うキーで変わる。SDK の `openAsCursorGithubApp` はサービスアカウントキーなら true（Cursor GitHub App 名義）、ユーザーキーなら false（自分の名義）が既定。ここでは明示指定していない
- `trigger-trivy-agent`（Cursor Automation の webhook）は移植していない。あのジョブは GitLab に Automation 向けの「CI 完了」トリガーが無いから存在するもので、GitHub なら Automation 側から直接トリガーできる。両方動かすと PR が二重になる

### 修正 MR / PR の CI が落ちたあと（resume）

元の Trivy ジョブの失敗では resume しません。修正ブランチ側の build/test 失敗、またはエージェント run の失敗が対象です。使う `agentId` は CI の cloud エージェント（`bc-...`）です。

```bash
npx tsx src/cli.ts resume \
  --agent-id bc-... \
  --log /tmp/ci.log \
  --note "バージョンを上げたあとテストが落ちた"
```

`--agent-id` を省略すると `.cursor/vuln-orchestrator-state.json` の `remediationAgentId` を使います。ローカルのトリアージ state にはこの ID は入りません。

## CLI オプション

| オプション | コマンド | 説明 |
| --- | --- | --- |
| `--report <path>` | `run` | Trivy の JSON（`--format json`） |
| `--runtime local\|cloud` | `run` | 省略時は `VULN_RUNTIME`。未設定なら CI 上は `cloud`、それ以外は `local` |
| `--skip-remediate` | `run` | トリアージと Policy まで。修正エージェントは起動しない |
| `--cwd <path>` | 両方 | リポジトリルート上書き |
| `--agent-id <id>` | `resume` | 続きから動かすエージェント。省略時は state ファイル |
| `--log <path>` | `resume` | CI ログや失敗出力 |
| `--note <text>` | `resume` | オペレータからの短い指示 |

## 環境変数

| 変数 | 必須 | 説明 |
| --- | --- | --- |
| `CURSOR_API_KEY` | はい | Cursor の API キー |
| `CURSOR_REPO_URL` | cloud 時 | clone 先。GitLab CI は `$CI_PROJECT_URL`、GitHub Actions は `$GITHUB_SERVER_URL/$GITHUB_REPOSITORY`、CI 外は `origin` の remote |
| `CURSOR_STARTING_REF` | cloud 時 | 開始 ref。GitLab CI は `$CI_COMMIT_SHA`、GitHub Actions は `$GITHUB_REF_NAME`（push 直後の SHA を Cursor が検証できないためブランチ名） |
| `CURSOR_MODEL` | いいえ | デフォルト `composer-2.5` |
| `VULN_RUNTIME` | いいえ | `local` または `cloud` |
| `VULN_HOST` | いいえ | `github` / `gitlab`。自動判定の上書き。変わるのはプロンプト内の MR / PR の呼び方だけ |

## Policy（自動修正する条件）

判定は `src/policy.ts` にあります。エージェントの提案をそのまま採用しません。

自動修正 (`auto_remediate`) になるのは次をすべて満たす場合です。

- 重大度が `CRITICAL`（`AUTO_REMEDIATE_SEVERITIES`）
- バージョンを上げるだけで直せる
- 推奨バージョンがある
- メジャーバージョン上げではない（先頭の数字成分が上がらない）

それ以外は `comment_only` で、MR / PR 本文の TODO に回します。Spring Boot 本体の 3.x → 4.x などは人が判断します。

面接で拡張するなら、`AUTO_REMEDIATE_SEVERITIES` に `"HIGH"` を足すのが最短です。

## フリートモード（複数リポジトリ）

`security-scan.yml` が守るのは 1 リポジトリだけです。`fleet-governance.yml` は同じフローをリポジトリ一覧に対して回します。単発の修正ではなく「統制」になるのはこの部分です。

| | 単一リポジトリ | フリート |
| --- | --- | --- |
| ワークフロー | `.github/workflows/security-scan.yml` | `.github/workflows/fleet-governance.yml` |
| トリガー | `push` / `pull_request` | `schedule`（毎日）/ `workflow_dispatch` / `governance/**` への `push` |
| 対象 | そのワークフローが置かれたリポジトリ | `governance/fleet.json` の全エントリ |
| 出力 | 修正 PR 1 本 + SARIF | リポジトリごとに修正 PR + 横断サマリの Issue |

設定は `governance/fleet.json` です。`url` は必須、`ref` を省略するとそのリポジトリのデフォルトブランチ、`name` はレポートの表示名になります。

```json
{
  "repos": [
    { "name": "petclinic (Java / Maven)", "url": "https://github.com/acme/petclinic", "ref": "main" },
    { "name": "web (npm)", "url": "https://github.com/acme/web" }
  ]
}
```

フリート実行の流れ:

1. `plan` が `fleet.json` を読み、`ref` 未指定はデフォルトブランチを解決して matrix を出す
2. `scan` がリポジトリごとに走る（`fail-fast: false`、同時 3 本）。クローン → Trivy → トリアージ → policy → 修正 PR → 影響分析。各リポジトリの state ファイルを成果物として上げる
3. `report` が `governance/render-report.py` でサマリを組み立て、Job Summary **と** コントロールリポジトリの Issue に出す。リポジトリ別の検出数、policy が自動修正したもの、見送ったものとその理由、全修正 PR へのリンクが載る

対象が public なら追加トークンは不要です。Actions はクローンするだけで、修正 PR を push するのは Cursor の GitHub App です。private を対象にする場合は `repo` スコープの PAT をシークレットに入れ、クローンのステップで使います。

エコシステムはリポジトリごとに判定し（`cursor-sdk/src/ecosystem.ts`）、プロンプトが切り替わります（Maven / Gradle の managed version、npm の range と lockfile の使い分け、Go の `go get` + `go mod tidy`、Python の lockfile ツール）。policy は共通で変わりません。

把握しておくべき制約が 2 つあります。

- `schedule` と `workflow_dispatch` は **デフォルトブランチ上のワークフローしか**動かしません。デモを feature ブランチで回している間は、`governance/**` への `push` トリガーが実際の起動手段になります
- 冪等キーは `<purpose>-<owner-repo>-<sha>` なので、同じコミットで単一リポジトリ版とフリート版の両方が走っても、リポジトリごとに同じエージェントが再利用され、PR が二重になりません

## CI ジョブ

| ホスト | ファイル | スキャンジョブ | オーケストレータジョブ |
| --- | --- | --- | --- |
| GitLab CI | `.gitlab-ci.yml` | `trivy-dependency-scan` | `sdk-vuln-orchestrator` |
| GitHub Actions | `.github/workflows/security-scan.yml` | `trivy-dependency-scan` | `sdk-vuln-orchestrator` |
| GitHub Actions（フリート） | `.github/workflows/fleet-governance.yml` | `scan`（matrix） | `scan`（matrix）+ `report` |

どちらのオーケストレータジョブも上の「CI で実行されるコマンド」を実行します。スキャンジョブはどちらも CRITICAL でも失敗しません。

## 状態ファイル

パス: リポジトリルートの `.cursor/vuln-orchestrator-state.json`（gitignore 済み）

`host` / `triageAgentId` / `remediationAgentId` / `runId` / Policy 判定 / MR・PR の URL を保存します。resume と障害調査用です。cloud の run は Cursor 上で **Filter → Source → SDK** から見られます。

## 終了コード

| コード | 意味 |
| --- | --- |
| `0` | 正常終了（脆弱性ゼロで何もしない場合も含む） |
| `1` | 起動失敗（`CursorAgentError`: 認証・設定・ネットワーク）または CLI の使い方ミス |
| `2` | エージェントは起動したが run が失敗した |
| `64` | 引数不足（usage） |

## テスト（SDK は呼ばない）

```bash
cd cursor-sdk
npm test
npm run typecheck
```
