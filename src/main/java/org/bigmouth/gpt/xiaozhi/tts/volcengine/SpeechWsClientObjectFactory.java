package org.bigmouth.gpt.xiaozhi.tts.volcengine;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author Allen Hu
 * @date 2025/3/24
 */
@Slf4j
public class SpeechWsClientObjectFactory extends BasePooledObjectFactory<SpeechWebSocketClient> {

    private final String url;
    private final String accessToken;

    public SpeechWsClientObjectFactory(String url, String accessToken) {
        this.url = url;
        this.accessToken = accessToken;
    }

    @Override
    public SpeechWebSocketClient create() throws Exception {
        SpeechWebSocketClient websocketClient = new SpeechWebSocketClient(url, accessToken);
        websocketClient.doConnect();
        log.info("SpeechWebSocketClient created");
        return websocketClient;
    }

    @Override
    public PooledObject<SpeechWebSocketClient> wrap(SpeechWebSocketClient ttsWebsocketClient) {
        return new DefaultPooledObject<>(ttsWebsocketClient);
    }
}
