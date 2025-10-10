package com.haderacher.bcbackend.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.comm.Protocol;
import com.aliyun.oss.common.comm.SignVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OSSConfiguration {
    @Bean
    OSS aliOssClient(@Value("${alioss.OSS_ACCESS_KEY_ID}") String accessKeyID,
                     @Value("${alioss.OSS_ACCESS_KEY_SECRET}") String accessKeySecret) {

        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        // 设置签名算法版本为 V4
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        // 设置使用 HTTPS 协议访问 OSS，保证传输安全性
        clientBuilderConfiguration.setProtocol(Protocol.HTTPS);



        return OSSClientBuilder.create()
                // 以华东1（杭州）地域的外网访问域名为例，Endpoint填写为oss-cn-hangzhou.aliyuncs.com
                .endpoint("oss-cn-hangzhou.aliyuncs.com")
                // 从环境变量中获取访问凭证（需提前配置 OSS_ACCESS_KEY_ID 和 OSS_ACCESS_KEY_SECRET）
                .credentialsProvider(CredentialsProviderFactory.newDefaultCredentialProvider(accessKeyID, accessKeySecret))
                // 设置客户端配置
                .clientConfiguration(clientBuilderConfiguration)
                // 以华东1（杭州）地域为例，Region填写为cn-hangzhou
                .region("cn-hangzhou")
                .build();
    }
}
