# MinIO 接入 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把 `file-service` 的文件存储从本地文件系统迁移到 MinIO 对象存储，前端取图逻辑零改动，并在 `docker-compose` 中一键跑通。

**Architecture:** `file-service` 用 `io.minio` 客户端把上传对象写入公开读桶 `hmall`，DB `uploads` 表的 `filePath` 改存对象 key；`FileController` 暴露 `/files/**` 流式代理读取 MinIO（保留 `/uploads/` 前缀的本地降级读，兼容历史数据）。gateway 既有 `/upload/**` + `/files/**` 路由与白名单无需改动。

**Tech Stack:** Spring Boot Web、MinIO Java SDK 8.5.x、MyBatis-Plus、JUnit 5 + Mockito + MockMvc、Testcontainers（集成测试）、docker-compose。

**前置：** 实施前在 `docs/agent-harness/tasks/active/` 建任务记录（slug 如 `2026-06-02-minio-integration`，`status: active`，含 context/verification/audit/handoff + task.yaml）。这是 `CLAUDE.md` Safety 约束。

---

## 文件结构（创建/修改清单）

- Create: `file-service/src/main/java/com/hmall/file/config/MinioProperties.java` —— `hm.minio.*` 配置绑定
- Create: `file-service/src/main/java/com/hmall/file/config/MinioConfig.java` —— `MinioClient` Bean
- Modify: `file-service/pom.xml` —— 加 minio + testcontainers(test) 依赖
- Modify: `file-service/src/main/java/com/hmall/file/service/impl/UploadServiceImpl.java` —— putObject 替代本地写
- Modify: `file-service/src/main/java/com/hmall/file/controller/FileController.java` —— 上传返回 `/files/{key}` + `/files/**` 代理
- Modify: `file-service/src/main/resources/application.yaml` —— `hm.minio.*` 配置项
- Modify: `file-service/src/test/java/com/hmall/file/service/impl/UploadServiceImplTest.java` —— 改用 `@MockBean MinioClient`
- Create: `file-service/src/test/java/com/hmall/file/web/FileControllerTest.java` —— 代理/上传 MockMvc 单测
- Create: `file-service/src/test/java/com/hmall/file/it/MinioUploadIT.java` —— Testcontainers MinIO 集成测试
- Modify: `docker-compose.yml` —— `minio` + `minio-init` 容器，file-service env + depends_on
- Modify: `docs/knowledge-base/modules/file-service.md` —— bump `last_synced_commit` + 内容
- Create: `scripts/smoke/minio_upload.sh` —— 冒烟脚本

---

## Task 1: 加 MinIO 依赖与配置项

**Files:**
- Modify: `file-service/pom.xml`
- Modify: `file-service/src/main/resources/application.yaml`

- [ ] **Step 1: 在 `file-service/pom.xml` 的 `<dependencies>` 末尾（`h2` 之前）加 minio 依赖**

```xml
        <dependency>
            <groupId>io.minio</groupId>
            <artifactId>minio</artifactId>
            <version>8.5.7</version>
        </dependency>
```

- [ ] **Step 2: 在 `application.yaml` 末尾（`logging:` 之前）加 minio 配置，沿用 `hm.*` + 环境变量回退模式**

```yaml
hm:
  minio:
    enabled: ${hm.minio.enabled:true}
    endpoint: http://${hm.minio.host:localhost}:${hm.minio.port:9000}
    access-key: ${hm.minio.access-key:minioadmin}
    secret-key: ${hm.minio.secret-key:minioadmin}
    bucket: ${hm.minio.bucket:hmall}
```

- [ ] **Step 3: 编译验证依赖可解析**

Run: `mvn -q -pl file-service -am compile`
Expected: BUILD SUCCESS（仅拉取 minio 依赖，无代码改动编译错误）

- [ ] **Step 4: Commit**

```bash
git add file-service/pom.xml file-service/src/main/resources/application.yaml
git commit -m "build(file-service): add minio dependency and hm.minio config"
```

