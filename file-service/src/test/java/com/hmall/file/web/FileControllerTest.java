package com.hmall.file.web;

import com.hmall.file.domain.po.Upload;
import com.hmall.file.service.IUploadService;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import okhttp3.Headers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "hm.minio.enabled=false")
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private IUploadService uploadService;
    @MockBean
    private MinioClient minioClient;

    @Test
    void uploadImage_returnsFilesUrlWithKey() throws Exception {
        Upload upload = new Upload();
        upload.setId(7L);
        upload.setFilePath("2026-06-02/abc.jpg");
        when(uploadService.uploadImage(any())).thenReturn(upload);

        MockMultipartFile file = new MockMultipartFile(
                "file", "p.jpg", "image/jpeg", "x".getBytes());

        mockMvc.perform(multipart("/upload/image").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("7"))
                .andExpect(jsonPath("$.url").value("/files/2026-06-02/abc.jpg"));
    }

    @Test
    void getByKey_proxiesBytesFromMinio() throws Exception {
        byte[] body = "image-bytes".getBytes();
        GetObjectResponse resp = new GetObjectResponse(
                Headers.of("Content-Type", "image/jpeg"),
                "hmall", null, "2026-06-02/abc.jpg",
                new ByteArrayInputStream(body));
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(resp);

        mockMvc.perform(get("/files/2026-06-02/abc.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(body));
    }
}
