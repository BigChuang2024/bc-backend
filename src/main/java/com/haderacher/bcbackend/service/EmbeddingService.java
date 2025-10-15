package com.haderacher.bcbackend.service;


import com.haderacher.bcbackend.model.JobDetails;
import com.haderacher.bcbackend.repository.JobDetailsRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    @Autowired
    private JobDetailsRepository jobDetailsRepository;

    @Autowired
    private VectorStore vectorStore;

    public void embedAllJobs() {
        List<JobDetails> all = jobDetailsRepository.findAll();
        // Convert each JobDetails to Document and add to the vector store;
        List<Document> documents = all.stream()
                .map(this::jobDetailsToDocument)
                .toList();
        // Use the vector store's add method to add the documents
        TokenCountBatchingStrategy batchingStrategy = new TokenCountBatchingStrategy();
        List<List<Document>> batch = batchingStrategy.batch(documents);
        for (List<Document> docBatch : batch) {
            vectorStore.add(docBatch);
        }
    }

    public void embedSingleJob(JobDetails jobDetails) {
        Document document = jobDetailsToDocument(jobDetails);
        vectorStore.add(List.of(document));
    }

    public void embedMultipleJobs(List<JobDetails> jobDetailsList) {
        // Convert each JobDetails to Document and add to the vector store;
        List<Document> documents = jobDetailsList.stream()
                .map(this::jobDetailsToDocument)
                .toList();
        // Use the vector store's add method to add the documents
        TokenCountBatchingStrategy batchingStrategy = new TokenCountBatchingStrategy();
        List<List<Document>> batch = batchingStrategy.batch(documents);
        for (List<Document> docBatch : batch) {
            vectorStore.add(docBatch);
        }
    }

    private Document jobDetailsToDocument(JobDetails jobDetails) {
        // 创建一个可变Map
        Map<String, Object> metaData = new HashMap<>();
        // 安全地添加非null值
        if (jobDetails.getActiveTimeDesc() != null) metaData.put("activeTimeDesc", jobDetails.getActiveTimeDesc());
        if (jobDetails.getAddress() != null) metaData.put("address", jobDetails.getAddress());
        if (jobDetails.getDegreeName() != null) metaData.put("degreeName", jobDetails.getDegreeName());
        if (jobDetails.getExperienceName() != null) metaData.put("experienceName", jobDetails.getExperienceName());
        if (jobDetails.getJobLabels() != null) metaData.put("jobLabels", jobDetails.getJobLabels());
        if (jobDetails.getJobName() != null) metaData.put("jobName", jobDetails.getJobName());
        if (jobDetails.getSalaryDesc() != null) metaData.put("salaryDesc", jobDetails.getSalaryDesc());
        if (jobDetails.getPostDescription() != null) metaData.put("postDescription", jobDetails.getPostDescription());
        return new Document(jobDetails.getId(), jobDetails.getPostDescription(), metaData);
    }
}