---

## Task 2: MinioProperties + MinioClient Bean

**Files:**
- Create: `file-service/src/main/java/com/hmall/file/config/MinioProperties.java`
- Create: `file-service/src/main/java/com/hmall/file/config/MinioConfig.java`

- [ ] **Step 1: 创建 `MinioProperties.java`**

```java
package com.hmall.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "hm.minio")
public class MinioProperties {
    /** 单元测试可置 false，跳过真实 MinioClient 创建 */
    private boolean enabled = true;
    private String endpoint = "http://localhost:9000";
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
    private String bucket = "hmall";
}
```

- [ ] **Step 2: 创建 `MinioConfig.java`**

```java
package com.hmall.file.config;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean
    @ConditionalOnProperty(prefix = "hm.minio", name = "enabled", havingValue = "true", matchIfMissing = true)
    public MinioClient minioClient(MinioProperties props) {
        return MinioClient.builder()
                .endpoint(props.getEndpoint())
                .credentials(props.getAccessKey(), props.getSecretKey())
                .build();
    }
}
```

- [ ] **Step 3: 编译验证**

Run: `mvn -q -pl file-service -am compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add file-service/src/main/java/com/hmall/file/config/
git commit -m "feat(file-service): add MinioProperties and MinioClient bean"
```

---

## Task 3: UploadServiceImpl 改用 MinIO putObject（TDD）

**Files:**
- Modify: `file-service/src/test/java/com/hmall/file/service/impl/UploadServiceImplTest.java`
- Modify: `file-service/src/main/java/com/hmall/file/service/impl/UploadServiceImpl.java`

- [ ] **Step 1: 改写单测——`@MockBean MinioClient`、`hm.minio.enabled=false`，断言 putObject 被调用且 filePath 存对象 key（不再带 `/uploads/`）**

替换 `UploadServiceImplTest.java` 全文为：

```java
package com.hmall.file.service.impl;

import com.hmall.common.exception.BadRequestException;
import com.hmall.file.domain.po.Upload;
import com.hmall.file.mapper.UploadMapper;
import com.hmall.file.service.IUploadService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
@TestPropertySource(properties = "hm.minio.enabled=false")
class UploadServiceImplTest {

    @Autowired
    private IUploadService uploadService;
    @Autowired
    private UploadMapper uploadMapper;
    @MockBean
    private MinioClient minioClient;

    @Test
    @DisplayName("uploadImage: 正常上传写入 MinIO 并持久化对象 key")
    void uploadImage_validFile_putsObjectAndStoresKey() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "test-image-content".getBytes());

        Upload result = uploadService.uploadImage(file);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getOriginalName()).isEqualTo("photo.jpg");
        assertThat(result.getContentType()).isEqualTo("image/jpeg");
        assertThat(result.getFileType()).isEqualTo("image");
        assertThat(result.getFileSize()).isGreaterThan(0L);
        // filePath 现在是对象 key：<date>/<uuid>.jpg，不再带 /uploads/
        assertThat(result.getFilePath()).doesNotStartWith("/uploads/");
        assertThat(result.getFilePath()).endsWith(".jpg");
        assertThat(result.getFilePath()).contains("/");
        // 验证真的调用了 MinIO putObject
        verify(minioClient).putObject(org.mockito.ArgumentMatchers.any(PutObjectArgs.class));
        // DB 持久化
        assertThat(uploadMapper.selectById(result.getId())).isNotNull();
    }

    @Test
    @DisplayName("uploadImage: 空文件抛 BadRequestException")
    void uploadImage_emptyFile_throwsBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);
        assertThatThrownBy(() -> uploadService.uploadImage(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("文件为空");
    }

    @Test
    @DisplayName("getFile: 根据ID查询已上传文件记录")
    void getFile_existingId_returnsUpload() {
        Upload upload = new Upload();
        upload.setOriginalName("test.jpg");
        upload.setFilePath("2026-01-01/test.jpg");
        upload.setFileSize(1024L);
        upload.setContentType("image/jpeg");
        upload.setFileType("image");
        uploadMapper.insert(upload);

        Upload result = uploadService.getFile(upload.getId());
        assertThat(result).isNotNull();
        assertThat(result.getFileSize()).isEqualTo(1024L);
    }

    @Test
    @DisplayName("getFile: 不存在的ID返回 null")
    void getFile_nonExistent_returnsNull() {
        assertThat(uploadService.getFile(999L)).isNull();
    }
}
```

