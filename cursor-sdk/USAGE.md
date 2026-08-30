# Vulnerability orchestrator (usage)

日本語: [USAGE.ja.md](./USAGE.ja.md)

What we built and which file does what: [ARCHITECTURE.md](./ARCHITECTURE.md).
Sequences: [SEQUENCE.md](./SEQUENCE.md).

Takes a Trivy report from GitLab CI or GitHub Actions and runs triage → policy → a fix MR/PR via the Cursor SDK. The scan job itself does not fail on CRITICAL findings. Remediation is asynchronous.

The host is detected from CI environment variables (`GITLAB_CI` / `GITHUB_ACTIONS`), so the same CLI runs on both. Only the repo URL, the starting ref, and the MR/PR wording in the prompts change.

## Flow

```
Trivy report
    → Agent.create (triage, read-mostly)
    → policy.ts (decided in code, not by the agent)
    → Agent.create (remediate; autoCreatePR on cloud)
    → Agent.create (impact analysis; append English to the MR/PR/Issue body; no code edits)
    → on a later failure, Agent.resume (same agent)
```

The SDK is used only by the CLI in this directory. Policy and report parsing are plain TypeScript.

## Requirements

- Node.js 22.13+
- [Trivy](https://trivy.dev/) for local scans (`brew install trivy` on macOS)
- `CURSOR_API_KEY` from the [Cursor Dashboard](https://cursor.com/dashboard/api) (user key or service-account key)
- For the cloud runtime: Cursor must be able to access this repository (GitLab or GitHub)

## Install

```bash
cd cursor-sdk
npm ci
```

## What to run where

Local is for finding out early. CI is for opening an MR/PR from an official scan. A laptop Trivy report can disagree with CI, so local runs do not open MRs or PRs.

| Where | Do | Do not |
| --- | --- | --- |
| Local | Trivy → triage → policy. Logs and the state file | Open an MR/PR (`--skip-remediate`, `--runtime local`) |
| CI | The same flow, then a cloud fix MR (GitLab) / PR (GitHub) for whatever policy allows, then English impact analysis appended to its description | Fail the pipeline because the scan found CRITICAL |

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

You do not need to pass `--runtime cloud` on a laptop. On a push pipeline with `CURSOR_API_KEY`, the orchestrator job runs this.

GitLab CI — `sdk-vuln-orchestrator` in `.gitlab-ci.yml`:

```bash
cd cursor-sdk
npm ci
npx tsx src/cli.ts run \
  --report "$CI_PROJECT_DIR/gl-trivy-fs-report.json" \
  --runtime cloud
```

GitHub Actions — `sdk-vuln-orchestrator` in `.github/workflows/security-scan.yml`:

```bash
cd cursor-sdk
npm ci
npx tsx src/cli.ts run \
  --report "$GITHUB_WORKSPACE/gl-trivy-fs-report.json" \
  --runtime cloud
```

Same command; only the workspace variable differs. There is no `--skip-remediate`. Input is the Trivy report artifact from the same pipeline / run. `CURSOR_REPO_URL` and `CURSOR_STARTING_REF` come from the host: `$CI_PROJECT_URL` / `$CI_COMMIT_SHA` on GitLab, `$GITHUB_SERVER_URL/$GITHUB_REPOSITORY` / `$GITHUB_SHA` on GitHub. An MR (GitLab) or PR (GitHub) is opened with `autoCreatePR` only on cloud, and only for packages that pass policy. An impact agent then reads that library's release notes and this repo's call sites, and **appends English analysis to the MR/PR description** (and to a linked Issue if it closes one). It does not post a comment. If impact analysis fails, the fix MR/PR is still kept.

An orchestrator failure does not block merge: `allow_failure: true` on GitLab, `continue-on-error: true` on GitHub. State is kept as a job artifact at `.cursor/vuln-orchestrator-state.json`.

Setup on GitLab:

1. **Settings → CI/CD → Variables** → add `CURSOR_API_KEY` (Masked. If it is Protected, only pushes to protected branches will see it)
2. Disable the existing Cursor Automation variables (`CURSOR_TRIVY_WEBHOOK_URL` / `CURSOR_TRIVY_API_KEY`). Leaving both enabled duplicates MRs
3. Push to `main`

Setup on GitHub:

1. **Settings → Secrets and variables → Actions → New repository secret** → add `CURSOR_API_KEY`. Without it the orchestrator job's steps are skipped and the workflow still passes
2. Connect this repository to Cursor. The Cursor GitHub App needs write access, because the cloud agent pushes the fix branch and opens the PR itself — not `GITHUB_TOKEN`
3. Optional: **Settings → Code security** → turn on Code scanning so the Trivy SARIF lands in the Security tab. Without it the upload step fails soft (`continue-on-error: true`)
4. Push to `main`

What the GitHub workflow does differently:

- `push` ignores `cursor/**`, so the agent's fix branch does not start another orchestrator run. The fix PR is still built and scanned through the `pull_request` trigger
- The orchestrator job only runs on `push`, never on `pull_request`
- Trivy also writes `trivy-results.sarif`, uploaded with `github/codeql-action/upload-sarif` (the GitLab side has no SARIF step)
- The scan job does not download the JAR artifact. `--skip-dirs target` excludes it anyway; what matters is `~/.m2` filled by `./mvnw dependency:resolve`
- Who authors the fix PR depends on the key. The SDK's `openAsCursorGithubApp` defaults to true for a service-account key (PR shows up as the Cursor GitHub App) and false for a user key (PR shows up as you). We do not set it explicitly
- `trigger-trivy-agent` (the Cursor Automation webhook) is not ported. That job exists because GitLab has no "CI completed" trigger for Automations; on GitHub an Automation can trigger natively, and running both paths would duplicate PRs

### After the fix MR/PR's CI fails (resume)

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
| `CURSOR_REPO_URL` | for cloud | Clone URL. GitLab CI: `$CI_PROJECT_URL`. GitHub Actions: `$GITHUB_SERVER_URL/$GITHUB_REPOSITORY`. Off CI: the `origin` remote |
| `CURSOR_STARTING_REF` | for cloud | Starting ref. GitLab CI: `$CI_COMMIT_SHA`. GitHub Actions: `$GITHUB_SHA` |
| `CURSOR_MODEL` | no | Defaults to `composer-2.5` |
| `VULN_RUNTIME` | no | `local` or `cloud` |
| `VULN_HOST` | no | `github` or `gitlab`. Overrides autodetection; only changes the MR/PR wording in prompts |

## Policy (when a fix is automatic)

Decisions live in `src/policy.ts`. The orchestrator does not take the agent's recommendation at face value.

`auto_remediate` requires all of:

- Severity is `CRITICAL` (`AUTO_REMEDIATE_SEVERITIES`)
- A version bump alone is enough
- A recommended version exists
- It is not a major bump (first numeric component does not increase)

Everything else is `comment_only` and becomes a TODO in the MR/PR body. Spring Boot 3.x → 4.x stays a human decision.

The shortest live extension is adding `"HIGH"` to `AUTO_REMEDIATE_SEVERITIES`.

## CI jobs

| Host | File | Scan job | Orchestrator job |
| --- | --- | --- | --- |
| GitLab CI | `.gitlab-ci.yml` | `trivy-dependency-scan` | `sdk-vuln-orchestrator` |
| GitHub Actions | `.github/workflows/security-scan.yml` | `trivy-dependency-scan` | `sdk-vuln-orchestrator` |

Both orchestrator jobs run the command in “Commands CI runs” above. Neither scan job fails on CRITICAL findings.

## State file

Path: `.cursor/vuln-orchestrator-state.json` at the repo root (gitignored)

It stores `host` / `triageAgentId` / `remediationAgentId` / run IDs / policy decisions / MR-PR URL, for resume and debugging. Cloud runs show up in Cursor under **Filter → Source → SDK**.

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
