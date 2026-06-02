---
title: file-service
tracks:
  - file-service/
last_synced_commit: 731e27d527f3914a293ec819b7afc080429ddd61
last_synced_date: 2026-06-02
sync_note: "接入 MinIO：本地 FS -> 公开桶 hmall + /files/** 网关反代理"
---

# file-service

## 职责

文件上传/下载与对象存储（MinIO）适配。统一前端图片/附件上传入口，
对象存入 MinIO 公开读桶，DB 仅存元数据，返回稳定可访问 URL。

## 公开接口与契约

- HTTP API：
  - `POST /upload/image`（multipart）：上传图片，返回 `{id, url}`，
    `url` 形如 `/files/<date>/<uuid>.<ext>`。
  - `GET /files/**`：按对象 key 从 MinIO 流式代理读取（前端取图入口）。
  - `GET /files/{id}`（数字 id）：按记录 id 查 key 再代理，兼容旧调用。
- 持久层：MySQL `hmall` 库表 `uploads`（仅元数据，`file_path` 存对象 key）
  + MinIO 桶 `hmall`（对象本体，公开读）。

## 上游

- [hm-gateway](hm-gateway.md) 转发外部请求（`/upload/**`、`/files/**` 已在鉴权白名单）。
- [hmall-admin](hmall-admin.md) 商品/品牌图上传。
- [hmall-web](hmall-web.md) 评价/头像上传。

## 下游

- MinIO（对象存储，公开桶 `hmall`）、MySQL（`hmall`，仅元数据）。

## 关键文件

- `FileApplication.java`、`controller/FileController.java`。
- `config/MinioConfig.java`、`config/MinioProperties.java`。
- `service/IUploadService.java`、`service/impl/UploadServiceImpl.java`。
- `domain/po/Upload.java`、`mapper/UploadMapper.java`。

## 注意事项与陷阱

- 上传必须校验文件类型与大小，拒绝可执行文件、超过阈值拒绝。
- 对象访问走**公开桶 + 网关反向代理**（`/files/**` 经 file-service 代理到 MinIO），
  不直接暴露 MinIO 端口给前端。
- 历史 `/uploads/` 前缀记录走本地盘降级读取（MinIO 全量迁移后可移除该分支）。
- `hm.minio.enabled=false` 用于单元测试跳过真实 `MinioClient` 创建。
- 元数据表是审计追踪入口，不允许直接删除记录（软删）。