- [ ] **Step 2: 运行测试确认失败（实现仍写本地、filePath 仍带 `/uploads/`、未调用 minioClient）**

Run: `mvn -q -pl file-service -am test -Dtest=UploadServiceImplTest`
Expected: FAIL —— `uploadImage_validFile_putsObjectAndStoresKey` 因 `verify(minioClient).putObject(...)` 未发生 / filePath 断言不符而失败

- [ ] **Step 3: 改写 `UploadServiceImpl.java` 用 MinIO**

替换全文为：

```java
package com.hmall.file.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.exception.BadRequestException;
import com.hmall.file.config.MinioProperties;
import com.hmall.file.domain.po.Upload;
import com.hmall.file.mapper.UploadMapper;
import com.hmall.file.service.IUploadService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl extends ServiceImpl<UploadMapper, Upload>
        implements IUploadService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public Upload uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("文件为空");
        }
        String originalName = file.getOriginalFilename();
        String ext = FileUtil.extName(originalName);
        String dateDir = LocalDateTime.now().toLocalDate().toString();
        String objectKey = dateDir + "/" + IdUtil.fastSimpleUUID() + "." + ext;
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectKey)
                    .stream(in, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败", e);
        }
        Upload upload = new Upload();
        upload.setOriginalName(originalName);
        upload.setFilePath(objectKey);
        upload.setFileSize(file.getSize());
        upload.setContentType(file.getContentType());
        upload.setFileType("image");
        upload.setCreateTime(LocalDateTime.now());
        save(upload);
        return upload;
    }

    @Override
    public Upload getFile(Long id) {
        return getById(id);
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `mvn -q -pl file-service -am test -Dtest=UploadServiceImplTest`
Expected: PASS（4 tests）

- [ ] **Step 5: Commit**

```bash
git add file-service/src/main/java/com/hmall/file/service/impl/UploadServiceImpl.java \
        file-service/src/test/java/com/hmall/file/service/impl/UploadServiceImplTest.java
