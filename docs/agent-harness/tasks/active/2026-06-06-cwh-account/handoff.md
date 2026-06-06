# Handoff: C 端账户域三项缺陷 (#129 #130 #133)

## Status
实现完成，本地验收全绿，待开 PR 过 CI 后合并。

## Files Changed
### 前端 hmall-web
- `hmall-web/src/views/FavoriteList.vue` —— 加购接 cartStore.addItem。
- `hmall-web/src/views/Profile.vue` —— 头像上传/资料回填/gender·birthday 持久化/订单统计分页/手机只读。
- `hmall-web/src/views/Login.vue` —— 文案校正（记住账号、邮箱找回）。
- `hmall-web/src/api/user.js` —— 新增 getProfile。
- `hmall-web/src/utils/cartFromFavorite.js`、`hmall-web/src/utils/profileStats.js`、`hmall-web/src/utils/avatarUpload.js` —— 抽出纯逻辑。
- `hmall-web/src/__tests__/cartFromFavorite.spec.js`、`hmall-web/src/__tests__/profileStats.spec.js`、`hmall-web/src/__tests__/avatarUpload.spec.js` —— 单测。

### 后端 user-service
- `user-service/src/main/java/com/hmall/controller/UserController.java` —— 新增 GET /users/profile。
- `user-service/src/main/java/com/hmall/service/IUserService.java`、`user-service/src/main/java/com/hmall/service/impl/UserServiceImpl.java` —— login 三标识匹配、updateProfile 持久化 gender/birthday、新增 getCurrentUserProfile、convertToUserVO 映射。
- `user-service/src/main/java/com/hmall/domain/po/User.java`、`user-service/src/main/java/com/hmall/domain/vo/UserVO.java` —— 增 gender/birthday。
- `user-service/src/test/java/com/hmall/service/impl/UserServiceImplTest.java` —— 新增 6 例。
- `user-service/src/test/resources/sql/schema.sql` —— 测试库增列。

### SQL / KB
- `docs/sql/user-service-profile-fields.sql`（新）、`docs/sql/init-all-tables.sql`（增列）。
- `docs/knowledge-base/modules/hmall-web.md`、`docs/knowledge-base/modules/user-service.md`、`docs/knowledge-base/flows/auth-and-gateway-flow.md`。

## Commands Run
- `cd hmall-web && npm ci && npm test && npm run build` — pass。
- `mvn -pl user-service -am test` — BUILD SUCCESS。
- harness check / KB check --base origin/main / engineering-lint — pass。

## Known Risks
- gender/birthday 为新增 DB 列：生产库需执行 `docs/sql/user-service-profile-fields.sql`（ALTER ADD COLUMN，幂等需人工保证不重复执行）。
- 手机号验证码找回 / 在线换绑未实现（无短信渠道），按 issue 验收降级为文案说明。
- codex-review job 依赖仓库 secrets（OPENAI_API_KEY / OPENAI_RESPONSES_API_ENDPOINT）；若缺失导致该 job 无法运行，按 CLAUDE.md 在 PR 描述说明，不视为实现缺陷。

## Next Action
开 PR → 过 CI → squash 合并 → 删远程分支 → 本任务移 completed 设 status: done。

## Worktree Or Branch
- `task/2026-06-06-cwh-account`
