import assert from "node:assert/strict";
import { test } from "node:test";
import { groupByPackage, parseFindings } from "./parse-report.ts";
import { decide, isMajorBump } from "./policy.ts";
import type { TriageItem } from "./types.ts";

test("parseFindings reads GitLab container_scanning locations", () => {
  const findings = parseFindings({
    vulnerabilities: [
      {
        id: "CVE-2026-1",
        severity: "Critical",
        name: "tomcat",
        description: "x",
        solution: "Upgrade to 10.1.55",
        location: {
          dependency: {
            package: { name: "org.apache.tomcat.embed:tomcat-embed-core" },
            version: "10.1.44",
          },
        },
      },
    ],
  });
  assert.equal(findings[0]?.package, "org.apache.tomcat.embed:tomcat-embed-core");
  assert.equal(findings[0]?.version, "10.1.44");
});

test("parseFindings reads the flattened webhook shape", () => {
  const findings = parseFindings({
    vulnerabilities: [
      {
        id: "CVE-1",
        severity: "Critical",
        package: "commons-lang:commons-lang",
        version: "2.6",
      },
    ],
  });
  assert.equal(findings[0]?.package, "commons-lang:commons-lang");
});

test("groupByPackage collapses duplicate CVEs on the same GAV", () => {
  const groups = groupByPackage([
    {
      id: "CVE-1",
      severity: "Critical",
      name: "",
      description: "",
      solution: "",
      package: "a:b",
      version: "1.0",
    },
    {
      id: "CVE-2",
      severity: "Critical",
      name: "",
      description: "",
      solution: "",
      package: "a:b",
      version: "1.0",
    },
  ]);
  assert.equal(groups.length, 1);
  assert.equal(groups[0]?.findings.length, 2);
});

function item(overrides: Partial<TriageItem>): TriageItem {
  return {
    package: "org.apache.tomcat.embed:tomcat-embed-core",
    currentVersion: "10.1.44",
    recommendedVersion: "10.1.55",
    cves: ["CVE-2026-43515"],
    severity: "CRITICAL",
    isDirectDependency: false,
    isMajorBump: false,
    canFixByVersionBump: true,
    notes: "",
    ...overrides,
  };
}

test("policy auto-remediates CRITICAL patch bumps", () => {
  assert.equal(decide(item({})).action, "auto_remediate");
});

test("policy defers major bumps even when the agent says it is safe", () => {
  const decision = decide(
    item({
      currentVersion: "3.5.0",
      recommendedVersion: "4.0.0",
      isMajorBump: false,
    }),
  );
  assert.equal(decision.action, "comment_only");
});

test("policy defers findings without a fixed version", () => {
  assert.equal(
    decide(item({ recommendedVersion: null, canFixByVersionBump: false })).action,
    "comment_only",
  );
});

test("isMajorBump uses the first numeric component", () => {
  assert.equal(isMajorBump("10.1.44", "10.1.55"), false);
  assert.equal(isMajorBump("3.5.0", "4.0.1"), true);
});
