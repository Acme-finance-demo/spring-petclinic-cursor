# Automation prompt: Trivy CRITICAL → dependency fix MR

Cursor の [Automations](https://cursor.com/automations/new) に貼るプロンプトです。
GitLab CI の `trigger-trivy-agent` が Trivy レポートを webhook で渡し、Cloud Agent が依存関係の修正 MR を作ります。

## 推奨トリガー

**Webhook のみ**（Scheduled は使わない）

GitLab の `trivy-dependency-scan` 完了後にだけ動かしたいためです。

リポジトリは **必ずこの GitLab リポジトリ** を指定してください。webhook のデフォルトは「リポジトリなし」で、そのままだと `pom.xml` を編集できません。

## Prompt

```
You received a GitLab CI webhook after a Trivy filesystem vulnerability scan.

The JSON body includes:
- source, project, pipeline_id, pipeline_url, ref, sha
- vulnerability_count
- vulnerabilities[]: id, severity, name, description, solution, package, version
- trivy_report: the full GitLab container_scanning-format report (optional detail)

Your job:

1. Read the webhook payload. If vulnerability_count is 0 or vulnerabilities is empty, do nothing and stop.

2. Focus on CRITICAL findings (and any other severities present in the payload). Group by Maven package (package field). For each package, choose the highest fixed version required by the solutions (e.g. if one CVE needs 3.1.4 and another needs 3.1.5, use 3.1.5.RELEASE).

3. Inspect this repository's dependency declarations (pom.xml and, if present, build.gradle). Prefer upgrading via the existing Spring Boot / BOM managed versions when possible. Only add explicit version overrides when necessary.

4. Apply the minimal dependency upgrades needed to remediate the reported CVEs. Do not refactor unrelated code. Do not change CI config unless required for the version bump.

5. Commit on a new branch and open (or update) a merge request. MR title/body should list the CVE IDs and packages upgraded.

6. If a finding cannot be fixed by a version bump alone, explain why in the MR description and leave a clear TODO; still open the MR with whatever safe upgrades you could make.
```

## Dashboard 手順

1. [cursor.com/automations/new](https://cursor.com/automations/new) を開く
2. Trigger に **Webhook** を追加
3. 上の Prompt を貼る
4. Repository に `gitlab.com/naoharu1218/gitlab-sample` を指定
5. Pull request creation を有効のまま保存・有効化
6. 保存後に表示される URL と API key を GitLab CI/CD Variables へ入れる
   - `CURSOR_TRIVY_WEBHOOK_URL`
   - `CURSOR_TRIVY_API_KEY`（Masked）

Tokyo weather 用の `CURSOR_AUTOMATION_*` とは別変数にしてください。
