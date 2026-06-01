#!/bin/bash
# Pre-commit hook: format and lint staged frontend files per project.
# Auto-fixes formatting with Prettier; reports ESLint errors.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
STAGED_FILES=$(git diff --cached --name-only --diff-filter=ACM)

if [ -z "$STAGED_FILES" ]; then
  exit 0
fi

# ── Group staged files by project ───────────────────────────────────────
declare -A PROJECT_FILES
PROJECT_DIRS=()

while IFS= read -r f; do
  ext="${f##*.}"
  case "$ext" in
    js|vue|ts|css) ;;
    *) continue ;;
  esac

  case "$f" in
    hmall-web/*)   proj="hmall-web" ;;
    hmall-admin/*) proj="hmall-admin" ;;
    e2e/*)         proj="e2e" ;;
    *)             continue ;;
  esac

  abs_path="$ROOT_DIR/$f"
  PROJECT_FILES["${proj}_files"]="${PROJECT_FILES["${proj}_files"]:-} $abs_path"

  if [[ ! " ${PROJECT_DIRS[*]:-} " =~ " $proj " ]]; then
    PROJECT_DIRS+=("$proj")
  fi
done <<<"$STAGED_FILES"

if [ ${#PROJECT_DIRS[@]} -eq 0 ]; then
  exit 0
fi

HAD_ERROR=0

for proj in "${PROJECT_DIRS[@]}"; do
  all="${PROJECT_FILES["${proj}_files"]}"
  all="${all# }"
  if [ -z "$all" ]; then continue; fi

  proj_dir="$ROOT_DIR/$proj"

  # ── Prettier ──────────────────────────────────────────────────────
  echo "[pre-commit] $proj: running Prettier..."
  (cd "$proj_dir" && npx --no-install prettier --write --log-level error $all) || true
  for pf in $all; do
    git add "$pf"
  done

  # ── ESLint (js/vue/ts only) ───────────────────────────────────────
  lint_files=()
  for pf in $all; do
    ext="${pf##*.}"
    case "$ext" in js|vue|ts) lint_files+=("$pf") ;; esac
  done
  if [ ${#lint_files[@]} -gt 0 ]; then
    echo "[pre-commit] $proj: running ESLint..."
    rel_lint_files=()
    for lf in "${lint_files[@]}"; do
      rel_lint_files+=("${lf#$proj_dir/}")
    done
    (cd "$proj_dir" && npx --no-install eslint --quiet "${rel_lint_files[@]}") || {
      echo ""
      echo "⚠️  ESLint found errors in $proj. Fix them before committing."
      echo "   Run: cd $proj && npm run lint:fix"
      HAD_ERROR=1
    }
  fi
done

if [ $HAD_ERROR -ne 0 ]; then
  exit 1
fi

echo "[pre-commit] Frontend lint: OK"
exit 0
