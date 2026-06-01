package com.hmall.file.service.impl;

import com.hmall.common.exception.BadRequestException;
import com.hmall.file.domain.po.Upload;
import com.hmall.file.mapper.UploadMapper;
import com.hmall.file.service.IUploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UploadServiceImplTest {

    @Autowired
    private IUploadService uploadService;

    @Autowired
    private UploadMapper uploadMapper;

    // ---- uploadImage ----

    @Test
    @DisplayName("uploadImage: 正常上传图片文件，返回 Upload 记录")
    void uploadImage_validFile_returnsUpload() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "test-image-content".getBytes());

        Upload result = uploadService.uploadImage(file);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getOriginalName()).isEqualTo("photo.jpg");
        assertThat(result.getContentType()).isEqualTo("image/jpeg");
        assertThat(result.getFileType()).isEqualTo("image");
        assertThat(result.getFileSize()).isGreaterThan(0L);
        assertThat(result.getFilePath()).startsWith("/uploads/");
        assertThat(result.getFilePath()).endsWith(".jpg");

        // 验证数据库持久化
        Upload saved = uploadMapper.selectById(result.getId());
        assertThat(saved).isNotNull();
        assertThat(saved.getOriginalName()).isEqualTo("photo.jpg");
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

    // ---- getFile ----

    @Test
    @DisplayName("getFile: 根据ID查询已上传文件记录")
    void getFile_existingId_returnsUpload() {
        Upload upload = new Upload();
        upload.setOriginalName("test.jpg");
        upload.setFilePath("/uploads/2026/01/test.jpg");
        upload.setFileSize(1024L);
        upload.setContentType("image/jpeg");
        upload.setFileType("image");
        uploadMapper.insert(upload);

        Upload result = uploadService.getFile(upload.getId());

        assertThat(result).isNotNull();
        assertThat(result.getOriginalName()).isEqualTo("test.jpg");
        assertThat(result.getFileSize()).isEqualTo(1024L);
    }

    @Test
    @DisplayName("getFile: 不存在的ID返回 null")
    void getFile_nonExistent_returnsNull() {
        assertThat(uploadService.getFile(999L)).isNull();
    }
}
