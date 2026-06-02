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
