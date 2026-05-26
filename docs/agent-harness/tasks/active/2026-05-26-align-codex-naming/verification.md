# Verification

## Pre-flight harness lints

| Command | Result | Evidence |
| --- | --- | --- |
| `python3 scripts/agent_harness.py check` | passed | active/completed 任务记录全部通过结构校验 |
| `python3 scripts/knowledge_base.py check` | passed | K001–K004/K006 通过；本 PR 只触碰 `.github/workflows/ci.yml`，不在 KB tracks 中，K005 无触发 |
| `python3 scripts/engineering-lint.py` | passed | engineering-lint: all checks passed |
| `diff -u AGENTS.md CLAUDE.md` | passed | identical（本 PR 未触碰这两份镜像文件） |

## Endpoint 根因定位证据

| Step | Outcome | Evidence |
| --- | --- | --- |
| 本地 curl `…/v1/responses` 非流式 ping | 200 + JSON 含 `output[].text=pong` | `model_not_found`/auth 错误均不触发，证明 key+endpoint 形态本身可用 |
| 本地 curl `…/v1/responses` 流式 ping (`stream:true`) | 完整 9 帧 SSE 含 `response.completed` | 流式协议在本网络可用 |
| 临时在 hmall ci.yml 加 Diagnose step（runner 执行 curl） | 流式 9 帧通过；非流式 60s 0 字节 TCP 卡死 | dogapi.cc 对 GitHub Actions runner 出口 IP 的非流式请求行为异常，但流式可用 |
| 临时在 bidking-controller `task/2026-05-26-diag-codex-secret-fingerprint` 分支加 fingerprint step（已 close + 删分支） | bidking key sha12=`bb3f1707778a` len=51；ep sha12=`78ea851cae4e` len=34 | bidking key+endpoint runtime 形态 |
| hmall PR #11 ci.yml diag step 输出 | hmall key sha12=`bb3f1707778a` len=51；ep sha12=`833c4d6a314e` len=24 | hmall key 与 bidking 字节一致；endpoint **少 10 字节** |
| 字节差比对推导后缀 | hmall=`…/v1`（24B）vs bidking=`…/v1/responses`（34B） | 差异恰为 10 字节 = `/responses` |
| `printf %s "https://www.dogapi.cc/v1/responses" | sha256sum | cut -c1-12` | `78ea851cae4e` | 与 bidking ep sha12 匹配，确认后缀候选 |
| `printf %s "https://www.dogapi.cc/v1/responses" | gh secret set OPENAI_RESPONSES_API_ENDPOINT` | 更新成功，timestamp 2026-05-26T15:26:45Z | `gh secret list` |

## Post-fix CI 行为对比

| Run | Trigger | codex-review 失败步骤 | 失败模式 |
| --- | --- | --- | --- |
| 26446725511 | 旧 endpoint (`…/v1`) | `Codex task completion and compliance review` | `Reconnecting... 1/5 → 5/5 → stream disconnected before completion` —— codex 启动即断流 |
| 26446967320 | 旧 endpoint，命名对齐前 | `Codex task completion and compliance review` | 同上 |
| 26455183720 | 旧 endpoint + 命名对齐后 | `Codex task completion and compliance review` | 同上（证明命名对齐不是根因） |
| 26456698172 | 旧 endpoint + 加 diag step | diag step 内 curl 流式可通；codex-action 依然 5/5 reconnect 失败 | 锁定 endpoint 体积/路径差异 |
| **26457968513** | **新 endpoint (`…/v1/responses`) + revert diag** | **`Fail on blocking Codex findings`** | **codex 跑完整次 review、命中工具调用，按预期在「内容缺 harness 记录」处阻塞** |

## Codex 给出的 blocking findings（本任务记录补齐即可消除）

1. PR 缺自有 harness 任务记录（违反 `docs/agent-harness/README.md::branch→task-record→PR`）。
2. CI workflow 改动缺 acceptance evidence（违反 `quality-rules.md::CI Verification`）。

本 task 记录的提交即针对这两条。期望下一次 CI run 的 codex-review 在内容判定上
返回 `blocking findings: none`。

## Cleanup 已执行

- bidking-controller PR #15（throwaway diagnostic）已 close。
- bidking 远程分支 `task/2026-05-26-diag-codex-secret-fingerprint` 已删除。
- bidking 本地分支同名已删除。
- bidking ci.yml 已恢复字节干净（无 diag step 残留）。
- hmall ci.yml 的 diag step 已在 commit `0153f51` revert。
