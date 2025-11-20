package com.haderacher.bcbackend.mq.consumer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haderacher.bcbackend.config.RabbitConfiguration;
import com.haderacher.bcbackend.dto.ResumeContentDto;
import com.haderacher.bcbackend.model.ResumeContent;
import com.haderacher.bcbackend.model.User;
import com.haderacher.bcbackend.mq.message.ExtractRequestMessage;
import com.haderacher.bcbackend.repository.ResumeContentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.StringJoiner;

@Component
@Slf4j
@AllArgsConstructor
public class ExtractListener {

    private final ResumeContentRepository contentRepository;
    private final ChatModel chatModel;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String PROMPT_TEMPLATE = """
            You are a helpful assistant that extracts structured resume fields from the provided resume text.
            Return a JSON object that contains only the following fields (use these exact property names):
            targetPositons (array of strings), skills (array of strings), internship_experiences (array of objects),
            work_experiences (array of objects), project_experiences (array of objects), certifications (array of strings),
            competition_experiences (array of objects), educations (array of strings).

            Exclude these fields entirely from the output: id, resumeId, markdown, text, createdAt, updatedAt.

            If a section is not present in the resume, return an empty array for that field.

            Here is the resume text (delimited by triple backticks):
            ```{context}```

            Output only valid JSON (no extra commentary).
            """;

    private static final String CONTEXT_PLACEHOLDER = "context";

