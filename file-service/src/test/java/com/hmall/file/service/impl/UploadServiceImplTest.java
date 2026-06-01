package com.hmall.file.service.impl;

import com.hmall.common.exception.BadRequestException;
import com.hmall.file.domain.po.Upload;
import com.hmall.file.mapper.UploadMapper;
import com.hmall.file.service.IUploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UploadServiceImplTest {

    @Autowired
    private IUploadService uploadService;

    @Autowired
    private UploadMapper uploadMapper;

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
