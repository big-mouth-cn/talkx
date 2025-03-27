package org.bigmouth.gpt.xiaozhi.tts.ali;

import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiAlibabaConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.locks.Lock;

/**
 * @author Allen Hu
 * @date 2025/3/24
 */
@Configuration
public class CosyVoiceObjectPool implements InitializingBean {

    private final Lock lock = new java.util.concurrent.locks.ReentrantLock();
    private final XiaozhiAlibabaConfig xiaozhiAlibabaConfig;
    private GenericObjectPool<SpeechSynthesizer> synthesizerPool;

    public CosyVoiceObjectPool(XiaozhiAlibabaConfig xiaozhiAlibabaConfig) {
        this.xiaozhiAlibabaConfig = xiaozhiAlibabaConfig;
    }

    @Override
    public void afterPropertiesSet() {
        this.initPool();
    }

    public void initPool() {
        lock.lock();
        if (synthesizerPool == null) {
            SpeechSynthesizerObjectFactory speechSynthesizerObjectFactory = new SpeechSynthesizerObjectFactory();
            GenericObjectPoolConfig<SpeechSynthesizer> config = new GenericObjectPoolConfig<>();
            config.setMaxTotal(xiaozhiAlibabaConfig.getCosyVoicePoolMaxTotal());
            config.setMaxIdle(xiaozhiAlibabaConfig.getCosyVoicePoolMaxIdle());
            config.setMinIdle(xiaozhiAlibabaConfig.getCosyVoicePoolMinIdle());
            synthesizerPool = new GenericObjectPool<>(speechSynthesizerObjectFactory, config);
        }
        lock.unlock();
    }

    public GenericObjectPool<SpeechSynthesizer> getInstance() {
        return synthesizerPool;
    }
}