    @RabbitListener(queues = RabbitConfiguration.RESUME_EXTRACT_QUEUE)
    public void onMessage(ExtractRequestMessage msg) {
        User user = User.builder().username(msg.getUsername()).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, null));
        log.info("接收到提取字段消息: {}", msg);

        ResumeContent content = contentRepository.findById(msg.getContentId()).orElse(null);
        if (content == null) {
            log.warn("找不到 ResumeContent, id={}", msg.getContentId());
            return;
        }

        String contextStr = content.getText() == null ? "" : content.getText();

        Prompt prompt = new PromptTemplate(PROMPT_TEMPLATE)
                .create(Map.of(CONTEXT_PLACEHOLDER, contextStr));

        String resultText;
        try {
            resultText = chatModel.call(prompt).getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("调用 LLM 提取失败", e);
            return;
        }

        log.debug("LLM 提取原始输出: {}", resultText);

        // 预处理：去除 code fence（例如 ```json ... ```）并尝试提取首个 JSON 对象或数组
        String cleaned = cleanJsonLikeString(resultText);
        log.debug("LLM 提取清理后文本: {}", cleaned);

        try {
            // 先解析为 JsonNode 用于生成 markdown（比直接绑定 DTO 更鲁棒）
            JsonNode root = objectMapper.readTree(cleaned);

            String markdown = generateMarkdownFromJson(root);
            // 保存 markdown 到 ResumeContent
            content.setMarkdown(markdown);

            // 尝试按 DTO 解析并映射到结构化字段（保留原有行为）
            try {
                ResumeContentDto dto = objectMapper.readValue(cleaned, ResumeContentDto.class);
                content.setTargetPositons(dto.getTargetPositons());
                content.setSkills(dto.getSkills());
                content.setInternship_experiences(dto.getInternship_experiences());
                content.setWork_experiences(dto.getWork_experiences());
                content.setProject_experiences(dto.getProject_experiences());
                content.setCertifications(dto.getCertifications());
                content.setCompetition_experiences(dto.getCompetition_experiences());
                content.setEducations(dto.getEducations());
            } catch (Exception ignore) {
                // DTO 映射失败不阻止 markdown 保存
                log.debug("解析为 DTO 失败，但已生成 Markdown：{}", ignore.getMessage());
            }

            contentRepository.save(content);
            log.info("已将提取字段与 Markdown 保存到 ResumeContent, id={}", content.getId());
        } catch (Exception e) {
            log.error("解析 LLM 输出为 JSON/生成 Markdown 失败，输出文本：{}", cleaned, e);
        }
    }

    // Helper: try to strip triple-backtick fences and extract the first JSON object or array
    private String cleanJsonLikeString(String raw) {
        if (raw == null) return "";
        String s = raw.trim();

        // If wrapped in triple backticks (with optional language), remove them
        if (s.startsWith("```")) {
            // remove leading ```[lang]? and trailing ```
            int firstNewline = s.indexOf('\n');
            if (firstNewline > 0) {
                s = s.substring(firstNewline + 1);
            } else {
                // no newline after ```, just strip the leading ```
                s = s.substring(3);
            }
            if (s.endsWith("```")) {
                s = s.substring(0, s.length() - 3).trim();
            }
        }

        // If still not starting with { or [, try to find the first JSON-like substring
        if (!s.startsWith("{") && !s.startsWith("[")) {
            int firstBrace = s.indexOf('{');
            int lastBrace = s.lastIndexOf('}');
            int firstBracket = s.indexOf('[');
            int lastBracket = s.lastIndexOf(']');

            if (firstBrace >= 0 && lastBrace > firstBrace) {
                s = s.substring(firstBrace, lastBrace + 1);
            } else if (firstBracket >= 0 && lastBracket > firstBracket) {
                s = s.substring(firstBracket, lastBracket + 1);
            }
        }

        return s.trim();
    }

    private String generateMarkdownFromJson(JsonNode root) {
        if (root == null || root.isMissingNode()) return "";
        StringBuilder sb = new StringBuilder();

        sb.append("# 提取的简历信息\n\n");

        appendStringArraySection(sb, root, "targetPositons", "投递方向（Target Positions]");
        appendStringArraySection(sb, root, "skills", "技能（Skills]");
        appendObjectArraySection(sb, root, "internship_experiences", "实习经历（Internship Experiences]");
        appendObjectArraySection(sb, root, "work_experiences", "工作经历（Work Experiences]");
        appendObjectArraySection(sb, root, "project_experiences", "项目经历（Project Experiences]");
        appendStringArraySection(sb, root, "certifications", "证书（Certifications]");
        appendObjectArraySection(sb, root, "competition_experiences", "竞赛经历（Competition Experiences]");
        appendStringArraySection(sb, root, "educations", "教育经历（Educations]");

        return sb.toString();
    }

    private void appendStringArraySection(StringBuilder sb, JsonNode root, String field, String title) {
        sb.append("## ").append(title).append("\n\n");
        JsonNode arr = root.get(field);
        if (arr != null && arr.isArray() && arr.size() > 0) {
            for (JsonNode n : arr) {
                sb.append("- ").append(n.asText()).append("\n");
            }
        } else {
            sb.append("- 无\n");
        }
        sb.append("\n");
    }

    private void appendObjectArraySection(StringBuilder sb, JsonNode root, String field, String title) {
        sb.append("## ").append(title).append("\n\n");
        JsonNode arr = root.get(field);
        if (arr != null && arr.isArray() && arr.size() > 0) {
            for (JsonNode item : arr) {
                if (item.isObject()) {
                    sb.append("- ");
                    boolean first = true;
                    Iterator<String> it = item.fieldNames();
                    while (it.hasNext()) {
                        String k = it.next();
                        JsonNode v = item.get(k);
                        String text;
                        if (v == null || v.isNull()) {
                            text = "";
                        } else if (v.isArray()) {
                            StringJoiner sj = new StringJoiner(", ");
                            for (JsonNode e : v) sj.add(e.asText());
                            text = sj.toString();
                        } else {
                            text = v.asText();
                        }
                        if (!first) sb.append(" | ");
                        sb.append("**").append(k).append("**: ").append(text);
                        first = false;
                    }
                    sb.append("\n");
                } else {
                    sb.append("- ").append(item.toString()).append("\n");
                }
            }
        } else {
            sb.append("- 无\n");
        }
        sb.append("\n");
    }
}
