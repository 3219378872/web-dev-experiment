# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| user-service 分支覆盖率 ≥ 70% | ✅ 97% | JaCoCo report: 1 of 34 branches missed |
| pay-service 分支覆盖率 ≥ 70% | ✅ 91% | JaCoCo report: 1 of 12 branches missed |
| 全模块 `mvn verify` 通过 | ✅ | BUILD SUCCESS |
| Knowledge base 同步 | ✅ | K005 通过，页面已更新 |
| 无硬编码密钥 | ✅ | 测试使用 mock，无真实凭据 |
