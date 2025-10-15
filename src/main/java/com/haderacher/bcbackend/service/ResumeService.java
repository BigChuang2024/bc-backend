package com.haderacher.bcbackend.service;

import com.haderacher.bcbackend.model.Resume;
import com.haderacher.bcbackend.repository.ResumeRepository;
import com.haderacher.bcbackend.model.Student;
import com.haderacher.bcbackend.exception.BadFormatException;
import com.haderacher.bcbackend.exception.EmptyFileException;
import com.haderacher.bcbackend.service.reader.MyPagePdfDocumentReader;
import com.haderacher.bcbackend.util.OSSUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ResumeService {

    private final OSSUtil ossUtil;

    private final VectorStore vectorStore;

    private final TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();

    private final MyPagePdfDocumentReader pdfReader;

    private final ResumeRepository resumeRepository;

    // 这个函数只会给学生使用
    public String processAndStoreResume(MultipartFile file) {
        try {
            // 1. 检查文件是否合法
            isFileValidate(file);
            List<Document> docsFromPdf;
            try {
                docsFromPdf = pdfReader.getDocsFromPdf(file);
            } catch (IOException e) {
                throw new BadFormatException("文件类型不合法");
            }
            // 2. 上传到对象存储 (OSSService)
            String fileUrl = ossUtil.upload(file.getBytes(), file.getOriginalFilename());

            // 3. 文档分片
            List<Document> split = tokenTextSplitter.apply(docsFromPdf);
            Student student = (Student) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userId = student.getId().toString();
            log.debug("adding resume for user:{}", userId);
            split.forEach(document -> document.getMetadata().put("user", userId));

            // 4. 存入向量数据库
            vectorStore.add(split);
            log.info("简历已成功分片并存入向量数据库。");

            // 5. 持久化到关系型数据库
            // 注意：这里假设您有一个 StudentService 来获取当前登录的学生
            // Student currentStudent = studentService.getCurrentStudent();
            Resume resume = new Resume();
            resume.setStudent(student);
            resume.setTitle(file.getOriginalFilename());
            // resume.setStudent(currentStudent); // 关联当前学生
            resumeRepository.save(resume);
            log.info("简历元数据已保存至数据库，URL: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("处理上传的简历文件时发生IO异常", e);
            // 根据您的业务需求，可以抛出自定义的运行时异常
            throw new RuntimeException("处理文件时出错，请重试。", e);
        }
    }

    public ByteArrayResource downloadResume(String fileName) {
            byte[] fileBytes = ossUtil.download(fileName);
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