git commit -m "feat(file-service): store uploads in MinIO instead of local FS"
```

---

## Task 4: FileController 返回 `/files/{key}` + `/files/**` 代理（TDD）

**Files:**
- Create: `file-service/src/test/java/com/hmall/file/web/FileControllerTest.java`
- Modify: `file-service/src/main/java/com/hmall/file/controller/FileController.java`

- [ ] **Step 1: 写 MockMvc 单测——上传返回 url=`/files/<key>`；`GET /files/<key>` 从 MinIO 代理出字节流**

创建 `FileControllerTest.java`：

```java
package com.hmall.file.web;

import com.hmall.file.controller.FileController;
import com.hmall.file.domain.po.Upload;
import com.hmall.file.service.IUploadService;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import okhttp3.Headers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private IUploadService uploadService;
    @MockBean
    private MinioClient minioClient;

    @Test
    void uploadImage_returnsFilesUrlWithKey() throws Exception {
        Upload upload = new Upload();
        upload.setId(7L);
        upload.setFilePath("2026-06-02/abc.jpg");
        when(uploadService.uploadImage(any())).thenReturn(upload);

        MockMultipartFile file = new MockMultipartFile(
                "file", "p.jpg", "image/jpeg", "x".getBytes());

        mockMvc.perform(multipart("/upload/image").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("7"))
                .andExpect(jsonPath("$.url").value("/files/2026-06-02/abc.jpg"));
    }

    @Test
    void getByKey_proxiesBytesFromMinio() throws Exception {
        byte[] body = "image-bytes".getBytes();
        GetObjectResponse resp = new GetObjectResponse(
                Headers.of("Content-Type", "image/jpeg"),
                "hmall", null, "2026-06-02/abc.jpg",
                new ByteArrayInputStream(body));
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(resp);

        mockMvc.perform(get("/files/2026-06-02/abc.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(body));
    }
}
```

- [ ] **Step 2: 运行确认失败（控制器尚未有 `/files/**` 代理、上传 url 仍是裸 filePath）**

Run: `mvn -q -pl file-service -am test -Dtest=FileControllerTest`
Expected: FAIL（`getByKey_proxiesBytesFromMinio` 404；`uploadImage` url 断言不符）

- [ ] **Step 3: 改写 `FileController.java`——上传拼 `/files/{key}`，新增 `/files/**` 代理 + `/uploads/` 本地降级**

替换全文为：

```java
package com.hmall.file.controller;

import cn.hutool.core.io.FileUtil;
import com.hmall.file.config.MinioProperties;
import com.hmall.file.domain.po.Upload;
import com.hmall.file.service.IUploadService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class FileController {

    private final IUploadService uploadService;
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadImage(@RequestPart("file") MultipartFile file) {
        Upload upload = uploadService.uploadImage(file);
        Map<String, String> result = new HashMap<>();
        result.put("id", upload.getId().toString());
        result.put("url", "/files/" + upload.getFilePath());
        return result;
    }

    /** 数字 id 兼容旧调用：查记录拿 key 再代理 */
    @GetMapping("/files/{id:\\d+}")
    public void downloadById(@PathVariable Long id, HttpServletResponse response) throws Exception {
        Upload upload = uploadService.getFile(id);
        if (upload == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        writeObject(upload.getFilePath(), upload.getContentType(), response);
    }

    /** 新公开 URL 形态：/files/<date>/<uuid>.<ext> 直接按 key 代理 */
    @GetMapping("/files/**")
    public void downloadByKey(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String key = path.substring("/files/".length());
        writeObject(key, null, response);
    }

    private void writeObject(String key, String contentType, HttpServletResponse response) throws Exception {
        // 历史数据降级：旧记录 filePath 形如 /uploads/...，回退本地盘读取
        // （MinIO 全量迁移后可移除此分支）
        if (key.startsWith("/uploads/") || key.startsWith("uploads/")) {
            byte[] bytes = FileUtil.readBytes(uploadDir + "/../" + (key.startsWith("/") ? key : "/" + key));
            response.getOutputStream().write(bytes);
            return;
        }
        try (InputStream in = minioClient.getObject(GetObjectArgs.builder()
                .bucket(minioProperties.getBucket())
                .object(key)
                .build())) {
            if (contentType != null) {
                response.setContentType(contentType);
            }
            StreamUtils.copy(in, response.getOutputStream());
        }
    }
}
```

注：`@WebMvcTest` 不加载 `MinioConfig`，故测试用 `@MockBean MinioClient` 注入；生产由 `MinioConfig` 提供。

- [ ] **Step 4: 运行确认通过**

Run: `mvn -q -pl file-service -am test -Dtest=FileControllerTest`
Expected: PASS（2 tests）

- [ ] **Step 5: 跑 file-service 全量单测确保无回归**

Run: `mvn -q -pl file-service -am test`
Expected: PASS（含原有 51 项相关测试不破）

- [ ] **Step 6: Commit**

```bash
git add file-service/src/main/java/com/hmall/file/controller/FileController.java \
        file-service/src/test/java/com/hmall/file/web/FileControllerTest.java
