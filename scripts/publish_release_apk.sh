#!/usr/bin/env bash
set -euo pipefail

APK_PATH="${1:-app/build/outputs/apk/debug/app-debug.apk}"
OUT_PATH="${2:-release/elder-security-debug.apk}"

if [[ ! -f "$APK_PATH" ]]; then
  echo "[ERROR] APK not found: $APK_PATH"
  echo "Run build first: ./gradlew assembleDebug"
  exit 1
fi

mkdir -p "$(dirname "$OUT_PATH")"
cp "$APK_PATH" "$OUT_PATH"

SHA256="$(sha256sum "$OUT_PATH" | awk '{print $1}')"
SIZE="$(stat -c%s "$OUT_PATH")"

cat > release/RELEASE_METADATA.txt <<META
artifact=$(basename "$OUT_PATH")
sha256=$SHA256
size_bytes=$SIZE
source_apk=$APK_PATH
META

echo "[OK] Release artifact created: $OUT_PATH"
echo "[OK] Metadata written: release/RELEASE_METADATA.txt"
