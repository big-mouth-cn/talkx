package org.bigmouth.gpt.xiaozhi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Allen Hu
 * @date 2025/2/20
 */
@Data
@ConfigurationProperties(prefix = "xiaozhi")
public class XiaozhiConfig {

    /**
     * 是否开启小智后台服务
     */
    private boolean enable = false;

    /**
     * 默认模型
     */
    private String defaultLlmModel = "qwen-turbo";

    /**
     * 是否使用redis缓存音频队列
     */
    private boolean enableRedisAudioQueue = false;
}
