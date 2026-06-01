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
  # Split files: "safe" (fully staged, no unstaged changes → can --write)
  # vs "unsafe" (partial staging, unstaged chunks exist → --check only)
  safe_files=()
  unsafe_files=()
  for pf in $all; do
    # repo-relative path for git operations (git diff works from repo root)
    git_rel="${pf#$ROOT_DIR/}"
    if git diff --quiet -- "$git_rel" 2>/dev/null; then
      safe_files+=("$pf")
    else
      unsafe_files+=("$pf")
    fi
  done

  # Format safe files (no unstaged hunks → safe to modify working tree)
  if [ ${#safe_files[@]} -gt 0 ]; then
    echo "[pre-commit] $proj: formatting ${#safe_files[@]} fully-staged file(s)..."
    if ! (cd "$proj_dir" && npx --no-install prettier --write --log-level error "${safe_files[@]}"); then
      echo ""
      echo "⚠️  Prettier failed on staged files in $proj."
      echo "   Check that prettier is installed: cd $proj && npm ci"
      HAD_ERROR=1
    fi
    for pf in "${safe_files[@]}"; do
      git add "$pf"
    done
  fi

  # Validate unsafe files (partially staged → check only, don't touch working tree)
  if [ ${#unsafe_files[@]} -gt 0 ]; then
    echo "[pre-commit] $proj: checking ${#unsafe_files[@]} partially-staged file(s)..."
    if ! (cd "$proj_dir" && npx --no-install prettier --check --log-level error "${unsafe_files[@]}"); then
      echo ""
      echo "⚠️  Prettier found formatting issues in partially-staged files."
      echo "   Run manually: cd $proj && npx prettier --write <file>"
      echo "   Then re-stage to include in this commit."
      HAD_ERROR=1
    fi
  fi

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
    if ! (cd "$proj_dir" && npx --no-install eslint --quiet "${rel_lint_files[@]}"); then
      echo ""
      echo "⚠️  ESLint found errors in $proj. Fix them before committing."
      echo "   Run: cd $proj && npm run lint:fix"
      HAD_ERROR=1
    fi
  fi
done

if [ $HAD_ERROR -ne 0 ]; then
  exit 1
fi

echo "[pre-commit] Frontend lint: OK"
exit 0
