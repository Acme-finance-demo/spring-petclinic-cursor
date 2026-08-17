export type Runtime = "local" | "cloud";

export type Finding = {
  id: string;
  severity: string;
  name: string;
  description: string;
  solution: string;
  package: string;
  version: string;
};

export type PackageGroup = {
  package: string;
  version: string;
  findings: Finding[];
};

export type TriageItem = {
  package: string;
  currentVersion: string;
  recommendedVersion: string | null;
  cves: string[];
  severity: string;
  isDirectDependency: boolean;
  isMajorBump: boolean;
  canFixByVersionBump: boolean;
  notes: string;
};

export type TriageReport = {
  summary: string;
  items: TriageItem[];
};

export type PolicyAction = "auto_remediate" | "comment_only";

export type PolicyDecision = {
  action: PolicyAction;
  reason: string;
  item: TriageItem;
};

export type OrchestratorState = {
  createdAt: string;
  runtime: Runtime;
  pipelineId?: string;
  sha?: string;
  triageAgentId?: string;
  triageRunId?: string;
  remediationAgentId?: string;
  remediationRunId?: string;
  prUrl?: string;
  branch?: string;
  decisions: PolicyDecision[];
};
