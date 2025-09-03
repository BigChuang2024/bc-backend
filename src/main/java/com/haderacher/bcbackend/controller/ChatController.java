package com.haderacher.bcbackend.controller;

import com.haderacher.bcbackend.service.component.CompressionDocumentPostProcessor;
import com.haderacher.bcbackend.service.component.SearchEngineDocumentRetriever;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class ChatController {

    private final DeepSeekChatModel chatModel;

    private final VectorStore vectorStore;

    private final SearchEngineDocumentRetriever searchEngineDocumentRetriever;
    private final ChatClient.Builder chatClientBuilder;

    @Autowired
    public ChatController(DeepSeekChatModel chatModel, VectorStore vectorStore, SearchEngineDocumentRetriever searchEngineDocumentRetriever, ChatClient.Builder chatClientBuilder) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
        this.searchEngineDocumentRetriever = searchEngineDocumentRetriever;
        this.chatClientBuilder = chatClientBuilder;
    }

    @GetMapping("/ai/RAGWithSearchEngine")
    public String RAGWithSearchEngine(@RequestParam(value = "message") String message) {

        RetrievalAugmentationAdvisor searchAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(searchEngineDocumentRetriever)
                .documentPostProcessors(CompressionDocumentPostProcessor.builder()
                        .chatClientBuilder(chatClientBuilder.clone())
                        .build())
                .build();

        String content = ChatClient.builder(chatModel)
                .build()
                .prompt("你需要回答用户的问题，并友好的和用户交流")
                .advisors(searchAdvisor)
                .user(message)
                .call()
                .content();

        return content;
    }

    @GetMapping("/ai/RAGWithVectorStore")
    public String RAGWithVectorStore(@RequestParam(value = "message") String message) {
        var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().similarityThreshold(0.8d).topK(6).build())
                .build();

        var ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryExpander(MultiQueryExpander.builder()
                        .chatClientBuilder(chatClientBuilder.clone())
                        .numberOfQueries(3)
                        .build())
                .queryTransformers(
                        RewriteQueryTransformer.builder()
                                .chatClientBuilder(chatClientBuilder.clone())
                                .targetSearchSystem("vector stores")
                                .build(),
                        TranslationQueryTransformer.builder()
                                .chatClientBuilder(chatClientBuilder.clone())
                                .targetLanguage("chinese")
                                .build()
                ).documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .build())
                .build();

        String content = ChatClient.builder(chatModel)
                .build()
                .prompt("你需要回答用户的问题，并友好的和用户交流")
                .advisors(ragAdvisor)
                .user(message)
                .call()
                .content();
        return content;
    }


}