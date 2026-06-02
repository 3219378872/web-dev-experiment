# Context: Minio Integration

## Objective
把 file-service 的文件存储从本地文件系统迁移到 MinIO 对象存储，前端取图逻辑零改动，并在 docker-compose 中一键跑通。

## Scope
- In scope: file-service（依赖/配置/UploadServiceImpl/FileController）、docker-compose（minio + minio-init）、单元/集成/冒烟测试、CI integration job、KB 页同步。
- Out of scope: RabbitMQ、Seata（同 spec 下后续独立 plan）；前端取图代码（设计上零改动）。

## Related Artifacts
- Spec: docs/superpowers/specs/2026-06-02-infra-integration-design.md
- Plan: docs/superpowers/plans/2026-06-02-minio-integration.md

## Likely Files
- `file-service/pom.xml`
- `file-service/src/main/java/com/hmall/file/config/{MinioProperties,MinioConfig}.java`
- `file-service/src/main/java/com/hmall/file/service/impl/UploadServiceImpl.java`
- `file-service/src/main/java/com/hmall/file/controller/FileController.java`
- `file-service/src/main/resources/application.yaml`
- `docker-compose.yml`
- `docs/knowledge-base/modules/file-service.md`

## Runtime Evidence To Inspect First
- 现状 file-service 用本地 FS（MultipartFile.transferTo 到 ./uploads）+ DB 元数据；无 MinIO client。
- gateway 已路由 /upload/** + /files/** 到 file-service 且在鉴权白名单内。

## Safety Constraints
- Do not commit secrets to `docker-compose.yml`, application.yaml, or .env-style files. MinIO 凭据走环境变量回退。
- Do not break public API response envelopes returned by `hm-common.dto.Result` / `PageDTO`.
- 上传接口返回结构保持 {id, url}，url 由 /uploads/... 平滑切到 /files/<key>，前端零改动。

## Worktree Or Branch
- `task/2026-06-02-minio-integration`（实际 worktree 分支 `worktree-task+2026-06-02-infra-integration`）

## Open Questions
- none
