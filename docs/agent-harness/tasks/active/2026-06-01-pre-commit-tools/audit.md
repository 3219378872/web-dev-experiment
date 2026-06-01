# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| pre-commit hooks 已配置 | passed | `.pre-commit-config.yaml` 含 engineering-lint + frontend-lint |
| ESLint 配置完整 | passed | hmall-web / hmall-admin / e2e 各有 `.eslintrc.cjs` + `.eslintignore` |
| Prettier 配置完整 | passed | hmall-web / hmall-admin / e2e 各有 `.prettierrc` + `.prettierignore` |
| K005 在 pre-commit 阶段检查 | passed | `engineering-lint.py` 传入 `--base origin/main` |
| K005 覆盖 staged 变更 | pending | `_git_changed_files` 需追加 `git diff --cached` |
| Knowledge base 页面同步 | passed | hmall-admin.md + hmall-web.md 已更新 |
| CLAUDE.md ↔ AGENTS.md 一致 | passed | byte identical |
| 前端 lint/format npm scripts 可用 | passed | `npm run lint` / `npm run format` 已配置 |
| 前端构建不破坏 | passed | hmall-web + hmall-admin build success |
| 不硬编码密钥 | passed | 仅配置文件与脚本，无密钥 |
