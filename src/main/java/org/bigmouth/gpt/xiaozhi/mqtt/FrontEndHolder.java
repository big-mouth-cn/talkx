package org.bigmouth.gpt.xiaozhi.mqtt;

import com.bxm.warcar.cache.Fetcher;
import com.bxm.warcar.cache.KeyGenerator;
import com.bxm.warcar.cache.Updater;
import com.bxm.warcar.utils.KeyBuilder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * @author Allen Hu
 * @date 2025/3/12
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class FrontEndHolder {

    private final Fetcher fetcher;
    private final Updater updater;

    public String get(String deviceClientId) {
        return fetcher.fetch(stringDeviceP2pTopic(deviceClientId), String.class);
    }

    public void save(String deviceClientId, String frontEndClientId) {
        log.info("save frontEndClientId: {} - {}", deviceClientId, frontEndClientId);
        updater.update(stringDeviceP2pTopic(deviceClientId), frontEndClientId);
    }

    public void remove(String deviceClientId) {
        log.info("remove frontEndClientId: {}", deviceClientId);
        updater.remove(stringDeviceP2pTopic(deviceClientId));
    }

    private KeyGenerator stringDeviceP2pTopic(String deviceP2pTopic) {
        return () -> KeyBuilder.build("talkx", "mqtt", "device_p2p_topic", deviceP2pTopic);
    }
}