git commit -m "feat(file-service): serve uploads via /files/** MinIO proxy with legacy fallback"
```

---

## Task 5: docker-compose 接入 minio + minio-init

**Files:**
- Modify: `docker-compose.yml`

- [ ] **Step 1: 在基础设施区（`redis:` 之后、`nacos-init:` 之前）加 `minio` 与 `minio-init` 服务**

```yaml
  minio:
    image: minio/minio:RELEASE.2024-01-16T16-07-38Z
    restart: unless-stopped
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: ${MINIO_ROOT_USER:-minioadmin}
      MINIO_ROOT_PASSWORD: ${MINIO_ROOT_PASSWORD:-minioadmin}
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - "./docker/minio:/data"
    healthcheck:
      test: ["CMD", "curl", "-sf", "http://localhost:9000/minio/health/live"]
      interval: 5s
      timeout: 3s
      retries: 10

  minio-init:
    image: minio/mc:latest
    restart: "no"
    depends_on:
      minio:
        condition: service_healthy
    entrypoint: ["/bin/sh", "-c"]
    command:
      - |
        mc alias set local http://minio:9000 "${MINIO_ROOT_USER:-minioadmin}" "${MINIO_ROOT_PASSWORD:-minioadmin}"
        mc mb --ignore-existing local/hmall
        mc anonymous set download local/hmall
        echo "minio bucket hmall ready (public download)"
```

- [ ] **Step 2: 给 `file-service` 注入 minio 环境变量并加 depends_on**

把 `file-service:` 块替换为（在既有 depends_on 中追加 minio，并在块内加 environment）：

```yaml
  file-service:
    <<: *java-service
    build: ./file-service
    environment:
      hm.minio.host: minio
      hm.minio.port: "9000"
      hm.minio.access-key: ${MINIO_ROOT_USER:-minioadmin}
      hm.minio.secret-key: ${MINIO_ROOT_PASSWORD:-minioadmin}
      hm.minio.bucket: hmall
    depends_on:
      nacos:
        condition: service_healthy
      nacos-init:
        condition: service_completed_successfully
      mysql:
        condition: service_healthy
      minio:
        condition: service_healthy
      minio-init:
        condition: service_completed_successfully
```

注：若 `<<: *java-service` 锚点本身带 `environment`，YAML 合并时同名块会被覆盖——确认 file-service 需要的 `hm.*` 公共变量（db/nacos/redis）仍由锚点提供；若锚点 environment 被本块覆盖导致丢失，改用 `<<: [*java-service-env]` 合并或在本块补全公共变量。实施时先 `docker compose config` 验证最终 environment 完整。

- [ ] **Step 3: 校验 compose 语法与最终配置**

Run: `docker compose config -q && docker compose config | grep -A15 "file-service:"`
Expected: 无报错；输出中 file-service 的 environment 同时含 db/nacos/redis 公共变量与 `hm.minio.*`

- [ ] **Step 4: 起 minio 单独验证桶就绪**

Run: `docker compose up -d minio minio-init && docker compose logs minio-init`
Expected: 日志含 `minio bucket hmall ready (public download)`

- [ ] **Step 5: Commit**

```bash
git add docker-compose.yml
git commit -m "build(compose): add minio + minio-init, wire file-service to MinIO"
```

---

## Task 6: Testcontainers MinIO 集成测试

**Files:**
- Modify: `file-service/pom.xml`
- Create: `file-service/src/test/java/com/hmall/file/it/MinioUploadIT.java`

- [ ] **Step 1: pom 加 testcontainers 依赖（test 作用域）**

在 `file-service/pom.xml` 的 `h2` 依赖后追加：

```xml
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>1.19.3</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>minio</artifactId>
            <version>1.19.3</version>
            <scope>test</scope>
        </dependency>
```

- [ ] **Step 2: 写集成测试——真实 MinIO 容器，上传→按 key 取回闭环**

创建 `MinioUploadIT.java`（命名 `*IT` 走 `-Pintegration` 的 failsafe，与 notify-service 的 IT 一致）：

```java
package com.hmall.file.it;

