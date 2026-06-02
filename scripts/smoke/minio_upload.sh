#!/bin/sh
set -eu

# MinIO upload+fetch smoke test against a running hmall stack.
# Verifies: POST /upload/image stores object in MinIO and returns a
# /files/<key> URL that GET /files/** proxies back with HTTP 200.
#
# Usage: sh scripts/smoke/minio_upload.sh [BASE_URL]
#   default BASE_URL: http://localhost:8080

BASE_URL="${1:-http://localhost:8080}"

green() { printf '\033[32m%s\033[0m\n' "$1"; }
red()   { printf '\033[31m%s\033[0m\n' "$1"; }

TMP="$(mktemp)"
trap 'rm -f "$TMP" /tmp/minio_smoke_resp.json' EXIT
head -c 1024 /dev/urandom > "$TMP"

printf 'uploading to %s/upload/image ...\n' "$BASE_URL"
if ! curl -sf -F "file=@${TMP};type=image/jpeg;filename=smoke.jpg" \
        "${BASE_URL}/upload/image" > /tmp/minio_smoke_resp.json; then
    red "FAIL: upload request failed"
    exit 1
fi

# Extract "url":"/files/..." from the JSON response without a JSON parser.
URL="$(sed -n 's/.*"url"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' /tmp/minio_smoke_resp.json)"
if [ -z "$URL" ]; then
    red "FAIL: no url field in upload response:"
    cat /tmp/minio_smoke_resp.json
    exit 1
fi
printf 'got url: %s\n' "$URL"

case "$URL" in
    /files/*) : ;;
    *) red "FAIL: url not in /files/<key> form: $URL"; exit 1 ;;
esac

printf 'fetching %s%s ...\n' "$BASE_URL" "$URL"
CODE="$(curl -s -o /dev/null -w '%{http_code}' "${BASE_URL}${URL}")"
if [ "$CODE" != "200" ]; then
    red "FAIL: fetch returned HTTP $CODE"
    exit 1
fi

green "SMOKE OK: upload+fetch via MinIO proxy"
