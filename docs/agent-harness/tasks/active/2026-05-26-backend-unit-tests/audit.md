# Audit

按 spec `2026-05-18-hmall-refactor-design.md` 的"测试金字塔"章节与
`docs/agent-harness/quality-rules.md` Backend Verification 要求逐项审计。

| Requirement | Status | Evidence |
| --- | --- | --- |
| 修复 PR #2 暴露的 hm-service CartServiceImpl 编译 bug（task #3+ CI 的前置阻塞）| done | `int → long` 两处修改；clean compile 全模块 SUCCESS |
| hm-common 公共契约（R / PageDTO / PageQuery）有可执行单测 | done | RTest 6 / PageDTOTest 8 / PageQueryTest 8 |
| hm-common 异常体系全部子类（5 个 + base）覆盖 code/cause 链 | done | CommonExceptionFamilyTest 8 |
| hm-common UserContext ThreadLocal 行为（含跨线程隔离）覆盖 | done | UserContextTest 5（含 ExecutorService 跨线程验证）|
| hm-common UserInfoInterceptor 全分支（null/empty/"null" 字符串/数字/role）覆盖 | done | UserInfoInterceptorTest 7 |
| hm-common 工具类（CollUtils/BeanUtils）核心方法覆盖 | done | CollUtilsTest 8 + BeanUtilsTest 7 |
| hm-gateway JwtTool 全路径覆盖（签发往返 + 5 类异常分支）| done | JwtToolTest 8 |
| user-service JwtTool 与 gateway 镜像测试覆盖 | done | user-service/JwtToolTest 4 |
| user-service UserStatus enum + of() 防御无效值 | done | UserStatusTest 3（含 BadRequestException("账户状态错误"）|
| pay-service PayStatus / PayType enum + equalsValue(null) 防御 | done | PayStatusTest 4 + PayTypeTest 3 |
| 旧 ItemServiceImplTest 显式 @Disabled 并写迁移原因 | done | `@Disabled("依赖运行中的 MySQL/Nacos/Redis，迁移到 task #3 集成测试")` |
| 单元测试不依赖外部基础设施（MySQL/Redis/Nacos）| done | 全部用 Mockito / spring-mock / 内存 KeyPair |
| 未修改公共 API 契约（R/PageDTO 字段与方法签名）| done | git diff hm-common 仅 src/test/* |
| BeanUtils 三参 copyBean null-source 的潜在 NPE 已记录 | done | BeanUtilsTest copyBean_threeArg_nullSource_invokesConverterWithNulls 注释 |
| 覆盖率门禁（jacoco）已接入 | deferred | task #3 一并加 jacoco-maven-plugin（避免本 PR 与 CI workflow PR 互相阻塞）|
| 业务 ServiceImpl 链式 query 路径覆盖 | deferred | task #3 Testcontainers 集成测试覆盖 |
| 前端 Vitest 单测 | deferred | 后续独立任务 |
