package com.haderacher.bcbackend.controller;

import com.haderacher.bcbackend.service.MinioService;
import com.haderacher.bcbackend.service.ResumeService;
import io.minio.MinioClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resumes")
@Slf4j
@AllArgsConstructor
public class ResumeController {

    private MinioService minioService;

    private ResumeService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadResume(
            @RequestParam("resume") MultipartFile file,
            @RequestParam("userId") String userId) {

        // 1. 基本的文件校验
        if (file.isEmpty()) {
            log.warn("Upload attempt with an empty file for user ID: {}", userId);
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "上传失败：文件不能为空。"),
                    HttpStatus.BAD_REQUEST
            );
        }

        // 2. (可选) 文件类型校验
        List<String> allowedTypes = List.of("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        if (!allowedTypes.contains(file.getContentType())) {
            log.warn("Upload attempt with unsupported file type '{}' for user ID: {}", file.getContentType(), userId);
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "上传失败：仅支持PDF, DOC, DOCX格式。"),
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE
            );
        }

        try {
            // 3. 调用Service层处理业务逻辑
            log.info("Received resume upload request for user ID: {}", userId);
            String fileKey = resumeService.processAndStoreResume(file, userId, "resumes");

            // 4. 返回成功的响应
            log.info("Successfully processed resume for user ID: {}. File key: {}", userId, fileKey);
            Map<String, String> response = Map.of(
                    "message", "简历上传成功！",
                    "fileKey", fileKey,
                    "userId", String.valueOf(userId)
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 5. 捕获并处理未知异常
            log.error("Error uploading resume for user ID: " + userId, e);
            return new ResponseEntity<>(
                    Collections.singletonMap("error", "服务器内部错误，上传失败：" + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /* 下载简历 */
    @GetMapping("/{fileKey}")
    public ResponseEntity<byte[]> downloadResume(@PathVariable("fileKey") String fileKey) {
        try {
            byte[] fileBytes = minioService.getFileBytesFromMinio(fileKey, "resumes");

            // 设置响应头，告诉浏览器这是一个需要下载的文件
            HttpHeaders headers = new HttpHeaders();
            // application/octet-stream 是通用的二进制文件类型
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // 1. 对包含中文的文件名进行 URL 编码，指定编码为 UTF-8
            String encodedFileName = URLEncoder.encode(fileKey, StandardCharsets.UTF_8);

            // 2. 手动构建符合 RFC 6266 标准的 Content-Disposition Header
            // 格式为: attachment; filename*="<charset>'<lang>'<encoded-value>"
            // 注意：filename* 后面的 UTF-8'' 是标准格式，两个单引号不能省略
            String contentDisposition = "attachment; filename*=UTF-8''" + encodedFileName;

            headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);

            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            // 同样，进行错误处理
            // log.error("Error downloading file '{}' from bucket '{}': {}", fileKey, bucket, e.getMessage());
            // 注意这里返回的 body 类型需要匹配 ResponseEntity<byte[]>，所以返回 null 或者一个空的 byte 数组
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
