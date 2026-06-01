# Handoff

## 后续步骤

1. 合并此 PR 后，创建 4 个 v2 测试 PR：
   - Phase 1: cart + trade + item (30 tests)
   - Phase 2: notify + file (10 tests)
   - Phase 3: hm-api (3 tests)
   - Coverage boost: user + item (8 tests)

2. 每个 v2 PR 严格遵循完整 CI 门控：
   - lint → test → integration → smoke → codex-review
   - 每个 PR 必须有其独立的 harness 任务记录
   - codex-review 通过（输出 "blocking findings: none"）方可合并

## v2 分支

所有 v2 分支已推送到 origin：
- task/2026-06-01-test-phase1-core-v2
- task/2026-06-01-test-phase2-support-v2
- task/2026-06-01-test-phase3-api-v2
- task/2026-06-01-test-coverage-boost-v2

## codex-review 豁免说明

此 PR（revert）的 codex-review 唯一的 blocking finding 是缺少 harness 任务记录。
现已创建本任务记录。修复后预期 codex-review 通过。
