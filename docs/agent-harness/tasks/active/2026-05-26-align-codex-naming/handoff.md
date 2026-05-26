# Handoff: Align Codex Naming

## Status
PR-open，等本次 task-record 提交后 codex-review 复跑验证。

## Files Changed
- `.github/workflows/ci.yml` —— `codex-review.Verify Codex secrets` step 的两个
  shell-local 临时变量 rename：
  - `HAS_KEY` → `HAS_OPENAI_API_KEY`
  - `HAS_ENDPOINT` → `HAS_OPENAI_RESPONSES_API_ENDPOINT`
- 本次 PR 期间额外的中间提交（`diag`/`revert`）只为定位根因，最终 diff
  与 main 相比仍只有上述一处 rename。
- GitHub 仓库 Secrets（仓库外部，不在 git 历史中）：
  - `OPENAI_RESPONSES_API_ENDPOINT`: `https://www.dogapi.cc/v1` → `https://www.dogapi.cc/v1/responses`
- `docs/agent-harness/tasks/active/2026-05-26-align-codex-naming/`（本任务记录）

## Commands Run
- `python3 scripts/agent_harness.py new align-codex-naming --date 2026-05-26`
- `python3 scripts/agent_harness.py check`
- `python3 scripts/knowledge_base.py check`
- `python3 scripts/engineering-lint.py`
- `printf %s "https://www.dogapi.cc/v1/responses" | gh secret set OPENAI_RESPONSES_API_ENDPOINT`
- `gh secret list` —— 确认 `OPENAI_RESPONSES_API_ENDPOINT` timestamp 刷新
- `gh run view 26457968513 --log` —— 确认 codex 跑完整次 review、只剩内容
  判定阻塞

## Known Risks
- dogapi.cc 是第三方聚合代理，未来若更换 OpenAI 官方 endpoint 或换代理，
  `OPENAI_RESPONSES_API_ENDPOINT` 的路径后缀规则可能不同；后续在 handoff
  里写明 endpoint 必须显式包含 `/responses`，避免再次踩坑。
- codex-action 在 endpoint 缺 `/responses` 时不返回 4xx，而是连续 reconnect
  断流，日志极易被误判为「网络抖动」；本任务记录的 verification 表格保留
  日志线索，未来类似问题可直接参照。
- 本次 PR 期间触发的"GitHub Actions 队列卡死 30+ 分钟"是 GitHub 侧瞬时
  事故，与本任务修复无关，但若再次发生需走 githubstatus.com 排查。

## Next Action
1. push 本任务记录到 `task/2026-05-26-align-codex-naming`。
2. 等 PR #11 自动触发 CI；期望 codex-review 这次输出 `blocking findings: none`。
3. 若 codex-review 通过，由 maintainer merge PR #11；`pr-cleanup.yml` 自动删
   远程分支；本任务由 `agent_harness.py complete` 归档到 `completed/`，
   `remote_cleanup: done`。
4. 若 codex-review 仍报 blocking，按其新 findings 迭代。

## Worktree Or Branch
- `task/2026-05-26-align-codex-naming`

## Branch And PR
- Base branch: `main`
- Task branch: `task/2026-05-26-align-codex-naming`
- Remote branch: `origin/task/2026-05-26-align-codex-naming`
- Pull request: https://github.com/3219378872/web-dev-experiment/pull/11
- Remote branch cleanup: pending until PR is merged.

## CI And Review
- CI status: lint / test / integration / smoke 全 green；codex-review 在
  endpoint 修复后已从「网络层失败」转为「内容判定阻塞」（run 26457968513）。
- Codex review: 上一次 run 输出两条 blocking findings（缺自有 harness
  记录、缺 acceptance evidence），本次提交即针对这两条补齐，期望复跑通过。
