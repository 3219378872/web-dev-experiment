# Context: Testing Enhancements

## Objective
新增单元测试、集成测试、增强冒烟测试，替换前端占位脚本为 vitest。

## Scope
- In scope: AuthGlobalFilter unit (8), Gateway IT (5), smoke cart/order/pay (16 total), frontend vitest (4)
- Out of scope: E2E purchase flow, 全模块 80% 覆盖率

## Related Artifacts
- Spec: null - 测试增强不需要独立 spec
- Plan: null - 改动明确

## Likely Files
- `hm-gateway/src/test/`
- `scripts/smoke/smoke.sh`
- `hmall-web/` `hmall-admin/` (vitest)

## Safety Constraints
- 不修改业务代码，不改 public API 契约
