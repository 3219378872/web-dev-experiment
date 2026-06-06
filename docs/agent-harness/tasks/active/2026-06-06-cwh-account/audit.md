# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| #129 收藏页加购接入购物车接口 | done | FavoriteList.vue addToCart(fav) 调 cartStore.addItem（payload 经 `hmall-web/src/utils/cartFromFavorite.js`），处理未登录与商品缺失，后端异常由 axios 拦截器统一提示 |
| #130 头像上传真实持久化 | done | Profile.vue triggerAvatar 触发文件选择→uploadImage(/upload/image)→PUT /users/profile；隐藏 input + 头像预览；校验 JPG/PNG≤2MB（`hmall-web/src/utils/avatarUpload.js`） |
| #130 性别/生日持久化 | done | User PO/UserVO 增 gender/birthday；UserServiceImpl.updateProfile 落库；迁移 `docs/sql/user-service-profile-fields.sql` + canonical schema 同步；save() 提交 gender/birthday |
| #130 手机更换误导文案 | done | 静态「更换」链接改为只读说明「暂不支持在线更换」 |
| #130 订单统计仅第一页 | done | loadStats 跨分页累加（`hmall-web/src/utils/profileStats.js` countOrderPage/mergeOrderStats/hasNextPage） |
| #130 资料回填 | done | 新增 `GET /users/profile`→UserVO，挂载时拉真实资料填表并同步 userStore |
| #133 登录支持邮箱/手机号 | done | UserServiceImpl.login 改 `username=? or email=? or phone=?`，UserLoginVO 形态不变 |
| #133 文案校正 | done | 「记住密码」→「记住账号」；找回密码文案与 placeholder 改为仅邮箱 |
| 新增 endpoint 遵循 R<Void>写/VO读 | done | GET /users/profile 返 UserVO；PUT /users/profile 维持 R<Void> |
| 不破坏既有契约 | done | login 仍返 UserLoginVO；未改动 /users/login 路由与白名单 |
| 测试覆盖 | done | 后端新增 6 例（login 标识、gender/birthday、getCurrentUserProfile）；前端新增 22 例 util 单测 |
| KB 共变 (K005) | done | hmall-web.md / user-service.md / auth-and-gateway-flow.md 同步并 bump last_synced_commit |
