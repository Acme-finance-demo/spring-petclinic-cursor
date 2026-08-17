import type { PackageGroup, PolicyDecision, TriageReport } from "./types.ts";

export function triagePrompt(groups: PackageGroup[]): string {
  return `You are triaging dependency vulnerabilities for a Spring Boot / Maven app (Spring PetClinic).

Read pom.xml (and build.gradle if present). Do not edit files. Do not commit. Do not open a merge request.

For each package below:
- Is it a direct dependency or BOM-managed / transitive?
- What is the highest fixed version required by the solutions?
- Would that be a major bump (first numeric version component increases)?
- Can it be fixed by a version bump alone (property override, BOM, or managed version)? Prefer Spring Boot BOM / existing properties over ad-hoc pins.

Return ONLY a JSON object in a fenced json code block, matching:
{
  "summary": "one paragraph",
  "items": [
    {
      "package": "group:artifact",
      "currentVersion": "x.y.z",
      "recommendedVersion": "x.y.z or null",
      "cves": ["CVE-..."],
      "severity": "CRITICAL",
      "isDirectDependency": false,
      "isMajorBump": false,
      "canFixByVersionBump": true,
      "notes": "blast radius / why this upgrade is or is not safe"
    }
  ]
}

Packages:
${JSON.stringify(groups, null, 2)}
`;
}

export function remediatePrompt(decisions: PolicyDecision[]): string {
  const auto = decisions.filter((d) => d.action === "auto_remediate");
  const deferred = decisions.filter((d) => d.action === "comment_only");

  return `You are remediating Maven dependency CVEs in this repository.

Policy already ran in the caller. Follow it exactly.

AUTO-REMEDIATE these packages (minimal version bumps only):
${JSON.stringify(auto, null, 2)}

DO NOT bump these; mention them in the MR description as TODOs:
${JSON.stringify(deferred, null, 2)}

Rules:
- Prefer existing Spring Boot / BOM managed versions and pom properties (e.g. tomcat.version).
- Only add explicit version overrides when necessary.
- Do not refactor unrelated code. Do not change CI unless the bump requires it.
- If a finding cannot be fixed by a version bump, leave a TODO in the MR body and still open the MR with whatever safe upgrades you made.
- If an MR/branch already exists for the same packages, update it instead of opening a duplicate.
- Commit on a new branch and open a merge request. Title/body must list CVE IDs and packages upgraded.
`;
}

export function impactPrompt(decisions: PolicyDecision[], prUrl?: string): string {
  const auto = decisions.filter((d) => d.action === "auto_remediate");
  const target = prUrl
    ? `Merge request: ${prUrl}
Append the analysis to that MR's description (the issue/MR body). Do not post a discussion comment. Do not open a new MR.
If the MR closes or mentions a GitLab Issue, append the same English section to that Issue description too.`
    : `No MR URL is available. Print the analysis in the format below. Do not open an MR.`;

  return `You are a review assistant for Maven / Spring Boot dependency upgrades (Spring PetClinic).

Do not edit application code. Do not commit. Do not change pom.xml. Do not post a discussion comment.

${target}

Policy already chose these upgrades:
${JSON.stringify(auto, null, 2)}

A version number change in pom.xml is the easy part. Your job is the hard part: what actually changed, whether this project is compatible, and what to re-test.

Follow these steps:

1. Read the MR (title, description, changed files) if a URL was given. Identify each library (group:artifact) and old → new version.

2. Read pom.xml (and build.gradle if present). For each library decide: direct vs transitive / BOM-managed, Maven scope (compile / runtime / test), and update kind (major / minor / patch).

3. Search src/main and src/test for types, packages, and annotations that library provides. If there is no direct usage, say so (typical for transitive / BOM-managed deps).

4. Recommend a re-test range:
   - Prefer the test classes that cover the call sites you found.
   - Narrow \`./mvnw test -Dtest=...\` vs full \`./mvnw verify\` based on update kind and coupling. Spring Boot BOM / starter / Tomcat / Thymeleaf template engine changes usually need a wider run.
   - Spring test slices: repository changes → @DataJpaTest; controller / MVC / template changes → @WebMvcTest / MockMvc.

5. Fetch that library's own release notes / changelog / GitHub releases for the version range (old exclusive → new inclusive). Look for removed/renamed APIs, config keys, and behavior changes. Judge whether those apply to THIS repo's call sites. Do not stop at "the number went up."

6. Write the analysis in English. Append it to the existing description; keep whatever is already there. Use exactly this shape (keep the emoji; they make the scan-at-a-glance DX better):

## 📦 Upgrade impact analysis

### 📋 What changed
- Library: \`<group:artifact>\` \`X.Y.Z → A.B.C\` (kind: major/minor/patch, scope: xxx, direct/transitive)

### 🔎 Code that may be affected
- \`src/main/java/...\` — how it is used
  (If nothing is found: "No direct usages found (likely a transitive / BOM-managed dependency).")

### 🧪 Recommended re-test scope
- Exact commands and test classes

### ⚠️ Breaking changes / notes
- Items from the library release notes that apply to this repo. If none: "No relevant breaking changes found."
- Include the release-notes URLs you read.

### Overall risk
- Use exactly one of: 🟢 Low / 🟡 Medium / 🔴 High, then a one-sentence rationale
`;
}

export function resumePrompt(logText: string, extra?: string): string {
  return `The remediation merge request's pipeline (or your previous run) needs a follow-up.

Continue the same work. Do not start over. Do not bump packages that policy marked comment_only.

${extra ? `Operator note:\n${extra}\n` : ""}
Failure / CI log:
${logText}
`;
}

export function extractJson<T>(text: string): T {
  const fenced = text.match(/```(?:json)?\s*([\s\S]*?)```/i);
  const raw = fenced?.[1] ?? text;
  const start = raw.indexOf("{");
  const end = raw.lastIndexOf("}");
  if (start === -1 || end === -1) {
    throw new Error("triage agent did not return JSON");
  }
  return JSON.parse(raw.slice(start, end + 1)) as T;
}

export function assertTriageReport(value: unknown): TriageReport {
  if (!value || typeof value !== "object") {
    throw new Error("triage JSON is not an object");
  }
  const report = value as TriageReport;
  if (!Array.isArray(report.items)) {
    throw new Error("triage JSON missing items[]");
  }
  return report;
}
