# Vulnerability orchestrator (usage)

日本語: [USAGE.ja.md](./USAGE.ja.md)

What we built and which file does what: [ARCHITECTURE.md](./ARCHITECTURE.md).
Sequences: [SEQUENCE.md](./SEQUENCE.md).

Takes a GitLab CI Trivy report and runs triage → policy → a fix MR via the Cursor SDK. The scan job itself does not fail on CRITICAL findings. Remediation is asynchronous.

## Flow

```
Trivy report
    → Agent.create (triage, read-mostly)
    → policy.ts (decided in code, not by the agent)
    → Agent.create (remediate; autoCreatePR on cloud)
    → Agent.create (impact analysis; append English to the MR/Issue body; no code edits)
    → on a later failure, Agent.resume (same agent)
```

The SDK is used only by the CLI in this directory. Policy and report parsing are plain TypeScript.

## Requirements

- Node.js 22.13+
- [Trivy](https://trivy.dev/) for local scans (`brew install trivy` on macOS)
- `CURSOR_API_KEY` from the [Cursor Dashboard](https://cursor.com/dashboard/api) (user key or service-account key)
- For the cloud runtime: Cursor must be able to access this GitLab repository

## Install

```bash
cd cursor-sdk
npm ci
```

## What to run where

Local is for finding out early. CI is for opening an MR from an official scan. A laptop Trivy report can disagree with CI, so local runs do not open MRs.

| Where | Do | Do not |
| --- | --- | --- |
| Local | Trivy → triage → policy. Logs and the state file | Open an MR (`--skip-remediate`, `--runtime local`) |
| CI | The same flow, then a cloud fix MR for whatever policy allows, then English impact analysis appended to the MR/Issue body | Fail the pipeline because the scan found CRITICAL |

Run from the repo root or from `cursor-sdk/`. If you omit `--cwd`, the CLI walks up until it finds `pom.xml`.

### Commands to run locally

Export `CURSOR_API_KEY` in the shell (`~/.zshrc` or similar). Do not put the value in the repo.

Scan only:

```bash
cd cursor-sdk
npm run scan
```

CRITICAL / HIGH / MEDIUM, native Trivy JSON, exit 0 even when findings exist. Auto-remediate stays CRITICAL-only in policy. `cursor-sdk/` is skipped. The report is `gl-trivy-fs-report.json` at the repository root. Skip Maven resolution with `bash scripts/trivy-scan.sh --skip-resolve`.

Scan through triage (recommended):

```bash
cd cursor-sdk
npm run scan:run
```

That is the same as the following. `--runtime local` and `--skip-remediate` mean no fix agent and no MR.

```bash
cd cursor-sdk
npx tsx src/cli.ts run \
  --report ../gl-trivy-fs-report.json \
  --runtime local \
  --skip-remediate
```

CLI only, with the sample JSON:

```bash
cd cursor-sdk
npx tsx src/cli.ts run \
  --report fixtures/gl-trivy-fs-report.sample.json \
  --runtime local \
  --skip-remediate
```

Output is the policy lines in the terminal plus `.cursor/vuln-orchestrator-state.json`. The working tree is unchanged.

### Commands CI runs

You do not need to pass `--runtime cloud` on a laptop. On a push pipeline with `CURSOR_API_KEY`, `sdk-vuln-orchestrator` runs:

```bash
cd cursor-sdk
npm ci
npx tsx src/cli.ts run \
  --report "$CI_PROJECT_DIR/gl-trivy-fs-report.json" \
  --runtime cloud
```

There is no `--skip-remediate`. Input is the `trivy-dependency-scan` artifact from the same pipeline. `CURSOR_REPO_URL` is `$CI_PROJECT_URL` and `CURSOR_STARTING_REF` is `$CI_COMMIT_SHA`. An MR is opened with `autoCreatePR` only on cloud, and only for packages that pass policy. An impact agent then reads that library's release notes and this repo's call sites, and **appends English analysis to the MR description** (and to a linked GitLab Issue if the MR closes one). It does not post a discussion comment. If impact analysis fails, the fix MR is still kept.

The job is `allow_failure: true`, so an orchestrator failure does not block merge. State is kept as a job artifact at `.cursor/vuln-orchestrator-state.json`.

Setup:

1. GitLab: **Settings → CI/CD → Variables** → add `CURSOR_API_KEY` (Masked. If it is Protected, only pushes to protected branches will see it)
2. Disable the existing Cursor Automation variables (`CURSOR_TRIVY_WEBHOOK_URL` / `CURSOR_TRIVY_API_KEY`). Leaving both enabled duplicates MRs
3. Push to `main`

### After the fix MR's CI fails (resume)

Do not resume on a red Trivy job. Resume is for a failed build/test on the fix branch, or a failed agent run. The `agentId` is the cloud agent from CI (`bc-...`).

```bash
npx tsx src/cli.ts resume \
  --agent-id bc-... \
  --log /tmp/ci.log \
  --note "tests failed after the version bump"
```

If you omit `--agent-id`, the CLI reads `remediationAgentId` from `.cursor/vuln-orchestrator-state.json`. A local triage-only state file does not contain that id.

## CLI options

| Option | Command | Description |
| --- | --- | --- |
| `--report <path>` | `run` | Native Trivy JSON (`--format json`) |
| `--runtime local\|cloud` | `run` | Falls back to `VULN_RUNTIME`, then `cloud` in CI and `local` otherwise |
| `--skip-remediate` | `run` | Stop after triage and policy; do not start the fix agent |
| `--cwd <path>` | both | Override the repository root |
| `--agent-id <id>` | `resume` | Agent to continue. Defaults to the state file |
| `--log <path>` | `resume` | CI log or failure output |
| `--note <text>` | `resume` | Short operator instruction |

## Environment variables

| Variable | Required | Description |
| --- | --- | --- |
| `CURSOR_API_KEY` | yes | Cursor API key |
| `CURSOR_REPO_URL` | for cloud | Clone URL. In CI: `$CI_PROJECT_URL` |
| `CURSOR_STARTING_REF` | for cloud | Starting ref. In CI: `$CI_COMMIT_SHA` |
| `CURSOR_MODEL` | no | Defaults to `composer-2.5` |
| `VULN_RUNTIME` | no | `local` or `cloud` |

## Policy (when a fix is automatic)

Decisions live in `src/policy.ts`. The orchestrator does not take the agent's recommendation at face value.

`auto_remediate` requires all of:

- Severity is `CRITICAL` (`AUTO_REMEDIATE_SEVERITIES`)
- A version bump alone is enough
- A recommended version exists
- It is not a major bump (first numeric component does not increase)

Everything else is `comment_only` and becomes a TODO in the MR body. Spring Boot 3.x → 4.x stays a human decision.

The shortest live extension is adding `"HIGH"` to `AUTO_REMEDIATE_SEVERITIES`.

## GitLab CI

The job is `sdk-vuln-orchestrator`. Its script is the command in “Commands CI runs” above. The scan job does not fail on CRITICAL findings.

## State file

Path: `.cursor/vuln-orchestrator-state.json` at the repo root (gitignored)

It stores `triageAgentId` / `remediationAgentId` / run IDs / policy decisions / MR URL, for resume and debugging. Cloud runs show up in Cursor under **Filter → Source → SDK**.

## Exit codes

| Code | Meaning |
| --- | --- |
| `0` | Success (including "no vulnerabilities, nothing to do") |
| `1` | Startup failure (`CursorAgentError`: auth, config, network) or CLI misuse |
| `2` | The agent started, but the run failed |
| `64` | Missing arguments (usage) |

## Tests (no SDK calls)

```bash
cd cursor-sdk
npm test
npm run typecheck
```
