#!/usr/bin/env bash
# CI の trivy-dependency-scan と同じ: CRITICAL/HIGH/MEDIUM、Trivy JSON、検出しても exit 0。
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$ROOT"

SKIP_RESOLVE=0
for arg in "$@"; do
  case "$arg" in
    --skip-resolve) SKIP_RESOLVE=1 ;;
    -h|--help)
      echo "Usage: $0 [--skip-resolve]"
      echo "Writes gl-trivy-fs-report.json at the repository root."
      exit 0
      ;;
    *)
      echo "unknown argument: $arg" >&2
      exit 64
      ;;
  esac
done

if ! command -v trivy >/dev/null 2>&1; then
  echo "trivy が見つかりません。macOS なら: brew install trivy" >&2
  exit 1
fi

if [[ "$SKIP_RESOLVE" -eq 0 ]]; then
  echo "Resolving Maven dependencies into ~/.m2…" >&2
  ./mvnw -B -DskipTests dependency:resolve
fi

OUT="$ROOT/gl-trivy-fs-report.json"

echo "Scanning (CRITICAL,HIGH,MEDIUM)…" >&2
trivy fs --scanners vuln --severity CRITICAL,HIGH,MEDIUM \
  --skip-dirs target --skip-dirs cursor-sdk --skip-dirs node_modules --skip-dirs .git \
  --format json --output "$OUT" \
  .

trivy fs --scanners vuln --severity CRITICAL,HIGH,MEDIUM \
  --skip-dirs target --skip-dirs cursor-sdk --skip-dirs node_modules --skip-dirs .git \
  --exit-code 0 --format table \
  .

echo "Wrote $OUT" >&2
