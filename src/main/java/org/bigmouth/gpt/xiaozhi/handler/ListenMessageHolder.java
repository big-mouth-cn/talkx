package org.bigmouth.gpt.xiaozhi.handler;

import com.bxm.warcar.cache.Fetcher;
import com.bxm.warcar.cache.KeyGenerator;
import com.bxm.warcar.cache.Updater;
import com.bxm.warcar.utils.KeyBuilder;
import lombok.AllArgsConstructor;
import org.bigmouth.gpt.xiaozhi.entity.DataPacket;
import org.springframework.context.annotation.Configuration;

/**
 * @author Allen Hu
 * @date 2025/3/12
 */
@Configuration
@AllArgsConstructor
public class ListenMessageHolder {

    private final Fetcher fetcher;
    private final Updater updater;

    public DataPacket getListenDataPacket(String sessionId) {
        return fetcher.fetch(stringListenStart(sessionId), DataPacket.class);
    }

    public void saveListenDataPacket(String sessionId, DataPacket listen) {
        updater.update(stringListenStart(sessionId), listen, 30 * 60);
    }

    public String getListenMode(String sessionId) {
        return fetcher.fetch(stringListenMode(sessionId), String.class);
    }

    public void saveListenMode(String sessionId, String mode) {
        updater.update(stringListenMode(sessionId), mode, 30 * 60);
    }

    private KeyGenerator stringListenStart(String sessionId) {
        return () -> KeyBuilder.build("talkx", "listen", "start", sessionId);
    }

    private KeyGenerator stringListenMode(String sessionId) {
        return () -> KeyBuilder.build("talkx", "listen", "mode", sessionId);
    }
}
