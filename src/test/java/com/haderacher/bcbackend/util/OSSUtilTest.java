package com.haderacher.bcbackend.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@WithMockUser(username = "testuser")
public class OSSUtilTest {

    @Autowired
    private OSSUtil ossUtil;

    private byte[] testFileBytes;
    private String testObjectName;

    @BeforeEach
    void setUp() {
        // 在每个测试方法执行前，准备测试数据
        // 生成一个唯一的文件名，避免测试间互相影响
        testObjectName = UUID.randomUUID().toString() + ".txt";
        testFileBytes = "This is a test file content.".getBytes();
    }

    @AfterEach
    void tearDown() {
        // 在每个测试方法执行后，尝试清理上传的文件，确保OSS环境干净
        // 即使测试失败，也会尝试删除，避免留下垃圾数据
        ossUtil.deleteObject(testObjectName);
    }

    @Test
    public void testUploadAndCheckAndClean() {
        // 1. 测试文件上传
        String fileUrl = ossUtil.upload(testFileBytes, testObjectName);
        assertNotNull(fileUrl, "上传后返回的URL不应为null");
        assertTrue(fileUrl.contains(testObjectName), "返回的URL应包含文件名");
        assertTrue(fileUrl.contains("testuser"), "返回的URL应包含当前用户名作为前缀");

        // 2. 测试文件是否存在 (doesObjectExist)
        boolean exists = ossUtil.doesObjectExist(testObjectName);
        assertTrue(exists, "上传后，文件应该存在于OSS中");

        // 3. 测试删除文件 (deleteObject)
        boolean deleted = ossUtil.deleteObject(testObjectName);
        assertTrue(deleted, "删除操作应返回true表示成功");

        // 4. 再次检查文件是否存在，确认已被删除
        boolean existsAfterDelete = ossUtil.doesObjectExist(testObjectName);
        assertFalse(existsAfterDelete, "删除后，文件不应再存在于OSS中");
    }

    @Test
    public void testCheckNonExistentObject() {
        // 测试一个不存在的文件
        String nonExistentObjectName = "non-existent-file-" + UUID.randomUUID() + ".txt";
        boolean exists = ossUtil.doesObjectExist(nonExistentObjectName);
        assertFalse(exists, "一个从未上传过的文件不应该存在");
    }

    @Test
    public void testDeleteNonExistentObject() {
        // 删除一个不存在的文件，根据OSS的行为，这通常不会报错，但我们的方法应该返回true
        // 因为OSS的删除是幂等的，删除一个不存在的对象也被认为是“成功”地达到了“不存在”的状态
        String nonExistentObjectName = "non-existent-file-" + UUID.randomUUID() + ".txt";
        boolean deleted = ossUtil.deleteObject(nonExistentObjectName);
        assertTrue(deleted, "删除一个不存在的对象也应该被视为成功");
    }

    @Test
    public void testDownload() {
        // 1. 先上传一个文件，确保OSS上有可供下载的对象
        String fileUrl = ossUtil.upload(testFileBytes, testObjectName);
        assertNotNull(fileUrl, "文件上传失败，无法进行下载测试");

        // 2. 执行下载操作
        byte[] downloadedBytes = ossUtil.download(testObjectName);

        // 3. 验证下载结果
        assertNotNull(downloadedBytes, "下载返回的字节数组不应为null");
        assertArrayEquals(testFileBytes, downloadedBytes, "下载的文件内容与上传时不一致");
    }

    @Test
    public void testDownloadNonExistentObject() {
        // 尝试下载一个不存在的文件
        String nonExistentObjectName = "non-existent-file-" + UUID.randomUUID() + ".txt";
        byte[] downloadedBytes = ossUtil.download(nonExistentObjectName);

        // 验证返回结果是否为null
        assertNull(downloadedBytes, "下载一个不存在的文件时，应返回null");
    }
}
