package org.bigmouth.gpt.xiaozhi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Allen Hu
 * @date 2025/2/28
 */
@Data
@ConfigurationProperties(prefix = "xiaozhi.bytedance")
public class XiaozhiByteDanceConfig {

    private String ttsUrl = "wss://openspeech.bytedance.com/api/v1/tts/ws_binary";
    private String appId;
    private String accessToken;
    private String defaultVoice = "zh_female_wanwanxiaohe_moon_bigtts";
    private int speechPoolMaxTotal = 500;
    private int speechPoolMaxIdle = 100;
    private int speechPoolMinIdle = 10;
}
