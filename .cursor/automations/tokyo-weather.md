# Automation prompt: Tokyo weather → weather.md

Cursor の [Automations](https://cursor.com/automations/new) に貼るプロンプトです。

## 推奨トリガー

GitLab には「CI completed」トリガーがありません。次のどちらかを使います。

1. **Scheduled（おすすめ）** — 例: `0 * * * *`（毎時）または `0 9 * * *`（毎日 09:00 UTC）
2. **Webhook** — GitLab CI の `trigger-tokyo-weather-agent` ジョブから POST する（CI のたびに動かしたい場合）

リポジトリは **必ずこの GitLab リポジトリ** を指定してください。cron / webhook のデフォルトは「リポジトリなし」で、そのままだと `weather.md` を編集できません。

## Prompt

```
Update weather.md in this repository.

1. Fetch the current Tokyo weather from Open-Meteo (no API key):
   https://api.open-meteo.com/v1/forecast?latitude=35.6812&longitude=139.7671&current=temperature_2m,relative_humidity_2m,precipitation&hourly=precipitation_probability&timezone=Asia%2FTokyo&forecast_days=1

2. Read the existing weather.md. Do not rewrite or delete previous rows.

3. Append exactly one new markdown table row with:
   - 観測時刻 (JST): current.time from the API, formatted as YYYY-MM-DD HH:mm
   - 気温 (°C): current.temperature_2m
   - 湿度 (%): current.relative_humidity_2m
   - 降水確率 (%): hourly.precipitation_probability for the hour closest to current.time
   - 出典: Open-Meteo

4. If weather.md is missing, recreate it with the same header and table columns as before, then add the new row.

5. Commit the change and open (or update) a merge request. Do not change any other files.
```

## Dashboard 手順

1. [cursor.com/automations/new](https://cursor.com/automations/new) を開く
2. Trigger に Scheduled または Webhook を追加
3. 上の Prompt を貼る
4. Repository に `gitlab.com/naoharu1218/gitlab-sample` を指定
5. Pull request creation を有効のまま保存・有効化
6. Webhook を使う場合は、保存後に表示される URL と API key を GitLab CI/CD Variables へ入れる
   - `CURSOR_AUTOMATION_WEBHOOK_URL`
   - `CURSOR_AUTOMATION_API_KEY`（Masked）
