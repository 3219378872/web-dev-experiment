# Audit

| Requirement | Status | Evidence |
| --- | --- | --- |
| MinIO replaces local upload storage for new images | pass | `UploadServiceImpl` writes `putObject` to bucket `hmall` and persists object keys like `<date>/<uuid>.jpg`. |
| Public image URL remains stable for frontend callers | pass | `FileController.uploadImage` returns `/files/{key}`; no frontend call-site changes required. |
| `/files/**` proxies bytes from MinIO and keeps legacy fallback | pass | `FileController.downloadByKey` streams `MinioClient.getObject`; `/uploads/` paths still read local disk during transition. |
| Docker Compose can provision MinIO | pass | `docker-compose.yml` adds `minio` + `minio-init`, creates public-download `hmall` bucket, and wires `file-service` env/depends_on. |
| Unit tests do not require external MinIO | pass | `UploadServiceImplTest` / `FileControllerTest` use `@MockBean MinioClient` and `hm.minio.enabled=false`. |
| Real MinIO integration is covered | pass | `MinioUploadIT` uses Testcontainers MinIO for upload -> fetch-by-key round trip. |
| Knowledge base co-change is present | pass | `docs/knowledge-base/modules/file-service.md` documents MinIO storage; `hm-service.md` documents Dockerfile fix. |
| Harness and engineering gates pass | pass | `agent_harness.py check`, `knowledge_base.py check --base main`, `engineering-lint.py`, and `docker compose config -q` passed after rebase onto `main`. |
| Branch is based on current main | pass | Rebased onto `main` commit `8ce6b23`, eliminating stale `docs/structure/*` deletion from the PR diff. |
