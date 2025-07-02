package org.bigmouth.gpt.autoconfigure.file;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("config.file")
public class HttpFileConfiguration {
    private String endPoint = "oss-cn-shanghai.aliyuncs.com";
    private String accessKeyId;
    private String accessKeySecret;
    private String host;
    private String bucketName;
    //区分是哪个服务上传的文件
    private String serviceName;
    //文件大小限制，默认4M
    private long limitSize = 1024 * 1024 * 4;
}
