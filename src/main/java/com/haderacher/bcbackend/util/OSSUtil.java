package com.haderacher.bcbackend.util;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.OSSObject;
import com.haderacher.bcbackend.service.UserService;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
@Component
public class OSSUtil {

    private final OSS ossClient;

    private final String bucketName;
    private final UserService userService;

    OSSUtil(OSS ossClient, @Value("${alioss.bucket}") String bucketName, UserService userService) {
        this.ossClient = ossClient;
        this.bucketName = bucketName;
        this.userService = userService;
    }

    /**
     * 文件上传
     *
     * @param bytes 文件的字节数组
     * @param originalFileName 文件名uuid
     * @return 返回文件的 阿里云url 路径
     */
    public String upload(byte[] bytes, String originalFileName) {
        var user = UserService.getCurrentUser();
        String currentUsername = user.getUsername();
        String objectName = currentUsername + originalFileName;
        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        }

        //约定文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append("oss-cn-hangzhou.aliyuncs.com")
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }

    /**
         * 从OSS删除文件
         *
         * @param objectName 要删除的文件名 (不包含用户名前缀)
         * @return 如果删除成功返回 true, 否则返回 false
         */
        public boolean deleteObject(String objectName) {
            // 拼接当前用户名作为前缀，定位到完整的文件路径
            String currentUsername = UserService.getCurrentUser().getUsername();
            String fullObjectName = currentUsername + objectName;

            try {
                // 调用SDK的deleteObject方法删除文件。
                ossClient.deleteObject(bucketName, fullObjectName);
                log.info("成功从存储桶 '{}' 中删除文件: {}", bucketName, fullObjectName);
                return true;
            } catch (OSSException oe) {
                log.error("删除OSS文件 '{}' 时发生OSS异常: {}", fullObjectName, oe.getErrorMessage(), oe);
                return false;
            } catch (ClientException ce) {
                log.error("删除OSS文件 '{}' 时发生客户端异常: {}", fullObjectName, ce.getMessage(), ce);
                return false;
            }
        }

    /**
     * 检查文件是否存在于OSS
     *
     * @param objectName 文件在OSS上的完整名称 (key)
     * @return 如果文件存在则返回 true, 否则返回 false
     */
    public boolean doesObjectExist(String objectName) {
        var currentUsername = userService.getCurrentUser().getUsername();
        objectName = currentUsername + objectName;
        try {
            // 调用SDK的doesObjectExist方法判断文件是否存在。
            boolean found = ossClient.doesObjectExist(bucketName, objectName);
            log.info("检查文件 '{}' 在存储桶 '{}' 中是否存在: {}", objectName, bucketName, found);
            return found;
        } catch (OSSException oe) {
            log.error("检查OSS文件存在性时发生OSS异常: {}", oe.getErrorMessage(), oe);
            // 发生服务端异常时，通常认为文件检查失败，返回false
            return false;
        } catch (ClientException ce) {
            log.error("检查OSS文件存在性时发生客户端异常: {}", ce.getMessage(), ce);
            // 发生客户端异常（如网络问题），也返回false
            return false;
        }
    }

    /**
     * 从OSS下载文件
     *
     * @param objectName 文件名 (不包含用户名前缀)
     * @return 文件的字节数组，如果文件不存在或发生错误则返回 null
     */
    public @Nullable byte[] download(String objectName) {
        // 拼接当前用户名作为前缀，定位到完整的文件路径
        String currentUsername = UserService.getCurrentUser().getUsername();
        String fullObjectName = currentUsername + objectName;

        try {
            // 调用 getObject 方法获取一个文件对象
            OSSObject ossObject = ossClient.getObject(bucketName, fullObjectName);
            log.info("开始从存储桶 '{}' 下载文件: {}", bucketName, fullObjectName);

            // 使用 try-with-resources 确保输入流被正确关闭
            try (var content = ossObject.getObjectContent()) {
                byte[] bytes = content.readAllBytes();
                log.info("文件 '{}' 下载成功。", fullObjectName);
                return bytes;
            } catch (IOException e) {
                log.error("读取OSS文件 '{}' 内容时发生IO异常: {}", fullObjectName, e.getMessage(), e);
                return null;
            }
        } catch (OSSException oe) {
            log.error("下载OSS文件 '{}' 时发生OSS异常: {}", fullObjectName, oe.getErrorMessage(), oe);
            return null;
        } catch (ClientException ce) {
            log.error("下载OSS文件 '{}' 时发生客户端异常: {}", fullObjectName, ce.getMessage(), ce);
            return null;
        }
    }
}
