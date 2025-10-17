package com.haderacher.bcbackend.service.reader;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class MyPagePdfDocumentReader {

    public List<Document> getDocsFromPdf(byte[] file, String fileName) throws IOException {
        Resource PdfResource = toResource(file, fileName);
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(PdfResource,
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)
                                .build())
                        .withPagesPerDocument(1)
                        .build());

        return pdfReader.read();
    }

    public Resource toResource(byte[] file, String fileName) throws IOException {
        return new ByteArrayResource(file) {
            @Override
            public String getFilename() {
                // MultipartFile 可能有原始文件名，这里返回它
                return fileName;
            }
        };
    }

}