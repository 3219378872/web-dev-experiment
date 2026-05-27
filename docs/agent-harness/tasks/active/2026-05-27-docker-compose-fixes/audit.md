# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| Gateway 鉴权白名单包含公开读接口 | passed | `/categories/**` 已加入 `hm.auth.excludePaths` |
| hm-service 部署 | passed | hm-service 已加入 docker-compose.yml |
| /hi 端点可用 | passed | smoke test #1 通过 |
| SQL 初始化数据类型与实体枚举匹配 | passed | `user.status` 列定义 INT + 种子数据 int (1=NORMAL) |
| SQL 密码 hash 与文档注释一致 | passed | hash 对应 `admin123`，与注释一致 |
| SQL 自动导入 | passed | `init-all-tables.sql` 挂载到 `/docker-entrypoint-initdb.d/` |
| Nacos 路由初始化脚本可用 | passed | `scripts/init-nacos-routes.sh` 通过 nacos-init 自动运行 |
| Smoke test 自动化 | passed | smoke-test 服务自动运行，含 jq 依赖 |
| .gitignore 排除 Docker 数据卷 | passed | `docker/mysql/` 已加入 .gitignore |
| 不引入新 tsc / lint 错误 | passed | 仅改 yaml / sql / sh / gitignore |
| 不破坏现有 API 响应格式 | passed | 无 Java 代码改动 |
