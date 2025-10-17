package com.haderacher.bcbackend.mq;

import com.haderacher.bcbackend.config.RabbitConfiguration;
import com.haderacher.bcbackend.model.ResumeContent;
import com.haderacher.bcbackend.model.User;
import com.haderacher.bcbackend.mq.message.ParseCompleteMessage;
import com.haderacher.bcbackend.repository.ResumeContentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class EmbedListener {

    private final ResumeContentRepository contentRepository;
    private final TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
    private final VectorStore vectorStore;

    @RabbitListener(queues = RabbitConfiguration.EMBED_QUEUE)
    public void onMessage(ParseCompleteMessage msg) {
        User user = User.builder().username(msg.getUsername()).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, null));
        log.info("接收到嵌入消息: {}", msg);
        ResumeContent content = contentRepository.findById(msg.getContentId()).orElse(null);
        if (content == null) {
            log.warn("找不到解析内容 id={}", msg.getContentId());
            return;
        }

        // 根据解析文本构造 Document（也可以拆成多 document）
        Document doc = Document.builder().text(content.getText()).build();
        doc.getMetadata().put("user", msg.getUsername());

        List<Document> docs = new ArrayList<>();
        docs.add(doc);

        // 分片并入向量库
        List<Document> split = tokenTextSplitter.apply(docs);
        vectorStore.add(split);
        log.info("嵌入已完成，向量已写入");
    }
}
