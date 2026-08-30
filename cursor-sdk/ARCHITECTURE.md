# What we built

日本語: [ARCHITECTURE.ja.md](./ARCHITECTURE.ja.md)

For how to run it, see [USAGE.md](./USAGE.md).
For sequences, see [SEQUENCE.md](./SEQUENCE.md).

## In one sentence

A Cursor SDK prototype that **triages CRITICAL dependency vulnerabilities and remediates only what policy allows**, against this repo's existing Spring PetClinic app. The Java app itself is largely untouched.

The scan does not fail the pipeline. Fixes arrive later as an agent-opened MR (GitLab) or PR (GitHub). The same CLI runs on both hosts; `host.ts` decides which.

## Where the Cursor SDK is used

Only `src/agent.ts` calls the SDK. There are four call sites.

| Call | API | Role |
| --- | --- | --- |
| Triage | `Agent.create` + `send` | Read the report and `pom.xml`, return JSON. Do not edit files |
| Remediate | `Agent.create` + `send` | Bump packages that passed policy; on cloud, open an MR / PR |
| Impact | `Agent.create` + `send` | No code edits. Read release notes and call sites, then **append English** compatibility and re-test scope to the MR / PR (and linked Issue) description |
| Resume | `Agent.resume` | Send a later CI/run failure to the same remediation agent |

These are not the SDK:

- Trivy scans
- The if/else in `policy.ts`
- GitLab CI and GitHub Actions YAML
- Cursor Automations (`trigger-trivy-agent`)

## Flow

```
Trivy (local scripts/trivy-scan.sh or CI trivy-dependency-scan)
        │  gl-trivy-fs-report.json
        ▼
cli.ts run
        │
        ├─ parse-report.ts     group findings by package
        ├─ agent.ts            triage agent
        ├─ policy.ts           auto_remediate / comment_only
        ├─ agent.ts            remediate agent (allow-listed packages only)
        └─ agent.ts            impact agent (append English to MR/PR/Issue body)
                │
                ▼
        .cursor/vuln-orchestrator-state.json
                │
cli.ts resume → same remediation agent
```

## Layout

Almost all new code lives in `cursor-sdk/`. PetClinic Java was not changed.

```
gitlab-sample/
├── .gitlab-ci.yml              GitLab CI: Trivy job + SDK job
├── .github/workflows/
│   └── security-scan.yml       GitHub Actions: the same two jobs + SARIF upload
├── .gitignore                  generated files ignored
├── pom.xml etc.                existing app (workspace under scan)
├── .cursor/automations/        existing Automations (not the SDK)
└── cursor-sdk/                 this prototype
    ├── src/                    orchestrator
    ├── scripts/trivy-scan.sh   local Trivy
    ├── fixtures/               sample report for demos
    ├── USAGE.ja.md / USAGE.md
    └── ARCHITECTURE.ja.md / ARCHITECTURE.md
```

## File roles

### Orchestrator (`cursor-sdk/src/`)

| File | Role |
| --- | --- |
| `cli.ts` | Entry point. `run` / `resume`, flags and env, find repo root |
| `host.ts` | GitLab vs GitHub: detect the host from CI env, resolve the clone URL / starting ref, pick the MR/PR wording |
| `orchestrate.ts` | The pipeline: report → triage → policy → remediate. Decides how many agents to start |
| `agent.ts` | Thin `@cursor/sdk` wrapper. `Agent.create` / `resume`, stream, `wait()`, startup vs run failures |
| `policy.ts` | Whether a bump may be automatic: severity, fixable by version, major bump. Does not trust the agent blindly |
| `parse-report.ts` | Normalize Trivy JSON into `Finding`s, group by package |
| `prompts.ts` | Triage / remediate / impact / resume prompts, plus JSON extraction from agent text |
| `state.ts` | Persist `agentId`s to `.cursor/vuln-orchestrator-state.json` for resume |
| `types.ts` | Finding / triage / policy / state types |
| `policy.test.ts` | Parser and policy unit tests. No SDK calls |
| `host.test.ts` | Host detection and clone-URL normalization tests. No SDK calls |

### Scan and demo data

| File | Role |
| --- | --- |
| `scripts/trivy-scan.sh` | Local Trivy. Same as CI: CRITICAL/HIGH/MEDIUM, native Trivy JSON, exit 0 on findings. Auto-remediate stays CRITICAL-only in policy |
| `fixtures/gl-trivy-fs-report.sample.json` | One-finding sample (Tomcat / CVE-2026-43515) so the CLI can run without a real scan |

### Package config

| File | Role |
| --- | --- |
| `package.json` | npm scripts (`scan` / `scan:run` / `run` / `resume` / `test`) and `@cursor/sdk` |
| `package-lock.json` | Lockfile for CI `npm ci` |
| `tsconfig.json` | TypeScript config for running `.ts` via `tsx` |

### Repo-level edits

| File | Role |
| --- | --- |
| `.gitlab-ci.yml` | `trivy-dependency-scan` skips `cursor-sdk`. `sdk-vuln-orchestrator` feeds the artifact to the SDK CLI (push pipelines with `CURSOR_API_KEY`) |
| `.github/workflows/security-scan.yml` | The same jobs on GitHub Actions, plus a SARIF upload to Code scanning. `push` ignores `cursor/**`; the orchestrator steps need the `CURSOR_API_KEY` secret |
| `.gitignore` | `node_modules/`, `gl-trivy-fs-report.json`, `trivy-results.sarif`, `.cursor/vuln-orchestrator-state.json` |

### Generated at runtime (not committed)

| Path | Role |
| --- | --- |
| `gl-trivy-fs-report.json` | Latest Trivy output; orchestrator input |
| `trivy-results.sarif` | GitHub only: same findings for Code scanning |
| `.cursor/vuln-orchestrator-state.json` | Latest `host` / `agentId` / run id / policy decisions / MR-PR URL |

### Existing (not this prototype)

| Path | Role |
| --- | --- |
| `.cursor/automations/trivy-remediation.md` | Cursor Automation prompt. A separate path that starts a Cloud Agent from a webhook |
| `trigger-trivy-agent` in `.gitlab-ci.yml` | CI job for that Automation. Enabling it together with the SDK job duplicates MRs. Not ported to GitHub: Automations can trigger on GitHub natively |
| `.cursor/automations/tokyo-weather.md` | Wiring sample for SDK / Automation. Not part of the vuln flow |
| `src/main/java/**` | PetClinic itself; the workspace triage and remediate run against |

## Why policy is code

If the agent decides everything, it may major-bump Spring Boot on its own. That does not fly in an enterprise, so the allow-list lives in `policy.ts`:

- `CRITICAL` only (`AUTO_REMEDIATE_SEVERITIES`)
- Fixable by a version bump
- Not a major bump (first numeric component does not increase)

The shortest live extension is adding `"HIGH"` to that set.

## Runtimes

| Where | Default | Meaning |
| --- | --- | --- |
| Laptop | `local` | Read / edit this machine's working tree |
| GitLab CI / GitHub Actions | `cloud` | Cursor VM clones the repo; remediate uses `autoCreatePR` |

Override with `--runtime` or `VULN_RUNTIME`.
