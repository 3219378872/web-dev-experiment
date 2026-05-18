package com.hmall.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.file.domain.po.Upload;
import org.springframework.web.multipart.MultipartFile;

public interface IUploadService extends IService<Upload> {
    Upload uploadImage(MultipartFile file);
    Upload getFile(Long id);
}
