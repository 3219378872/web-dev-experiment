# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| 所有 img 添加 alt | done | 6 组件 |
| 缺失回退图片补 | done | OrderConfirm + ItemList + BannerList |
| router-link-active CSS | done | AppHeader.vue |
| scrollBehavior | done | router/index.js |
| AdminLayout default-openeds | done | AdminLayout.vue |
| 截图脚本 | done | e2e/visual/screenshots.spec.ts |
| npm build 不回归 | done | PR #8 合并时 main push CI smoke job 实跑两端 npm ci/test/build 全绿 |
