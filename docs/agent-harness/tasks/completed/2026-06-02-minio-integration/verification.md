# Verification

| Command | Result | Evidence |
| --- | --- | --- |
| `mvn -pl file-service test` | PASS | 6 tests（UploadServiceImplTest 4 + FileControllerTest 2），0 失败 |
| `mvn -B -ntp -q test`（全量） | PASS | 146 tests，27 报告，0 失败 0 错误 |
| `mvn -B -ntp -q -pl file-service -am verify -Pintegration -DfailIfNoTests=false -Dit.test=MinioUploadIT` | PASS | MinioUploadIT 1 test：Testcontainers 真实 MinIO 上传→按 key 取回字节一致 |
| `SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/hm?... SPRING_DATASOURCE_USERNAME=root SPRING_DATASOURCE_PASSWORD=123 mvn -B -ntp -q -pl file-service -am verify -Pintegration -DfailIfNoTests=false -Dit.test=MinioUploadIT` | PASS | 覆盖 CI integration job 注入 MySQL datasource env 时，MinioUploadIT 仍固定使用 H2 测试库并通过 |
| `docker compose up -d minio minio-init` | PASS | minio healthy（`mc ready local`）；bucket `hmall` 公开下载就绪 |
| 全栈 e2e（容器内网经 gateway） | PASS | `POST /upload/image` 返回 `{"id","url":"/files/<key>"}`；`GET /files/<key>` → 200 且字节与上传一致 |
| `python3 scripts/agent_harness.py check` | PASS | agent harness check passed |
| `python3 scripts/knowledge_base.py check --base main` | PASS | K001–K006 全过（file-service/hm-service 页共变） |
| `python3 scripts/engineering-lint.py` | PASS | all checks passed |
| `docker compose config -q` | PASS | compose config OK |

## 端到端证据（节选）

```
upload resp: {"id":"1","url":"/files/2026-06-02/ba69d121954a4bca98894d0b111172fe.jpg"}
fetch code: 200
fetched bytes: img-content-1780369258   # 与上传内容逐字节一致
```

## 备注

- 内置 smoke-test 报 `user=500`、hm-service 重启为**既有问题**（与 MinIO 无关）；
  items/categories/notifications/file 上传链路均 200。
- 单元测试不依赖外部 MinIO（`@MockBean MinioClient` + `hm.minio.enabled=false`）。
- 聚合根模块直接定向 `-Dit.test=MinioUploadIT` 时父 POM 的 failsafe 会先因无 IT
  报 `No tests were executed`；本任务使用 `-DfailIfNoTests=false` 让 reactor 前置
  模块跳过空 IT，并确认 `file-service` 的 MinioUploadIT 实际执行 1 test。
- 2026-06-02 PR #45 首轮 CI integration 失败根因是 workflow 注入 MySQL datasource
  env 覆盖了 file-service 的 H2 测试配置；`MinioUploadIT` 已通过
  `DynamicPropertySource` 显式固定 H2 datasource，并用同款 env 本地复现后验证通过。
