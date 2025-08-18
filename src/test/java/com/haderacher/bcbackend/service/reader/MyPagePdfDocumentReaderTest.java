package com.haderacher.bcbackend.service.reader;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MyPagePdfDocumentReaderTest {

    @Autowired
    MyPagePdfDocumentReader myPagePdfDocumentReader;

    @Test
    public void getDocsFromPdfTest() {
    }
}
