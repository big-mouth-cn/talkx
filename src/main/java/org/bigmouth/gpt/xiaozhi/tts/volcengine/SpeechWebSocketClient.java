package org.bigmouth.gpt.xiaozhi.tts.volcengine;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.bigmouth.gpt.xiaozhi.tts.TtsRequest;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;

import java.math.BigInteger;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.function.Consumer;

/**
 * @author Allen Hu
 * @date 2025/3/24
 */
@Slf4j
public class SpeechWebSocketClient extends WebSocketClient {

    private Consumer<byte[]> frameHandler;

    public SpeechWebSocketClient(String url, String accessToken) {
        super(URI.create(url), Collections.singletonMap("Authorization", "Bearer; " + accessToken));
    }

    public SpeechWebSocketClient setFrameHandler(Consumer<byte[]> frameHandler) {
        this.frameHandler = frameHandler;
        return this;
    }

    public void submit(TtsRequest ttsRequest) throws InterruptedException {
        String json = JSON.toJSONString(ttsRequest);
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        byte[] header = {0x11, 0x10, 0x10, 0x00};
        ByteBuffer requestByte = ByteBuffer.allocate(8 + jsonBytes.length);
        requestByte.put(header).putInt(jsonBytes.length).put(jsonBytes);
        synchronized (this) {
            this.doSend(requestByte);
            wait();
        }
    }

    public void doSend(ByteBuffer requestByte) {
        byte[] array = requestByte.array();
        try {
            this.send(array);
        } catch (WebsocketNotConnectedException e) {
            if (doReconnect()) {
                this.send(array);
            } else {
                log.error("doSend: ", e);
            }
        }
    }

    public boolean doReconnect() {
        int times = 0;
        while (times++ < 3) {
            try {
                this.reconnectBlocking();
                return true;
            } catch (InterruptedException e) {
                log.error("reconnect: ", e);
            }
        }
        return false;
    }

    public boolean doConnect() {
        int times = 0;
        while (times++ < 3) {
            try {
                this.connectBlocking();
                return true;
            } catch (InterruptedException e) {
                log.error("reconnect: ", e);
            }
        }
        return false;
    }

    public void doClose() {
        this.close(CloseFrame.NORMAL, "received all audio data.");
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        int protocolVersion = (bytes.get(0) & 0xff) >> 4;
        int headerSize = bytes.get(0) & 0x0f;
        int messageType = (bytes.get(1) & 0xff) >> 4;
        int messageTypeSpecificFlags = bytes.get(1) & 0x0f;
        int serializationMethod = (bytes.get(2) & 0xff) >> 4;
        int messageCompression = bytes.get(2) & 0x0f;
        int reserved = bytes.get(3) & 0xff;
        bytes.position(headerSize * 4);
        byte[] fourByte = new byte[4];
        if (messageType == 11) {
            // Audio-only server response
            log.debug("received audio-only response.");
            if (messageTypeSpecificFlags == 0) {
                // Ack without audio data
            } else {
                bytes.get(fourByte, 0, 4);
                int sequenceNumber = new BigInteger(fourByte).intValue();
                bytes.get(fourByte, 0, 4);
                int payloadSize = new BigInteger(fourByte).intValue();
                byte[] payload = new byte[payloadSize];
                bytes.get(payload, 0, payloadSize);
                frameHandler.accept(payload);
                if (sequenceNumber < 0) {
                    // received the last segment
                    notify0();
                }
            }
        } else if (messageType == 15) {
            // Error message from server
            bytes.get(fourByte, 0, 4);
            int code = new BigInteger(fourByte).intValue();
            bytes.get(fourByte, 0, 4);
            int messageSize = new BigInteger(fourByte).intValue();
            byte[] messageBytes = new byte[messageSize];
            bytes.get(messageBytes, 0, messageSize);
            String message = new String(messageBytes, StandardCharsets.UTF_8);
            throw new TtsException(code, message);
        } else {
            log.warn("Received unknown response message type: {}", messageType);
        }
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        log.debug("opened connection");
    }

    @Override
    public void onMessage(String message) {
        log.debug("received message: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.debug("Connection closed by {}, Code: {}, Reason: {}", (remote ? "remote" : "us"), code, reason);
        this.notify0();
    }

    @Override
    public void onError(Exception e) {
        close(CloseFrame.NORMAL, e.toString());
    }

    private void notify0() {
        synchronized (this) {
            notify();
        }
    }
}
