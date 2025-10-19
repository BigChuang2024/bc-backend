// java
package com.haderacher.bcbackend.mq;

import com.haderacher.bcbackend.config.RabbitConfiguration;
import com.haderacher.bcbackend.model.ResumeContent;
import com.haderacher.bcbackend.model.User;
import com.haderacher.bcbackend.mq.message.ParseCompleteMessage;
import com.haderacher.bcbackend.mq.message.ResumeMessage;
import com.haderacher.bcbackend.mq.message.ExtractRequestMessage;
import com.haderacher.bcbackend.repository.ResumeContentRepository;
import com.haderacher.bcbackend.repository.ResumeRepository;
import com.haderacher.bcbackend.service.reader.MyPagePdfDocumentReader;
import com.haderacher.bcbackend.util.OSSUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class ParseListener {

    private final OSSUtil ossUtil;
    private final MyPagePdfDocumentReader pdfReader;
    private final ResumeContentRepository contentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ResumeRepository resumeRepository;

    @RabbitListener(queues = RabbitConfiguration.PARSE_QUEUE)
    public void onMessage(ResumeMessage msg) throws IOException {
        User user = User.builder().username(msg.getUsername()).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, null));
        log.info("接收到解析消息: {}", msg);
        // 从 OSS 下载文件（假设 fileName 可用于 ossUtil.download）
        byte[] bytes = ossUtil.download(msg.getFileName());

        List<Document> docs = pdfReader.getDocsFromPdf(bytes, msg.getFileName());
        StringBuilder sbText = new StringBuilder();

        for (Document d : docs) {
            sbText.append(d.getText()).append("\n\n");
        }

        ResumeContent content = new ResumeContent();
        content.setText(sbText.toString());
        ResumeContent saved = contentRepository.save(content);
        log.info("解析并保存到 Mongo, id={}", saved.getId());
        resumeRepository.updateMongoIdByFileName(saved.getId(), msg.getFileName());

        // 发送解析完成消息，通知嵌入消费者
        ParseCompleteMessage complete = new ParseCompleteMessage(msg.getUsername(), saved.getId());
        rabbitTemplate.convertAndSend(RabbitConfiguration.EXCHANGE, RabbitConfiguration.EMBED_ROUTING, complete);
        log.info("已发送嵌入消息: {}", complete);

        // 发送提取结构化字段的请求，通知 ExtractListener
        ExtractRequestMessage extractReq = new ExtractRequestMessage(msg.getUsername(), saved.getId());
        rabbitTemplate.convertAndSend(RabbitConfiguration.EXCHANGE, RabbitConfiguration.EXTRACT_ROUTING, extractReq);
        log.info("已发送提取字段消息: {}", extractReq);
    }
}
