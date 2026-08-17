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
