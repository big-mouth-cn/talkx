package org.bigmouth.gpt.xiaozhi.tts;

import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.xiaozhi.tts.volcengine.SpeechWebSocketClient;
import org.bigmouth.gpt.xiaozhi.tts.volcengine.SpeechWsClientObjectPool;

import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
public class ByteDanceTtsService implements TtsService {
    private final String appId;
    private final String voiceType;
    private final SpeechWsClientObjectPool speechWsClientObjectPool;

    public ByteDanceTtsService(String appId, String voiceType, SpeechWsClientObjectPool speechWsClientObjectPool) {
        this.appId = appId;
        this.voiceType = voiceType;
        this.speechWsClientObjectPool = speechWsClientObjectPool;
    }

    @Override
    public TtsPlatformType of() {
        return TtsPlatformType.ByteDance;
    }

    @Override
    public void stream(String text, Consumer<byte[]> frameHandler) {
        SpeechWebSocketClient ttsWebsocketClient = null;
        try {
            ttsWebsocketClient = this.speechWsClientObjectPool.getInstance().borrowObject();
            ttsWebsocketClient.setFrameHandler(frameHandler);

            TtsRequest ttsRequest = TtsRequest.builder()
                    .app(TtsRequest.App.builder()
                            .appid(appId)
                            .cluster("volcano_tts")
                            .build())
                    .user(TtsRequest.User.builder()
                            .uid("uid")
                            .build())
                    .audio(TtsRequest.Audio.builder()
                            .encoding("pcm")
                            .rate(16000)
                            .voiceType(voiceType)
                            .build())
                    .request(TtsRequest.Request.builder()
                            .reqID(UUID.randomUUID().toString())
                            .operation("query")
                            .text(text)
                            .build())
                    .build();
            ttsWebsocketClient.submit(ttsRequest);
        } catch (Exception e) {
            log.error("stream: ", e);
            throw new RuntimeException(e);
        } finally {
            if (null != ttsWebsocketClient) {
                try {
                    speechWsClientObjectPool.getInstance().returnObject(ttsWebsocketClient);
                } catch (Exception e) {
                    log.error("returnObject error: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }
}