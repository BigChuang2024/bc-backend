package com.haderacher.bcbackend.controller;

import com.haderacher.bcbackend.common.ApiResponse;
import com.haderacher.bcbackend.dto.ResumeVo;
import com.haderacher.bcbackend.service.ResumeService;
import com.haderacher.bcbackend.util.OSSUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;

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

    @GetMapping("/download/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadResume(@PathVariable("fileName") String fileName) throws FileNotFoundException {
        byte[] file = ossUtil.download(fileName);
        if (file == null) {
            log.warn("trying to download a not exist resume");
            throw new FileNotFoundException();
        }
        ByteArrayResource fileResource = new ByteArrayResource(file);
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

    @GetMapping("/{filename}")
    public ResponseEntity<ApiResponse<ResumeVo>> getResume(@PathVariable("filename") String filename) {
        ResumeVo vo = resumeService.getResumeVoByFileName(filename);
        if (vo == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("resume not found"));
        return ResponseEntity.ok(ApiResponse.success(vo));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResumeVo>>> listResumes() {
        List<ResumeVo> list = resumeService.listResumesForCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PutMapping("/{filename}")
    public ResponseEntity<ApiResponse<ResumeVo>> updateResume(@PathVariable("filename") String filename, @RequestBody ResumeVo vo) {
        try {
            ResumeVo updated = resumeService.updateResumeContentByFileName(filename, vo);
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail(e.getMessage()));
        }
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<ApiResponse<Object>> deleteResume(@PathVariable("filename") String filename) {
        try {
            resumeService.deleteResumeByFileName(filename);
            return ResponseEntity.ok(ApiResponse.success(Map.of()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail(e.getMessage()));
        }
    }

}
