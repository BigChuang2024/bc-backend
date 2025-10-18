package com.haderacher.bcbackend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haderacher.bcbackend.config.RabbitConfiguration;
import com.haderacher.bcbackend.exception.BadFormatException;
import com.haderacher.bcbackend.exception.EmptyFileException;
import com.haderacher.bcbackend.model.ParseStatus;
import com.haderacher.bcbackend.model.Resume;
import com.haderacher.bcbackend.model.ResumeContent;
import com.haderacher.bcbackend.model.User;
import com.haderacher.bcbackend.mq.message.ResumeMessage;
import com.haderacher.bcbackend.repository.ResumeContentRepository;
import com.haderacher.bcbackend.repository.ResumeRepository;
import com.haderacher.bcbackend.service.reader.MyPagePdfDocumentReader;
import com.haderacher.bcbackend.util.OSSUtil;
import com.haderacher.bcbackend.dto.ResumeVo;
import com.haderacher.bcbackend.model.valueobject.InternshipExperience;
import com.haderacher.bcbackend.model.valueobject.WorkExperience;
import com.haderacher.bcbackend.model.valueobject.ProjectExperience;
import com.haderacher.bcbackend.model.valueobject.CompetitionExperience;
import org.springframework.security.access.AccessDeniedException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class ResumeService {

    private final OSSUtil ossUtil;
    private final VectorStore vectorStore;
    private final TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
    private final MyPagePdfDocumentReader pdfReader;
    private final ResumeRepository resumeRepository;
    private final ResumeContentRepository contentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final DateTimeFormatter TS_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public String uploadResume(MultipartFile file) throws IOException {
        // 1. 检查文件是否合法
        isFileValidate(file);
        String fileUrl = ossUtil.upload(file.getBytes(), file.getOriginalFilename());
        User currentUser = userService.getCurrentUser();

        // 3. 存入mysql
        Resume resume = Resume.builder()
                .user(currentUser)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .status(ParseStatus.PENDING)
                .build();
        resumeRepository.save(resume);
        log.info("简历元数据已保存至数据库，URL: {}", fileUrl);
        ResumeMessage msg = new ResumeMessage(fileUrl, file.getOriginalFilename(), UserService.getCurrentUserDetails().getUsername());
        rabbitTemplate.convertAndSend(RabbitConfiguration.EXCHANGE, RabbitConfiguration.PARSE_ROUTING, msg);
        log.info("已发送解析消息到 MQ: {}", msg);
        return fileUrl;
    }

    @Deprecated
    @PreAuthorize("hasRole('STUDENT')")
    public String processAndStoreResume(MultipartFile file) {
        try {
            // 1. 检查文件是否合法
            isFileValidate(file);
            List<Document> docsFromPdf;
            try {
                docsFromPdf = pdfReader.getDocsFromPdf(file.getBytes(), file.getOriginalFilename());
            } catch (IOException e) {
                throw new BadFormatException("文件类型不合法");
            }
            // 2. 上传到对象存储 (OSSService)
            String fileUrl = ossUtil.upload(file.getBytes(), file.getOriginalFilename());

            // 3. 文档分片
            List<Document> split = tokenTextSplitter.apply(docsFromPdf);
            UserDetails currentUser = UserService.getCurrentUserDetails();
            String userName = currentUser.getUsername();
            log.debug("adding resume for user:{}", userName);
            split.forEach(document -> document.getMetadata().put("user", userName));

            // 4. 存入向量数据库
            vectorStore.add(split);
            log.info("简历已成功分片并存入向量数据库。");

            // 5. 持久化到关系型数据库
            // 注意：这里假设您有一个 StudentService 来获取当前登录的学生
            // Student currentStudent = studentService.getCurrentStudent();
            Resume resume = new Resume();
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

    @SuppressWarnings("unused")
    public ByteArrayResource downloadResume(String fileName) {
        byte[] fileBytes = ossUtil.download(fileName);
        if (fileBytes == null) {
            log.warn("downloadResume: OSS returned null for {}", fileName);
            return null;
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

    // --- New CRUD related methods ---

    @SuppressWarnings("unused")
    public ResumeVo getResumeVoById(Long resumeId) {
        Optional<Resume> resumeOpt = resumeRepository.findById(resumeId);
        if (resumeOpt.isEmpty()) return null;
        Resume resume = resumeOpt.get();
        ResumeContent content = contentRepository.findByResumeId(resume.getId());
        return buildResumeVo(resume, content);
    }

    public ResumeVo getResumeVoByFileName(String fileName) {
        Resume resume = resumeRepository.findByFileName(fileName).orElse(null);
        if (resume == null) return null;
        ResumeContent content = contentRepository.findByResumeId(resume.getId());
        return buildResumeVo(resume, content);
    }

    public List<ResumeVo> listResumesForCurrentUser() {
        User currentUser = userService.getCurrentUser();
        List<Resume> list = resumeRepository.findAll().stream().filter(r -> r.getUser() != null && r.getUser().getId().equals(currentUser.getId())).toList();
        return list.stream().map(r -> buildResumeVo(r, contentRepository.findByResumeId(r.getId()))).toList();
    }

    public ResumeVo updateResumeContent(Long resumeId, ResumeVo vo) {
        Resume resume = resumeRepository.findById(resumeId).orElseThrow(() -> new IllegalArgumentException("resume not found"));
        ResumeContent existing = contentRepository.findByResumeId(resumeId);
        if (existing == null) {
            // create new ResumeContent
            existing = new ResumeContent();
            existing.setResumeId(resumeId);
        }
        if (vo.getMarkdown() != null) existing.setMarkdown(vo.getMarkdown());
        // json could be a map or object; try to convert to Map
        if (vo.getJson() != null) {
            Map<String, Object> map = objectMapper.convertValue(vo.getJson(), new TypeReference<>() {});
            // populate known fields if present, use convertValue with TypeReference to avoid unchecked casts
            if (map.containsKey("markdown")) existing.setMarkdown((String) map.get("markdown"));
            try {
                if (map.containsKey("targetPositons")) {
                    List<String> t = objectMapper.convertValue(map.get("targetPositons"), new TypeReference<>() {});
                    existing.setTargetPositons(t);
                }
            } catch (Exception e) { log.warn("failed to convert targetPositons: {}", e.getMessage()); }
            try {
                if (map.containsKey("skills")) {
                    List<String> s = objectMapper.convertValue(map.get("skills"), new TypeReference<>() {});
                    existing.setSkills(s);
                }
            } catch (Exception e) { log.warn("failed to convert skills: {}", e.getMessage()); }
            try {
                if (map.containsKey("internship_experiences")) {
                    List<InternshipExperience> ies = objectMapper.convertValue(map.get("internship_experiences"), new TypeReference<>() {});
                    existing.setInternship_experiences(ies);
                }
            } catch (Exception e) { log.warn("failed to convert internship_experiences: {}", e.getMessage()); }
            try {
                if (map.containsKey("work_experiences")) {
                    List<WorkExperience> wes = objectMapper.convertValue(map.get("work_experiences"), new TypeReference<>() {});
                    existing.setWork_experiences(wes);
                }
            } catch (Exception e) { log.warn("failed to convert work_experiences: {}", e.getMessage()); }
            try {
                if (map.containsKey("project_experiences")) {
                    List<ProjectExperience> pes = objectMapper.convertValue(map.get("project_experiences"), new TypeReference<>() {});
                    existing.setProject_experiences(pes);
                }
            } catch (Exception e) { log.warn("failed to convert project_experiences: {}", e.getMessage()); }
            try {
                if (map.containsKey("certifications")) {
                    List<String> certs = objectMapper.convertValue(map.get("certifications"), new TypeReference<>() {});
                    existing.setCertifications(certs);
                }
            } catch (Exception e) { log.warn("failed to convert certifications: {}", e.getMessage()); }
            try {
                if (map.containsKey("competition_experiences")) {
                    List<CompetitionExperience> ces = objectMapper.convertValue(map.get("competition_experiences"), new TypeReference<>() {});
                    existing.setCompetition_experiences(ces);
                }
            } catch (Exception e) { log.warn("failed to convert competition_experiences: {}", e.getMessage()); }
            try {
                if (map.containsKey("educations")) {
                    List<Map<String, Object>> eds = objectMapper.convertValue(map.get("educations"), new TypeReference<>() {});
                    existing.setEducations(eds);
                }
            } catch (Exception e) { log.warn("failed to convert educations: {}", e.getMessage()); }
        }
        ResumeContent saved = contentRepository.save(existing);
        // touch resume updatedAt
        resume.setUpdatedAt(java.time.LocalDateTime.now());
        resumeRepository.save(resume);
        return buildResumeVo(resume, saved);
    }

    // --- filename based helpers ---
    public ResumeVo updateResumeContentByFileName(String fileName, ResumeVo vo) {
        Resume resume = resumeRepository.findByFileName(fileName).orElseThrow(() -> new IllegalArgumentException("resume not found"));
        // ownership check
        User currentUser = userService.getCurrentUser();
        if (resume.getUser() == null || !resume.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("not owner of resume");
        }
        return updateResumeContent(resume.getId(), vo);
    }

    public void deleteResumeByFileName(String fileName) {
        Resume resume = resumeRepository.findByFileName(fileName).orElseThrow(() -> new IllegalArgumentException("resume not found"));
        // ownership check
        User currentUser = userService.getCurrentUser();
        if (resume.getUser() == null || !resume.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("not owner of resume");
        }
        deleteResume(resume.getId());
    }

    public void deleteResume(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId).orElseThrow(() -> new IllegalArgumentException("resume not found"));
        // delete content from mongo if exists
        ResumeContent existing = contentRepository.findByResumeId(resumeId);
        if (existing != null) contentRepository.delete(existing);
        // delete resume record
        resumeRepository.delete(resume);
        // optionally delete from OSS if fileName exists
        if (resume.getFileName() != null) {
            try {
                // Use the provided deleteObject method on OSSUtil to remove the file (it handles username prefix)
                boolean deleted = ossUtil.deleteObject(resume.getFileName());
                if (!deleted) {
                    log.warn("OSSUtil.deleteObject returned false for {}", resume.getFileName());
                }
            } catch (Exception e) {
                log.warn("Failed to delete file from OSS: {}", e.getMessage());
            }
        }
    }

    // helper to build ResumeVo
    private ResumeVo buildResumeVo(Resume resume, ResumeContent content) {
        ResumeVo vo = new ResumeVo();
        vo.setFileName(resume.getFileName());
        if (content != null) {
            vo.setMarkdown(content.getMarkdown());
            Map<String, Object> json = Map.of(
                    "targetPositons", content.getTargetPositons(),
                    "skills", content.getSkills(),
                    "internship_experiences", content.getInternship_experiences(),
                    "work_experiences", content.getWork_experiences(),
                    "project_experiences", content.getProject_experiences(),
                    "certifications", content.getCertifications(),
                    "competition_experiences", content.getCompetition_experiences(),
                    "educations", content.getEducations()
            );
            vo.setJson(json);
        }
        if (resume.getUpdatedAt() != null) {
            vo.setTimestamp(resume.getUpdatedAt().format(TS_FORMATTER));
        } else if (resume.getCreatedAt() != null) {
            vo.setTimestamp(resume.getCreatedAt().format(TS_FORMATTER));
        }
        return vo;
    }

}
