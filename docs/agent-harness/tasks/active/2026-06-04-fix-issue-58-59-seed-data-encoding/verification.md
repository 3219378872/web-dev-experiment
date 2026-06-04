# Verification

| Command | Result | Evidence |
| --- | --- | --- |
| `docker compose config -q` | PASS | 配置验证通过 |
| `mvn -q -pl user-service -am test` | PASS | 后端单测通过（不依赖 seed） |
