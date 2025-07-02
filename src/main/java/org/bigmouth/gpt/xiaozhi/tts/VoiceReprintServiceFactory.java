package org.bigmouth.gpt.xiaozhi.tts;

import com.bxm.warcar.utils.AbstractBeanBus;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiVoiceReprintConfig;
import org.springframework.context.annotation.Configuration;

/**
 * @author Allen Hu
 * @date 2025/3/2
 */
@Configuration
public class VoiceReprintServiceFactory extends AbstractBeanBus<TtsPlatformType, VoiceReprintService> {

    private final XiaozhiVoiceReprintConfig xiaozhiVoiceReprintConfig;

    public VoiceReprintServiceFactory(XiaozhiVoiceReprintConfig xiaozhiVoiceReprintConfig) {
        this.xiaozhiVoiceReprintConfig = xiaozhiVoiceReprintConfig;
    }

    @Override
    protected Class<VoiceReprintService> getInstanceClazz() {
        return VoiceReprintService.class;
    }

    @Override
    protected TtsPlatformType getKey(String beanName, VoiceReprintService bean) {
        return bean.of();
    }

    public VoiceReprintService get() {
        TtsPlatformType type = xiaozhiVoiceReprintConfig.getType();
        VoiceReprintService voiceReprintService = super.get(type);
        if (voiceReprintService == null) {
            throw new IllegalArgumentException("不支持的语音平台类型：" + type);
        }
        return voiceReprintService;
    }
}
