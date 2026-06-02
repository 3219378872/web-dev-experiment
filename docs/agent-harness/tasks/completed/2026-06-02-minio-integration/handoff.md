# Handoff: Minio Integration

## Status
Implemented; PR #45 review iteration in progress.

## Files Changed
- `file-service/pom.xml` — minio 8.5.7 + testcontainers(test) 依赖
- `file-service/src/main/resources/application.yaml` — `hm.minio.*` 配置（修自引用占位符；MinIO 凭据由环境/compose 注入）
- `file-service/src/main/java/com/hmall/file/config/{MinioProperties,MinioConfig}.java` — 新增
- `file-service/src/main/java/com/hmall/file/service/impl/UploadServiceImpl.java` — putObject 存对象 key
- `file-service/src/main/java/com/hmall/file/controller/FileController.java` — `/files/**` 代理 + `/uploads/` 降级（normalize 限制在 upload root）
- 测试：`UploadServiceImplTest`（改）、`web/FileControllerTest`（新）、`it/MinioUploadIT`（新）
- `docker-compose.yml` — minio + minio-init 容器、file-service 注入 `hm.minio.*`（`<<: *java-env` 合并）、nacos-init `user: root`
- `hm-service/Dockerfile` — 失效 base 镜像修复（既有 bug）
- `scripts/smoke/minio_upload.sh` — 新增冒烟脚本
- `docs/knowledge-base/modules/{file-service,hm-service}.md` — 同步

## Commands Run
见 verification.md（单测/集成/全栈 e2e/三门禁全过）。收尾时已 rebase 到当前
`main`，避免把已合入的 `docs/structure/*` 误显示为本 PR 删除。

## Known Risks
- 历史 `/uploads/` 前缀记录走本地盘降级读取（过渡期逻辑，注释标注可移除条件）；
  fallback 已限制 normalize 后路径必须位于 `file.upload-dir` 下。
- CI `integration` job 未加 minio service container：MinioUploadIT 自带 Testcontainers
  起 MinIO（runner 自带 Docker）。
- **CI codex-review 强门控需 secrets**（`OPENAI_API_KEY`、`OPENAI_RESPONSES_API_ENDPOINT`）；
  若缺失会阻塞合并，需仓库管理员配置。
- 既有问题（非本次范围）：user-service 冒烟 500、hm-service 运行期重启 —— 不影响
  MinIO 上传链路，建议另开任务。

## Next Action
提交并推送 PR #45 review 修复，重新过 CI 与 codex-review，合并后删远程分支。

## Worktree Or Branch
- worktree 分支 `worktree-task+2026-06-02-infra-integration`（已 rebase 到 `main`）
