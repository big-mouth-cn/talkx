package org.bigmouth.gpt.xiaozhi.tts.ali;

import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author Allen Hu
 * @date 2025/3/24
 */
@Slf4j
class SpeechSynthesizerObjectFactory extends BasePooledObjectFactory<SpeechSynthesizer> {

    @Override
    public SpeechSynthesizer create() throws Exception {
        log.info("SpeechSynthesizer created");
        return new SpeechSynthesizer();
    }

    @Override
    public PooledObject<SpeechSynthesizer> wrap(SpeechSynthesizer speechSynthesizer) {
        return new DefaultPooledObject<>(speechSynthesizer);
    }
}
