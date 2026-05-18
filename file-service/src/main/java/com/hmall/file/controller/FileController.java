package com.hmall.file.controller;

import cn.hutool.core.io.FileUtil;
import com.hmall.file.domain.po.Upload;
import com.hmall.file.service.IUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class FileController {

    private final IUploadService uploadService;
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadImage(@RequestPart("file") MultipartFile file) {
        Upload upload = uploadService.uploadImage(file);
        Map<String, String> result = new HashMap<>();
        result.put("id", upload.getId().toString());
        result.put("url", upload.getFilePath());
        return result;
    }

    @GetMapping("/files/{id}")
    public byte[] download(@PathVariable Long id) {
        Upload upload = uploadService.getFile(id);
        if (upload == null) {
            throw new RuntimeException("文件不存在");
        }
        return FileUtil.readBytes(uploadDir + "/../" + upload.getFilePath());
    }
}
