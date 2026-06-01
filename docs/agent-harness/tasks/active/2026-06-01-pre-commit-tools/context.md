# Context: pre-commit hooks 与前端代码质量工具

## Objective

为仓库添加 pre-commit hooks 和前端代码质量工具（ESLint + Prettier），
并确保 CI lint 门控全部通过（含 K005 共变检查）。

## Scope

- In scope: `.pre-commit-config.yaml`、`scripts/pre-commit-frontend.sh`、
  `scripts/engineering-lint.py`（K005 增强）、hmall-web / hmall-admin / e2e 的
  ESLint + Prettier 配置、knowledge base 页面同步
- Out of scope: 后端 Java 代码风格工具、e2e 测试逻辑变更

## Related Artifacts

- Spec: none — 开发工具链配置，不需要独立 spec
- Plan: none — 实施目标明确，不需要独立 plan

## Likely Files

- `.pre-commit-config.yaml`
- `scripts/pre-commit-frontend.sh`（新增）
- `scripts/engineering-lint.py`
- `scripts/_knowledge_base/checks.py`
- `hmall-web/package.json`、`.eslintrc.cjs`、`.prettierrc`
- `hmall-admin/package.json`、`.eslintrc.cjs`、`.prettierrc`
- `e2e/package.json`、`.eslintrc.cjs`、`.prettierrc`
- `docs/knowledge-base/modules/hmall-admin.md`
- `docs/knowledge-base/modules/hmall-web.md`

## Runtime Evidence

- `python3 scripts/agent_harness.py check` → passed
- `python3 scripts/knowledge_base.py check --base origin/main` → passed
- `python3 scripts/engineering-lint.py` → passed
- `npm run lint`（hmall-web / hmall-admin / e2e）→ passed

## Safety Constraints

- 不在源码中硬编码密钥/口令/Token
- 不破坏现有 API 响应格式
- pre-commit hook 区分 safe/unsafe 文件：safe（全部 staged → Prettier --write + git add）、
  unsafe（partial staging → Prettier --check 仅验证，不修改工作区文件）
