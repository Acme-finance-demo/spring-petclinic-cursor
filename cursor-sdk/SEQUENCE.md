# Vulnerability orchestrator sequences

日本語: [SEQUENCE.ja.md](./SEQUENCE.ja.md)

Usage: [USAGE.md](./USAGE.md) / layout: [ARCHITECTURE.md](./ARCHITECTURE.md)

Every prompt is built in `src/prompts.ts` and passed to `agent.send()`. The text pasted into a Cursor Automation is not used on the SDK path.

## Participants

| Name | What it is | SDK? |
| --- | --- | --- |
| Dev | Developer on a laptop | no |
| Trivy | `scripts/trivy-scan.sh` or CI `trivy-dependency-scan` | no |
| CLI | `cli.ts` → `orchestrate.ts` | caller |
| Policy | `policy.ts` | no |
| Triage | Cursor agent (`triagePrompt`) | yes |
| Fix | Cursor agent (`remediatePrompt`) | yes |
| Impact | Cursor agent (`impactPrompt`) | yes |
| GitLab | repo / MR | no |
| Automation | Cursor Automation + `trigger-trivy-agent` | no (separate path) |

---

## 1. Local (no MR)

Recommended: `cd cursor-sdk && npm run scan:run`  
(`--runtime local --skip-remediate`)

```mermaid
sequenceDiagram
    actor Dev
    participant Trivy as Trivy<br/>trivy-scan.sh
    participant CLI as CLI<br/>orchestrate.ts
    participant Parse as parse-report.ts
    participant Triage as Triage agent<br/>triagePrompt
    participant Policy as policy.ts
    participant State as vuln-orchestrator-state.json

    Dev->>Trivy: npm run scan
    Trivy->>Trivy: CRITICAL/HIGH/MEDIUM<br/>skip cursor-sdk
    Trivy-->>Dev: gl-trivy-fs-report.json<br/>exit 0 even if findings exist

    Dev->>CLI: run --runtime local --skip-remediate
    CLI->>Parse: normalize report, group by package
    Parse-->>CLI: PackageGroup[]

    alt zero findings
        CLI->>State: save decisions: []
        CLI-->>Dev: exit, nothing else
    else one or more
        CLI->>Triage: Agent.create(local) + send(triagePrompt)
        Note over Triage: reads pom.xml<br/>no edits, commits, or MR
        Triage-->>CLI: JSON (fixed version, major?, bump-only?)

        CLI->>Policy: decideAll(items)
        Policy-->>CLI: auto_remediate / comment_only

        Note over CLI: --skip-remediate<br/>Fix agent is not started
        CLI->>State: save triageAgentId and decisions
        CLI-->>Dev: policy lines on stderr
    end
```

No MR is opened. `remediationAgentId` is not written.

---

## 2. CI (MR only for packages that pass policy)

You do not pass `--runtime cloud` by hand. On a push with `CURSOR_API_KEY`, `sdk-vuln-orchestrator` runs this.

