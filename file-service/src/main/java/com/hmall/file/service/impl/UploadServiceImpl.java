package com.hmall.file.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.exception.BadRequestException;
import com.hmall.file.domain.po.Upload;
import com.hmall.file.mapper.UploadMapper;
import com.hmall.file.service.IUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.time.LocalDateTime;

@Service
public class UploadServiceImpl extends ServiceImpl<UploadMapper, Upload>
        implements IUploadService {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public Upload uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("文件为空");
        }
        String originalName = file.getOriginalFilename();
        String ext = FileUtil.extName(originalName);
        String newFileName = IdUtil.fastSimpleUUID() + "." + ext;
        String dateDir = LocalDateTime.now().toLocalDate().toString();
        File dir = new File(uploadDir, dateDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(dir, newFileName);
        try {
            file.transferTo(dest);
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败", e);
        }
        Upload upload = new Upload();
        upload.setOriginalName(originalName);
        upload.setFilePath("/uploads/" + dateDir + "/" + newFileName);
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
