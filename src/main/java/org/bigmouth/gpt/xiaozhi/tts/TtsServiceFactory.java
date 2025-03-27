package org.bigmouth.gpt.xiaozhi.tts;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiAlibabaConfig;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiByteDanceConfig;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiTalkXConfig;
import org.bigmouth.gpt.xiaozhi.forest.TalkXApi;
import org.bigmouth.gpt.xiaozhi.tts.ali.CosyVoiceObjectPool;
import org.bigmouth.gpt.xiaozhi.tts.volcengine.SpeechWsClientObjectPool;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * @author Allen Hu
 * @date 2025/2/27
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class TtsServiceFactory {

    private final XiaozhiAlibabaConfig xiaozhiAlibabaConfig;
    private final XiaozhiByteDanceConfig xiaozhiByteDanceConfig;
    private final XiaozhiTalkXConfig xiaozhiTalkXConfig;
    private final CosyVoiceObjectPool cosyVoiceObjectPool;
    private final SpeechWsClientObjectPool speechWsClientObjectPool;
    private final TalkXApi talkXApi;

    public TtsService createInstance(String sessionId, TtsPlatformType ttsPlatformType, String voiceModel, String voiceRole) {
        try {
            return createInstanceThrowsIllegal(sessionId, ttsPlatformType, voiceModel, voiceRole);
        } catch (IllegalArgumentException e) {
            log.warn("{}, but use TalkX instead.", e.getMessage());
            return createTalkX(sessionId, ttsPlatformType, voiceModel, voiceRole);
        }
    }

    private TtsService createInstanceThrowsIllegal(String sessionId, TtsPlatformType ttsPlatformType, String voiceModel, String voiceRole) {
        switch (ttsPlatformType) {
            case Alibaba:
                String dashscopeApiKey = xiaozhiAlibabaConfig.getDashscopeApiKey();
                if (dashscopeApiKey == null || dashscopeApiKey.isEmpty()) {
                    throw new IllegalArgumentException("Dashscope API key is not set.");
                }
                voiceModel = Optional.ofNullable(voiceModel).orElse(xiaozhiAlibabaConfig.getCosyVoiceDefaultModel());
                voiceRole = Optional.ofNullable(voiceRole).orElse(xiaozhiAlibabaConfig.getCosyVoiceDefaultVoice());
                return new AlibabaDashscopeTtsService(cosyVoiceObjectPool, dashscopeApiKey, voiceModel, voiceRole);
            case ByteDance:
                String ttsUrl = xiaozhiByteDanceConfig.getTtsUrl();
                String appId = xiaozhiByteDanceConfig.getAppId();
                String accessToken = xiaozhiByteDanceConfig.getAccessToken();
                if (ttsUrl == null || ttsUrl.isEmpty() || appId == null || appId.isEmpty() || accessToken == null || accessToken.isEmpty()) {
                    throw new IllegalArgumentException("ByteDance TTS URL, App ID or Access Token is not set.");
                }
                voiceRole = Optional.ofNullable(voiceRole).orElse(xiaozhiByteDanceConfig.getDefaultVoice());
                return new ByteDanceTtsService(appId, voiceRole, speechWsClientObjectPool);
            case TalkX:
                TtsPlatformType defaultTtsPlatformType = xiaozhiTalkXConfig.getDefaultTtsPlatformType();
                voiceModel = Optional.ofNullable(voiceModel).orElse(xiaozhiTalkXConfig.getDefaultVoiceModel());
                voiceRole = Optional.ofNullable(voiceRole).orElse(xiaozhiTalkXConfig.getDefaultVoiceRole());
                return createTalkX(sessionId, defaultTtsPlatformType, voiceModel, voiceRole);
            default:
                throw new UnsupportedOperationException("Unsupported TTS platform type: " + ttsPlatformType);
        }
    }

    private TtsService createTalkX(String sessionId, TtsPlatformType ttsPlatformType, String voiceModel, String voiceRole) {
        TalkXTtsService.TtsConfig ttsConfig = new TalkXTtsService.TtsConfig();
        ttsConfig.setSessionId(sessionId);
        ttsConfig.setTtsPlatformType(ttsPlatformType);
        ttsConfig.setVoiceModel(voiceModel);
        ttsConfig.setVoiceRole(voiceRole);
        return new TalkXTtsService(ttsConfig, talkXApi);
    }
}
