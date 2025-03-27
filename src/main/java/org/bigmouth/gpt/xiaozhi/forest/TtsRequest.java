package org.bigmouth.gpt.xiaozhi.forest;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bigmouth.gpt.xiaozhi.tts.TtsPlatformType;

/**
 * @author Allen Hu
 * @date 2025/3/18
 */
@Data
@Accessors(chain = true)
public class TtsRequest {
    /**
     * sessionId
     */
    private String sessionId;
    /**
     * 平台
     */
    private TtsPlatformType ttsPlatformType;
    /**
     * 合成大模型
     */
    private String voiceModel;
    /**
     * 音色角色
     */
    private String voiceRole;

    /**
     * 说话内容
     */
    private String text;
}
