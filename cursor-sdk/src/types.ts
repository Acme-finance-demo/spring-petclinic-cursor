export type Runtime = "local" | "cloud";

/** どのフォージ上で動いているか。MR/PR の語彙と repo URL の解決に使う。 */
export type Host = "github" | "gitlab";

export type HostVocab = {
  /** "GitHub" / "GitLab" */
  host: string;
  /** "pull request" / "merge request" */
  pr: string;
  /** "PR" / "MR" */
  prShort: string;
  /** "GitHub Issue" / "GitLab Issue" */
  issue: string;
  /** "pull request comment" / "discussion comment" */
  comment: string;
};

export type HostContext = {
  host: Host;
  vocab: HostVocab;
  repoUrl: string;
  startingRef: string;
  sha?: string;
  pipelineId?: string;
};

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
  host?: Host;
  pipelineId?: string;
  sha?: string;
  triageAgentId?: string;
  triageRunId?: string;
  remediationAgentId?: string;
  remediationRunId?: string;
  impactAgentId?: string;
  impactRunId?: string;
  prUrl?: string;
  branch?: string;
  decisions: PolicyDecision[];
};
