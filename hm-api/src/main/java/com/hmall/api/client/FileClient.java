package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@FeignClient("file-service")
public interface FileClient {

    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, String> uploadImage(@RequestPart("file") MultipartFile file);

    @GetMapping("/files/{id}")
    byte[] downloadFile(@PathVariable("id") Long id);
}
