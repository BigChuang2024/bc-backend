package com.haderacher.bcbackend.service.reader;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MyPagePdfDocumentReaderTest {

    @Autowired
    MyPagePdfDocumentReader myPagePdfDocumentReader;

    @Test
    public void getDocsFromPdfTest() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/stample_resume1.pdf")) {
            assertNotNull(is, "资源 `src/main/resources/stample_resume1.pdf` 未找到");
            byte[] bytes = is.readAllBytes();
            List<org.springframework.ai.document.Document> docs = myPagePdfDocumentReader.getDocsFromPdf(bytes, "stample_resume1.pdf");
            assertNotNull(docs);
            assertFalse(docs.isEmpty(), "解析后文档列表应非空");
        }
    }
}