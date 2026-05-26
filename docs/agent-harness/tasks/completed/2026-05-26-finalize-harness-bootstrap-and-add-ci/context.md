# Context: Finalize Harness Bootstrap And Add Ci

## Objective
完成 task #1 的 PR 闭环收尾（任务记录归档到 completed/）并按 bidking-controller
模式补齐 GitHub Actions 三个工作流，使后续所有任务都能在真实 CI 上验证。

## Scope
- In scope:
  - 把 `2026-05-26-establish-harness-and-test-pyramid` 从 `active/` 移到 `completed/`
    （task.yaml status: merged，pr_waiver 解释 bootstrap chicken-and-egg）。
  - 新增 `.github/workflows/ci.yml`（lint / test / integration / smoke /
    codex-review 五个 job）。
  - 新增 `.github/workflows/knowledge-base-sync.yml`（每周一 KB 漂移巡检 PR）。
  - 新增 `.github/workflows/pr-cleanup.yml`（PR 合并后删除远程分支）。
  - 更新 `CLAUDE.md` / `AGENTS.md` / `docs/agent-harness/quality-rules.md`
    增加 CI 描述。
- Out of scope:
  - 实际新增测试代码（属于 task #2–#5）。
  - 配置仓库 secrets（用户侧操作）。

## Related Artifacts
- Spec: none - 工程化补丁，无独立 spec；参考 `../bidking-controller/.github/workflows/`。
- Plan: none - 三个文件 + 文档同步，无需多步实施 plan。

## Likely Files
- `.github/workflows/ci.yml`
- `.github/workflows/knowledge-base-sync.yml`
- `.github/workflows/pr-cleanup.yml`
- `CLAUDE.md`
- `AGENTS.md`
- `docs/agent-harness/quality-rules.md`
- `docs/agent-harness/tasks/active/2026-05-26-establish-harness-and-test-pyramid/`
  （移到 completed/）

## Runtime Evidence To Inspect First
- 参考实现：`/home/dev/projects/bidking-controller/.github/workflows/`。
- 本仓库现有 `docker-compose.yml`：确认 service 名称与 health 配置一致。
- `pom.xml` 与各服务 pom：确认 `mvn -B -ntp test` 当前能跑（即便 0 测试）。

## Safety Constraints
- 不在 workflow 中硬编码任何凭据；codex-review 走 secrets，缺失即硬失败。
- `pr-cleanup` 只能删同仓库的分支（fork PR 不动）。
- 不修改既有业务代码。

## Branch And PR
- Base branch: `main`
- Task branch: `task/2026-05-26-finalize-harness-bootstrap-and-add-ci`
- Remote branch: `origin/task/2026-05-26-finalize-harness-bootstrap-and-add-ci`
- Pull request: none - create after local acceptance and push.

## Open Questions
- 仓库尚未配置 `OPENAI_API_KEY` / `OPENAI_RESPONSES_API_ENDPOINT` secrets，
  本 PR 的 codex-review job 必然失败。用户在 PR 描述中确认并继续 merge，
  或先用 admin 临时绕过；都需在 handoff.md 记录。
