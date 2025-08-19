package com.haderacher.bcbackend.service.retriever;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.bridge.Message;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
public class SearchEngineDocumentRetriever implements DocumentRetriever {

    private static final String API_KEY = "bce-v3/ALTAK-BGhtegTFJkULJnGxHMkcc/f3ff4636793f90557908993ee2f0f4bdad68ac02";

    private final RestClient restClient;

    public SearchEngineDocumentRetriever(RestClient.Builder restClientBuilder) {
        Assert.notNull(restClientBuilder, "restClientBuilder cannot be null");
        this.restClient = restClientBuilder
                .baseUrl("https://qianfan.baidubce.com/v2/ai_search/chat/completions")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY)
                .build();
    }

    @NotNull
    @Override
    public List<Document> retrieve(@NotNull Query query) {
        Assert.notNull(query, "query cannot be null");

        ApiResponse body = restClient.post()
                .body(new SearchRequest(List.of(new Message("user", query.text())), "baidu_search_v2", "week"))
                .retrieve()
                .body(ApiResponse.class);
        if (body == null || CollectionUtils.isEmpty(body.references)) {
            return List.of();
        };
        List<Document> list = body.references.stream()
                .map(reference ->
                        Document.builder()
                                .text(reference.content)
                                .metadata("title", reference.title)
                                .metadata("url", reference.url)
                                .build()
                ).toList();
        log.debug("百度搜索找到：{}", list);
        return list;
    }


    public record Message(String role, String content){}
    public record SearchRequest(
            List<Message> messages,

            @JsonProperty("search_source")
            String searchSource,

            @JsonProperty("search_recency_filter")
            String searchRecencyFilter
    ){}
    public record Reference(
            int id,
            String url,
            String title,
            String date,
            String content,
            String icon,

            @JsonProperty("web_anchor")
            String webAnchor,

            String type,
            String website,
            Object video, // 在示例中为 null，使用 Object 类型增加灵活性
            Object image, // 在示例中为 null，使用 Object 类型增加灵活性

            @JsonProperty("is_aladdin")
            boolean isAladdin
    ) {}


    /**
     * 代表整个 JSON 响应体
     */
    public record ApiResponse(
            @JsonProperty("request_id")
            String requestId,

            List<Reference> references
    ) {}
}
