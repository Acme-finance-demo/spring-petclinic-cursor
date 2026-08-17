import type { PolicyDecision, TriageItem } from "./types.ts";

/** Live-extend hook: add "HIGH" here to auto-remediate that severity too. */
export const AUTO_REMEDIATE_SEVERITIES = new Set(["CRITICAL"]);

function normalizeSeverity(severity: string): string {
  return severity.trim().toUpperCase();
}

function majorVersion(version: string): number | null {
  const match = version.replace(/^[^0-9]*/, "").match(/^(\d+)/);
  return match ? Number(match[1]) : null;
}

export function isMajorBump(
  currentVersion: string,
  recommendedVersion: string | null,
): boolean {
  if (!recommendedVersion) return false;
  const current = majorVersion(currentVersion);
  const recommended = majorVersion(recommendedVersion);
  if (current === null || recommended === null) return false;
  return recommended > current;
}

export function decide(item: TriageItem): PolicyDecision {
  const severity = normalizeSeverity(item.severity);
  if (!AUTO_REMEDIATE_SEVERITIES.has(severity)) {
    return {
      action: "comment_only",
      reason: `severity ${severity} is outside the auto-remediate set`,
      item,
    };
  }
  if (!item.canFixByVersionBump || !item.recommendedVersion) {
    return {
      action: "comment_only",
      reason: "no safe version bump is available",
      item,
    };
  }
  if (
    item.isMajorBump ||
    isMajorBump(item.currentVersion, item.recommendedVersion)
  ) {
    return {
      action: "comment_only",
      reason: `major bump ${item.currentVersion} → ${item.recommendedVersion} needs a human`,
      item,
    };
  }
  return {
    action: "auto_remediate",
    reason: `CRITICAL patch/minor bump ${item.currentVersion} → ${item.recommendedVersion}`,
    item,
  };
}

export function decideAll(items: TriageItem[]): PolicyDecision[] {
  return items.map(decide);
}
