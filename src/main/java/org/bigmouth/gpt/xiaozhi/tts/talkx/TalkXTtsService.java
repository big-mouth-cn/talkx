package org.bigmouth.gpt.xiaozhi.tts.talkx;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.xiaozhi.forest.TalkXApi;
import org.bigmouth.gpt.xiaozhi.forest.TtsRequest;
import org.bigmouth.gpt.xiaozhi.tts.TtsPlatformType;
import org.bigmouth.gpt.xiaozhi.tts.TtsService;

import java.util.function.Consumer;

/**
 * @author Allen Hu
 * @author DeepSeek R1
 * @date 2025/3/18
 */
@Slf4j
public class TalkXTtsService implements TtsService {

    private final TalkXTtsService.TtsConfig ttsConfig;
    private final TalkXApi talkXApi;

    public TalkXTtsService(TtsConfig ttsConfig, TalkXApi talkXApi) {
        this.ttsConfig = ttsConfig;
        this.talkXApi = talkXApi;
    }

    @Override
    public TtsPlatformType of() {
        return TtsPlatformType.TalkX;
    }

    @Override
    public void stream(String text, Consumer<byte[]> frameHandler) {
        TtsRequest ttsRequest = new TtsRequest()
                .setSessionId(ttsConfig.getSessionId())
                .setTtsPlatformType(ttsConfig.getTtsPlatformType())
                .setVoiceModel(ttsConfig.getVoiceModel())
                .setVoiceRole(ttsConfig.getVoiceRole())
                .setText(text);
        byte[] pcmBytes = talkXApi.tts(ttsRequest);
        frameHandler.accept(pcmBytes);
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }

    @Data
    public static class TtsConfig {
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
}
