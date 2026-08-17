import { existsSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { resumeOrchestrator, runOrchestrator } from "./orchestrate.ts";
import { defaultStatePath, readState } from "./state.ts";
import type { Runtime } from "./types.ts";

function flag(name: string): boolean {
  return process.argv.includes(name);
}

function opt(name: string): string | undefined {
  const index = process.argv.indexOf(name);
  if (index === -1) return undefined;
  return process.argv[index + 1];
}

function findRepoRoot(start: string): string {
  let dir = start;
  for (;;) {
    if (existsSync(join(dir, "pom.xml")) || existsSync(join(dir, ".gitlab-ci.yml"))) {
      return dir;
    }
    const parent = dirname(dir);
    if (parent === dir) return start;
    dir = parent;
  }
}

function defaultRuntime(): Runtime {
  const fromEnv = process.env.VULN_RUNTIME;
  if (fromEnv === "local" || fromEnv === "cloud") return fromEnv;
  return process.env.CI ? "cloud" : "local";
}

function usage(): never {
  console.error(`Usage:
  npx tsx src/cli.ts run --report <gl-trivy-fs-report.json> [--runtime local|cloud] [--skip-remediate]
  npx tsx src/cli.ts resume [--agent-id <id>] [--log <ci-log.txt>] [--note <text>]

Env:
  CURSOR_API_KEY          required
  CURSOR_REPO_URL         cloud clone URL (CI: $CI_PROJECT_URL)
  CURSOR_STARTING_REF     cloud git ref (CI: $CI_COMMIT_SHA)
  CURSOR_MODEL            default composer-2.5
  VULN_RUNTIME            local | cloud (CI defaults to cloud)
`);
  process.exit(64);
}

async function main(): Promise<void> {
  const command = process.argv[2];
  if (command !== "run" && command !== "resume") usage();

  const apiKey = process.env.CURSOR_API_KEY;
  if (!apiKey) {
    console.error("CURSOR_API_KEY is required");
    process.exit(1);
  }

  const here = dirname(fileURLToPath(import.meta.url));
  const cwd = resolve(opt("--cwd") ?? findRepoRoot(resolve(here, "../..")));

  if (command === "run") {
    const reportPath = opt("--report");
    if (!reportPath) usage();
    const runtime = (opt("--runtime") as Runtime | undefined) ?? defaultRuntime();
    if (runtime !== "local" && runtime !== "cloud") usage();

    await runOrchestrator({
      apiKey,
      reportPath: resolve(reportPath),
      runtime,
      cwd,
      repoUrl:
        process.env.CURSOR_REPO_URL ??
        process.env.CI_PROJECT_URL ??
        "https://gitlab.com/naoharu1218/gitlab-sample",
      startingRef:
        process.env.CURSOR_STARTING_REF ??
        process.env.CI_COMMIT_SHA ??
        process.env.CI_COMMIT_REF_NAME ??
        "main",
      pipelineId: process.env.CI_PIPELINE_ID,
      sha: process.env.CI_COMMIT_SHA,
      skipRemediate: flag("--skip-remediate"),
    });
    return;
  }

  const statePath = defaultStatePath(cwd);
  const saved = existsSync(statePath) ? await readState(statePath) : undefined;
  const agentId = opt("--agent-id") ?? saved?.remediationAgentId;
  if (!agentId) {
    console.error("resume needs --agent-id or a saved remediationAgentId in state");
    process.exit(1);
  }

  await resumeOrchestrator({
    apiKey,
    agentId,
    cwd,
    logPath: opt("--log"),
    note: opt("--note"),
  });
}

main().catch((err: unknown) => {
  const message = err instanceof Error ? err.message : String(err);
  console.error(message);
  const exitCode =
    err && typeof err === "object" && "exitCode" in err && typeof err.exitCode === "number"
      ? err.exitCode
      : 1;
  process.exit(exitCode);
});
