package org.bigmouth.gpt.xiaozhi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Allen Hu
 * @date 2025/2/28
 */
@Data
@ConfigurationProperties(prefix = "xiaozhi.alibaba")
public class XiaozhiAlibabaConfig {

    private String dashscopeApiKey = "";
    private String cosyVoiceDefaultModel = "cosyvoice-v2";
    private String cosyVoiceDefaultVoice = "longxiaoxia_v2";
}
