package com.hmall.file.controller;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            writeLegacyUpload(key, response);
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

    private void writeLegacyUpload(String key, HttpServletResponse response) throws Exception {
        String relativePath = key.startsWith("/") ? key.substring(1) : key;
        relativePath = relativePath.substring("uploads/".length());

        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path localFile = uploadRoot.resolve(relativePath).normalize();
        if (!localFile.startsWith(uploadRoot) || !Files.isRegularFile(localFile)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Files.copy(localFile, response.getOutputStream());
    }
}
