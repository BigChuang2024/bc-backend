package com.haderacher.bcbackend.service;

import com.haderacher.bcbackend.entity.aggregates.student.Student;
import com.haderacher.bcbackend.exception.BadFormatException;
import com.haderacher.bcbackend.exception.EmptyFileException;
import com.haderacher.bcbackend.service.reader.MyPagePdfDocumentReader;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ResumeService {

    private final MinioService minioService;

    private final VectorStore vectorStore;

    private final TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();

    private final MyPagePdfDocumentReader pdfReader;

    ResumeService(@Qualifier("resumeVectorStore") VectorStore vectorStore, MinioService minioService, MyPagePdfDocumentReader pdfReader) {
        this.vectorStore = vectorStore;
        this.minioService = minioService;
        this.pdfReader = pdfReader;
    }

    public String processAndStoreResume(MultipartFile file) throws MinioException {
        // 检查文件类型是否合法
        isFileValidate(file);
        List<Document> docsFromPdf;
        try {
            docsFromPdf= pdfReader.getDocsFromPdf(file);
        } catch (IOException e) {
            throw new BadFormatException("文件类型不合法");
        }
        List<Document> split = tokenTextSplitter.apply(docsFromPdf);
        Student student = (Student) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = student.getId().toString();
        log.debug("adding resume for user:{}", userId);
        split.forEach(document -> document.getMetadata().put("user", userId));
        vectorStore.accept(split);

        String fileKey;
        try {
            fileKey = minioService.uploadFileToMinio(file, userId, "resumes");
        } catch (Exception e) {
            throw new MinioException("minio 出现异常");
        }
        return fileKey;
    }

    public ByteArrayResource downloadResume(String fileKey) throws MinioException {
        byte[] fileBytes;
        try {
             fileBytes = minioService.getFileBytesFromMinio(fileKey, "resumes");
        } catch (Exception e) {
            throw new MinioException();
        }
        return new ByteArrayResource(fileBytes);
    }


    private void isFileValidate(MultipartFile file) {

        // 1. 基本的文件校验
        if (file.isEmpty()) {
            throw new EmptyFileException("文件为空");
        }

        // 2. (可选) 文件类型校验
        List<String> allowedTypes = List.of("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        if (!allowedTypes.contains(file.getContentType())) {
            log.warn("Upload attempt with unsupported file type '{}'", file.getContentType());
            throw new BadFormatException("文件类型错误");
        }

    }
}
