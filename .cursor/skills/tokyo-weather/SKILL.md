---
name: tokyo-weather
description: Append the current Tokyo temperature, humidity, and precipitation probability to weather.md. Use when asked to update weather.md or log Tokyo weather.
---

# Tokyo weather log

Append one observation to `weather.md`. Do not rewrite existing rows or edit other files.

## Fetch

```bash
curl -fsS "https://api.open-meteo.com/v1/forecast?latitude=35.6812&longitude=139.7671&current=temperature_2m,relative_humidity_2m,precipitation&hourly=precipitation_probability&timezone=Asia%2FTokyo&forecast_days=1"
```

## Row format

`| YYYY-MM-DD HH:mm | <temp> | <humidity> | <precip%> | Open-Meteo |`

- Timezone is Asia/Tokyo.
- Precipitation probability is the hourly value closest to `current.time`.
- If `weather.md` is missing, recreate the header and table from git history, then append.
