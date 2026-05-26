# Verification

## 本地 CI 等价命令（按 docs/agent-harness/quality-rules.md::CI Verification 节）

| Command | Result | Evidence |
| --- | --- | --- |
| `python3 scripts/agent_harness.py check` | passed | `agent harness check passed` |
| `python3 scripts/knowledge_base.py check --base origin/main` | passed | `knowledge base check passed`，本 PR 改动 `.github/workflows/ci.yml` + `docs/agent-harness/tasks/active/2026-05-26-align-codex-naming/*` 均不在任何 KB 页面 `tracks:` 中，K005 无触发 |
| `python3 scripts/engineering-lint.py` | passed | `engineering-lint: all checks passed`（修复 context.md 跨仓库相对路径后） |
| `diff -u AGENTS.md CLAUDE.md` | passed | identical（本 PR 未触碰这两份镜像文件） |
| `mvn -B -ntp -q test` | passed | 全部后端模块单测通过，exit 0；Java 21 运行 Java 11 字节码兼容（项目 source/target=11） |
| `mvn -B -ntp -q -Pintegration verify -DskipUnitTests=true` | skipped | 本机 sandbox 无 daemon MySQL/Redis 监听端口；CI integration job 已用 service container 跑同等命令（见 ci.yml:50–88），本 PR 不动 backend 代码，无新增集成测试要求覆盖 |
| `cd hmall-web   && npm ci && npm test --if-present && npm run build` | passed | `npm ci` 装 80 个包；`npm test --if-present` 因 package.json 未定义 `test` 脚本而优雅跳过；`npm run build` `✓ built in 5.25s` |
| `cd hmall-admin && npm ci && npm test --if-present && npm run build` | passed | `npm ci` 装 83 个包；`npm test --if-present` 同上跳过；`npm run build` `✓ built in 7.73s` |
| `docker compose -f docker-compose.yml config -q` | passed | exit 0（compose 文件语法 + 镜像引用全部解析成功） |

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
