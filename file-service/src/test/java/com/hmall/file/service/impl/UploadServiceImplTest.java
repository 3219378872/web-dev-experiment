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
