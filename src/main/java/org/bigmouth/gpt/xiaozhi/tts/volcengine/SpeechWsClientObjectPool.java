package org.bigmouth.gpt.xiaozhi.tts.volcengine;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.bigmouth.gpt.xiaozhi.config.XiaozhiByteDanceConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.locks.Lock;

/**
 * @author Allen Hu
 * @date 2025/3/24
 */
@Configuration
public class SpeechWsClientObjectPool implements InitializingBean {

    private final Lock lock = new java.util.concurrent.locks.ReentrantLock();
    private final XiaozhiByteDanceConfig xiaozhiByteDanceConfig;
    private GenericObjectPool<SpeechWebSocketClient> synthesizerPool;

    public SpeechWsClientObjectPool(XiaozhiByteDanceConfig xiaozhiByteDanceConfig) {
        this.xiaozhiByteDanceConfig = xiaozhiByteDanceConfig;
    }

    @Override
    public void afterPropertiesSet() {
        this.initPool();
    }

    public void initPool() {
        lock.lock();
        if (synthesizerPool == null) {
            String ttsUrl = xiaozhiByteDanceConfig.getTtsUrl();
            String accessToken = xiaozhiByteDanceConfig.getAccessToken();
            SpeechWsClientObjectFactory objectFactory = new SpeechWsClientObjectFactory(ttsUrl, accessToken);
            GenericObjectPoolConfig<SpeechWebSocketClient> config = new GenericObjectPoolConfig<>();
            config.setMaxTotal(xiaozhiByteDanceConfig.getSpeechPoolMaxTotal());
            config.setMaxIdle(xiaozhiByteDanceConfig.getSpeechPoolMaxIdle());
            config.setMinIdle(xiaozhiByteDanceConfig.getSpeechPoolMinIdle());
            synthesizerPool = new GenericObjectPool<>(objectFactory, config);
        }
        lock.unlock();
    }

    public GenericObjectPool<SpeechWebSocketClient> getInstance() {
        return synthesizerPool;
    }
}
