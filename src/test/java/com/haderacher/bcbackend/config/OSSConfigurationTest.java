package com.haderacher.bcbackend.config;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.Bucket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class OSSConfigurationTest {

    @Autowired
    private OSS ossClient;

    @Test
    public void testOssAvailable() {

        try {
            // 列出当前用户的所有 Bucket
            List<Bucket> buckets = ossClient.listBuckets();
            // 遍历打印每个 Bucket 的名称
            for (Bucket bucket : buckets) {
                System.out.println(bucket.getName());
            }
        } catch (OSSException | ClientException e) {
            throw new RuntimeException(e);
        }
    }
}
