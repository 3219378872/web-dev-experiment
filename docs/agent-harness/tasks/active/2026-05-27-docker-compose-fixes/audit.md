# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| Gateway 鉴权白名单包含公开读接口 | passed | `/categories/**` 已加入 `hm.auth.excludePaths` |
| SQL 初始化数据类型与实体枚举匹配 | passed | `user.status` 列定义 INT + 种子数据 int (1=NORMAL) |
| SQL 密码 hash 与文档注释一致 | passed | hash 对应 `admin123`，与注释一致 |
| Nacos 路由初始化脚本可用 | passed | `scripts/init-nacos-routes.sh` 发布成功 |
| .gitignore 排除 Docker 数据卷 | passed | `docker/mysql/` 已加入 .gitignore |
| 不引入新 tsc / lint 错误 | passed | 仅改 yaml / sql / sh / gitignore |
| 不破坏现有 API 响应格式 | passed | 无 Java 代码改动 |
