package com.haderacher.bcbackend.controller;

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

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadResume(
            @RequestParam("resume") MultipartFile file) {
        String url = null;
        try {
            url = ossUtil.upload(file.getBytes(), file.getOriginalFilename());
        } catch (IOException e) {
            log.error("上传文件出现IO异常");
            throw new RuntimeException(e);
        }

        return ResponseEntity.created(java.net.URI.create(url)).body(Map.of("url", url));
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
