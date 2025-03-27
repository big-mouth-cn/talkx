package org.bigmouth.gpt.xiaozhi.tts;

import com.alibaba.dashscope.audio.tts.SpeechSynthesisResult;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisAudioFormat;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.alibaba.dashscope.common.ResultCallback;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.xiaozhi.tts.ali.CosyVoiceObjectPool;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * @author Allen Hu
 * @date 2025/2/24
 */
@Slf4j
@Getter
public class AlibabaDashscopeTtsService implements TtsService {

    private final CosyVoiceObjectPool cosyVoiceObjectPool;
    private final String apiKey;
    private final String model;
    private final String voice;

    private SpeechSynthesisParam param;

    public AlibabaDashscopeTtsService(CosyVoiceObjectPool cosyVoiceObjectPool, String apiKey, String model, String voice) {
        this.cosyVoiceObjectPool = cosyVoiceObjectPool;
        this.apiKey = apiKey;
        this.model = model;
        this.voice = voice;
        this.param = SpeechSynthesisParam.builder()
                .apiKey(apiKey)
                .model(model)
                .voice(voice)
                .format(SpeechSynthesisAudioFormat.PCM_16000HZ_MONO_16BIT)
                .build();
    }

    @Override
    public TtsPlatformType of() {
        return TtsPlatformType.Alibaba;
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void stream(String text, Consumer<byte[]> frameHandler) {
        ResultCallback<SpeechSynthesisResult> callback = new ResultCallback<SpeechSynthesisResult>() {
            @Override
            public void onEvent(SpeechSynthesisResult speechSynthesisResult) {
                ByteBuffer audioFrame = speechSynthesisResult.getAudioFrame();
                if (null != audioFrame) {
                    if (null != frameHandler) {
                        frameHandler.accept(audioFrame.array());
                    }
                }
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Exception e) {
                log.error("onError: {}", e.getMessage());
            }
        };
        SpeechSynthesizer speechSynthesizer = null;
        try {
            speechSynthesizer = cosyVoiceObjectPool.getInstance().borrowObject();
            speechSynthesizer.updateParamAndCallback(param, callback);
            speechSynthesizer.streamingCall(text);
            speechSynthesizer.streamingComplete();
        } catch (Exception e) {
            log.error("onError: {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            if (null != speechSynthesizer) {
                try {
                    cosyVoiceObjectPool.getInstance().returnObject(speechSynthesizer);
                } catch (Exception e) {
                    log.error("returnObject error: {}", e.getMessage());
                }
            }
        }
    }
}
