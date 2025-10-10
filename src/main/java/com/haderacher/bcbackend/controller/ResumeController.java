//package com.haderacher.bcbackend.controller;
//
//import com.haderacher.bcbackend.entity.aggregates.student.Student;
//import com.haderacher.bcbackend.service.OSSService;
//import com.haderacher.bcbackend.service.ResumeService;
//import io.minio.MinioClient;
//import io.minio.errors.MinioException;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.http.*;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/resumes")
//@Slf4j
//@AllArgsConstructor
//public class ResumeController {
//
//    private final OSSService OSSService;
//    private ResumeService resumeService;
//
//    @PostMapping("/upload")
//    public ResponseEntity<Map<String, String>> uploadResume(
//            @RequestParam("resume") MultipartFile file) throws MinioException {
//        String fileKey = resumeService.processAndStoreResume(file);
//        Student student = (Student) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        log.info("Successfully processed resume for user ID: {}. File key: {}", student.getId(), fileKey);
//        Map<String, String> response = Map.of(
//                "message", "简历上传成功！",
//                "fileKey", fileKey,
//                "userId", String.valueOf(student.getId())
//        );
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/{fileKey}")
//    public ResponseEntity<ByteArrayResource> downloadResume(@PathVariable("fileKey") String fileKey) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
//        ByteArrayResource fileResource = resumeService.downloadResume(fileKey);
//        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
//                .filename(OSSService.getFileName(fileKey, "resumes"), StandardCharsets.UTF_8)
//                .build();
//        String headerValue = contentDisposition.toString();
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .contentLength(fileResource.contentLength())
//                .body(fileResource);
//    }
//
//}
