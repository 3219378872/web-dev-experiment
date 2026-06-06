# Handoff: Issue 131 Order Payment

## Status
实现完成，本地验收全绿，待提交、推 PR、跟进 CI/codex-review。

## Files Changed
- `hmall-web/src/views/OrderConfirm.vue` —— 结算页只展示余额支付；配送/发票改只读说明；提交 payload 改由 checkoutOptions 构造。
- `hmall-web/src/utils/checkoutOptions.js` —— 抽出支付选项、配送/发票说明、OrderFormDTO payload 构造。
- `hmall-web/src/__tests__/checkoutOptions.spec.js` —— 覆盖余额支付选项与 payload 字段白名单。
- `hmall-web/src/__tests__/OrderConfirm.spec.js` —— 覆盖结算页不渲染支付宝/微信和不可提交控件。
- `docs/knowledge-base/modules/hmall-web.md` —— 记录 #131 结算页支付契约。

## Commands Run
- `cd hmall-web && npm ci` — pass（npm audit 报既有 4 个漏洞，未在本任务改依赖）。
- `cd hmall-web && npm test -- --run src/__tests__/checkoutOptions.spec.js src/__tests__/OrderConfirm.spec.js` — RED 后 GREEN，最终 5 tests passed。
- `cd hmall-web && npm test` — pass，14 files / 97 tests。
- `cd hmall-web && npm run build` — pass，Vite build 完成，保留既有 chunk size warning。
- `python3 scripts/agent_harness.py check` — pass。
- `python3 scripts/knowledge_base.py check --base origin/main` — pass。
- `python3 scripts/engineering-lint.py` — pass。

## Known Risks
- 本次选择收敛前端能力，不实现支付宝/微信真实支付。
- 发票/配送时间/备注没有后端字段，当前以只读说明避免误导；后续若要支持需独立后端字段、迁移、详情展示与测试。

## Next Action
提交并开 PR，跟进 CI 与 codex-review。

## Worktree Or Branch
- `task/2026-06-06-issue-131-order-payment`
