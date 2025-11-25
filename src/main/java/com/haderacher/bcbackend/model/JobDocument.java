package com.haderacher.bcbackend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "job_index")
public class JobDocument {

    @Id
    private Long id;

    // Text 类型支持分词查询，Keyword 支持精确匹配
    // analyzer = "ik_max_word" 是中文分词器（必须要装 IK 插件，否则用 "standard"）
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String description;

    @Field(type = FieldType.Keyword)
    private String recruiter;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String address;

    @Field(type = FieldType.Integer)
    private Integer salaryMin;

    @Field(type = FieldType.Integer)
    private Integer salaryMax;

    // 将 JPA 的 ElementCollection 映射为普通 List
    @Field(type = FieldType.Keyword)
    private List<String> labels;
    
    // 将 JPA 的 ManyToMany Skills 转换为简单的技能名称列表
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private List<String> skillNames; 
    
    @Field(type = FieldType.Keyword)
    private String category;
}