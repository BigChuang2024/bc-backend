package com.haderacher.bcbackend.controller;

import com.haderacher.bcbackend.common.ApiResponse;
import com.haderacher.bcbackend.service.ResumeService;
import com.haderacher.bcbackend.util.OSSUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/resumes")
@Slf4j
@AllArgsConstructor
public class ResumeController {

    private final OSSUtil ossUtil;
    private final ResumeService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadResume(
            @RequestParam("resume") MultipartFile file) throws IOException {
        String url = resumeService.uploadResume(file);
        return ResponseEntity.created(java.net.URI.create(url)).
                body(ApiResponse.success(Map.of("url", url)));
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadResume(@PathVariable("fileName") String fileName) throws FileNotFoundException {
        byte[] file = ossUtil.download(fileName);
        ByteArrayResource fileResource = null;
        if (file == null) {
            log.warn("trying to download a not exist resume");
            throw new FileNotFoundException();
        }
        fileResource = new ByteArrayResource(file);
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(fileName, StandardCharsets.UTF_8)
                .build();
        String headerValue = contentDisposition.toString();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(fileResource.contentLength())
                .body(fileResource);
    }
}
