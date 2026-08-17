import type { Finding, PackageGroup } from "./types.ts";

type UnknownRecord = Record<string, unknown>;

function asRecord(value: unknown): UnknownRecord | undefined {
  return value !== null && typeof value === "object" && !Array.isArray(value)
    ? (value as UnknownRecord)
    : undefined;
}

function asString(value: unknown, fallback = ""): string {
  return typeof value === "string" && value.length > 0 ? value : fallback;
}

function packageName(vuln: UnknownRecord): string {
  if (typeof vuln.package === "string") return vuln.package;
  const location = asRecord(vuln.location);
  const dependency = asRecord(location?.dependency);
  const pkg = asRecord(dependency?.package);
  return asString(pkg?.name, "unknown");
}

function packageVersion(vuln: UnknownRecord): string {
  if (typeof vuln.version === "string") return vuln.version;
  const location = asRecord(vuln.location);
  const dependency = asRecord(location?.dependency);
  return asString(dependency?.version, "unknown");
}

export function parseFindings(raw: unknown): Finding[] {
  const root = asRecord(raw);
  const list = Array.isArray(root?.vulnerabilities)
    ? root.vulnerabilities
    : Array.isArray(raw)
      ? raw
      : [];

  return list.flatMap((entry) => {
    const vuln = asRecord(entry);
    if (!vuln) return [];
    return [
      {
        id: asString(vuln.id, asString(vuln.cve, "unknown")),
        severity: asString(vuln.severity, "UNKNOWN"),
        name: asString(vuln.name),
        description: asString(vuln.description),
        solution: asString(vuln.solution),
        package: packageName(vuln),
        version: packageVersion(vuln),
      },
    ];
  });
}

export function groupByPackage(findings: Finding[]): PackageGroup[] {
  const groups = new Map<string, PackageGroup>();
  for (const finding of findings) {
    const key = `${finding.package}@${finding.version}`;
    const existing = groups.get(key);
    if (existing) {
      existing.findings.push(finding);
      continue;
    }
    groups.set(key, {
      package: finding.package,
      version: finding.version,
      findings: [finding],
    });
  }
  return [...groups.values()];
}
