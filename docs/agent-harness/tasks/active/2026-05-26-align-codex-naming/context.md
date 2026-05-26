# Context: Align Codex Naming

## Objective
对齐 hmall 与 bidking-controller 的 codex-review 配置 + 定位并修复 hmall 仓库
`OPENAI_RESPONSES_API_ENDPOINT` 仓库密钥缺少 `/responses` 路径后缀导致 codex
启动即 reconnect 5/5 断流的根因。

## Scope
- In scope:
  - 把 `.github/workflows/ci.yml::codex-review.Verify Codex secrets` 步骤的
    本地 shell 临时变量 `HAS_KEY` / `HAS_ENDPOINT` 改为更明确的
    `HAS_OPENAI_API_KEY` / `HAS_OPENAI_RESPONSES_API_ENDPOINT`，与
    bidking-controller 同名同形。
  - 通过 fingerprint 对照（hmall sha12 vs bidking sha12）锁定差异来源。
  - 用 `gh secret set` 把 `OPENAI_RESPONSES_API_ENDPOINT` 从
    `https://www.dogapi.cc/v1` 改为 `https://www.dogapi.cc/v1/responses`。
- Out of scope:
  - 修改 codex-action 版本 / 模型（仍为 `gpt-5.4`，`effort: high`）。
  - 改动 `OPENAI_API_KEY`（指纹证明两仓库 key 字节一致，无需动）。
  - 改动除 `Verify Codex secrets` 步骤外的其它 CI job 配置。
  - 修改 prompt 内容（codex 审查规则保持现状）。

## Related Artifacts
- Spec: none - 单点配置对齐 + 一次 secret 值校正，无需独立 spec。
- Plan: none - 改动量极小且线性。
- 参考实现: `../bidking-controller/.github/workflows/ci.yml` 同 step。

## Likely Files
- `.github/workflows/ci.yml`（仅 codex-review.Verify Codex secrets 步骤的两个
  env var 名变更）
- GitHub 仓库 Secrets（仓库外部 —— Settings → Secrets and variables → Actions）：
  - `OPENAI_RESPONSES_API_ENDPOINT` 值从 `…/v1` 改为 `…/v1/responses`
  - `OPENAI_API_KEY` 值未改

## Runtime Evidence To Inspect First
- `gh run view <ID> --log-failed` 抓 codex-review 历次失败的 `Reconnecting…`
  日志，确认网络层失败而非内容判定失败。
- `gh secret list` 比对两仓库 secrets 更新时间。
- 本地 `curl /v1/responses` 流式/非流式回放，证明 endpoint+key 本身可达。

## Safety Constraints
- 不在 PR 描述、commit message、harness 任务记录中暴露 `OPENAI_API_KEY` 明文
  或可逆形态；只允许出现 sha12 指纹。
- 不动 bidking-controller 的 secret 值（对照实验只用临时 PR 加 fingerprint
  step，完成后立刻 close PR + 删分支 + revert workflow，确认两仓库 ci.yml
  仍字节一致）。
- 不破坏 codex-review 强门控语义（不引入 `continue-on-error`，不下调 effort）。

## Branch And PR
- Base branch: `main`
- Task branch: `task/2026-05-26-align-codex-naming`
- Remote branch: `origin/task/2026-05-26-align-codex-naming`
- Pull request: https://github.com/3219378872/web-dev-experiment/pull/11

## Open Questions
- none —— 根因已定位、修复已生效、codex-review 已从「网络层失败」转为
  「内容判定」失败，剩下的内容缺口（本任务记录缺失）正在本提交补齐。
