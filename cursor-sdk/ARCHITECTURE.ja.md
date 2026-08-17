# 何を作ったか

English: [ARCHITECTURE.md](./ARCHITECTURE.md)

使い方は [USAGE.ja.md](./USAGE.ja.md) を参照。
シーケンスは [SEQUENCE.ja.md](./SEQUENCE.ja.md) を参照。

## ひとことで

企業の SDLC 向けに、**CRITICAL 依存関係脆弱性をトリアージし、方針を通ったものだけ直す** Cursor SDK プロトタイプです。対象アプリは既存の Spring PetClinic（このリポジトリ）で、アプリ本体はほとんど触っていません。

スキャンでパイプラインは落としません。修正はあとからエージェントが MR で追います。

## Cursor SDK を使う場所

SDK を呼ぶのは `src/agent.ts` だけです。呼び出しは 4 種類です。

| 呼び出し | API | 役割 |
| --- | --- | --- |
| トリアージ | `Agent.create` + `send` | レポートと `pom.xml` を読み、JSON で分類する。ファイルは編集しない |
| 修正 | `Agent.create` + `send` | Policy を通ったパッケージだけバージョンを上げ、cloud なら MR を開く |
| 影響分析 | `Agent.create` + `send` | コードは触らず、Release Notes と利用箇所から互換性・再テスト範囲を MR にコメントする |
| 続き | `Agent.resume` | 修正 MR の CI 失敗などを、同じエージェントに渡して直す |

次は SDK ではありません。

- Trivy スキャン
- `policy.ts` の if/else
- GitLab CI の YAML
- Cursor Automations（`trigger-trivy-agent`）

## 処理の流れ

```
Trivy（ローカル scripts/trivy-scan.sh または CI trivy-dependency-scan）
        │  gl-trivy-fs-report.json
        ▼
cli.ts run
        │
        ├─ parse-report.ts     パッケージ単位にまとめる
        ├─ agent.ts            トリアージエージェント
        ├─ policy.ts           auto_remediate / comment_only
        ├─ agent.ts            修正エージェント（通ったものだけ）
        └─ agent.ts            影響分析エージェント（MR コメント）
                │
                ▼
        .cursor/vuln-orchestrator-state.json
                │
cli.ts resume → 同じ remediation agent
```

## ディレクトリ

新しいコードはほぼすべて `cursor-sdk/` にあります。PetClinic の Java は変更していません。

```
gitlab-sample/
├── .gitlab-ci.yml              Trivy ジョブ + SDK ジョブを追加
├── .gitignore                  生成物を除外
├── pom.xml など                既存アプリ（対象リポジトリ）
├── .cursor/automations/        既存の Automation（SDK ではない）
└── cursor-sdk/                 今回のプロトタイプ
    ├── src/                    オーケストレータ
    ├── scripts/trivy-scan.sh   ローカル Trivy
    ├── fixtures/               デモ用サンプルレポート
    ├── USAGE.ja.md / USAGE.md
    └── ARCHITECTURE.ja.md / ARCHITECTURE.md
```

## ファイルの役割

### オーケストレータ（`cursor-sdk/src/`）

| ファイル | 役割 |
| --- | --- |
| `cli.ts` | 入口。`run` / `resume`、引数と環境変数、リポジトリルートの特定 |
| `orchestrate.ts` | 手順の組み立て。レポート → トリアージ → Policy → 修正。エージェントを何台立てるかはここが決める |
| `agent.ts` | `@cursor/sdk` の薄い包み。`Agent.create` / `resume`、stream、`wait()`、起動失敗と run 失敗の切り分け |
| `policy.ts` | 自動修正してよいかの判定。重大度、直せるか、メジャーバージョンが上がるか。エージェントの提案は信用しすぎない |
| `parse-report.ts` | Trivy の JSON を `Finding` に直し、パッケージでグループ化する |
| `prompts.ts` | トリアージ・修正・影響分析・resume のプロンプト。エージェント出力から JSON を抜く |
| `state.ts` | `agentId` などを `.cursor/vuln-orchestrator-state.json` に保存・読み込み。resume 用 |
| `types.ts` | Finding / Triage / Policy / State の型 |
| `policy.test.ts` | パーサと Policy のユニットテスト。SDK は呼ばない |

### スキャンとデモデータ

| ファイル | 役割 |
| --- | --- |
| `scripts/trivy-scan.sh` | ローカル Trivy。CI と同じく CRITICAL のみ、Trivy JSON、検出しても exit 0。`cursor-sdk/` は対象外 |
| `fixtures/gl-trivy-fs-report.sample.json` | 実スキャンなしで CLI を試すための 1 件サンプル（Tomcat / CVE-2026-43515） |

### パッケージ設定

| ファイル | 役割 |
| --- | --- |
| `package.json` | npm スクリプト（`scan` / `scan:run` / `run` / `resume` / `test`）と `@cursor/sdk` |
| `package-lock.json` | CI の `npm ci` 用ロック |
| `tsconfig.json` | `tsx` で `.ts` を直接実行するための TypeScript 設定 |

### リポジトリ側の変更

| ファイル | 役割 |
| --- | --- |
| `.gitlab-ci.yml` | `trivy-dependency-scan` が `cursor-sdk` を skip。`sdk-vuln-orchestrator` が成果物を SDK CLI に渡す（`CURSOR_API_KEY` がある push のみ） |
| `.gitignore` | `node_modules/`、`gl-trivy-fs-report.json`、`.cursor/vuln-orchestrator-state.json` |

### 実行時にだけできるファイル（コミットしない）

| パス | 役割 |
| --- | --- |
| `gl-trivy-fs-report.json` | 直近の Trivy 出力。オーケストレータの入力 |
| `.cursor/vuln-orchestrator-state.json` | 直近の `agentId` / run id / Policy 判定 / MR URL |

### 既存のまま（このプロトタイプの本体ではない）

| パス | 役割 |
| --- | --- |
| `.cursor/automations/trivy-remediation.md` | Cursor Automation 用プロンプト。webhook で Cloud Agent を直接起動する別経路 |
| `.gitlab-ci.yml` の `trigger-trivy-agent` | その Automation を CI から叩くジョブ。SDK ジョブと両方有効だと MR が二重になる |
| `.cursor/automations/tokyo-weather.md` | SDK / Automation の配線確認用サンプル。脆弱性フローとは無関係 |
| `src/main/java/**` | PetClinic 本体。トリアージ・修正の対象ワークスペース |

## Policy がコードにある理由

「全部エージェントに任せる」と、Spring Boot のメジャーバージョン上げまで自動になり得ます。企業では通らないので、自動にする条件は `policy.ts` に固定しています。

- `CRITICAL` だけ自動（`AUTO_REMEDIATE_SEVERITIES`）
- バージョンを上げるだけで直せる
- メジャーバージョン上げではない（先頭の数字が上がらない）

面接で足すなら、この Set に `"HIGH"` を足すのが最短です。

## ランタイム

| 場所 | デフォルト | 意味 |
| --- | --- | --- |
| 手元 | `local` | このマシンの作業ツリーを読む / 編集する |
| GitLab CI | `cloud` | Cursor の VM がリポジトリを clone する。修正時は `autoCreatePR` |

`--runtime` または `VULN_RUNTIME` で上書きできます。
