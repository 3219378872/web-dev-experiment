# Handoff: pre-commit hooks 与前端代码质量工具

## Status

PR #25 已创建。CI lint/test/integration/smoke 已通过。
Codex review 发现 2 个 blocking issues（正在修复中 Round 3）。

## Files Changed

- `.pre-commit-config.yaml`（新增 pre-commit hooks）
- `scripts/pre-commit-frontend.sh`（新增：前端 staged 文件格式化与 lint）
- `scripts/engineering-lint.py`（K005 增强：预提交阶段传入 --base）
- `scripts/_knowledge_base/checks.py`（K005 独立 committed/staged 检查）
- `hmall-web/.eslintrc.cjs`、`.eslintignore`、`.prettierrc`、`.prettierignore`（新增）
- `hmall-admin/.eslintrc.cjs`、`.eslintignore`、`.prettierrc`、`.prettierignore`（新增）
- `e2e/.eslintrc.cjs`、`.eslintignore`、`.prettierrc`、`.prettierignore`（新增）
- `hmall-web/package.json`（新增 lint/format scripts + devDependencies）
- `hmall-admin/package.json`（新增 lint/format scripts + devDependencies）
- `e2e/package.json`（新增 lint/format scripts + devDependencies）
- `docs/knowledge-base/modules/hmall-admin.md`（同步更新）
- `docs/knowledge-base/modules/hmall-web.md`（同步更新）
- `docs/agent-harness/tasks/active/2026-06-01-pre-commit-tools/`（本任务记录）

## Commands Run

- `cmp CLAUDE.md AGENTS.md` → byte equal
- `python3 scripts/agent_harness.py check` → passed
- `python3 scripts/knowledge_base.py check --base origin/main` → passed
- `python3 scripts/engineering-lint.py` → passed
- `cd hmall-web && npm run lint` → passed
- `cd hmall-admin && npm run lint` → passed
- `cd e2e && npm run lint` → passed

## Known Risks

- pre-commit hook 依赖 `npx --no-install`，需要 `node_modules` 已安装（`npm ci`）。
  首次 clone 后需先运行 `npm ci` 才能通过 pre-commit。
- `scripts/_knowledge_base/checks.py` 中的 `_git_changed_files` 修改会影响
  CI 和 pre-commit 两个调用路径。需要验证 CI 场景下 `git diff --cached` 为空
  不引入假阴性。

## Next Action

修复 codex review Round 2 blocking issues：
1. `pre-commit-frontend.sh:54` Prettier `|| true` 掩盖错误 → 改为 `if ! ...; then HAD_ERROR=1; fi`
2. 已提交的 KB 页面掩盖未 staged 的 tracked-path K005 检查 → 拆分为独立的 committed + staged 检查

修复后推送并等待 CI + codex review 重新通过。

## Worktree Or Branch

- `task/2026-06-01-pre-commit-tools`

## Branch And PR

- Base branch: `main`
- Task branch: `task/2026-06-01-pre-commit-tools`
- Remote branch: `origin/task/2026-06-01-pre-commit-tools`
- Pull request: #25（https://github.com/3219378872/web-dev-experiment/pull/25）

## CI And Review

- Round 1 (PR #24): lint passed, test/integration/smoke passed, codex-review failed — 2 blocking:
  1. Missing agent harness task record
  2. K005 pre-commit doesn't check staged changes
- Round 2 (PR #25): lint passed, test/integration/smoke passed, codex-review failed — 2 blocking:
  1. Prettier `|| true` masks failures (CSS-only, parse errors, missing binary)
  2. Committed KB page update masks unstaged tracked-path K005 bypass
- Round 3 (PR #25): 已推送修复
  - Prettier: `|| true` → `if ! ...; then echo error; HAD_ERROR=1; fi`
  - K005: `_git_changed_files` → `(committed, staged)` tuple;
    `check_co_change` runs separate committed + staged checks
