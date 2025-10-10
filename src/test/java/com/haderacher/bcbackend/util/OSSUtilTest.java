package com.haderacher.bcbackend.util;

import io.jsonwebtoken.lang.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class OSSUtilTest {

    @Autowired
    private OSSUtil ossUtil;


    @Test
    public void uploadFileTest() throws IOException, URISyntaxException {
        byte[] data = Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("stample_resume1.pdf").toURI()));
        String upload = ossUtil.upload(data, "stample_resume1.pdf");
        Assert.notNull(upload);
    }
}
