---
title: file-service
tracks:
  - file-service/
last_synced_commit: bc09d0e
last_synced_date: 2026-05-26
sync_note: ""
---

# file-service

## 职责

文件上传/下载与对象存储（MinIO）适配。统一前端图片/附件上传入口，
返回可访问 URL 与元数据记录。

## 公开接口与契约

- HTTP API：`/files/upload`（multipart）、`/files/{id}`（查元数据）。
- 持久层：`hm-file` 数据库 + MinIO bucket；表 `upload`。

## 上游

- [hm-gateway](hm-gateway.md) 转发外部请求。
- [hmall-admin](hmall-admin.md) 商品/品牌图上传。
- [hmall-web](hmall-web.md) 评价/头像上传。

## 下游

- MinIO（对象存储）、MySQL（`hm-file`，仅元数据）。

## 关键文件

- `FileApplication.java`、`controller/FileController.java`。
- `service/IUploadService.java`、`service/impl/UploadServiceImpl.java`。
- `domain/po/Upload.java`、`mapper/UploadMapper.java`。

## 注意事项与陷阱

- 上传必须校验文件类型与大小，拒绝可执行文件、超过阈值拒绝。
- 公开 URL 优先用预签名，避免暴露 MinIO 内部地址。
- 元数据表是审计追踪入口，不允许直接删除记录（软删）。
