# 脆弱性オーケストレータ（使い方）

English: [USAGE.md](./USAGE.md)

構成とファイルの役割は [ARCHITECTURE.ja.md](./ARCHITECTURE.ja.md) を参照。
シーケンスは [SEQUENCE.ja.md](./SEQUENCE.ja.md) を参照。

GitLab CI の Trivy レポートを受け取り、Cursor SDK でトリアージ → 方針判定 → 修正 MR まで進めるプロトタイプです。スキャンジョブ自体は CRITICAL が出ても失敗しません。修正は非同期です。

## 全体像

```
Trivy レポート
    → Agent.create（トリアージ、読み取り中心）
    → policy.ts（コードで判定。エージェントには任せない）
    → Agent.create（修正。cloud なら autoCreatePR）
    → CI 失敗時は Agent.resume（同じエージェントの続き）
```

SDK を呼ぶのはこのディレクトリの CLI だけです。Policy 判定とレポート整形は通常の TypeScript です。

## 必要要件

- Node.js 22.13 以上
- [Trivy](https://trivy.dev/)（ローカルスキャン用。macOS なら `brew install trivy`）
- [Cursor Dashboard](https://cursor.com/dashboard/api) の `CURSOR_API_KEY`（ユーザーキーまたはサービスアカウントキー）
- cloud ランタイムを使う場合: Cursor からこの GitLab リポジトリへアクセスできること

## インストール

```bash
cd cursor-sdk
npm ci
```

## どこで何を実行するか

ローカルは早く知るため、CI は正式なスキャン結果から MR を出すため、です。手元の Trivy は CI と一致するとは限らないので、ローカルでは MR を開きません。

| 場所 | やること | やらないこと |
| --- | --- | --- |
| ローカル | Trivy → トリアージ → Policy。ログと state ファイル | MR を開く（`--skip-remediate`。`--runtime local`） |
| CI | 同じフローのあと、Policy を通ったものだけ cloud で修正 MR | スキャン失敗でパイプラインを落とすこと |

リポジトリルート、または `cursor-sdk/` のどちらからでも実行できます。`--cwd` を省略すると `pom.xml` のあるリポジトリルートを探します。

### ローカルで実行するコマンド

`CURSOR_API_KEY` はシェルに export しておく（`~/.zshrc` など）。値はリポジトリに置かない。

スキャンだけ:

```bash
cd cursor-sdk
npm run scan
```

CRITICAL のみ、Trivy ネイティブ JSON、検出しても exit 0。`cursor-sdk/` は対象外。レポートはリポジトリルートの `gl-trivy-fs-report.json`。依存解決を省略するなら `bash scripts/trivy-scan.sh --skip-resolve`。

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

人が手元で `--runtime cloud` を叩く必要はありません。`CURSOR_API_KEY` が入った push パイプラインで `sdk-vuln-orchestrator` が次を実行します。

```bash
cd cursor-sdk
npm ci
npx tsx src/cli.ts run \
  --report "$CI_PROJECT_DIR/gl-trivy-fs-report.json" \
  --runtime cloud
```

ここでは `--skip-remediate` を付けません。入力は同じパイプラインの `trivy-dependency-scan` 成果物です。`CURSOR_REPO_URL` は `$CI_PROJECT_URL`、`CURSOR_STARTING_REF` は `$CI_COMMIT_SHA` です。cloud かつ Policy 通過時だけ `autoCreatePR` で MR が開きます。

ジョブは `allow_failure: true` です。オーケストレータが落ちてもマージは止まりません。state は成果物 `.cursor/vuln-orchestrator-state.json` に残ります。

セットアップ:

1. GitLab: **Settings → CI/CD → Variables** に `CURSOR_API_KEY` を追加（Masked。Protected にするなら protected ブランチの push だけが対象）
2. 既存の Cursor Automation（`CURSOR_TRIVY_WEBHOOK_URL` / `CURSOR_TRIVY_API_KEY`）は無効化。両方有効だと MR が二重になる
3. `main` へ push する

### 修正 MR の CI が落ちたあと（resume）

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
| `CURSOR_REPO_URL` | cloud 時 | clone 先。CI では `$CI_PROJECT_URL` |
| `CURSOR_STARTING_REF` | cloud 時 | 開始 ref。CI では `$CI_COMMIT_SHA` |
| `CURSOR_MODEL` | いいえ | デフォルト `composer-2.5` |
| `VULN_RUNTIME` | いいえ | `local` または `cloud` |

## Policy（自動修正する条件）

判定は `src/policy.ts` にあります。エージェントの提案をそのまま採用しません。

自動修正 (`auto_remediate`) になるのは次をすべて満たす場合です。

- 重大度が `CRITICAL`（`AUTO_REMEDIATE_SEVERITIES`）
- バージョンを上げるだけで直せる
- 推奨バージョンがある
- メジャーバージョン上げではない（先頭の数字成分が上がらない）

それ以外は `comment_only` で、MR 本文の TODO に回します。Spring Boot 本体の 3.x → 4.x などは人が判断します。

面接で拡張するなら、`AUTO_REMEDIATE_SEVERITIES` に `"HIGH"` を足すのが最短です。

## GitLab CI

ジョブ名は `sdk-vuln-orchestrator` です。上の「CI で実行されるコマンド」がその script です。スキャンジョブは CRITICAL でも失敗しません。

## 状態ファイル

パス: リポジトリルートの `.cursor/vuln-orchestrator-state.json`（gitignore 済み）

`triageAgentId` / `remediationAgentId` / `runId` / Policy 判定 / MR URL を保存します。resume と障害調査用です。cloud の run は Cursor 上で **Filter → Source → SDK** から見られます。

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