import com.hmall.file.config.MinioProperties;
import com.hmall.file.domain.po.Upload;
import com.hmall.file.service.IUploadService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
class MinioUploadIT {

    @Container
    static MinIOContainer minio = new MinIOContainer("minio/minio:RELEASE.2024-01-16T16-07-38Z");

    @Autowired
    private IUploadService uploadService;
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioProperties props;

    @DynamicPropertySource
    static void minioProps(DynamicPropertyRegistry r) {
        r.add("hm.minio.enabled", () -> "true");
        r.add("hm.minio.endpoint", minio::getS3URL);
        r.add("hm.minio.access-key", minio::getUserName);
        r.add("hm.minio.secret-key", minio::getPassword);
        r.add("hm.minio.bucket", () -> "hmall");
    }

    @org.junit.jupiter.api.BeforeEach
    void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(
                io.minio.BucketExistsArgs.builder().bucket("hmall").build());
        if (!exists) {
            minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket("hmall").build());
        }
    }

    @Test
    void upload_thenFetchByKey_roundTrips() throws Exception {
        byte[] content = "real-image".getBytes();
        Upload up = uploadService.uploadImage(new MockMultipartFile(
                "file", "r.jpg", "image/jpeg", content));

        try (InputStream in = minioClient.getObject(GetObjectArgs.builder()
                .bucket(props.getBucket()).object(up.getFilePath()).build())) {
            assertThat(in.readAllBytes()).isEqualTo(content);
        }
    }
}
```

- [ ] **Step 3: 运行集成测试（需本地 docker）**

Run: `mvn -q -pl file-service -am verify -Pintegration -Dit.test=MinioUploadIT`
Expected: PASS（MinIO 容器拉起，上传后按 key 取回字节一致）

- [ ] **Step 4: Commit**

```bash
git add file-service/pom.xml file-service/src/test/java/com/hmall/file/it/MinioUploadIT.java
git commit -m "test(file-service): add Testcontainers MinIO upload integration test"
```

---

## Task 7: 冒烟脚本 + 知识库页 + CI integration job

**Files:**
- Create: `scripts/smoke/minio_upload.sh`
- Modify: `docs/knowledge-base/modules/file-service.md`
- Modify: `.github/workflows/ci.yml`

- [ ] **Step 1: 写冒烟脚本（针对运行中的 compose 栈）**

创建 `scripts/smoke/minio_upload.sh`：

```bash
#!/usr/bin/env bash
set -euo pipefail
BASE="${1:-http://localhost:8080}"
TMP=$(mktemp --suffix=.jpg)
head -c 1024 /dev/urandom > "$TMP"
echo "uploading via $BASE/upload/image ..."
URL=$(curl -sf -F "file=@${TMP}" "$BASE/upload/image" | python3 -c 'import sys,json;print(json.load(sys.stdin)["url"])')
echo "got url: $URL"
echo "fetching $BASE$URL ..."
code=$(curl -s -o /dev/null -w '%{http_code}' "$BASE$URL")
test "$code" = "200" || { echo "FAIL: fetch returned $code" >&2; exit 1; }
echo "SMOKE OK: upload+fetch via MinIO proxy"
rm -f "$TMP"
```

- [ ] **Step 2: 赋可执行位**

Run: `chmod +x scripts/smoke/minio_upload.sh`
Expected: 无输出

- [ ] **Step 3: 更新知识库页 `docs/knowledge-base/modules/file-service.md`**

把 frontmatter 的 `last_synced_commit` bump 到当前 HEAD（`git rev-parse HEAD`），`last_synced_date` 改 `2026-06-02`，`sync_note` 改为 `"接入 MinIO：本地 FS -> 公开桶 hmall + /files/** 代理"`。在正文"## 职责"后补一段说明对象 key 存储与代理读取、历史 `/uploads/` 降级。

- [ ] **Step 4: CI `integration` job 加 minio service container**

在 `.github/workflows/ci.yml` 的 `integration` job `services:` 下，仿照 mysql/redis 增加：

```yaml
      minio:
        image: minio/minio:RELEASE.2024-01-16T16-07-38Z
        env:
          MINIO_ROOT_USER: minioadmin
          MINIO_ROOT_PASSWORD: minioadmin
        ports:
          - 9000:9000
        options: >-
          --health-cmd "curl -sf http://localhost:9000/minio/health/live"
          --health-interval 5s --health-timeout 3s --health-retries 10
