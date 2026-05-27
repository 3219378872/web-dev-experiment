#!/bin/sh
set -eu

# Smoke tests for hmall microservice stack
# Usage: sh scripts/smoke/smoke.sh [BASE_URL]
#   default BASE_URL: http://localhost:8080

BASE_URL="${1:-http://localhost:8080}"
PASS=0
FAIL=0

green()  { printf '\033[32m%s\033[0m\n' "$1"; }
red()    { printf '\033[31m%s\033[0m\n' "$1"; }

check() {
    _num="$1"; _method="$2"; _path="$3"; _expected="$4"
    shift 4
    _url="${BASE_URL}${_path}"

    _http_code=$(curl -s -o /tmp/smoke_resp.txt -w '%{http_code}' \
        --connect-timeout 5 --max-time 10 \
        -X "$_method" "$@" "$_url" 2>/tmp/smoke_err.txt || true)

    if [ "$_http_code" = "$_expected" ]; then
        green "[PASS] #${_num} ${_method} ${_path} -> ${_http_code}"
        PASS=$((PASS + 1))
        return 0
    else
        red "[FAIL] #${_num} ${_method} ${_path} -> expected ${_expected}, got ${_http_code}"
        echo "  Response: $(head -c 200 /tmp/smoke_resp.txt)"
        FAIL=$((FAIL + 1))
        return 1
    fi
}

check_json() {
    _num="$1"; _method="$2"; _path="$3"; _expected="$4"
    shift 4
    _url="${BASE_URL}${_path}"

    _http_code=$(curl -s -o /tmp/smoke_resp.txt -w '%{http_code}' \
        --connect-timeout 5 --max-time 10 \
        -X "$_method" "$@" "$_url" 2>/tmp/smoke_err.txt || true)

    if [ "$_http_code" != "$_expected" ]; then
        red "[FAIL] #${_num} ${_method} ${_path} -> expected ${_expected}, got ${_http_code}"
        echo "  Response: $(head -c 200 /tmp/smoke_resp.txt)"
        FAIL=$((FAIL + 1))
        return 1
    fi

    if jq -e . /tmp/smoke_resp.txt >/dev/null 2>&1; then
        green "[PASS] #${_num} ${_method} ${_path} -> ${_http_code} (valid JSON)"
        PASS=$((PASS + 1))
        return 0
    else
        red "[FAIL] #${_num} ${_method} ${_path} -> ${_http_code} but invalid JSON"
        FAIL=$((FAIL + 1))
        return 1
    fi
}

echo "============================================"
echo " hmall Smoke Tests"
echo " target: ${BASE_URL}"
echo "============================================"
echo ""

# ---- Phase 1: Health Check ----
echo "--- Phase 1: Health ---"

check 1 GET "/hi" 200

# ---- Phase 2: Public Item Endpoints ----
echo ""
echo "--- Phase 2: Public APIs ---"

check_json 2 GET "/items/page?page=1&size=10" 200
check_json 3 GET "/search/list?key=%E6%B5%8B%E8%AF%95" 200
check_json 4 GET "/categories" 200
check_json 5 GET "/notifications/active" 200

# ---- Phase 3: Auth Flow ----
echo ""
echo "--- Phase 3: Auth ---"

LOGIN_RESP=$(curl -s --connect-timeout 5 --max-time 10 \
    -X POST "${BASE_URL}/users/login" \
    -H 'Content-Type: application/json' \
    -d '{"username":"testuser","password":"admin123"}')

TOKEN=$(echo "$LOGIN_RESP" | jq -r '.data.token // .token // empty' 2>/dev/null || true)

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    green "[PASS] #6 POST /users/login -> token extracted"
    PASS=$((PASS + 1))
else
    red "[FAIL] #6 POST /users/login -> failed to extract token"
    echo "  Response: ${LOGIN_RESP}"
    FAIL=$((FAIL + 1))
fi

# Authenticated request
if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    check 7 GET "/addresses" 200 -H "authorization:${TOKEN}"
    # Auth rejection
    check 8 GET "/addresses" 401
fi

# ---- Phase 4: Auth Error Cases ----
echo ""
echo "--- Phase 4: Auth Errors ---"

WRONG_RESP=$(curl -s -o /tmp/smoke_resp.txt -w '%{http_code}' \
    --connect-timeout 5 --max-time 10 \
    -X POST "${BASE_URL}/users/login" \
    -H 'Content-Type: application/json' \
    -d '{"username":"testuser","password":"wrongpassword"}')

if [ "$WRONG_RESP" = "400" ] || [ "$WRONG_RESP" = "401" ]; then
    green "[PASS] #9 POST /users/login (bad pw) -> ${WRONG_RESP}"
    PASS=$((PASS + 1))
else
    red "[FAIL] #9 POST /users/login (bad pw) -> expected 400/401, got ${WRONG_RESP}"
    FAIL=$((FAIL + 1))
fi

check 10 GET "/addresses" 401 -H "authorization:invalid-token"

# ---- Summary ----
echo ""
echo "============================================"
TOTAL=$((PASS + FAIL))
echo " Results: ${PASS}/${TOTAL} passed"
echo "============================================"

if [ "$FAIL" -gt 0 ]; then
    red "${FAIL} test(s) FAILED"
    exit 1
else
    green "All smoke tests PASSED"
    exit 0
fi
