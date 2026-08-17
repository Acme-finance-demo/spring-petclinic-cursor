import { readFile } from "node:fs/promises";
import { resumeAgent, runAgent } from "./agent.ts";
import { groupByPackage, parseFindings } from "./parse-report.ts";
import { decideAll } from "./policy.ts";
import {
  assertTriageReport,
  extractJson,
  remediatePrompt,
  resumePrompt,
  triagePrompt,
} from "./prompts.ts";
import { defaultStatePath, writeState } from "./state.ts";
import type { OrchestratorState, Runtime } from "./types.ts";

export type RunArgs = {
  apiKey: string;
  reportPath: string;
  runtime: Runtime;
  cwd: string;
  repoUrl: string;
  startingRef: string;
  pipelineId?: string;
  sha?: string;
  skipRemediate?: boolean;
};

export async function runOrchestrator(args: RunArgs): Promise<OrchestratorState> {
  const raw = JSON.parse(await readFile(args.reportPath, "utf8")) as unknown;
  const findings = parseFindings(raw);
  if (findings.length === 0) {
    console.error("[orchestrator] no vulnerabilities; stopping");
    const empty: OrchestratorState = {
      createdAt: new Date().toISOString(),
      runtime: args.runtime,
      pipelineId: args.pipelineId,
      sha: args.sha,
      decisions: [],
    };
    await writeState(defaultStatePath(args.cwd), empty);
    return empty;
  }

  const groups = groupByPackage(findings);
  console.error(
    `[orchestrator] ${findings.length} finding(s) in ${groups.length} package group(s)`,
  );

  const triage = await runAgent({
    apiKey: args.apiKey,
    name: "vuln-triage",
    prompt: triagePrompt(groups),
    runtime: args.runtime,
    cwd: args.cwd,
    repoUrl: args.repoUrl,
    startingRef: args.startingRef,
    autoCreatePR: false,
    idempotencyKey: args.sha ? `vuln-triage-${args.sha}` : undefined,
  });

  const report = assertTriageReport(extractJson(triage.text));
  const decisions = decideAll(report.items);
  const auto = decisions.filter((d) => d.action === "auto_remediate");
  const deferred = decisions.filter((d) => d.action === "comment_only");

  console.error("[orchestrator] policy:");
  for (const decision of decisions) {
    console.error(
      `  ${decision.action.padEnd(15)} ${decision.item.package}  ${decision.reason}`,
    );
  }

  const state: OrchestratorState = {
    createdAt: new Date().toISOString(),
    runtime: args.runtime,
    pipelineId: args.pipelineId,
    sha: args.sha,
    triageAgentId: triage.agentId,
    triageRunId: triage.runId,
    decisions,
  };

  if (args.skipRemediate || auto.length === 0) {
    if (auto.length === 0) {
      console.error(
        `[orchestrator] nothing to auto-remediate (${deferred.length} comment-only); skipping fix agent`,
      );
    }
    await writeState(defaultStatePath(args.cwd), state);
    return state;
  }

  const remediation = await runAgent({
    apiKey: args.apiKey,
    name: "vuln-remediate",
    prompt: remediatePrompt(decisions),
    runtime: args.runtime,
    cwd: args.cwd,
    repoUrl: args.repoUrl,
    startingRef: args.startingRef,
    autoCreatePR: args.runtime === "cloud",
    idempotencyKey: args.sha ? `vuln-fix-${args.sha}` : undefined,
  });

  state.remediationAgentId = remediation.agentId;
  state.remediationRunId = remediation.runId;
  state.prUrl = remediation.prUrl;
  state.branch = remediation.branch;
  await writeState(defaultStatePath(args.cwd), state);

  if (state.prUrl) console.error(`[orchestrator] MR: ${state.prUrl}`);
  console.error(`[orchestrator] resume with: agentId=${state.remediationAgentId}`);
  return state;
}

export async function resumeOrchestrator(args: {
  apiKey: string;
  agentId: string;
  cwd: string;
  logPath?: string;
  note?: string;
}): Promise<void> {
  const logText = args.logPath
    ? await readFile(args.logPath, "utf8")
    : "(no log file; follow the operator note)";
  const result = await resumeAgent({
    apiKey: args.apiKey,
    agentId: args.agentId,
    cwd: args.cwd,
    prompt: resumePrompt(logText, args.note),
  });
  console.error(`[orchestrator] resumed agentId=${result.agentId} runId=${result.runId}`);
  if (result.prUrl) console.error(`[orchestrator] MR: ${result.prUrl}`);
}
