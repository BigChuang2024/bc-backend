package com.haderacher.bcbackend.service;

import com.haderacher.bcbackend.entity.aggregates.student.Student;
import com.haderacher.bcbackend.service.reader.MyPagePdfDocumentReader;
import io.minio.errors.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

    public String processAndStoreResume(MultipartFile file, String userId, String bucket) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        List<Document> docsFromPdf = pdfReader.getDocsFromPdf(file);
        List<Document> split = tokenTextSplitter.apply(docsFromPdf);
        log.debug("adding resume for user:{}", userId);
        split.forEach(document -> document.getMetadata().put("user", userId));
        vectorStore.accept(split);

        return minioService.uploadFileToMinio(file, userId, bucket);
    }

    private void readPDFandStoreVector() {

    }
}
