package org.bigmouth.gpt.xiaozhi.tts;

import com.bxm.warcar.utils.JsonHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiTalkXConfig;
import org.bigmouth.gpt.xiaozhi.forest.TalkXApi;
import org.bigmouth.gpt.xiaozhi.forest.TtsRequest;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
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
