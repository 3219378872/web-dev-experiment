# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| smoke.sh 语法正确 | done | `bash -n` 无错误 |
| 10 个测试场景覆盖 | done | 健康+公开API+认证+错误 |
| 公开端点可匿名访问 | done | /items, /search, /categories, /notifications, /hi |
| 登录流程可提取 token | done | jq 解析 token |
| 认证拒绝返回 401 | done | 无 token + 无效 token |
| CI smoke job 更新 | done | ci.yml 加 docker compose up + smoke.sh 步骤 |
| 本地可执行 | done | 需 docker compose up |
