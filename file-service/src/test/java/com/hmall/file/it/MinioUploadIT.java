package com.hmall.file.it;

import com.hmall.file.config.MinioProperties;
import com.hmall.file.domain.po.Upload;
import com.hmall.file.service.IUploadService;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
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
    static MinIOContainer minio = new MinIOContainer("minio/minio:RELEASE.2024-01-16T16-07-38Z")
            .withUserName("hmall-it-user")
            .withPassword("hmall-it-password");

    @Autowired
    private IUploadService uploadService;
    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioProperties props;

    @DynamicPropertySource
    static void minioProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",
                () -> "jdbc:h2:mem:file_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=true");
        r.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        r.add("spring.datasource.username", () -> "sa");
        r.add("spring.datasource.password", () -> "");
        r.add("hm.minio.enabled", () -> "true");
        r.add("hm.minio.endpoint", minio::getS3URL);
        r.add("hm.minio.access-key", minio::getUserName);
        r.add("hm.minio.secret-key", minio::getPassword);
        r.add("hm.minio.bucket", () -> "hmall");
    }

    @BeforeEach
    void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket("hmall").build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket("hmall").build());
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