```

注：GitHub service container 无法跑 `minio server` 自定义 command；若该镜像默认 entrypoint 不启动 server，改用 `bitnami/minio`（默认启动 server）或在 job step 里 `docker run` 起 MinIO。实施时先在分支 CI 验证容器健康，再定型。集成测试本身用 Testcontainers 自带容器，故 service container 仅为非 Testcontainers 场景；若全部 IT 走 Testcontainers，可不加此 service container，仅确保 runner 有 docker（默认有）。

- [ ] **Step 5: 验证 lint/harness 门禁**

Run: `python3 scripts/knowledge_base.py check && python3 scripts/agent_harness.py check && python3 scripts/engineering-lint.py`
Expected: 全部 passed（K002/K003/K004/K006 通过，file-service 页已存在）

- [ ] **Step 6: Commit**

```bash
git add scripts/smoke/minio_upload.sh docs/knowledge-base/modules/file-service.md .github/workflows/ci.yml
git commit -m "test(smoke+ci): minio upload smoke script, KB sync, CI integration service"
```

---

## Task 8: 端到端验收与收尾

**Files:**
- Modify: harness 任务记录（`docs/agent-harness/tasks/active/2026-06-02-minio-integration/`）

- [ ] **Step 1: 全量后端单测**

Run: `mvn -B -ntp -q test`
Expected: BUILD SUCCESS，无失败

- [ ] **Step 2: 起全栈跑冒烟**

Run: `docker compose up -d && sleep 30 && bash scripts/smoke/minio_upload.sh http://localhost:8080`
Expected: `SMOKE OK: upload+fetch via MinIO proxy`

- [ ] **Step 3: 三门禁 + compose 校验**

Run: `python3 scripts/agent_harness.py check && python3 scripts/knowledge_base.py check --base origin/main && python3 scripts/engineering-lint.py && docker compose config -q`
Expected: 全部通过（含 K005 共变检查：改了 file-service/ 也改了其 KB 页）

- [ ] **Step 4: harness 任务移至 completed、status: done**

按 `CLAUDE.md`：`python3 scripts/agent_harness.py complete 2026-06-02-minio-integration`，填好 verification/audit/handoff。

- [ ] **Step 5: 开 PR**

```bash
git push -u origin HEAD
gh pr create --base main --title "feat: integrate MinIO object storage for file-service" \
  --body "实现 docs/superpowers/plans/2026-06-02-minio-integration.md：file-service 从本地 FS 迁移到 MinIO 公开桶 + /files/** 代理，历史 /uploads/ 降级兼容。compose 加 minio/minio-init，集成测试用 Testcontainers。"
```

Expected: PR 创建，CI 5 个 job 触发；codex-review 需 `blocking findings: none`。

---

## 验收标准（对应 spec §8）

- [ ] `docker compose up -d` 后 minio healthy，上传+取图链路跑通（Task 8 Step 2）
- [ ] 单元测试不依赖外部 MinIO（`@MockBean MinioClient` + `hm.minio.enabled=false`）（Task 3/4）
- [ ] 集成测试用 Testcontainers 覆盖真实上传/取回（Task 6）
- [ ] `docs/knowledge-base/modules/file-service.md` 已更新，K005 通过（Task 7）
- [ ] harness/KB/engineering-lint 三门禁全过（Task 8 Step 3）
- [ ] 走 branch → PR → CI → review → 合并 → 删远程分支
