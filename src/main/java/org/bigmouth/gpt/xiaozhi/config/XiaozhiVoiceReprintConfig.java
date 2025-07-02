package org.bigmouth.gpt.xiaozhi.config;

import lombok.Data;
import org.bigmouth.gpt.xiaozhi.tts.TtsPlatformType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Allen Hu
 * @date 2025/4/18
 */
@Data
@ConfigurationProperties(prefix = "xiaozhi.voice.reprint")
public class XiaozhiVoiceReprintConfig {

    private TtsPlatformType type = TtsPlatformType.TalkX;
}
