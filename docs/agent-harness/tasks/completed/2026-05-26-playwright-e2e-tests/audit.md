# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| e2e/package.json 含 @playwright/test | done | package.json |
| playwright.config.ts 正确配置 | done | hmall-web + hmall-admin projects |
| hmall-web 测试覆盖核心流程 | done | browse/auth/cart 3 个 spec |
| hmall-admin 测试覆盖核心流程 | done | login/items 2 个 spec |
| auth.setup.ts token 注入 | done | loginAs + injectToken |
| TypeScript 编译通过 | done | tsconfig.json |
| CI integration | pending | ci.yml 待更新 |
