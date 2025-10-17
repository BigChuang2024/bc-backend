package com.haderacher.bcbackend.mq.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeMessage {
    private String fileUrl;   // OSS 上的完整 URL 或 key（与 ossUtil.download 配合）
    private String fileName;  // 原始文件名，方便下载或日志
    private String username;  // 上传用户
}
