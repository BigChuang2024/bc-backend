package com.haderacher.bcbackend.service.reader;

import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.document.MetadataMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PDFToJsonTransformer implements DocumentTransformer {

    public static final String DEFAULT_JSON_EXTRACT_TEMPLATE = """
			Here is the resume content of the section:
			{context_str}

			Read the attached resume content and output the information in JSON format by sections.

			output:""";

    private static final String CONTEXT_STR_PLACEHOLDER = "context_str";

    private final ChatModel chatModel;

    private final MetadataMode metadataMode;

    private String extractTemplate;

    public PDFToJsonTransformer(ChatModel chatModel) {
        this.chatModel = chatModel;
        this.metadataMode = MetadataMode.NONE;
        this.extractTemplate = DEFAULT_JSON_EXTRACT_TEMPLATE;
    }

    @Override
    public List<Document> apply(List<Document> documents) {

        List<String> transformedStrings = new ArrayList<>();
        for (Document document : documents) {
            var documentContext = document.getFormattedContent(this.metadataMode);

            Prompt prompt = new PromptTemplate(this.extractTemplate)
                    .create(Map.of(CONTEXT_STR_PLACEHOLDER, documentContext));
            transformedStrings.add(this.chatModel.call(prompt).getResult().getOutput().getText());
        }

        return documents;

    }
}
