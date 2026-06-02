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