```mermaid
sequenceDiagram
    actor Dev
    participant GL as GitLab CI
    participant Scan as trivy-dependency-scan
    participant CLI as CLI<br/>orchestrate.ts
    participant Triage as Triage agent<br/>triagePrompt
    participant Policy as policy.ts
    participant Fix as Fix agent<br/>remediatePrompt
    participant Impact as Impact agent<br/>impactPrompt
    participant Cursor as Cursor cloud VM
    participant GitLab as GitLab MR

    Dev->>GL: git push
    GL->>Scan: CRITICAL/HIGH/MEDIUM (job stays green)
    Scan-->>GL: gl-trivy-fs-report.json

    GL->>CLI: tsx cli.ts run --runtime cloud<br/>(no --skip-remediate)

    CLI->>Triage: Agent.create(cloud, autoCreatePR=false)<br/>send(triagePrompt)
    Note over Triage: prompt from prompts.ts<br/>Automation text is unused
    Triage->>Cursor: clone repo, read pom.xml
    Cursor-->>Triage: working tree
    Triage-->>CLI: JSON

    CLI->>Policy: CRITICAL? bump-only? not major?
    Policy-->>CLI: decisions[]

    alt zero auto_remediate
        CLI-->>GL: skip Fix agent (no MR)
    else one or more
        CLI->>Fix: Agent.create(cloud, autoCreatePR=true)<br/>send(remediatePrompt)
        Note over Fix: bump AUTO only<br/>comment_only → TODO in MR body
        Fix->>Cursor: clone → minimal pom edit → commit
        Cursor->>GitLab: push branch + open MR
        GitLab-->>Fix: MR URL
        Fix-->>CLI: prUrl, agentId (bc-...)
        CLI->>Impact: Agent.create(cloud, autoCreatePR=false)<br/>send(impactPrompt)
        Note over Impact: no code edits<br/>release notes + call sites
        Impact->>GitLab: append English to MR/Issue body
        CLI-->>GL: save state artifact
    end
```

`auto_remediate` requires all of:

1. Severity is CRITICAL
2. A version bump alone is enough (`recommendedVersion` present)
3. Not a major bump (first numeric component does not increase)

The MR body is not a TypeScript template. The Fix agent writes it from `remediatePrompt`. Impact analysis from `impactPrompt` is **appended in English to the MR/Issue description**, not posted as a discussion comment.

---

## 3. After the fix MR's CI fails (resume)

Do not resume on a red Trivy job. Resume is for a failed build/test on the fix branch, or a failed agent run.

```mermaid
sequenceDiagram
    actor Op as Operator / follow-up job
    participant CLI as CLI<br/>resume
    participant Fix as same Fix agent<br/>Agent.resume
    participant GitLab as fix MR

    Op->>CLI: resume --agent-id bc-... --log ci.log
    CLI->>Fix: Agent.resume(bc-...)<br/>send(resumePrompt)
    Note over Fix: no new agent<br/>resumePrompt from prompts.ts
    Fix->>GitLab: extra commit if needed
    Fix-->>CLI: runId / prUrl
```

A local triage-only state file has no `remediationAgentId`, so it cannot drive this path.

---

## 4. Unused path (Cursor Automation)

This is separate from the SDK job. If `CURSOR_TRIVY_WEBHOOK_URL` is still set, both can run and duplicate MRs.

```mermaid
sequenceDiagram
    participant GL as GitLab CI
    participant Hook as trigger-trivy-agent
    participant Auto as Cursor Automation
    participant Agent as Cloud Agent
    participant GitLab as GitLab MR

    GL->>Hook: Trivy artifact
    Hook->>Auto: POST webhook + report JSON
    Note over Auto: dashboard prompt from<br/>trivy-remediation.md
    Auto->>Agent: start with that prompt
    Agent->>GitLab: dependency bump + MR

    Note over GL,GitLab: sdk-vuln-orchestrator does not call this
```

| Path | Prompt | Opens an MR? |
| --- | --- | --- |
| Local `scan:run` | `triagePrompt` only | no |
| CI `sdk-vuln-orchestrator` | `triagePrompt` → `remediatePrompt` if allowed → `impactPrompt` | only on policy pass (impact is appended to the body) |
| CI `trigger-trivy-agent` | Automation markdown | whatever Automation is configured to do |

---

## Where prompts enter the SDK

```mermaid
flowchart LR
    subgraph code [cursor-sdk/src]
        P[prompts.ts]
        O[orchestrate.ts]
        A[agent.ts]
    end

    P -->|triagePrompt| O
    P -->|remediatePrompt| O
    P -->|impactPrompt| O
    P -->|resumePrompt| O
    O -->|agent.send prompt| A
    A -->|Agent.create / resume| SDK["@cursor/sdk"]
    SDK --> Local[local: this machine cwd]
    SDK --> Cloud[cloud: Cursor VM + optional autoCreatePR]
```
