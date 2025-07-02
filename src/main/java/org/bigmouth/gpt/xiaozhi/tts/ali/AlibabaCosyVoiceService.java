package org.bigmouth.gpt.xiaozhi.tts.ali;

import com.alibaba.dashscope.audio.ttsv2.enrollment.Voice;
import com.alibaba.dashscope.audio.ttsv2.enrollment.VoiceEnrollmentService;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiAlibabaConfig;
import org.bigmouth.gpt.xiaozhi.tts.TtsPlatformType;
import org.bigmouth.gpt.xiaozhi.tts.VoiceReprintRequest;
import org.bigmouth.gpt.xiaozhi.tts.VoiceReprintResult;
import org.bigmouth.gpt.xiaozhi.tts.VoiceReprintService;
import org.springframework.context.annotation.Configuration;

/**
 * @author Allen Hu
 * @date 2025/3/2
 */
@Slf4j
@Configuration
public class AlibabaCosyVoiceService implements VoiceReprintService {

    private final XiaozhiAlibabaConfig xiaozhiAlibabaConfig;

    public AlibabaCosyVoiceService(XiaozhiAlibabaConfig xiaozhiAlibabaConfig) {
        this.xiaozhiAlibabaConfig = xiaozhiAlibabaConfig;
    }

    @Override
    public TtsPlatformType of() {
        return TtsPlatformType.Alibaba;
    }

    @Override
    public VoiceReprintResult reprint(VoiceReprintRequest request) {
        try {
            String modelNamePrefix = request.getModelNamePrefix();
            String voiceSrcUrl = request.getVoiceSrcUrl();
            String targetModel = "cosyvoice-v1";
            String apiKey = xiaozhiAlibabaConfig.getDashscopeApiKey();
            VoiceEnrollmentService service = new VoiceEnrollmentService(apiKey);
            Voice myVoice = service.createVoice(targetModel, modelNamePrefix, voiceSrcUrl);
            log.info("声音复刻成功。{}", myVoice);
            String voiceId = myVoice.getVoiceId();
            return new VoiceReprintResult().setTtsPlatformType(of()).setAudioModel(targetModel).setAudioRole(voiceId);
        } catch (NoApiKeyException | InputRequiredException e) {
            log.error("创建复刻声音失败", e);
            return null;
        }
    }
}
