package com.haderacher.bcbackend.repository;

import com.haderacher.bcbackend.model.JobDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSearchRepository extends ElasticsearchRepository<JobDocument, Long> {
    // 只要定义方法名，ES 会自动帮你写查询逻辑
    
    // 简单搜索：根据名字或描述查找
    List<JobDocument> findByNameOrDescription(String name, String description);
    
    // 范围查询：找薪资大于多少的
    List<JobDocument> findBySalaryMinGreaterThanEqual(Integer min);
}