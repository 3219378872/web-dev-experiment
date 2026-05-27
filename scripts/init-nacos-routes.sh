#!/usr/bin/env bash
set -euo pipefail

# Seed Nacos with gateway-routes.json so hm-gateway can route to microservices.
# Usage: bash scripts/init-nacos-routes.sh [NACOS_URL]
#   default NACOS_URL: http://localhost:8848
#
# Run AFTER docker compose up -d and Nacos health check passes.
# If running from the host and Docker ports are unreachable (e.g. WSL2),
# run from inside a container:
#   docker compose exec nacos bash /path/to/init-nacos-routes.sh http://localhost:8848

NACOS_URL="${1:-http://localhost:8848}"

ROUTES='[
  {
    "id": "item-service",
    "uri": "lb://item-service",
    "predicates": [
      {
        "name": "Path",
        "args": {
          "_genkey_0": "/items/**",
          "_genkey_1": "/search/**",
          "_genkey_2": "/categories/**",
          "_genkey_3": "/admin/items/**"
        }
      }
    ]
  },
  {
    "id": "user-service",
    "uri": "lb://user-service",
    "predicates": [
      {
        "name": "Path",
        "args": {
          "_genkey_0": "/users/**",
          "_genkey_1": "/addresses/**",
          "_genkey_2": "/favorites/**"
        }
      }
    ]
  },
  {
    "id": "cart-service",
    "uri": "lb://cart-service",
    "predicates": [
      {
        "name": "Path",
        "args": { "_genkey_0": "/carts/**" }
      }
    ]
  },
  {
    "id": "trade-service",
    "uri": "lb://trade-service",
    "predicates": [
      {
        "name": "Path",
        "args": {
          "_genkey_0": "/orders/**",
          "_genkey_1": "/coupons/**",
          "_genkey_2": "/my-coupons/**",
          "_genkey_3": "/admin/coupons/**",
          "_genkey_4": "/admin/orders/**"
        }
      }
    ]
  },
  {
    "id": "pay-service",
    "uri": "lb://pay-service",
    "predicates": [
      {
        "name": "Path",
        "args": { "_genkey_0": "/pay-orders/**" }
      }
    ]
  },
  {
    "id": "notify-service",
    "uri": "lb://notify-service",
    "predicates": [
      {
        "name": "Path",
        "args": {
          "_genkey_0": "/notifications/**",
          "_genkey_1": "/messages/**",
          "_genkey_2": "/admin/notifications/**",
          "_genkey_3": "/admin/messages/**",
          "_genkey_4": "/feedbacks/**",
          "_genkey_5": "/my-feedbacks/**",
          "_genkey_6": "/admin/feedbacks/**"
        }
      }
    ]
  },
  {
    "id": "file-service",
    "uri": "lb://file-service",
    "predicates": [
      {
        "name": "Path",
        "args": {
          "_genkey_0": "/upload/**",
          "_genkey_1": "/files/**"
        }
      }
    ]
  }
]'

ENCODED=$(python3 -c "import urllib.parse, sys; print(urllib.parse.quote(sys.stdin.read(), safe=''))" <<< "$ROUTES")

HTTP_CODE=$(curl -s -o /dev/null -w '%{http_code}' --connect-timeout 10 --max-time 15 \
  -X POST "${NACOS_URL}/nacos/v1/cs/configs" \
  -d "dataId=gateway-routes.json&group=DEFAULT_GROUP&content=${ENCODED}")

if [ "$HTTP_CODE" = "200" ]; then
  echo "[OK] gateway-routes.json published to Nacos (${NACOS_URL})"
else
  echo "[FAIL] Nacos API returned HTTP ${HTTP_CODE}" >&2
  exit 1
fi
