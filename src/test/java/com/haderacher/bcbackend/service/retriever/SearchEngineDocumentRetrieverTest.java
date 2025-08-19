package com.haderacher.bcbackend.service.retriever;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SearchEngineDocumentRetrieverTest {

    @Autowired
    private SearchEngineDocumentRetriever searchEngineDocumentRetriever;

    @Test
    public void retrieveTest() {
        Query query = Query.builder()
                        .text("热点新闻")
                                .build();

        List<Document> retrieve = searchEngineDocumentRetriever.retrieve(query);
        retrieve.forEach(System.out::println);

    }
}
