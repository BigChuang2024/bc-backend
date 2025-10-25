package com.haderacher.bcbackend.service;

import com.haderacher.bcbackend.model.Job;
import com.haderacher.bcbackend.repository.JobRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EmbeddingService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private VectorStore vectorStore;

    public void embedAllJobs() {
        List<Job> all = jobRepository.findAll();
        // Convert each Job to Document and add to the vector store;
        List<Document> documents = all.stream()
                .map(this::jobToDocument)
                .toList();
        // Use the vector store's add method to add the documents
        TokenCountBatchingStrategy batchingStrategy = new TokenCountBatchingStrategy();
        List<List<Document>> batch = batchingStrategy.batch(documents);
        for (List<Document> docBatch : batch) {
            vectorStore.add(docBatch);
        }
    }

    public void embedSingleJob(Job job) {
        Document document = jobToDocument(job);
        vectorStore.add(List.of(document));
    }

    public void embedMultipleJobs(List<Job> jobs) {
        // Convert each Job to Document and add to the vector store;
        List<Document> documents = jobs.stream()
                .map(this::jobToDocument)
                .toList();
        // Use the vector store's add method to add the documents
        TokenCountBatchingStrategy batchingStrategy = new TokenCountBatchingStrategy();
        List<List<Document>> batch = batchingStrategy.batch(documents);
        for (List<Document> docBatch : batch) {
            vectorStore.add(docBatch);
        }
    }

    private Document jobToDocument(Job job) {
        // 创建一个可变Map
        Map<String, Object> metaData = new HashMap<>();
        // 安全地添加非null值
        if (job.getName() != null) metaData.put("jobName", job.getName());
        if (job.getAddress() != null) metaData.put("address", job.getAddress());
        if (job.getDegreeReq() != null) metaData.put("degreeName", job.getDegreeReq());
        if (job.getRecruiter() != null) metaData.put("recruiter", job.getRecruiter());
        if (job.getLabels() != null) metaData.put("jobLabels", job.getLabels());
        if (job.getSalaryMin() != null) metaData.put("salaryMin", job.getSalaryMin());
        if (job.getSalaryMax() != null) metaData.put("salaryMax", job.getSalaryMax());
        if (job.getSalaryType() != null) metaData.put("salaryType", job.getSalaryType());
        if (job.getSalaryAnnual() != null) metaData.put("salaryAnnual", job.getSalaryAnnual());
        if (job.getCategory() != null) metaData.put("category", job.getCategory());

        String id = job.getId() != null ? job.getId().toString() : UUID.randomUUID().toString();
        String content = job.getDescription() != null ? job.getDescription() : job.getName();
        return new Document(id, content, metaData);
    }
}
