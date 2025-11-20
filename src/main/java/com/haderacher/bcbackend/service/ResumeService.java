package com.haderacher.bcbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.haderacher.bcbackend.config.RabbitConfiguration;
import com.haderacher.bcbackend.dto.ResumeContentDto;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.AccessDeniedException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
        rabbitTemplate.convertAndSend(RabbitConfiguration.RESUME_EXCHANGE, RabbitConfiguration.RESUME_PARSE_ROUTING, msg);
        log.info("已发送解析消息到 MQ: {}", msg);
        return fileUrl;
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
        // ownership check: only owner can fetch this resume
        User currentUser = userService.getCurrentUser();
        if (resume.getUser() == null || !resume.getUser().getId().equals(currentUser.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("not owner of resume");
        }
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

        // Use typed DTO instead of generic Object/Map
        if (vo.getJson() != null) {
            ResumeContentDto dto = vo.getJson();
            if (dto.getTargetPositons() != null) existing.setTargetPositons(dto.getTargetPositons());
            if (dto.getSkills() != null) existing.setSkills(dto.getSkills());
            if (dto.getInternship_experiences() != null) existing.setInternship_experiences(dto.getInternship_experiences());
            if (dto.getWork_experiences() != null) existing.setWork_experiences(dto.getWork_experiences());
            if (dto.getProject_experiences() != null) existing.setProject_experiences(dto.getProject_experiences());
            if (dto.getCertifications() != null) existing.setCertifications(dto.getCertifications());
            if (dto.getCompetition_experiences() != null) existing.setCompetition_experiences(dto.getCompetition_experiences());
            if (dto.getEducations() != null) existing.setEducations(dto.getEducations());
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
            ResumeContentDto dto = getResumeContentDto(content);
            vo.setJson(dto);
        }
        if (resume.getUpdatedAt() != null) {
            long epochSec = resume.getUpdatedAt().toEpochSecond(ZoneOffset.UTC);
            vo.setTimestamp(String.valueOf(epochSec));
        } else if (resume.getCreatedAt() != null) {
            long epochSec = resume.getCreatedAt().toEpochSecond(ZoneOffset.UTC);
            vo.setTimestamp(String.valueOf(epochSec));
        }

        return vo;
    }


    @NotNull
    private static ResumeContentDto getResumeContentDto(ResumeContent content) {
        ResumeContentDto dto = new ResumeContentDto();
        dto.setTargetPositons(content.getTargetPositons());
        dto.setSkills(content.getSkills());
        dto.setInternship_experiences(content.getInternship_experiences());
        dto.setWork_experiences(content.getWork_experiences());
        dto.setProject_experiences(content.getProject_experiences());
        dto.setCertifications(content.getCertifications());
        dto.setCompetition_experiences(content.getCompetition_experiences());
        dto.setEducations(content.getEducations());
        return dto;
    }

}
